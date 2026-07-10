package com.pmlp.edutask.ui.alumno

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

// ── Estados de la UI ────────────────────────────────────────────────────────
sealed class EnviarEvidenciaUiState {
    object Idle      : EnviarEvidenciaUiState()
    object Uploading : EnviarEvidenciaUiState()
    object Success   : EnviarEvidenciaUiState()
    data class Error(val mensaje: String) : EnviarEvidenciaUiState()
}

class EnviarEvidenciaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EnviarEvidenciaUiState>(EnviarEvidenciaUiState.Idle)
    val uiState: StateFlow<EnviarEvidenciaUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    // ── Enviar evidencia ─────────────────────────────────────────────────────
    fun enviarEvidencia(
        idAsignacion: String,
        nombreAlumno: String,
        tituloTarea:  String,
        bitmap:       Bitmap
    ) {
        if (idAsignacion.isBlank()) {
            _uiState.value = EnviarEvidenciaUiState.Error("ID de asignación inválido.")
            return
        }

        viewModelScope.launch {
            _uiState.value = EnviarEvidenciaUiState.Uploading
            try {
                // 1. Convertir bitmap → Base64 con compresión JPEG al 50%
                val fotoBase64 = bitmapToBase64(bitmap)

                // 2. Guardar en Firestore colección "evidencias_tarea"
                val evidenciaData = hashMapOf(
                    "idAsignacion" to idAsignacion,
                    "tituloTarea"  to tituloTarea,
                    "nombreAlumno" to nombreAlumno,
                    "fechaEnvio"   to FieldValue.serverTimestamp(),
                    "estado"       to "Pendiente",
                    "fotoBase64"   to fotoBase64
                )

                db.collection("evidencias_tarea")
                    .add(evidenciaData)
                    .await()

                _uiState.value = EnviarEvidenciaUiState.Success

            } catch (e: Exception) {
                _uiState.value = EnviarEvidenciaUiState.Error(
                    e.message ?: "Error desconocido al subir la evidencia."
                )
            }
        }
    }

    // ── Resetear estado (para re-intentar o navegar atrás) ──────────────────
    fun resetState() {
        _uiState.value = EnviarEvidenciaUiState.Idle
    }

    // ── Helper privado: Bitmap → Base64 String ───────────────────────────────
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Compresión al 50% para mantenerse dentro del límite de 1 MiB de Firestore
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
