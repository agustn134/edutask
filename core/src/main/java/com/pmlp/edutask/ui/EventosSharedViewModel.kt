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