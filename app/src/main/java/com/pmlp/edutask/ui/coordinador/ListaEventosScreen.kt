package com.pmlp.edutask.ui.coordinador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.ui.components.ShimmerPlaceholder
import com.pmlp.edutask.model.Evento
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.edutask.ui.EventosUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaEventosScreen(
    viewModel: EventosSharedViewModel,
    onBack: () -> Unit,
    onNavigateToFormulario: (String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteEventoDialog by remember { mutableStateOf<Evento?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchEventos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos Escolares", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToFormulario(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar Evento")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is EventosUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(4) {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ShimmerPlaceholder(modifier = Modifier.width(180.dp).height(20.dp).weight(1f))
                                        ShimmerPlaceholder(modifier = Modifier.size(24.dp), shape = androidx.compose.foundation.shape.CircleShape)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        ShimmerPlaceholder(modifier = Modifier.size(24.dp), shape = androidx.compose.foundation.shape.CircleShape)
                                    }
                                    ShimmerPlaceholder(modifier = Modifier.width(280.dp).height(16.dp))
                                    ShimmerPlaceholder(modifier = Modifier.width(120.dp).height(14.dp))
                                    ShimmerPlaceholder(modifier = Modifier.width(90.dp).height(20.dp), shape = RoundedCornerShape(10.dp))
                                }
                            }
                        }
                    }
                }
                is EventosUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is EventosUiState.Success -> {
                    val eventos = state.eventos
                    if (eventos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay eventos registrados.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(eventos) { evento ->
                                EventoItem(
                                    evento = evento,
                                    onEdit = { onNavigateToFormulario(evento.idEvento) },
                                    onDelete = { showDeleteEventoDialog = evento }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    showDeleteEventoDialog?.let { evento ->
        AlertDialog(
            onDismissRequest = { showDeleteEventoDialog = null },
            title = { Text("Eliminar Evento", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar el evento o anuncio \"${evento.titulo}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEvento(evento.idEvento)
                        showDeleteEventoDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteEventoDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EventoItem(
    evento: Evento,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val fechaFormat = dateFormat.format(Date(evento.fechaPublicacion))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = evento.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = evento.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Publicado: $fechaFormat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
