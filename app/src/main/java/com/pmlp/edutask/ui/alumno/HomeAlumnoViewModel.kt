package com.pmlp.edutask.ui.alumno

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.Tarea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

sealed class HomeAlumnoState {
    object Loading : HomeAlumnoState()
    data class Success(
        val clases: List<String>,
        val tareas: List<Pair<Tarea, EstadoEvidencia>>
    ) : HomeAlumnoState()
    data class Error(val message: String) : HomeAlumnoState()
}

class HomeAlumnoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeAlumnoState>(HomeAlumnoState.Loading)
    val uiState: StateFlow<HomeAlumnoState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    fun fetchUserData(idUsuario: String) {
        if (idUsuario.isBlank()) {
            _uiState.value = HomeAlumnoState.Error("ID de usuario no válido")
            return
        }

        viewModelScope.launch {
            _uiState.value = HomeAlumnoState.Loading
            try {
                // 1. Fetch asignaciones
                val asignacionesSnapshot = db.collection("asignaciones_tarea")
                    .whereEqualTo("idUsuario", idUsuario)
                    .get()
                    .await()

                if (asignacionesSnapshot.isEmpty) {
                    _uiState.value = HomeAlumnoState.Success(emptyList(), emptyList())
                    return@launch
                }

                val paresTareas = mutableListOf<Pair<Tarea, EstadoEvidencia>>()
                val clasesSet = mutableSetOf<String>()

                // Cache para las clases y tareas
                val clasesCache = mutableMapOf<String, String>()
                val tareasCache = mutableMapOf<String, Tarea>()

                for (asignacionDoc in asignacionesSnapshot.documents) {
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
                        val fecha = fechaTimestamp?.toDate() ?: Date()

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

                    // 4. Fetch Evidencia
                    val evidenciasSnapshot = db.collection("evidencias_tarea")
                        .whereEqualTo("idAsignacion", idAsignacion)
                        .get()
                        .await()

                    var estadoEvidencia = EstadoEvidencia.Pendiente
                    if (!evidenciasSnapshot.isEmpty) {
                        val evidenciaDoc = evidenciasSnapshot.documents[0]
                        val estadoStr = evidenciaDoc.getString("estado")
                        estadoEvidencia = when (estadoStr) {
                            "Aprobada" -> EstadoEvidencia.Aprobada
                            "Rechazada" -> EstadoEvidencia.Rechazada
                            else -> EstadoEvidencia.Pendiente
                        }
                    }

                    paresTareas.add(Pair(tarea, estadoEvidencia))
                }

                paresTareas.sortBy { it.first.fechaLimite }

                _uiState.value = HomeAlumnoState.Success(
                    clases = clasesSet.toList().sorted(),
                    tareas = paresTareas
                )

            } catch (e: Exception) {
                _uiState.value = HomeAlumnoState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
