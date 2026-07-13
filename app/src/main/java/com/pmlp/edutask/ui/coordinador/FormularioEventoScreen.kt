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
                customDate = evento.fechaPublicacion
                hasCustomDate = true
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
                        fechaPublicacion = finalFecha
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
