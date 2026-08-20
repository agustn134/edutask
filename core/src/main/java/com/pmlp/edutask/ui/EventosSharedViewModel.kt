/**
 * ViewModel compartido entre modulos que sincroniza en tiempo real los eventos escolares
 * y recalcula los promedios y metricas de grupos para el modulo de TV.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.pmlp.edutask.model.Evento
import com.pmlp.edutask.model.EstadisticaGrupo
import com.pmlp.edutask.model.PromedioMateria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class EventosUiState {
    object Idle : EventosUiState()
    object Loading : EventosUiState()
    data class Success(val eventos: List<Evento>) : EventosUiState()
    data class Error(val message: String) : EventosUiState()
}

sealed class EstadisticasUiState {
    object Loading : EstadisticasUiState()
    data class Success(val grupos: List<EstadisticaGrupo>) : EstadisticasUiState()
    data class Error(val message: String) : EstadisticasUiState()
}

class EventosSharedViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<EventosUiState>(EventosUiState.Idle)
    val uiState: StateFlow<EventosUiState> = _uiState

    private val _estadisticasState = MutableStateFlow<EstadisticasUiState>(EstadisticasUiState.Loading)
    val estadisticasState: StateFlow<EstadisticasUiState> = _estadisticasState

    /** Listener de evidencias en tiempo real para el dashboard TV */
    private var evidenciasListener: ListenerRegistration? = null

    init {
        fetchEventos()
        fetchEstadisticasInstitucionales()
    }

    /**
     * Obtiene o recupera datos asociados a fetchEventos desde la base de datos o API.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun fetchEventos() {
        Log.d("FIRESTORE_DEBUG", "Iniciando listener a la coleccion 'eventos_escolares'...")
        _uiState.value = EventosUiState.Loading

        db.collection("eventos_escolares")
            .orderBy("fechaEvento", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIRESTORE_DEBUG", "Error al escuchar Firestore: ${error.message}", error)
                    _uiState.value = EventosUiState.Error(error.message ?: "Error al obtener eventos")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("FIRESTORE_DEBUG", "Snapshot recibido con ${snapshot.size()} documentos")
                    val list = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val titulo = doc.getString("titulo") ?: ""
                        val descripcion = doc.getString("descripcion") ?: ""

                        // Extracción segura del campo 'fechaEvento' soportando múltiples formatos
                        val fechaMillis: Long = try {
                            when (val fechaRaw = doc.get("fechaEvento")) {
                                is Timestamp -> fechaRaw.toDate().time
                                is String -> {
                                    // Si la fecha viene como ISO-8601 o String numérico
                                    try {
                                        java.time.Instant.parse(fechaRaw).toEpochMilli()
                                    } catch (_: Exception) {
                                        fechaRaw.toLongOrNull() ?: System.currentTimeMillis()
                                    }
                                }
                                is Number -> fechaRaw.toLong()
                                is Map<*, *> -> {
                                    // Maneja estructuras exportadas como {_seconds: X, _nanoseconds: Y}
                                    val seconds = (fechaRaw["_seconds"] as? Number)?.toLong() ?: 0L
                                    seconds * 1000
                                }
                                else -> {
                                    doc.getLong("fechaPublicacion") ?: System.currentTimeMillis()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FIRESTORE_DEBUG", "Error al procesar la fecha en documento $id: ${e.message}")
                            doc.getLong("fechaPublicacion") ?: System.currentTimeMillis()
                        }

                        val lugar = doc.getString("lugar") ?: ""
                        val imagenUrl = doc.getString("imagenUrl")

                        Evento(
                            idEvento = id,
                            titulo = titulo,
                            descripcion = descripcion,
                            lugar = lugar,
                            fechaPublicacion = fechaMillis,
                            imagenUrl = imagenUrl
                        )
                    }

                    if (list.isEmpty()) {
                        Log.w("FIRESTORE_DEBUG", "La lista procesada esta vacia.")
                    } else {
                        Log.d("FIRESTORE_DEBUG", "Exito: Se cargaron ${list.size} eventos correctamente.")
                    }

                    _uiState.value = EventosUiState.Success(list)
                }
            }
    }

    /**
     * Escucha en tiempo real la colección evidencias_tarea y recalcula las estadísticas
     * por grupo cada vez que el profesor guarda o actualiza una calificación.
     */
    private fun fetchEstadisticasInstitucionales() {
        evidenciasListener?.remove()

        // Listener en tiempo real sobre evidencias_tarea.
        // Cuando un profesor califica, este listener se activa automáticamente.
        evidenciasListener = db.collection("evidencias_tarea")
            .addSnapshotListener { evidenciasSnap, error ->
                if (error != null) {
                    Log.e("ESTADISTICAS_TV", "Error escuchando evidencias: ${error.message}")
                    _estadisticasState.value = EstadisticasUiState.Error(error.message ?: "Error")
                    return@addSnapshotListener
                }
                if (evidenciasSnap == null) return@addSnapshotListener

                // Cada vez que hay un cambio en evidencias, recalculamos las estadísticas
                viewModelScope.launch {
                    try {
                        calcularEstadisticas()
                    } catch (e: Exception) {
                        Log.e("ESTADISTICAS_TV", "Error calculando estadísticas: ${e.message}")
                        _estadisticasState.value = EstadisticasUiState.Error(e.message ?: "Error")
                    }
                }
            }
    }

    /**
     * Metodo principal que ejecuta la operacion: calcularEstadisticas.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private suspend fun calcularEstadisticas() {
        // 1. Obtener todas las clases
        val clasesSnap = db.collection("clases").get().await()
        val clases = clasesSnap.documents.map { doc ->
            Pair(doc.id, doc.getString("nombre") ?: doc.getString("nombreClase") ?: "Grupo sin nombre")
        }

        if (clases.isEmpty()) {
            _estadisticasState.value = EstadisticasUiState.Success(emptyList())
            return
        }

        val grupos = mutableListOf<EstadisticaGrupo>()

        for ((idClase, nombreClase) in clases) {
            try {
                // 2. Total de alumnos en el grupo
                val alumnosSnap = db.collection("clase_alumno")
                    .whereEqualTo("idClase", idClase)
                    .get().await()
                val totalAlumnos = alumnosSnap.size()
                val alumnoIds = alumnosSnap.documents.mapNotNull { it.getString("idUsuario") }

                // 3. Tareas del grupo
                val tareasSnap = db.collection("tareas")
                    .whereEqualTo("idClase", idClase)
                    .get().await()
                val tareas = tareasSnap.documents.map { it.id to (it.getString("titulo") ?: "Sin título") }

                if (tareas.isEmpty()) {
                    grupos.add(
                        EstadisticaGrupo(
                            idClase = idClase,
                            nombreClase = nombreClase,
                            promedioGeneral = null,
                            promediosPorTarea = emptyList(),
                            totalAlumnos = totalAlumnos,
                            alumnosCalificados = 0
                        )
                    )
                    continue
                }

                val tareaIds = tareas.map { it.first }

                // 4. Asignaciones de esas tareas
                // Map idAsignacion → (idAlumno, idTarea)
                val asigMap = mutableMapOf<String, Pair<String, String>>()
                val chunks = tareaIds.chunked(10)
                for (chunk in chunks) {
                    val asigSnap = db.collection("asignaciones_tarea")
                        .whereIn("idTarea", chunk)
                        .get().await()
                    for (doc in asigSnap.documents) {
                        val idAlumno = doc.getString("idUsuario") ?: continue
                        val idTarea = doc.getString("idTarea") ?: continue
                        asigMap[doc.id] = Pair(idAlumno, idTarea)
                    }
                }

                // 5. Evidencias con calificaciones
                // Map (idAlumno, idTarea) → calificacion
                val calificaciones = mutableMapOf<Pair<String, String>, Int>()
                val asigIds = asigMap.keys.toList()
                val asigChunks = asigIds.chunked(10)
                for (chunk in asigChunks) {
                    val evSnap = db.collection("evidencias_tarea")
                        .whereIn("idAsignacion", chunk)
                        .get().await()
                    for (ev in evSnap.documents) {
                        val califRaw = ev.get("calificacion")
                        val calif = when (califRaw) {
                            is Number -> califRaw.toInt()
                            is String -> califRaw.toIntOrNull()
                            else -> null
                        } ?: continue
                        val idAsig = ev.getString("idAsignacion") ?: continue
                        val pair = asigMap[idAsig] ?: continue
                        calificaciones[pair] = calif
                    }
                }

                // 6. Calcular promedio por tarea (promedio del grupo en esa materia)
                val promediosPorTarea = tareas.map { (idTarea, nombreTarea) ->
                    val califsEnTarea = alumnoIds.mapNotNull { idAlumno ->
                        calificaciones[Pair(idAlumno, idTarea)]
                    }
                    val promedio = if (califsEnTarea.isNotEmpty())
                        califsEnTarea.sum().toDouble() / califsEnTarea.size.toDouble()
                    else null
                    PromedioMateria(
                        idTarea = idTarea,
                        nombreTarea = nombreTarea,
                        promedio = promedio,
                        totalCalificados = califsEnTarea.size
                    )
                }

                // 7. Promedio general del grupo
                val todasLasCalifs = calificaciones.filter { (key, _) ->
                    alumnoIds.contains(key.first)
                }.values.toList()

                val promedioGeneral = if (todasLasCalifs.isNotEmpty())
                    todasLasCalifs.sum().toDouble() / todasLasCalifs.size.toDouble()
                else null

                val alumnosConCalif = alumnoIds.count { idAlumno ->
                    tareaIds.any { idTarea -> calificaciones.containsKey(Pair(idAlumno, idTarea)) }
                }

                grupos.add(
                    EstadisticaGrupo(
                        idClase = idClase,
                        nombreClase = nombreClase,
                        promedioGeneral = promedioGeneral,
                        promediosPorTarea = promediosPorTarea,
                        totalAlumnos = totalAlumnos,
                        alumnosCalificados = alumnosConCalif
                    )
                )
            } catch (e: Exception) {
                Log.e("ESTADISTICAS_TV", "Error procesando grupo $idClase: ${e.message}")
            }
        }

        _estadisticasState.value = EstadisticasUiState.Success(grupos.sortedBy { it.nombreClase })
        Log.d("ESTADISTICAS_TV", "Estadísticas actualizadas: ${grupos.size} grupos procesados")
    }

    /**
     * Manejador de evento para la accion onCleared.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override fun onCleared() {
        super.onCleared()
        evidenciasListener?.remove()
    }

    /**
     * Guarda o actualiza los datos de saveEvento en la base de datos.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun saveEvento(evento: Evento, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val map = mutableMapOf<String, Any>(
                    "titulo" to evento.titulo,
                    "descripcion" to evento.descripcion,
                    "lugar" to evento.lugar,
                    "fechaEvento" to Timestamp(java.util.Date(evento.fechaPublicacion)),
                    "fechaPublicacion" to evento.fechaPublicacion
                )
                evento.imagenUrl?.let { map["imagenUrl"] = it }
                if (evento.idEvento.isEmpty()) {
                    db.collection("eventos_escolares").add(map).await()
                } else {
                    db.collection("eventos_escolares").document(evento.idEvento).set(map).await()
                }
                // No need to fetchEventos() here since snapshot listener updates automatically
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al guardar evento")
            }
        }
    }

    /**
     * Elimina el registro correspondiente a deleteEvento del sistema.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun deleteEvento(idEvento: String) {
        viewModelScope.launch {
            try {
                db.collection("eventos_escolares").document(idEvento).delete().await()
                // No need to fetchEventos() here since snapshot listener updates automatically
            } catch (e: Exception) {
                _uiState.value = EventosUiState.Error(e.message ?: "Error al eliminar evento")
            }
        }
    }
}