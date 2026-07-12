package com.pmlp.edutask.ui.alumno

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.Tarea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

data class TareaItem(
    val tarea: Tarea,
    val estado: EstadoEvidencia,
    val idAsignacion: String,
    val calificacion: Int? = null,
    val comentario: String? = null,
    val idEvidencia: String? = null
)

sealed class HomeAlumnoState {
    object Loading : HomeAlumnoState()
    data class Success(
        val clases: List<String>,
        val tareas: List<TareaItem>
    ) : HomeAlumnoState()
    data class Error(val message: String) : HomeAlumnoState()
}

class HomeAlumnoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeAlumnoState>(HomeAlumnoState.Loading)
    val uiState: StateFlow<HomeAlumnoState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    private var asignacionesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private val evidenciasListeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
    
    private var inscripcionesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var tareasListener: com.google.firebase.firestore.ListenerRegistration? = null

    private var enrolledClassesNames = mutableListOf<String>()

    override fun onCleared() {
        super.onCleared()
        asignacionesListener?.remove()
        evidenciasListeners.forEach { it.remove() }
        inscripcionesListener?.remove()
        tareasListener?.remove()
    }

    fun fetchUserData(idUsuario: String) {
        if (idUsuario.isBlank()) {
            _uiState.value = HomeAlumnoState.Error("ID de usuario no válido")
            return
        }

        // Limpiar listeners anteriores si se recarga
        asignacionesListener?.remove()
        evidenciasListeners.forEach { it.remove() }
        evidenciasListeners.clear()

        _uiState.value = HomeAlumnoState.Loading
        loadData(idUsuario)
    }

    fun refresh(idUsuario: String) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(1000) // Simulación UX
            loadData(idUsuario)
            _isRefreshing.value = false
        }
    }

    private fun loadData(idUsuario: String) {
        // Iniciar la sincronización automática de nuevas tareas
        startAutoSync(idUsuario)

        asignacionesListener = db.collection("asignaciones_tarea")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = HomeAlumnoState.Error(error.message ?: "Error al cargar tareas")
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    _uiState.value = HomeAlumnoState.Success(enrolledClassesNames.toList(), emptyList())
                    return@addSnapshotListener
                }

                viewModelScope.launch {
                    try {
                        val paresTareasMap = mutableMapOf<String, TareaItem>()
                        val clasesSet = mutableSetOf<String>()

                        val clasesCache = mutableMapOf<String, String>()
                        val tareasCache = mutableMapOf<String, Tarea>()

                        // Limpiar listeners de evidencias anteriores para esta nueva carga
                        evidenciasListeners.forEach { it.remove() }
                        evidenciasListeners.clear()

                        for (asignacionDoc in snapshot.documents) {
                            val idAsignacion = asignacionDoc.id
                            val idTarea = asignacionDoc.getString("idTarea") ?: continue

                            // 2. Fetch Tarea
                            val tarea = if (tareasCache.containsKey(idTarea)) {
                                tareasCache[idTarea]!!
                            } else {
                                val tareaDoc = db.collection("tareas").document(idTarea).get().await()
                                if (!tareaDoc.exists()) continue
                                
                                val idClase = tareaDoc.getString("idClase") ?: ""
                                val titulo = tareaDoc.getString("titulo") ?: ""
                                val desc = tareaDoc.getString("descripcion") ?: ""
                                val fechaTimestamp = tareaDoc.getTimestamp("fechaLimite")
                                val fecha = fechaTimestamp?.toDate() ?: java.util.Date()

                                // 3. Fetch Clase
                                val nombreClase = if (clasesCache.containsKey(idClase)) {
                                    clasesCache[idClase]!!
                                } else {
                                    val claseDoc = db.collection("clases").document(idClase).get().await()
                                    val nombre = claseDoc.getString("nombre") ?: "Sin Clase"
                                    clasesCache[idClase] = nombre
                                    nombre
                                }

                                val t = Tarea(idTarea, titulo, desc, fecha, idClase, nombreClase)
                                tareasCache[idTarea] = t
                                t
                            }
                            
                            clasesSet.add(tarea.nombreClase)

                            // Estado inicial mientras se resuelve la evidencia
                            paresTareasMap[idAsignacion] = TareaItem(tarea, EstadoEvidencia.Pendiente, idAsignacion)

                            // 4. Snapshot Listener para Evidencia
                            val evListener = db.collection("evidencias_tarea")
                                .whereEqualTo("idAsignacion", idAsignacion)
                                .addSnapshotListener { evSnapshot, _ ->
                                    if (evSnapshot != null && !evSnapshot.isEmpty) {
                                        val evidenciaDoc = evSnapshot.documents[0]
                                        val estadoStr = evidenciaDoc.getString("estado")
                                        val idEvidenciaRaw = evidenciaDoc.get("idEvidencia")
                                        val idEvidencia = when (idEvidenciaRaw) {
                                            is Number -> idEvidenciaRaw.toLong().toString()
                                            else -> idEvidenciaRaw?.toString() ?: evidenciaDoc.id
                                        }
                                        
                                        val estadoEvidencia = when (estadoStr) {
                                            "Aprobada" -> EstadoEvidencia.Aprobada
                                            "Rechazada" -> EstadoEvidencia.Rechazada
                                            else -> EstadoEvidencia.Pendiente
                                        }

                                        // Si está evaluada, buscamos la calificación
                                        if (estadoEvidencia != EstadoEvidencia.Pendiente) {
                                            db.collection("calificaciones")
                                                .whereEqualTo("idEvidencia", idEvidencia)
                                                .get()
                                                .addOnSuccessListener { califSnapshot ->
                                                    var calificacion: Int? = null
                                                    var comentario: String? = null
                                                    if (!califSnapshot.isEmpty) {
                                                        val valorRaw = califSnapshot.documents[0].get("valor")
                                                        calificacion = when (valorRaw) {
                                                            is Number -> valorRaw.toInt()
                                                            is String -> valorRaw.toIntOrNull()
                                                            else -> null
                                                        }
                                                        comentario = califSnapshot.documents[0].getString("comentario")
                                                    }
                                                    if (calificacion == null) {
                                                        val califRaw = evidenciaDoc.get("calificacion")
                                                        calificacion = when (califRaw) {
                                                            is Number -> califRaw.toInt()
                                                            is String -> califRaw.toIntOrNull()
                                                            else -> null
                                                        }
                                                    }
                                                    
                                                    paresTareasMap[idAsignacion] = TareaItem(tarea, estadoEvidencia, idAsignacion, calificacion, comentario, idEvidencia)
                                                    _uiState.value = HomeAlumnoState.Success(
                                                        clases = enrolledClassesNames.ifEmpty { clasesSet.toList().sorted() },
                                                        tareas = paresTareasMap.values.distinctBy { it.tarea.idTarea }.sortedBy { it.tarea.fechaLimite }
                                                    )
                                                }
                                        } else {
                                            paresTareasMap[idAsignacion] = TareaItem(tarea, estadoEvidencia, idAsignacion, null, null, idEvidencia)
                                            _uiState.value = HomeAlumnoState.Success(
                                                clases = enrolledClassesNames.ifEmpty { clasesSet.toList().sorted() },
                                                tareas = paresTareasMap.values.distinctBy { it.tarea.idTarea }.sortedBy { it.tarea.fechaLimite }
                                            )
                                        }
                                    } else {
                                        paresTareasMap[idAsignacion] = TareaItem(tarea, EstadoEvidencia.Pendiente, idAsignacion, null, null, null)
                                        _uiState.value = HomeAlumnoState.Success(
                                            clases = enrolledClassesNames.ifEmpty { clasesSet.toList().sorted() },
                                            tareas = paresTareasMap.values.distinctBy { it.tarea.idTarea }.sortedBy { it.tarea.fechaLimite }
                                        )
                                    }
                                }
                            evidenciasListeners.add(evListener)
                        }

                        // Emitimos éxito preliminar (se sobreescribirá si las evidencias cargan en milisegundos)
                        _uiState.value = HomeAlumnoState.Success(
                            clases = enrolledClassesNames.ifEmpty { clasesSet.toList().sorted() },
                            tareas = paresTareasMap.values.distinctBy { it.tarea.idTarea }.sortedBy { it.tarea.fechaLimite }
                        )

                    } catch (e: Exception) {
                        _uiState.value = HomeAlumnoState.Error(e.message ?: "Error desconocido")
                    }
                }
            }
    }

    private fun startAutoSync(idUsuario: String) {
        inscripcionesListener?.remove()
        tareasListener?.remove()

        inscripcionesListener = db.collection("clase_alumno")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                val clasesIds = snapshot.documents.mapNotNull { it.getString("idClase") }
                if (clasesIds.isEmpty()) {
                    enrolledClassesNames.clear()
                    val curr = _uiState.value
                    if (curr is HomeAlumnoState.Success) {
                        _uiState.value = curr.copy(clases = emptyList())
                    }
                    return@addSnapshotListener
                }

                // Cargar nombres de clases para mostrarlos en UI independientemente de si hay tareas
                viewModelScope.launch {
                    try {
                        val nombres = mutableListOf<String>()
                        for (id in clasesIds) {
                            val doc = db.collection("clases").document(id).get().await()
                            doc.getString("nombre")?.let { nombres.add(it) }
                        }
                        enrolledClassesNames = nombres.sorted().toMutableList()
                        val curr = _uiState.value
                        if (curr is HomeAlumnoState.Success) {
                            _uiState.value = curr.copy(clases = enrolledClassesNames.toList())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                tareasListener?.remove()
                // Firestore whereIn solo soporta hasta 30 elementos en una consulta.
                val clasesChunk = clasesIds.take(30)
                
                tareasListener = db.collection("tareas")
                    .whereIn("idClase", clasesChunk)
                    .addSnapshotListener { tareasSnapshot, err ->
                        if (err != null || tareasSnapshot == null) return@addSnapshotListener

                        viewModelScope.launch {
                            try {
                                val asignacionesSnapshot = db.collection("asignaciones_tarea")
                                    .whereEqualTo("idUsuario", idUsuario)
                                    .get().await()

                                val tareasAsignadasIds = asignacionesSnapshot.documents
                                    .mapNotNull { it.getString("idTarea") }.toSet()

                                for (tareaDoc in tareasSnapshot.documents) {
                                    val idTarea = tareaDoc.id
                                    if (!tareasAsignadasIds.contains(idTarea)) {
                                        // Generar la asignación faltante automáticamente
                                        val nuevaAsignacion = hashMapOf(
                                            "idUsuario" to idUsuario,
                                            "idTarea" to idTarea,
                                            "fechaAsignacion" to java.util.Date()
                                        )
                                        db.collection("asignaciones_tarea").add(nuevaAsignacion).await()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            }
    }

    fun unirseAClase(codigoClase: String, idUsuario: String) {
        if (codigoClase.isBlank() || idUsuario.isBlank()) return

        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = HomeAlumnoState.Loading
            try {
                // 1. Verificar si la clase existe
                val claseDoc = db.collection("clases").document(codigoClase).get().await()
                if (!claseDoc.exists()) {
                    _uiState.value = HomeAlumnoState.Error("El código de clase no existe o es incorrecto.")
                    return@launch
                }

                // 2. Verificar si el alumno ya está inscrito
                val inscripciones = db.collection("clase_alumno")
                    .whereEqualTo("idClase", codigoClase)
                    .whereEqualTo("idUsuario", idUsuario)
                    .get()
                    .await()

                if (!inscripciones.isEmpty) {
                    _uiState.value = HomeAlumnoState.Error("Ya estás inscrito en esta clase.")
                    return@launch
                }

                // 3. Inscribir al alumno
                val nuevaInscripcion = hashMapOf(
                    "idClase" to codigoClase,
                    "idUsuario" to idUsuario
                )
                db.collection("clase_alumno").add(nuevaInscripcion).await()

                // 4. Buscar tareas de la clase y crear asignaciones retroactivamente
                val tareasSnapshot = db.collection("tareas")
                    .whereEqualTo("idClase", codigoClase)
                    .get()
                    .await()

                for (tareaDoc in tareasSnapshot.documents) {
                    val idTarea = tareaDoc.id
                    val nuevaAsignacion = hashMapOf(
                        "idUsuario" to idUsuario,
                        "idTarea" to idTarea,
                        "fechaAsignacion" to Date()
                    )
                    db.collection("asignaciones_tarea").add(nuevaAsignacion).await()
                }

                // 5. Recargar la pantalla para mostrar la nueva clase y tareas
                fetchUserData(idUsuario)
                
            } catch (e: Exception) {
                _uiState.value = HomeAlumnoState.Error(e.message ?: "Error al unirse a la clase.")
            }
        }
    }
}
