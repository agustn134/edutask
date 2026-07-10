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
    var fechaPublicacion by remember { mutableStateOf(System.currentTimeMillis()) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idEvento, uiState) {
        if (isEdit && uiState is EventosUiState.Success) {
            val evento = (uiState as EventosUiState.Success).eventos.find { it.idEvento == idEvento }
            if (evento != null) {
                titulo = evento.titulo
                descripcion = evento.descripcion
                fechaPublicacion = evento.fechaPublicacion
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Editar Evento" else "Nuevo Evento", fontWeight = FontWeight.Bold) },
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
                label = { Text("Título del Evento") },
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (titulo.isBlank() || descripcion.isBlank()) {
                        errorMsg = "Todos los campos son obligatorios"
                        return@Button
                    }
                    isLoading = true
                    errorMsg = null
                    val evento = Evento(
                        idEvento = idEvento ?: "",
                        titulo = titulo,
                        descripcion = descripcion,
                        fechaPublicacion = fechaPublicacion
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
                    Text(if (isEdit) "Guardar Cambios" else "Publicar Evento")
                }
            }
        }
    }
}
