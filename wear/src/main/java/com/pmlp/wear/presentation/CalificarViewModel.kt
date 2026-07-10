package com.pmlp.wear.presentation

import androidx.lifecycle.ViewModel
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
    val idAsignacion: String = ""
)

// ── Estados UI ───────────────────────────────────────────────────────────────
sealed class CalificarUiState {
    object Cargando    : CalificarUiState()
    object Idle        : CalificarUiState()
    object Exito       : CalificarUiState()
    data class Error(val msg: String) : CalificarUiState()
    data class ListaLista(val items: List<EvidenciaPendiente>) : CalificarUiState()
}

class CalificarViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<CalificarUiState>(CalificarUiState.Cargando)
    val uiState: StateFlow<CalificarUiState> = _uiState.asStateFlow()

    // ── Cargar evidencias pendientes ─────────────────────────────────────────
    init { cargarPendientes() }

    fun cargarPendientes() {
        viewModelScope.launch {
            _uiState.value = CalificarUiState.Cargando
            try {
                val snap = db.collection("evidencias_tarea")
                    .whereEqualTo("estado", "Pendiente")
                    .get()
                    .await()

                val lista = snap.documents.map { doc ->
                    EvidenciaPendiente(
                        id           = doc.id,
                        nombreAlumno = doc.getString("nombreAlumno") ?: "Alumno",
                        tituloTarea  = doc.getString("tituloTarea")  ?: "Sin título",
                        idAsignacion = doc.getString("idAsignacion") ?: ""
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
                db.collection("evidencias_tarea")
                    .document(idEvidencia)
                    .update(
                        "estado",          if (nota > 0) "Aprobada" else "Rechazada",
                        "calificacion",    nota,
                        "fechaCalificada", FieldValue.serverTimestamp()
                    )
                    .await()

                _uiState.value = CalificarUiState.Exito
                onDone()

            } catch (e: Exception) {
                _uiState.value = CalificarUiState.Error(e.message ?: "Error al guardar")
            }
        }
    }

    fun resetEstado() { _uiState.value = CalificarUiState.Idle }
}
