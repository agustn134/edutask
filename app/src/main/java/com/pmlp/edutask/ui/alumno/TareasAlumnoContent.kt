package com.pmlp.edutask.ui.alumno

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.ui.components.EmptyStateIllustration
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasContent(modifier: Modifier, claseSelected: String?, onClaseSelected: (String) -> Unit,
                  tareas: List<TareaItem>, pendienteCount: Int, clases: List<String>,
                  isRefreshing: Boolean, onRefresh: () -> Unit,
                  onVerTarea: (TareaItem) -> Unit = {}) {
    val now = java.util.Date()
    val tareasPendientes = tareas.filter { it.estado == EstadoEvidencia.Pendiente && it.idEvidencia == null && !now.after(it.tarea.fechaLimite) }
    val tareasVencidas = tareas.filter { it.estado == EstadoEvidencia.Pendiente && it.idEvidencia == null && now.after(it.tarea.fechaLimite) }
    val tareasEntregadas = tareas.filter { it.estado == EstadoEvidencia.Pendiente && it.idEvidencia != null }
    val tareasEvaluadas = tareas.filter { it.estado == EstadoEvidencia.Aprobada || it.estado == EstadoEvidencia.Rechazada }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pendientes", "Entregadas", "Evaluadas")

    val currentTareas = when (selectedTabIndex) {
        0 -> tareasPendientes
        1 -> tareasEntregadas
        else -> tareasEvaluadas
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de Filtros de Clase
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text("Filtrar por Clase", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        // Chip para "Todas"
                        item {
                            FilterChip(
                                selected = claseSelected == null,
                                onClick = { onClaseSelected(claseSelected ?: "") }, // Al pasar la misma o vacío, si la lógica dice "if (it == claseSelected) null", pasamos "" para forzar null. Espera, el onClaseSelected en HomeAlumnoScreen hace: `if (claseSelected == it) null else it`. Entonces si pasamos `claseSelected`, se vuelve `null`. ¡Perfecto!
                                label = { Text("Todas", style = MaterialTheme.typography.labelMedium) },
                                leadingIcon = { if (claseSelected == null) Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                            )
                        }
                        // Chips de cada clase
                        items(clases, key = { it }) { clase ->
                            FilterChip(
                                selected = claseSelected == clase,
                                onClick = { onClaseSelected(clase) },
                                label = { Text(clase, style = MaterialTheme.typography.labelMedium) },
                                leadingIcon = { if (claseSelected == clase) Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                            )
                        }
                    }
                }
            }

            // Título de la lista y Tabs Kanban
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Text("Lista de Tareas", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.weight(1f))
                        if (pendienteCount > 0 && claseSelected == null) {
                            Badge(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError) { 
                                Text("$pendienteCount pendientes") 
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, title ->
                            val count = when (index) {
                                0 -> tareasPendientes.size
                                1 -> tareasEntregadas.size
                                else -> tareasEvaluadas.size
                            }
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text("$title ($count)", style = MaterialTheme.typography.titleSmall) }
                            )
                        }
                    }
                }
            }

            // Lista de Tareas o Estado Vacío
            if (currentTareas.isEmpty()) {
                item {
                    val (icon, title, subtitle) = when (selectedTabIndex) {
                        0 -> Triple(Icons.Default.TaskAlt, "Sin Tareas Pendientes", "¡Estás al día con tus entregas!")
                        1 -> Triple(Icons.Default.PendingActions, "Aún No Hay Entregas", "Cuando entregues tus tareas, aparecerán aquí.")
                        else -> Triple(Icons.Default.FactCheck, "Sin Tareas Evaluadas", "Tus calificaciones aparecerán aquí cuando el profesor revise.")
                    }
                    EmptyStateIllustration(
                        icon = icon,
                        title = title,
                        subtitle = subtitle,
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
            } else {
                items(currentTareas, key = { it.idAsignacion }) { item -> 
                    TareaCard(item.tarea, item.estado, onClick = { onVerTarea(item) }) 
                }
            }

            if (tareasVencidas.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                        Text(
                            "Tareas Fuera de Límite", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(tareasVencidas, key = { it.idAsignacion }) { item -> 
                    TareaCard(item.tarea, item.estado, isVencida = true, onClick = { onVerTarea(item) }) 
                }
            }
            
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun TareaCard(tarea: Tarea, estado: EstadoEvidencia, isVencida: Boolean = false, onClick: () -> Unit = {}) {
    val fmt = SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp, pressedElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Icono de estado
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = when (estado) {
                        EstadoEvidencia.Aprobada -> MaterialTheme.colorScheme.tertiaryContainer
                        EstadoEvidencia.Rechazada -> MaterialTheme.colorScheme.errorContainer
                        EstadoEvidencia.Pendiente -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (estado) {
                                EstadoEvidencia.Aprobada -> Icons.Default.CheckCircle
                                EstadoEvidencia.Rechazada -> Icons.Default.Cancel
                                EstadoEvidencia.Pendiente -> Icons.Default.HourglassEmpty
                            },
                            contentDescription = "Estado: ${estado.name}",
                            modifier = Modifier.size(20.dp),
                            tint = when (estado) {
                                EstadoEvidencia.Aprobada -> MaterialTheme.colorScheme.onTertiaryContainer
                                EstadoEvidencia.Rechazada -> MaterialTheme.colorScheme.onErrorContainer
                                EstadoEvidencia.Pendiente -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                }
                
                // Textos
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = tarea.titulo, 
                        style = MaterialTheme.typography.titleSmall, 
                        maxLines = 2, 
                        overflow = TextOverflow.Ellipsis,
                        color = if (isVencida) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(tarea.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            
            // Fila inferior con Chips
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = { Text(tarea.nombreClase, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.School, null, Modifier.size(16.dp)) },
                        border = null,
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(fmt.format(tarea.fechaLimite), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                EstadoChip(estado)
            }
        }
    }
}

@Composable
fun EstadoChip(estado: EstadoEvidencia) {
    val label = when (estado) { EstadoEvidencia.Pendiente -> "Pend."; EstadoEvidencia.Aprobada -> "Aprob."; EstadoEvidencia.Rechazada -> "Rec." }
    val icon  = when (estado) { EstadoEvidencia.Pendiente -> Icons.Default.Schedule; EstadoEvidencia.Aprobada -> Icons.Default.CheckCircle; EstadoEvidencia.Rechazada -> Icons.Default.Cancel }
    val container = when (estado) { EstadoEvidencia.Pendiente -> MaterialTheme.colorScheme.secondaryContainer; EstadoEvidencia.Aprobada -> MaterialTheme.colorScheme.tertiaryContainer; EstadoEvidencia.Rechazada -> MaterialTheme.colorScheme.errorContainer }
    val content   = when (estado) { EstadoEvidencia.Pendiente -> MaterialTheme.colorScheme.onSecondaryContainer; EstadoEvidencia.Aprobada -> MaterialTheme.colorScheme.onTertiaryContainer; EstadoEvidencia.Rechazada -> MaterialTheme.colorScheme.onErrorContainer }
    SuggestionChip(onClick = {}, label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                   icon = { Icon(icon, null, Modifier.size(AssistChipDefaults.IconSize)) },
                   colors = SuggestionChipDefaults.suggestionChipColors(containerColor = container, labelColor = content, iconContentColor = content),
                   border = null)
}
