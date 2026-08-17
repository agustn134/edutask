package com.pmlp.edutask.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
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

                        // Extraer fecha del campo 'fechaEvento' o 'fechaPublicacion' si existe
                        val timestamp = doc.getTimestamp("fechaEvento")
                        val fechaMillis = timestamp?.toDate()?.time
                            ?: doc.getLong("fechaPublicacion")
                            ?: System.currentTimeMillis()

                        Evento(
                            idEvento = id,
                            titulo = titulo,
                            descripcion = descripcion,
                            fechaPublicacion = fechaMillis
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

    fun saveEvento(evento: Evento, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val map = mapOf(
                    "titulo" to evento.titulo,
                    "descripcion" to evento.descripcion,
                    "fechaEvento" to Timestamp(java.util.Date(evento.fechaPublicacion)),
                    "fechaPublicacion" to evento.fechaPublicacion
                )
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