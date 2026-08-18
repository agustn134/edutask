package com.pmlp.edutask.ui.profesor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradebookScreen(
    idClase: String,
    viewModel: GradebookViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(idClase) {
        viewModel.loadGradebook(idClase)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Libreta de Calificaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (!state.isLoading && state.error == null && state.alumnos.isNotEmpty()) {
                        IconButton(onClick = {
                            val uri = viewModel.exportarAExcel(context)
                            if (uri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Exportar Calificaciones"))
                            }
                        }) {
                            Icon(Icons.Filled.Download, contentDescription = "Exportar a Excel")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                
                val columnWidth = 120.dp
                val nameColumnWidth = 150.dp
                val horizontalScrollState = rememberScrollState()

                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    // Fixed Name Header
                    Box(
                        modifier = Modifier
                            .width(nameColumnWidth)
                            .padding(8.dp)
                    ) {
                        Text("Alumno", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    
                    // Scrollable Tasks + Average Header
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        state.tareas.forEach { tarea ->
                            Box(
                                modifier = Modifier
                                    .width(columnWidth)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tarea.titulo,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        // Average Header
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Promedio", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                
                Divider()

                // Data Rows
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.alumnos) { alumno ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Fixed Name Column
                            Box(
                                modifier = Modifier
                                    .width(nameColumnWidth)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = alumno.nombre.ifBlank { alumno.matricula },
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 14.sp
                                )
                            }
                            
                            // Scrollable Tasks + Average Columns
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(horizontalScrollState) // Sync scroll with header
                            ) {
                                state.tareas.forEach { tarea ->
                                    val grade = state.calificaciones["${alumno.idUsuario}_${tarea.idTarea}"]
                                    Box(
                                        modifier = Modifier
                                            .width(columnWidth)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = grade?.toString() ?: "-",
                                            textAlign = TextAlign.Center,
                                            fontSize = 14.sp,
                                            color = if (grade == null) Color.Gray else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                
                                // Average Column
                                val average = state.promedios[alumno.idUsuario]
                                val avgColor = when {
                                    average == null -> Color.Gray
                                    average >= 8.0 -> Color(0xFF388E3C) // Green
                                    average >= 6.0 -> Color(0xFFFBC02D) // Yellow
                                    else -> Color.Red
                                }
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (average != null) String.format("%.1f", average) else "-",
                                        fontWeight = FontWeight.Bold,
                                        color = avgColor,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
