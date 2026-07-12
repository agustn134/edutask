package com.pmlp.edutask.ui.alumno

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.EstadoEvidencia
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import com.pmlp.edutask.model.Evento
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.style.TextOverflow
import com.pmlp.edutask.ui.components.EmptyStateIllustration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioContent(
    modifier: Modifier = Modifier, 
    pendientes: Int, 
    tareas: List<TareaItem>,
    eventos: List<Evento> = emptyList(),
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onVerTarea: (TareaItem) -> Unit,
    onUnirseAClase: (String) -> Unit = {}
) {
    var codigoClase by remember { mutableStateOf("") }
    
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        
        if (eventos.isNotEmpty()) {
            item {
                Text("Anuncios Recientes", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(eventos, key = { it.idEvento }) { evento ->
                        EventoCarouselCard(evento)
                    }
                }
            }
        }
        
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Mi Progreso", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text(if (pendientes > 0) "Tienes $pendientes tareas pendientes." else "¡Felicidades! Estás al día con tus tareas.",
                             style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Inscribirse a una clase", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    OutlinedTextField(
                        value = codigoClase,
                        onValueChange = { codigoClase = it },
                        label = { Text("Código de clase.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (codigoClase.isNotBlank()) {
                                onUnirseAClase(codigoClase)
                                codigoClase = "" // Limpiar después de intentar
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Unirme")
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Próximas Entregas", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (pendientes > 0) Badge(containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError) { Text(pendientes.toString()) }
            }
        }
        
        val now = java.util.Date()
        val proximasTareas = tareas.filter { it.estado == EstadoEvidencia.Pendiente && it.idEvidencia == null && !now.after(it.tarea.fechaLimite) }.take(3)
        if (proximasTareas.isEmpty()) {
            item {
                EmptyStateIllustration(
                    icon = Icons.Default.TaskAlt,
                    title = "¡Todo al día!",
                    subtitle = "Puedes relajarte, no hay tareas próximas.",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            items(proximasTareas, key = { it.idAsignacion }) { item -> TareaCard(item.tarea, item.estado, onClick = { onVerTarea(item) }) }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
    }
}

@Composable
fun EventoCarouselCard(evento: Evento) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val fechaFormat = dateFormat.format(Date(evento.fechaPublicacion))

    OutlinedCard(
        modifier = Modifier.width(260.dp).height(120.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(evento.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(evento.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text(fechaFormat, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
