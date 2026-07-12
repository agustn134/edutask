package com.pmlp.edutask.ui.alumno

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.ui.theme.EduTaskTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.provider.OpenableColumns

// ── Pantalla principal ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnviarEvidenciaScreen(
    tarea:        Tarea,
    idAsignacion: String,
    nombreAlumno: String,
    viewModel:    EnviarEvidenciaViewModel = viewModel(),
    onBack:       () -> Unit = {}
) {
    val context  = LocalContext.current
    val uiState  by viewModel.uiState.collectAsState()

    // Bitmap de la foto capturada (null = sin foto aún)
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Archivo genérico adjunto (para documentos no-imagen)
    var archivoUri by remember { mutableStateOf<Uri?>(null) }
    var nombreArchivo by remember { mutableStateOf<String?>(null) }
    var textoEvidencia by remember { mutableStateOf("") }

    // URI temporal para escribir la foto de la cámara
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val getFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            archivoUri = uri
            fotoBitmap = null // limpiar foto si seleccionan archivo
            nombreArchivo = getFileName(context, uri)
        }
    }
    
    // ── Launcher para la galería (con fallback para compatibilidad) ─────────────
    // PickVisualMedia es el selector moderno (Android 11+). En dispositivos que
    // no lo soporten (algunos ROMs como ColorOS), usamos GetContent como respaldo.
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { 
            fotoBitmap = decodeUriToSafeBitmap(context, it) 
            archivoUri = null
            nombreArchivo = getFileName(context, it)
        }
    }

    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            fotoBitmap = decodeUriToSafeBitmap(context, it) 
            archivoUri = null
            nombreArchivo = getFileName(context, it)
        }
    }

    // ── Dialogo de éxito ─────────────────────────────────────────────────────
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is EnviarEvidenciaUiState.Success) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon    = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary) },
            title   = { Text("¡Evidencia Enviada!", style = MaterialTheme.typography.headlineSmall) },
            text    = {
                Text(
                    "Tu evidencia ha sido enviada correctamente y está en revisión por el profesor.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    viewModel.resetState()
                    onBack()
                }) { Text("Aceptar") }
            }
        )
    }

    // ── Launcher para la cámara ──────────────────────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            fotoUri?.let { uri ->
                fotoBitmap = decodeUriToSafeBitmap(context, uri)
                archivoUri = null
                nombreArchivo = "foto_camara.jpg"
            }
        }
    }


    // ── Launchers para Permisos Nativos ──────────────────────────────────────
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = crearUriParaFoto(context)
            fotoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // En Android 12 (API 32) y anterior se requiere READ_EXTERNAL_STORAGE
    // En Android 13+ (API 33) el Photo Picker no necesita ningún permiso
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getContentLauncher.launch("image/*")
        }
    }

    fun abrirGaleria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: PickVisualMedia no necesita permisos, lanzar directo
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
            // Android 11-12 con Photo Picker disponible (via Play Services)
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            // Fallback: Android 12 sin Photo Picker — necesita permiso READ_EXTERNAL_STORAGE
            val permiso = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED) {
                getContentLauncher.launch("image/*")
            } else {
                storagePermissionLauncher.launch(permiso)
            }
        }
    }

    // ── Snackbars de UI ────────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is EnviarEvidenciaUiState.Error) {
            snackbarHostState.showSnackbar(
                message     = (uiState as EnviarEvidenciaUiState.Error).mensaje,
                actionLabel = "OK"
            )
            viewModel.resetState()
        }
    }

    // ── Scaffold principal ───────────────────────────────────────────────────
    Scaffold(
        containerColor  = MaterialTheme.colorScheme.background,
        snackbarHost    = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                title = {
                    Column {
                        Text(
                            "Entregar Evidencia",
                            style    = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            tarea.nombreClase,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // ── Botones de acción ─────────────────────────────────────────
            Surface(
                color       = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Botón Tomar Foto
                        OutlinedButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    val uri = crearUriParaFoto(context)
                                    fotoUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cámara")
                        }

                        // Botón Subir Galería (verifica permisos según versión Android)
                        OutlinedButton(
                            onClick = { abrirGaleria() },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Galería")
                        }
                    }

                    // Botón Archivo (nuevo)
                    OutlinedButton(
                        onClick = { getFileLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Adjuntar Documento")
                    }

                    // Botón Enviar Evidencia
                    Button(
                        onClick = {
                            var bytes: ByteArray? = null
                            archivoUri?.let { uri ->
                                try {
                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                        bytes = input.readBytes()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            viewModel.enviarEvidencia(
                                idAsignacion = idAsignacion,
                                nombreAlumno = nombreAlumno,
                                tituloTarea  = tarea.titulo,
                                archivoBytes = bytes,
                                nombreArchivo = nombreArchivo,
                                textoEvidencia = textoEvidencia,
                                bitmap       = fotoBitmap
                            )
                        },
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(12.dp),
                        enabled   = (fotoBitmap != null || archivoUri != null || textoEvidencia.isNotBlank()) && uiState !is EnviarEvidenciaUiState.Uploading,
                        colors    = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState is EnviarEvidenciaUiState.Uploading) {
                            CircularProgressIndicator(
                                modifier  = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Enviando…", style = MaterialTheme.typography.labelLarge)
                        } else {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Enviar Evidencia", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Card de detalle de la tarea ───────────────────────────────
            ElevatedCard(
                modifier  = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors    = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Encabezado
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape    = MaterialTheme.shapes.medium,
                            color    = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                tarea.titulo,
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines   = 2,
                                overflow   = TextOverflow.Ellipsis
                            )
                            Text(
                                tarea.nombreClase,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Descripción
                    if (tarea.descripcion.isNotBlank()) {
                        Text(
                            tarea.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Fecha límite
                    val fmt = SimpleDateFormat("dd 'de' MMMM 'a las' HH:mm", Locale("es", "MX"))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint     = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Fecha límite: ${fmt.format(tarea.fechaLimite)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // Alumno
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            nombreAlumno,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Zona de texto de evidencia ─────────────────────────────────────
            OutlinedTextField(
                value = textoEvidencia,
                onValueChange = { textoEvidencia = it },
                label = { Text("Texto o Enlace (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            // ── Zona de previsualización del archivo ───────────────────────
            Text(
                "Archivos Adjuntos",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier            = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Placeholder — visible cuando no hay foto ni archivo
                    AnimatedVisibility(
                        visible = fotoBitmap == null && archivoUri == null,
                        enter   = fadeIn(),
                        exit    = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(72.dp),
                                shape    = MaterialTheme.shapes.extraLarge,
                                color    = MaterialTheme.colorScheme.surface
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint     = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                "Toca los botones para adjuntar\ntu evidencia",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // Documento genérico — visible cuando hay archivo pero no foto
                    AnimatedVisibility(
                        visible = archivoUri != null && fotoBitmap == null,
                        enter   = fadeIn() + scaleIn(initialScale = 0.92f),
                        exit    = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                nombreArchivo ?: "Archivo seleccionado",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Imagen capturada — visible cuando hay foto
                    AnimatedVisibility(
                        visible = fotoBitmap != null,
                        enter   = fadeIn() + scaleIn(initialScale = 0.92f),
                        exit    = fadeOut()
                    ) {
                        fotoBitmap?.let { bmp ->
                            Image(
                                bitmap             = bmp.asImageBitmap(),
                                contentDescription = "Foto de evidencia",
                                modifier           = Modifier.fillMaxSize(),
                                contentScale       = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // ── Nota informativa ──────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp),
                        tint     = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Asegúrate de que los archivos o el enlace " +
                        "sean correctos antes de enviar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Espacio extra para el bottom bar
            Spacer(Modifier.height(8.dp))
        }

        // El espacio extra se mantiene si es necesario
    }
}

// ── Helper: crear URI segura para FileProvider ───────────────────────────────
private fun crearUriParaFoto(context: Context): Uri {
    // Usar estrictamente caché interna para evitar el error de "Tarjeta SD" en emuladores
    val cacheDir = File(context.cacheDir, "camera").also { it.mkdirs() }
    val archivo  = File.createTempFile("evidencia_", ".jpg", cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )
}

// ── Helper: Decodificar Bitmap previniendo OutOfMemoryError (Crashes) ──────────
private fun decodeUriToSafeBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        // 1. Decodificar solo los límites para obtener las dimensiones reales
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        
        // 2. Calcular inSampleSize para reducir la imagen (ej: a max 1024x1024)
        val reqWidth = 1024
        val reqHeight = 1024
        var inSampleSize = 1
        
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            val halfHeight: Int = options.outHeight / 2
            val halfWidth: Int = options.outWidth / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        // 3. Decodificar la imagen real escalada
        val finalOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, finalOptions)
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

// ── Helper: Obtener nombre de archivo de la URI ──────────────────────────────
private fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "archivo"
}

// ── Preview ──────────────────────────────────────────────────────────────────
@Preview(name = "Enviar Evidencia", showBackground = true, showSystemUi = true,
         widthDp = 360, heightDp = 800)
@Composable
private fun PreviewEnviarEvidencia() {
    EduTaskTheme(darkTheme = false, dynamicColor = false) {
        EnviarEvidenciaScreen(
            tarea = Tarea(
                idTarea     = "1",
                titulo      = "Actividad 3 — Diagramas UML",
                descripcion = "Fotografía los diagramas de clases y de secuencia de tu libreta.",
                fechaLimite = Date(),
                idClase     = "c1",
                nombreClase = "Programación Móvil PMLP"
            ),
            idAsignacion = "asig_001",
            nombreAlumno = "Juan Ramírez"
        )
    }
}
