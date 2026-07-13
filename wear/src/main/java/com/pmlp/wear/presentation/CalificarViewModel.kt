package com.pmlp.wear.presentation

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── Modelo de datos ──────────────────────────────────────────────────────────
data class EvidenciaPendiente(
    val id:           String = "",
    val nombreAlumno: String = "",
    val tituloTarea:  String = "",
    val idAsignacion: String = "",
    val fotos:        List<String> = emptyList(),
    val tieneArchivosNoImagen: Boolean = false
)

// ── Estados UI ───────────────────────────────────────────────────────────────
sealed class CalificarUiState {
    object Cargando    : CalificarUiState()
    object Idle        : CalificarUiState()
    object Exito       : CalificarUiState()
    data class Error(val msg: String) : CalificarUiState()
    data class ListaLista(val items: List<EvidenciaPendiente>) : CalificarUiState()
}

class CalificarViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<CalificarUiState>(CalificarUiState.Cargando)
    val uiState: StateFlow<CalificarUiState> = _uiState.asStateFlow()

    // ── Cargar evidencias pendientes ─────────────────────────────────────────
    init { cargarPendientes() }

    fun cargarPendientes() {
        viewModelScope.launch {
            _uiState.value = CalificarUiState.Cargando
            try {
                // Retrieve synchronized professor ID from SharedPreferences (default: "profesor_001")
                val prefs = getApplication<Application>().getSharedPreferences("edutask_wear_prefs", Context.MODE_PRIVATE)
                val idProfesor = prefs.getString("idUsuario", "profesor_001") ?: "profesor_001"

                // 1. Fetch classes of this professor
                val classesSnap = db.collection("clases")
                    .whereEqualTo("idUsuario", idProfesor)
                    .get()
                    .await()
                val classIds = classesSnap.documents.map { it.id }

                if (classIds.isEmpty()) {
                    _uiState.value = CalificarUiState.ListaLista(emptyList())
                    return@launch
                }

                // 2. Fetch tasks for these classes
                val tasksSnap = db.collection("tareas")
                    .whereIn("idClase", classIds)
                    .get()
                    .await()
                val taskIds = tasksSnap.documents.map { it.id }

                if (taskIds.isEmpty()) {
                    _uiState.value = CalificarUiState.ListaLista(emptyList())
                    return@launch
                }

                // 3. Fetch assignments matching these tasks
                val assignmentsSnap = db.collection("asignaciones_tarea")
                    .get()
                    .await()
                val assignmentIds = assignmentsSnap.documents
                    .filter { doc -> (doc.getString("idTarea") ?: "") in taskIds }
                    .map { doc -> doc.id }

                if (assignmentIds.isEmpty()) {
                    _uiState.value = CalificarUiState.ListaLista(emptyList())
                    return@launch
                }

                // 4. Fetch pending evidence matching these assignments
                val evidencesSnap = db.collection("evidencias_tarea")
                    .whereEqualTo("estado", "Pendiente")
                    .get()
                    .await()

                val lista = evidencesSnap.documents
                    .filter { doc -> (doc.getString("idAsignacion") ?: "") in assignmentIds }
                    .map { doc ->
                        val fotos = mutableListOf<String>()
                        val legacyFoto = doc.getString("fotoBase64") ?: doc.getString("fotoUrl") ?: ""
                        if (legacyFoto.isNotEmpty()) {
                            fotos.add(legacyFoto)
                        }
                        var tieneArchivosNoImagen = false
                        val archivosRaw = doc.get("archivos") as? List<*>
                        archivosRaw?.forEach { item ->
                            if (item is Map<*, *>) {
                                val nombre = item["nombre"]?.toString() ?: ""
                                val base64 = item["base64"]?.toString() ?: ""
                                val isImage = nombre.lowercase().run {
                                    endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") || endsWith(".webp") || endsWith(".gif")
                                }
                                if (isImage && base64.isNotEmpty()) {
                                    fotos.add(base64)
                                }
                                if (!isImage && base64.isNotEmpty()) {
                                    tieneArchivosNoImagen = true
                                }
                            }
                        }
                        EvidenciaPendiente(
                            id           = doc.id,
                            nombreAlumno = doc.getString("nombreAlumno") ?: "Alumno",
                            tituloTarea  = doc.getString("tituloTarea")  ?: "Sin título",
                            idAsignacion = doc.getString("idAsignacion") ?: "",
                            fotos        = fotos,
                            tieneArchivosNoImagen = tieneArchivosNoImagen
                        )
                    }

                _uiState.value = CalificarUiState.ListaLista(lista)

            } catch (e: Exception) {
                _uiState.value = CalificarUiState.Error(e.message ?: "Error de red")
            }
        }
    }

    // ── Guardar calificación ─────────────────────────────────────────────────
    fun calificar(idEvidencia: String, nota: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = CalificarUiState.Cargando
            try {
                // Update evidencias_tarea
                db.collection("evidencias_tarea")
                    .document(idEvidencia)
                    .update(
                        "estado",          if (nota > 0) "Aprobada" else "Rechazada",
                        "calificacion",    nota,
                        "fechaCalificada", FieldValue.serverTimestamp()
                    )
                    .await()

                // Fetch evidence's idUsuario (student ID) if possible
                val evDoc = db.collection("evidencias_tarea").document(idEvidencia).get().await()
                val idUsuario = evDoc.getString("idUsuario") ?: ""

                // Add to calificaciones collection
                val califDoc = hashMapOf(
                    "idEvidencia" to idEvidencia,
                    "idUsuario" to idUsuario,
                    "valor" to nota,
                    "comentario" to "Calificado desde Wear OS",
                    "esBorrador" to false,
                    "fechaCalificacion" to FieldValue.serverTimestamp()
                )
                db.collection("calificaciones").add(califDoc).await()

                _uiState.value = CalificarUiState.Exito
                onDone()

            } catch (e: Exception) {
                _uiState.value = CalificarUiState.Error(e.message ?: "Error al guardar")
            }
        }
    }

    fun resetEstado() { _uiState.value = CalificarUiState.Idle }
}
