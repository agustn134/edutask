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
        archivoBytes: ByteArray? = null,
        nombreArchivo: String? = null,
        textoEvidencia: String = "",
        bitmap: Bitmap? = null
    ) {
        if (idAsignacion.isBlank()) {
            _uiState.value = EnviarEvidenciaUiState.Error("ID de asignación inválido.")
            return
        }

        if (textoEvidencia.isBlank() && archivoBytes == null && bitmap == null) {
            _uiState.value = EnviarEvidenciaUiState.Error("Debes enviar un texto, enlace o un archivo adjunto.")
            return
        }

        viewModelScope.launch {
            _uiState.value = EnviarEvidenciaUiState.Uploading
            try {
                var finalBase64: String? = null

                if (bitmap != null) {
                    finalBase64 = withContext(Dispatchers.IO) { bitmapToBase64(bitmap) }
                } else if (archivoBytes != null) {
                    finalBase64 = withContext(Dispatchers.IO) { bytesToBase64(archivoBytes) }
                }

                // Verificar que el tamaño no exceda el límite seguro para Firestore (~900KB en Base64)
                if (finalBase64 != null && finalBase64.length > 900_000) {
                    _uiState.value = EnviarEvidenciaUiState.Error(
                        "El archivo es demasiado grande. Intenta con un archivo más pequeño."
                    )
                    return@launch
                }

                // Guardar en Firestore colección "evidencias_tarea"
                val evidenciaData = hashMapOf<String, Any>(
                    "idAsignacion" to idAsignacion,
                    "tituloTarea"  to tituloTarea,
                    "nombreAlumno" to nombreAlumno,
                    "fechaEnvio"   to FieldValue.serverTimestamp(),
                    "estado"       to "Pendiente"
                )

                if (finalBase64 != null) {
                    // Usamos "fotoBase64" por retrocompatibilidad con la pantalla del profesor
                    evidenciaData["fotoBase64"] = finalBase64
                    evidenciaData["nombreArchivo"] = nombreArchivo ?: "evidencia"
                }

                if (textoEvidencia.isNotBlank()) {
                    evidenciaData["textoEvidencia"] = textoEvidencia
                }

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
        scaled.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        if (scaled != bitmap) scaled.recycle() 
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    // ── Helper privado: ByteArray → Base64 String ────────────────────────────
    private fun bytesToBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
