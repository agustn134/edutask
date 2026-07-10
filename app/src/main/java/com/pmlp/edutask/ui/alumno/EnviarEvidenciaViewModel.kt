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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
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
                // 1. Convertir bitmap → Base64 en hilo de IO (no bloquea la UI)
                val fotoBase64 = withContext(Dispatchers.IO) { bitmapToBase64(bitmap) }

                // 2. Verificar que el tamaño no exceda el límite de Firestore (1 MiB)
                if (fotoBase64.length > 900_000) {
                    _uiState.value = EnviarEvidenciaUiState.Error(
                        "La imagen es demasiado grande. Intenta con una foto de menor resolución."
                    )
                    return@launch
                }

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

    // ── Helper privado: Bitmap → Base64 String (máx. 800×800, 60% JPEG) ──────
    private fun bitmapToBase64(bitmap: Bitmap): String {
        // Escalar si la imagen supera 800x800 para asegurar que quepa en Firestore
        val scaled = if (bitmap.width > 800 || bitmap.height > 800) {
            val ratio = minOf(800f / bitmap.width, 800f / bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else bitmap

        val outputStream = ByteArrayOutputStream()
        // 60% calidad JPEG: buen equilibrio entre nitidez y tamaño (<800KB Base64)
        scaled.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        if (scaled != bitmap) scaled.recycle() // liberar memoria del Bitmap escalado
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
