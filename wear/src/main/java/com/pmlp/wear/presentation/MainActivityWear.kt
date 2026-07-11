/* EduTask — Wear OS
 * Módulo de calificación rápida para el profesor.
 * Pantallas: lista de evidencias pendientes → calificación con un toque.
 */

package com.pmlp.wear.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import coil.compose.AsyncImage
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.pmlp.wear.presentation.theme.EdutaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EdutaskTheme {
                EduTaskWearApp()
            }
        }
    }
}

// ── Navegación simple entre pantallas ────────────────────────────────────────
private sealed class WearDestino {
    object Pendientes                          : WearDestino()
    data class Calificar(val e: EvidenciaPendiente) : WearDestino()
    data class NuevaEntrega(
        val idEvidencia: String,
        val nombreAlumno: String,
        val tituloTarea: String,
        val foto: String
    ) : WearDestino()
    data class VerFoto(val foto: String, val prevDestino: WearDestino) : WearDestino()
}

@Composable
fun EduTaskWearApp() {
    val vm: CalificarViewModel = viewModel()
    val uiState by vm.uiState.collectAsState()

    var destino: WearDestino by remember { mutableStateOf(WearDestino.Pendientes) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    var isFirstLoad by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val prefs = context.getSharedPreferences("edutask_wear_prefs", Context.MODE_PRIVATE)
        val idProfesor = prefs.getString("idUsuario", "profesor_001") ?: "profesor_001"

        val listener = db.collection("evidencias_tarea")
            .orderBy("fechaEnvio", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                if (isFirstLoad) {
                    isFirstLoad = false
                    return@addSnapshotListener
                }

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val estado = doc.getString("estado") ?: "Pendiente"
                    val idAsignacion = doc.getString("idAsignacion") ?: ""

                    if (estado == "Pendiente") {
                        // Check if this assignment belongs to this professor in a coroutine
                        scope.launch {
                            try {
                                val assignDoc = db.collection("asignaciones_tarea").document(idAsignacion).get().await()
                                val idTarea = assignDoc.getString("idTarea") ?: ""
                                if (idTarea.isNotEmpty()) {
                                    val taskDoc = db.collection("tareas").document(idTarea).get().await()
                                    val idClase = taskDoc.getString("idClase") ?: ""
                                    if (idClase.isNotEmpty()) {
                                        val classDoc = db.collection("clases").document(idClase).get().await()
                                        val classProfId = classDoc.getString("idUsuario") ?: ""
                                        if (classProfId == idProfesor) {
                                            val nombreAlumno = doc.getString("nombreAlumno") ?: "Alumno"
                                            val tituloTarea = doc.getString("tituloTarea") ?: "Tarea"
                                            val foto = doc.getString("fotoBase64") ?: doc.getString("fotoUrl") ?: ""
                                            val idEvidencia = doc.id

                                            // Trigger haptic physical motor vibration (400ms duration)
                                            try {
                                                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    vibrator.vibrate(400)
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }

                                            // Shift to NuevaEntrega screen
                                            destino = WearDestino.NuevaEntrega(
                                                idEvidencia = idEvidencia,
                                                nombreAlumno = nombreAlumno,
                                                tituloTarea = tituloTarea,
                                                foto = foto
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        onDispose { listener.remove() }
    }

    when (val dest = destino) {

        // ── Lista de evidencias pendientes ────────────────────────────────
        is WearDestino.Pendientes -> {
            val items = when (val s = uiState) {
                is CalificarUiState.ListaLista -> s.items
                else                           -> emptyList()
            }
            PendientesScreen(
                items         = items,
                onSeleccionar = { evidencia -> destino = WearDestino.Calificar(evidencia) },
                onRefrescar   = { vm.cargarPendientes() }
            )
        }

        // ── Pantalla de calificación rápida ───────────────────────────────
        is WearDestino.Calificar -> {
            val cargando = uiState is CalificarUiState.Cargando

            CalificarScreen(
                evidencia   = dest.e,
                esCargando  = cargando,
                onCalificar = { nota ->
                    vm.calificar(dest.e.id, nota) {
                        // Tras guardar: volver a la lista actualizada
                        vm.cargarPendientes()
                        destino = WearDestino.Pendientes
                    }
                },
                onVolver    = { destino = WearDestino.Pendientes }
            )
        }

        // ── Nueva Entrega (Smart Stack Detail) ─────────────────────────────
        is WearDestino.NuevaEntrega -> {
            NuevaEntregaScreen(
                nombreAlumno = dest.nombreAlumno,
                tituloTarea = dest.tituloTarea,
                onVerFoto = {
                    destino = WearDestino.VerFoto(foto = dest.foto, prevDestino = dest)
                },
                onCalificar = {
                    destino = WearDestino.Calificar(
                        EvidenciaPendiente(
                            id = dest.idEvidencia,
                            nombreAlumno = dest.nombreAlumno,
                            tituloTarea = dest.tituloTarea
                        )
                    )
                },
                onVolver = {
                    destino = WearDestino.Pendientes
                }
            )
        }

        // ── Visualización de Evidencia Fullscreen ─────────────────────────
        is WearDestino.VerFoto -> {
            VerFotoScreen(
                foto = dest.foto,
                onVolver = {
                    destino = dest.prevDestino
                }
            )
        }
    }
}

@Composable
fun NuevaEntregaScreen(
    nombreAlumno: String,
    tituloTarea: String,
    onVerFoto: () -> Unit,
    onCalificar: () -> Unit,
    onVolver: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Nueva Entrega",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "$nombreAlumno entregó:\n$tituloTarea",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onVerFoto,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Ver Foto", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onCalificar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Calificar", style = MaterialTheme.typography.labelSmall)
                }
            }

            TextButton(
                onClick = onVolver,
                modifier = Modifier.height(24.dp)
            ) {
                Text("Descartar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun VerFotoScreen(foto: String, onVolver: () -> Unit) {
    val isUrl = remember(foto) { foto.startsWith("http://") || foto.startsWith("https://") }
    val bitmap = remember(foto) {
        if (!isUrl) decodeBase64ToBitmap(foto) else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (isUrl) {
            AsyncImage(
                model = foto,
                contentDescription = "Evidencia Alumno",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Evidencia Alumno",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = "Sin evidencia fotográfica",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = onVolver,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Volver", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val cleanString = if (base64Str.contains(",")) {
            base64Str.substring(base64Str.indexOf(",") + 1)
        } else {
            base64Str
        }
        val decodedBytes = Base64.decode(cleanString, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}