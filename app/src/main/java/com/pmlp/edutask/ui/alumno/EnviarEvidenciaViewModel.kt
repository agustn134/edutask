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
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

// ── Estados de la UI ────────────────────────────────────────────────────────
sealed class EnviarEvidenciaUiState {
    object Idle      : EnviarEvidenciaUiState()
    object Uploading : EnviarEvidenciaUiState()
    object Success   : EnviarEvidenciaUiState()
    data class Error(val mensaje: String) : EnviarEvidenciaUiState()
}

data class EvidenciaEnviadaData(
    val idEvidencia: String,
    val estado: String,
    val nombreArchivo: String?,
    val textoEvidencia: String?,
    val fotoBase64: String?,
    val archivos: List<Map<String, String>>,
    val vinculos: List<String>,
    val comentarioProfesor: String? = null,
    val calificacionProfesor: Int? = null
)

data class ArchivoSubir(
    val uri: Uri? = null,
    val bitmap: Bitmap? = null,
    val nombre: String
)

class EnviarEvidenciaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EnviarEvidenciaUiState>(EnviarEvidenciaUiState.Idle)
    val uiState: StateFlow<EnviarEvidenciaUiState> = _uiState.asStateFlow()

    private val _evidenciaEnviada = MutableStateFlow<EvidenciaEnviadaData?>(null)
    val evidenciaEnviada: StateFlow<EvidenciaEnviadaData?> = _evidenciaEnviada.asStateFlow()

    private val _isLoadingEvidencia = MutableStateFlow(false)
    val isLoadingEvidencia: StateFlow<Boolean> = _isLoadingEvidencia.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    fun cargarEvidenciaEnviada(idEvidencia: String) {
        if (idEvidencia.isBlank()) return
        viewModelScope.launch {
            _isLoadingEvidencia.value = true
            try {
                val doc = db.collection("evidencias_tarea").document(idEvidencia).get().await()
                if (doc.exists()) {
                    val estado = doc.getString("estado") ?: "Pendiente"
                    var comentarioProfesor: String? = null
                    var calificacionProfesor: Int? = null

                    if (estado != "Pendiente") {
                        val califSnapshot = db.collection("calificaciones")
                            .whereEqualTo("idEvidencia", idEvidencia)
                            .get().await()
                        if (!califSnapshot.isEmpty) {
                            val califDoc = califSnapshot.documents[0]
                            comentarioProfesor = califDoc.getString("comentario")
                            calificacionProfesor = califDoc.getLong("valor")?.toInt()
                        }
                    }

                    _evidenciaEnviada.value = EvidenciaEnviadaData(
                        idEvidencia = doc.id,
                        estado = estado,
                        nombreArchivo = doc.getString("nombreArchivo"),
                        textoEvidencia = doc.getString("textoEvidencia"),
                        fotoBase64 = doc.getString("fotoBase64"),
                        archivos = (doc.get("archivos") as? List<Map<String, String>>) ?: emptyList(),
                        vinculos = (doc.get("vinculos") as? List<String>) ?: emptyList(),
                        comentarioProfesor = comentarioProfesor,
                        calificacionProfesor = calificacionProfesor
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingEvidencia.value = false
            }
        }
    }

    fun anularEvidencia(idEvidencia: String, onSuccess: () -> Unit) {
        if (idEvidencia.isBlank()) return
        viewModelScope.launch {
            _isLoadingEvidencia.value = true
            try {
                db.collection("evidencias_tarea").document(idEvidencia).delete().await()
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = EnviarEvidenciaUiState.Error("Error al anular la entrega: ${e.message}")
            } finally {
                _isLoadingEvidencia.value = false
            }
        }
    }

    // ── Enviar evidencia ─────────────────────────────────────────────────────
    fun enviarEvidencia(
        context: android.content.Context,
        idAsignacion: String,
        nombreAlumno: String,
        tituloTarea:  String,
        archivosSubir: List<ArchivoSubir>,
        vinculos: List<String>,
        textoEvidencia: String
    ) {
        if (idAsignacion.isBlank()) {
            _uiState.value = EnviarEvidenciaUiState.Error("ID de asignación inválido.")
            return
        }

        if (textoEvidencia.isBlank() && archivosSubir.isEmpty() && vinculos.isEmpty()) {
            _uiState.value = EnviarEvidenciaUiState.Error("Debes enviar un texto, enlace o un archivo adjunto.")
            return
        }

        if (archivosSubir.size > 3) {
            _uiState.value = EnviarEvidenciaUiState.Error("Máximo 3 archivos permitidos.")
            return
        }

        viewModelScope.launch {
            _uiState.value = EnviarEvidenciaUiState.Uploading
            try {
                val subidos = mutableListOf<Map<String, String>>()

                // Convertir cada archivo a Base64
                for (archivo in archivosSubir) {
                    val base64String = withContext(Dispatchers.IO) {
                        try {
                            if (archivo.bitmap != null) {
                                val baos = ByteArrayOutputStream()
                                archivo.bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                                val bytes = baos.toByteArray()
                                Base64.encodeToString(bytes, Base64.NO_WRAP)
                            } else if (archivo.uri != null) {
                                context.contentResolver.openInputStream(archivo.uri)?.use { input ->
                                    val bytes = input.readBytes()
                                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                                } ?: ""
                            } else {
                                ""
                            }
                        } catch (e: Exception) {
                            ""
                        }
                    }
                    if (base64String.isNotBlank()) {
                        subidos.add(mapOf("nombre" to archivo.nombre, "base64" to base64String))
                    }
                }

                // Guardar en Firestore colección "evidencias_tarea"
                val evidenciaData = hashMapOf<String, Any>(
                    "idAsignacion" to idAsignacion,
                    "tituloTarea"  to tituloTarea,
                    "nombreAlumno" to nombreAlumno,
                    "fechaEnvio"   to FieldValue.serverTimestamp(),
                    "estado"       to "Pendiente",
                    "archivos"     to subidos,
                    "vinculos"     to vinculos
                )

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
