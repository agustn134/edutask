package com.pmlp.edutask.ui.coordinador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.pmlp.edutask.model.Evento
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.edutask.ui.EventosUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioEventoScreen(
    viewModel: EventosSharedViewModel,
    idEvento: String?,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = idEvento != null

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf<String?>(null) }
    
    var hasCustomDate by remember { mutableStateOf(false) }
    var customDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idEvento, uiState) {
        if (isEdit && uiState is EventosUiState.Success) {
            val evento = (uiState as EventosUiState.Success).eventos.find { it.idEvento == idEvento }
            if (evento != null) {
                titulo = evento.titulo
                descripcion = evento.descripcion
                lugar = evento.lugar
                imagenUrl = evento.imagenUrl
                customDate = evento.fechaPublicacion
                hasCustomDate = true
            }
        }
    }
    
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val compressedBase64 = compressImageToBase64(context, it)
            if (compressedBase64 != null) {
                imagenUrl = compressedBase64
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Editar Evento o Anuncio" else "Nuevo Evento o Anuncio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMsg != null) {
                Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del Evento o Anuncio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5
            )

            OutlinedTextField(
                value = lugar,
                onValueChange = { lugar = it },
                label = { Text("Lugar del Evento (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Selector de Imagen
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Imagen de Fondo (Opcional)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                if (imagenUrl != null) {
                    val imageModel = remember(imagenUrl) {
                        try {
                            val base64Str = if (imagenUrl!!.startsWith("data:image")) imagenUrl!!.substringAfter("base64,") else imagenUrl!!
                            if (base64Str.length > 500 && !base64Str.startsWith("http")) {
                                val bytes = Base64.decode(base64Str, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } else {
                                imagenUrl
                            }
                        } catch (e: Exception) {
                            imagenUrl
                        }
                    }
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Vista previa",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (imagenUrl == null) "Seleccionar Foto" else "Cambiar Foto")
                }
                if (imagenUrl != null) {
                    TextButton(onClick = { imagenUrl = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("Quitar Foto", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Añadir fecha al evento",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = hasCustomDate,
                    onCheckedChange = { hasCustomDate = it }
                )
            }

            if (hasCustomDate) {
                val dateFormat = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
                val timeFormat = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
                
                val fechaFormateada = remember(customDate) { dateFormat.format(java.util.Date(customDate)) }
                val horaFormateada = remember(customDate) { timeFormat.format(java.util.Date(customDate)) }
                
                var showDatePicker by remember { mutableStateOf(false) }
                var showTimePicker by remember { mutableStateOf(false) }

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = customDate
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
                                            timeInMillis = customDate
                                            set(java.util.Calendar.YEAR, utcCal.get(java.util.Calendar.YEAR))
                                            set(java.util.Calendar.MONTH, utcCal.get(java.util.Calendar.MONTH))
                                            set(java.util.Calendar.DAY_OF_MONTH, utcCal.get(java.util.Calendar.DAY_OF_MONTH))
                                        }
                                        customDate = newCal.timeInMillis
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
                    val calendar = remember(customDate) {
                        java.util.Calendar.getInstance().apply { timeInMillis = customDate }
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
                                        timeInMillis = customDate
                                        set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                                        set(java.util.Calendar.MINUTE, timePickerState.minute)
                                        set(java.util.Calendar.SECOND, 0)
                                        set(java.util.Calendar.MILLISECOND, 0)
                                    }
                                    customDate = updatedCal.timeInMillis
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
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                TimePicker(state = timePickerState)
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("Fecha: $fechaFormateada")
                    }

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("Hora: $horaFormateada")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (titulo.isBlank() || descripcion.isBlank()) {
                        errorMsg = "Todos los campos son obligatorios"
                        return@Button
                    }
                    isLoading = true
                    errorMsg = null
                    
                    val finalFecha = if (hasCustomDate) customDate else System.currentTimeMillis()
                    
                    val evento = Evento(
                        idEvento = idEvento ?: "",
                        titulo = titulo,
                        descripcion = descripcion,
                        lugar = lugar,
                        fechaPublicacion = finalFecha,
                        imagenUrl = imagenUrl
                    )
                    viewModel.saveEvento(
                        evento = evento,
                        onSuccess = {
                            isLoading = false
                            onBack()
                        },
                        onError = {
                            isLoading = false
                            errorMsg = it
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (isEdit) "Guardar Cambios" else "Publicar Evento o Anuncio")
                }
            }
        }
    }
}

fun compressImageToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Reducir la imagen a máximo 800px de ancho/alto manteniendo la proporción
        val maxDimension = 800
        val ratio = Math.min(
            maxDimension.toFloat() / originalBitmap.width,
            maxDimension.toFloat() / originalBitmap.height
        )
        val width = Math.round(ratio * originalBitmap.width)
        val height = Math.round(ratio * originalBitmap.height)

        val scaledBitmap = if (ratio < 1f) {
            Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        } else {
            originalBitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
