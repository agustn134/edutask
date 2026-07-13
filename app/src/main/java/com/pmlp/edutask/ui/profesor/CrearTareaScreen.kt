package com.pmlp.edutask.ui.profesor

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.model.ClaseInfo
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat

data class ArchivoAdjunto(
    val uri: Uri? = null,
    val nombre: String,
    val base64: String? = null,
    val esLink: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearTareaScreen(
    idUsuario: String = "",
    idTarea: String? = null,
    onTareaCreadaExitosa: () -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var limitTimestamp by remember {
        mutableStateOf(
            java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        )
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var archivosAdjuntos by remember { mutableStateOf(listOf<ArchivoAdjunto>()) }
    val selectFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris != null) {
            val nuevosArchivos = uris.map { uri ->
                val nombre = getFileName(context, uri)
                ArchivoAdjunto(uri = uri, nombre = nombre)
            }
            archivosAdjuntos = (archivosAdjuntos + nuevosArchivos).distinctBy { it.uri?.toString() ?: it.nombre }
        }
    }
    
    var isLoading by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }

    // Dynamic Class selector states
    var clasesList by remember { mutableStateOf<List<ClaseInfo>>(emptyList()) }
    var selectedClaseId by remember { mutableStateOf("") }
    var selectedClaseNombre by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    var showLinkDialog by remember { mutableStateOf(false) }
    var linkUrl by remember { mutableStateOf("") }
    var linkTitle by remember { mutableStateOf("") }

    // Load professor classes
    LaunchedEffect(idUsuario) {
        if (idUsuario.isNotBlank()) {
            db.collection("clases")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .addOnSuccessListener { snapshot ->
                    val clases = snapshot.documents.map { doc ->
                        ClaseInfo(
                            idClase = doc.id,
                            nombre = doc.getString("nombre") ?: "Sin Nombre"
                        )
                    }
                    clasesList = clases
                    // Default selection
                    if (clases.isNotEmpty() && selectedClaseId.isBlank()) {
                        selectedClaseId = clases[0].idClase
                        selectedClaseNombre = clases[0].nombre
                    }
                }
        }
    }

    // Load task details if editing
    LaunchedEffect(idTarea) {
        if (!idTarea.isNullOrBlank()) {
            isLoading = true
            db.collection("tareas").document(idTarea).get()
                .addOnSuccessListener { doc ->
                    isLoading = false
                    if (doc.exists()) {
                        titulo = doc.getString("titulo") ?: ""
                        descripcion = doc.getString("descripcion") ?: ""
                        
                        val date = doc.getTimestamp("fechaLimite")?.toDate() ?: Date()
                        limitTimestamp = date.time
                        
                        selectedClaseId = doc.getString("idClase") ?: ""
                        selectedClaseNombre = doc.getString("nombreClase") ?: ""

                        val rawArchivos = doc.get("archivos") as? List<*>
                        val cargados = mutableListOf<ArchivoAdjunto>()
                        rawArchivos?.forEach { item ->
                            if (item is Map<*, *>) {
                                val nombre = item["nombre"]?.toString() ?: ""
                                val base64 = item["base64"]?.toString() ?: ""
                                val esLink = item["esLink"] as? Boolean ?: false
                                cargados.add(ArchivoAdjunto(nombre = nombre, base64 = base64, esLink = esLink))
                            }
                        }
                        archivosAdjuntos = cargados
                    }
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (idTarea.isNullOrBlank()) "Asignar Nueva Actividad" else "Editar Actividad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdown Selector for classes list
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedClaseNombre.ifBlank { "Selecciona una clase" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Asignar a Clase") },
                    trailingIcon = {
                        IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                            Icon(
                                imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Expandir"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { dropdownExpanded = !dropdownExpanded },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    clasesList.forEach { clase ->
                        DropdownMenuItem(
                            text = { Text(clase.nombre) },
                            onClick = {
                                selectedClaseId = clase.idClase
                                selectedClaseNombre = clase.nombre
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título de la tarea") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp) // Diseño institucional redondeado sin emojis
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción larga de la actividad") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )

            val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
            val timeFormat = remember { SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
            
            val fechaFormateada = remember(limitTimestamp) { dateFormat.format(Date(limitTimestamp)) }
            val horaFormateada = remember(limitTimestamp) { timeFormat.format(Date(limitTimestamp)) }

            Text(
                text = "Fecha y Hora Límite:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { if (!isLoading) showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(fechaFormateada)
                }

                OutlinedButton(
                    onClick = { if (!isLoading) showTimePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(horaFormateada)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Adjuntar archivos o imágenes
            if (archivosAdjuntos.isNotEmpty()) {
                Text(
                    text = "Material Complementario:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                archivosAdjuntos.forEach { archivo ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (archivo.esLink) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = archivo.nombre,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            archivosAdjuntos = archivosAdjuntos.filter { it != archivo }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { selectFilesLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(imageVector = Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Archivo", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
                
                OutlinedButton(
                    onClick = { showLinkDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(imageVector = Icons.Default.Link, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enlace", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (titulo.isNotBlank() && descripcion.isNotBlank() && selectedClaseId.isNotBlank()) {
                        isLoading = true
                        val parsedDate = Date(limitTimestamp)

                        scope.launch {
                            try {
                                val subidos = mutableListOf<Map<String, Any>>()
                                
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    for (archivo in archivosAdjuntos) {
                                        if (archivo.esLink && archivo.base64 != null) {
                                            subidos.add(mapOf("nombre" to archivo.nombre, "base64" to archivo.base64, "esLink" to true))
                                        } else if (archivo.base64 != null) {
                                            subidos.add(mapOf("nombre" to archivo.nombre, "base64" to archivo.base64))
                                        } else if (archivo.uri != null) {
                                            try {
                                                context.contentResolver.openInputStream(archivo.uri)?.use { input ->
                                                    val bytes = input.readBytes()
                                                    val base64Str = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                                                    if (base64Str.isNotBlank()) {
                                                        subidos.add(mapOf("nombre" to archivo.nombre, "base64" to base64Str))
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }

                                val nuevaTarea = hashMapOf(
                                    "titulo" to titulo.trim(),
                                    "descripcion" to descripcion.trim(),
                                    "fechaLimite" to com.google.firebase.Timestamp(parsedDate),
                                    "idClase" to selectedClaseId,
                                    "nombreClase" to selectedClaseNombre,
                                    "archivos" to subidos
                                )

                                val docRef = if (idTarea.isNullOrBlank()) {
                                    db.collection("tareas").document()
                                } else {
                                    db.collection("tareas").document(idTarea)
                                }

                                docRef.set(nuevaTarea)
                                    .addOnSuccessListener {
                                        if (idTarea.isNullOrBlank()) {
                                            val newTaskId = docRef.id
                                            db.collection("clase_alumno")
                                                .whereEqualTo("idClase", selectedClaseId)
                                                .get()
                                                .addOnSuccessListener { snapshot ->
                                                    val batch = db.batch()
                                                    for (doc in snapshot.documents) {
                                                        val idAlumno = doc.getString("idUsuario") ?: continue
                                                        val asigRef = db.collection("asignaciones_tarea").document()
                                                        val asigDoc = hashMapOf(
                                                            "idTarea" to newTaskId,
                                                            "idUsuario" to idAlumno,
                                                            "fechaAsignacion" to com.google.firebase.Timestamp.now()
                                                        )
                                                        batch.set(asigRef, asigDoc)
                                                    }
                                                    batch.commit()
                                                        .addOnSuccessListener {
                                                            isLoading = false
                                                            onTareaCreadaExitosa()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            isLoading = false
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Error al crear asignaciones: ${e.localizedMessage}")
                                                            }
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Error al buscar alumnos: ${e.localizedMessage}")
                                                    }
                                                }
                                        } else {
                                            isLoading = false
                                            onTareaCreadaExitosa()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Error al guardar: ${e.localizedMessage}")
                                        }
                                    }
                            } catch (e: Exception) {
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al guardar: ${e.localizedMessage}")
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading && titulo.isNotBlank() && descripcion.isNotBlank() && selectedClaseId.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (idTarea.isNullOrBlank()) "Publicar Actividad" else "Guardar Cambios", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = limitTimestamp
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = selectedMillis
                            }
                            val newCal = java.util.Calendar.getInstance().apply {
                                timeInMillis = limitTimestamp
                                set(java.util.Calendar.YEAR, utcCal.get(java.util.Calendar.YEAR))
                                set(java.util.Calendar.MONTH, utcCal.get(java.util.Calendar.MONTH))
                                set(java.util.Calendar.DAY_OF_MONTH, utcCal.get(java.util.Calendar.DAY_OF_MONTH))
                            }
                            limitTimestamp = newCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val calendar = remember(limitTimestamp) {
            java.util.Calendar.getInstance().apply { timeInMillis = limitTimestamp }
        }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(java.util.Calendar.MINUTE),
            is24Hour = true
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedCal = java.util.Calendar.getInstance().apply {
                            timeInMillis = limitTimestamp
                            set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(java.util.Calendar.MINUTE, timePickerState.minute)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                        limitTimestamp = updatedCal.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text("Agregar Enlace Externo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it },
                        label = { Text("URL del enlace (https://...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = linkTitle,
                        onValueChange = { linkTitle = it },
                        label = { Text("Título (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (linkUrl.isNotBlank()) {
                            val urlFixed = if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) "https://$linkUrl" else linkUrl
                            val finalTitle = if (linkTitle.isNotBlank()) linkTitle else urlFixed
                            archivosAdjuntos = archivosAdjuntos + ArchivoAdjunto(
                                uri = null,
                                nombre = "Enlace: $finalTitle",
                                base64 = urlFixed,
                                esLink = true
                            )
                        }
                        showLinkDialog = false
                        linkUrl = ""
                        linkTitle = ""
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLinkDialog = false
                        linkUrl = ""
                        linkTitle = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String {
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