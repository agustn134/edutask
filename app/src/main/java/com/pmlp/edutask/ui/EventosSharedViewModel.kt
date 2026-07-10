package com.pmlp.edutask.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pmlp.edutask.model.Evento
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

class EventosSharedViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<EventosUiState>(EventosUiState.Idle)
    val uiState: StateFlow<EventosUiState> = _uiState

    init {
        fetchEventos()
    }

    fun fetchEventos() {
        viewModelScope.launch {
            _uiState.value = EventosUiState.Loading
            try {
                // Ordenar por fecha, los más recientes primero
                val snapshot = db.collection("eventos")
                    .orderBy("fechaPublicacion", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    
                val list = snapshot.documents.mapNotNull { doc ->
                    Evento(
                        idEvento = doc.id,
                        titulo = doc.getString("titulo") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        fechaPublicacion = doc.getLong("fechaPublicacion") ?: System.currentTimeMillis()
                    )
                }
                _uiState.value = EventosUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = EventosUiState.Error(e.message ?: "Error al obtener eventos")
            }
        }
    }

    fun saveEvento(evento: Evento, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val map = mapOf(
                    "titulo" to evento.titulo,
                    "descripcion" to evento.descripcion,
                    "fechaPublicacion" to evento.fechaPublicacion
                )
                if (evento.idEvento.isEmpty()) {
                    db.collection("eventos").add(map).await()
                } else {
                    db.collection("eventos").document(evento.idEvento).set(map).await()
                }
                fetchEventos() // Refresh
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al guardar evento")
            }
        }
    }

    fun deleteEvento(idEvento: String) {
        viewModelScope.launch {
            try {
                db.collection("eventos").document(idEvento).delete().await()
                fetchEventos() // Refresh
            } catch (e: Exception) {
                _uiState.value = EventosUiState.Error(e.message ?: "Error al eliminar evento")
            }
        }
    }
}
