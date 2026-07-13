/* EduTask — Wear OS
 * Módulo de calificación rápida para el profesor.
 * Pantallas: lista de evidencias pendientes → calificación con un toque.
 */

package com.pmlp.wear.presentation

import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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

        // Request notifications permissions for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Start background task notification service
        try {
            val serviceIntent = Intent(this, TaskNotificationService::class.java)
            startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
        val fotos: List<String>,
        val tieneArchivosNoImagen: Boolean
    ) : WearDestino()
    data class VerFoto(val fotos: List<String>, val prevDestino: WearDestino) : WearDestino()
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

    val prefs = remember(context) { context.getSharedPreferences("edutask_wear_prefs", Context.MODE_PRIVATE) }
    var nombreProfesor by remember { mutableStateOf(prefs.getString("nombre", "Sin sincronizar") ?: "Sin sincronizar") }
    var idProfesor by remember { mutableStateOf(prefs.getString("idUsuario", "profesor_001") ?: "profesor_001") }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "nombre") {
                nombreProfesor = sharedPreferences.getString("nombre", "Sin sincronizar") ?: "Sin sincronizar"
            }
            if (key == "idUsuario") {
                idProfesor = sharedPreferences.getString("idUsuario", "profesor_001") ?: "profesor_001"
                vm.cargarPendientes()
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    // Sincronización en la nube vía Firestore (Fallback)
    DisposableEffect(Unit) {
        val listener = db.collection("sesion_wear").document("default")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val idUsuarioVal = snapshot.getString("idUsuario") ?: ""
                    val nombreVal = snapshot.getString("nombre") ?: ""
                    if (idUsuarioVal.isNotEmpty() && nombreVal.isNotEmpty()) {
                        prefs.edit()
                            .putString("idUsuario", idUsuarioVal)
                            .putString("nombre", nombreVal)
                            .apply()
                        
                        idProfesor = idUsuarioVal
                        nombreProfesor = nombreVal
                        vm.cargarPendientes()
                    }
                }
            }
        onDispose {
            listener.remove()
        }
    }

    DisposableEffect(idProfesor) {
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
                                                fotos = fotos,
                                                tieneArchivosNoImagen = tieneArchivosNoImagen
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
                nombreProfesor = nombreProfesor,
                items         = items,
                onSeleccionar = { evidencia ->
                    destino = WearDestino.NuevaEntrega(
                        idEvidencia = evidencia.id,
                        nombreAlumno = evidencia.nombreAlumno,
                        tituloTarea = evidencia.tituloTarea,
                        fotos = evidencia.fotos,
                        tieneArchivosNoImagen = evidencia.tieneArchivosNoImagen
                    )
                },
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
                tieneArchivosNoImagen = dest.tieneArchivosNoImagen,
                tieneFoto = dest.fotos.isNotEmpty(),
                onVerFoto = {
                    destino = WearDestino.VerFoto(fotos = dest.fotos, prevDestino = dest)
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
                fotos = dest.fotos,
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
    tieneArchivosNoImagen: Boolean,
    tieneFoto: Boolean,
    onVerFoto: () -> Unit,
    onCalificar: () -> Unit,
    onVolver: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Nueva Entrega",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "$nombreAlumno entregó:\n$tituloTarea",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (tieneArchivosNoImagen) {
                Text(
                    text = "Subió archivos. Ver en el móvil.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (!tieneFoto) {
                Text(
                    text = "Debe evaluar esta entrega en el móvil.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
fun VerFotoScreen(fotos: List<String>, onVolver: () -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    val foto = remember(fotos, currentIndex) { fotos.getOrNull(currentIndex) ?: "" }
    val isUrl = remember(foto) { foto.startsWith("http://") || foto.startsWith("https://") }
    val bitmap = remember(foto) {
        if (!isUrl && foto.isNotEmpty()) decodeBase64ToBitmap(foto) else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Evidencia Foto ${currentIndex + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else if (isUrl) {
                AsyncImage(
                    model = foto,
                    contentDescription = "Evidencia Foto ${currentIndex + 1}",
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
        }

        // Overlay elements: Page Indicator and Navigation buttons
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (fotos.size > 1) {
                Text(
                    text = "${currentIndex + 1} / ${fotos.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                if (fotos.size > 1) {
                    Button(
                        onClick = { currentIndex = if (currentIndex > 0) currentIndex - 1 else fotos.size - 1 },
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("<", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onVolver,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Volver", style = MaterialTheme.typography.labelSmall)
                }

                if (fotos.size > 1) {
                    Button(
                        onClick = { currentIndex = (currentIndex + 1) % fotos.size },
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(">", fontWeight = FontWeight.Bold)
                    }
                }
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