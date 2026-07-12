package com.pmlp.edutask.ui.alumno

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.EstadoEvidencia
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Alignment
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalificacionesContent(
    modifier: Modifier = Modifier, 
    tareas: List<TareaItem>, 
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onVerTarea: (TareaItem) -> Unit = {}
) {
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(),
                   contentPadding = PaddingValues(16.dp),
                   verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Mis Calificaciones", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
            
            val tareasEvaluadas = tareas.filter { it.estado != EstadoEvidencia.Pendiente }
            if (tareasEvaluadas.isEmpty()) {
                item { Text("Aún no tienes calificaciones.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(tareasEvaluadas, key = { it.idAsignacion }) { item -> 
                    CalificacionCard(item, onVerTarea) 
                }
            }
        }
    }
}

@Composable
fun CalificacionCard(item: TareaItem, onClick: (TareaItem) -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val calificacionStr = item.calificacion?.toString() ?: "-"
    
    val containerColor = if (item.estado == EstadoEvidencia.Aprobada) 
        MaterialTheme.colorScheme.tertiaryContainer 
    else 
        MaterialTheme.colorScheme.errorContainer
        
    val contentColor = if (item.estado == EstadoEvidencia.Aprobada) 
        MaterialTheme.colorScheme.onTertiaryContainer 
    else 
        MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = { onClick(item) }
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = item.tarea.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(text = "Clase: ${item.tarea.nombreClase}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                if (!item.comentario.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Comentario: ${item.comentario}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Surface(
                shape = MaterialTheme.shapes.large,
                color = containerColor,
                contentColor = contentColor,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Calificación", modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$calificacionStr / 10",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
