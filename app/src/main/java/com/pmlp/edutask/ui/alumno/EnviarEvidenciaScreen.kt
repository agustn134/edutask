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
import java.io.FileOutputStream
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
    idEvidenciaRecibida: String? = null,
    viewModel:    EnviarEvidenciaViewModel = viewModel(),
    onBack:       () -> Unit = {}
) {
    val context  = LocalContext.current
    val uiState  by viewModel.uiState.collectAsState()
    val evidenciaEnviada by viewModel.evidenciaEnviada.collectAsState()
    val isLoadingEvidencia by viewModel.isLoadingEvidencia.collectAsState()

    val now = java.util.Date()
    val isVencida = evidenciaEnviada == null && now.after(tarea.fechaLimite)
    val isReadOnlyMode = evidenciaEnviada != null || isVencida

    LaunchedEffect(idEvidenciaRecibida) {
        if (idEvidenciaRecibida != null) {
            viewModel.cargarEvidenciaEnviada(idEvidenciaRecibida)
        }
    }

    // ── Estados para Múltiples Archivos y Vínculos ───────────────────────────
    var archivosSubir by remember { mutableStateOf(listOf<ArchivoSubir>()) }
    var vinculos by remember { mutableStateOf(listOf<String>()) }
    var nuevoVinculo by remember { mutableStateOf("") }
    var textoEvidencia by remember { mutableStateOf("") }

    // Rellenar estados si estamos en modo lectura
    val actualTexto = if (isReadOnlyMode) evidenciaEnviada?.textoEvidencia ?: "" else textoEvidencia
    val actualVinculos = if (isReadOnlyMode) evidenciaEnviada?.vinculos ?: emptyList() else vinculos
    val actualArchivosEnviados = if (isReadOnlyMode) evidenciaEnviada?.archivos ?: emptyList() else emptyList()
    
    // Bitmap en modo lectura (decodificado de forma asíncrona para no bloquear la UI) - LEGACY
    val base64Foto = evidenciaEnviada?.fotoBase64
    val actualNombreArchivoLegacy = evidenciaEnviada?.nombreArchivo
    val bitmapLectura by produceState<Bitmap?>(initialValue = null, key1 = base64Foto) {
        if (base64Foto != null) {
            value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                decodeBase64ToBitmap(base64Foto)
            }
        } else {
            value = null
        }
    }

    fun addArchivo(archivo: ArchivoSubir) {
        if (archivosSubir.size < 3) {
            archivosSubir = archivosSubir + archivo
        } else {
            Toast.makeText(context, "Máximo 3 archivos permitidos", Toast.LENGTH_SHORT).show()
        }
    }

    // URI temporal para escribir la foto de la cámara
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val getFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            addArchivo(ArchivoSubir(uri = uri, nombre = getFileName(context, uri) ?: "documento"))
        }
    }
    
    // ── Launcher para la galería (con fallback para compatibilidad) ─────────────
    // PickVisualMedia es el selector moderno (Android 11+). En dispositivos que
    // no lo soporten (algunos ROMs como ColorOS), usamos GetContent como respaldo.
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { 
            val bitmap = decodeUriToSafeBitmap(context, it)
            if (bitmap != null) {
                addArchivo(ArchivoSubir(bitmap = bitmap, nombre = getFileName(context, it) ?: "imagen.jpg"))
            }
        }
    }

    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            val bitmap = decodeUriToSafeBitmap(context, it)
            if (bitmap != null) {
                addArchivo(ArchivoSubir(bitmap = bitmap, nombre = getFileName(context, it) ?: "imagen.jpg"))
            }
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
                val bitmap = decodeUriToSafeBitmap(context, uri)
                if (bitmap != null) {
                    addArchivo(ArchivoSubir(bitmap = bitmap, nombre = "foto_camara.jpg"))
                }
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
                    if (isReadOnlyMode) {
                        if (evidenciaEnviada?.estado == "Pendiente") {
                            OutlinedButton(
                                onClick = { 
                                    evidenciaEnviada?.idEvidencia?.let {
                                        viewModel.anularEvidencia(it) { onBack() }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(12.dp),
                                border   = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Anular Entrega")
                            }
                        } else {
                            // Está evaluada
                            Button(
                                onClick = { onBack() },
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(12.dp)
                            ) {
                                Text("Regresar")
                            }
                        }
                    } else {
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
                            viewModel.enviarEvidencia(
                                context = context,
                                idAsignacion = idAsignacion,
                                nombreAlumno = nombreAlumno,
                                tituloTarea  = tarea.titulo,
                                archivosSubir = archivosSubir,
                                vinculos = vinculos,
                                textoEvidencia = textoEvidencia
                            )
                        },
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(12.dp),
                        enabled   = (archivosSubir.isNotEmpty() || vinculos.isNotEmpty() || textoEvidencia.isNotBlank()) && uiState !is EnviarEvidenciaUiState.Uploading,
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
                    } // Fin de if (!isReadOnlyMode)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
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

                if (isVencida) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Esta tarea está vencida y ya no se puede entregar.", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ── Zona de texto de evidencia ─────────────────────────────────────
                OutlinedTextField(
                    value = actualTexto,
                    onValueChange = { if (!isReadOnlyMode) textoEvidencia = it },
                    label = { Text("Texto o Enlace (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5,
                    readOnly = isReadOnlyMode
                )

                // ── Zona de Vínculos ───────────────────────────────────────────────────────
                if (!isReadOnlyMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nuevoVinculo,
                            onValueChange = { nuevoVinculo = it },
                            label = { Text("Añadir un enlace") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (nuevoVinculo.isNotBlank()) {
                                    vinculos = vinculos + nuevoVinculo
                                    nuevoVinculo = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Añadir")
                        }
                    }
                }

                if (actualVinculos.isNotEmpty()) {
                    Text(
                        "Enlaces añadidos:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    actualVinculos.forEach { link ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(link, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // ── Zona de Archivos ───────────────────────────────────────────────────────
                Text(
                    "Archivos Adjuntos (${if (isReadOnlyMode) actualArchivosEnviados.size + (if(base64Foto!=null) 1 else 0) else archivosSubir.size}/3)",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                // Mostrar archivos en modo escritura
                if (!isReadOnlyMode && archivosSubir.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        archivosSubir.forEachIndexed { index, archivo ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (archivo.bitmap != null) Icons.Default.Image else Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(archivo.nombre, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(200.dp))
                                    }
                                    IconButton(onClick = { 
                                        val mList = archivosSubir.toMutableList()
                                        mList.removeAt(index)
                                        archivosSubir = mList
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // Mostrar archivos en modo lectura
                if (isReadOnlyMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Archivos guardados (Base64)
                        actualArchivosEnviados.forEach { archivoMap ->
                            val base64Data = archivoMap["base64"] ?: ""
                            val nombre = archivoMap["nombre"] ?: "Archivo"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(nombre, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(200.dp))
                                    }
                                    val ctx = LocalContext.current
                                    IconButton(onClick = { 
                                        if (base64Data.isNotBlank()) {
                                            abrirArchivoBase64(ctx, base64Data, nombre)
                                        }
                                    }) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = "Abrir", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        // Archivo Legacy Base64
                        if (base64Foto != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(actualNombreArchivoLegacy ?: "Archivo antiguo", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(200.dp))
                                    }
                                    val ctx = LocalContext.current
                                    IconButton(onClick = { 
                                        abrirArchivoBase64(ctx, base64Foto, actualNombreArchivoLegacy) 
                                    }) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = "Abrir", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            
                            // Vista previa de imagen si es bitmap
                            if (bitmapLectura != null) {
                                Image(
                                    bitmap = bitmapLectura!!.asImageBitmap(),
                                    contentDescription = "Foto de evidencia",
                                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
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
                            if (isReadOnlyMode) {
                                if (evidenciaEnviada?.estado == "Pendiente") "Esta evidencia ha sido enviada y está pendiente de revisión."
                                else "Esta evidencia ya fue evaluada: ${evidenciaEnviada?.estado}."
                            } else {
                                "Asegúrate de que los archivos o el enlace " +
                                "sean correctos antes de enviar."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Espacio extra para el bottom bar
                Spacer(Modifier.height(8.dp))
            }

            // Overlay Loading 
            if (isLoadingEvidencia) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
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

// ── Helper: Decodificar Base64 a Bitmap ──────────────────────────────────────
private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val bytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}

private fun abrirArchivoBase64(context: Context, base64Str: String, fileName: String?) {
    try {
        val cleanString = if (base64Str.contains(",")) {
            base64Str.substring(base64Str.indexOf(",") + 1)
        } else {
            base64Str
        }
        val bytes = android.util.Base64.decode(cleanString, android.util.Base64.DEFAULT)
        val safeFileName = fileName ?: "documento.pdf"
        val file = File(context.cacheDir, safeFileName)
        FileOutputStream(file).use { it.write(bytes) }
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Abrir archivo con"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al abrir el archivo", Toast.LENGTH_SHORT).show()
    }
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
