package com.pmlp.edutask.ui.alumno

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.EstadoEvidencia

@Composable
fun CalificacionesContent(modifier: Modifier = Modifier, tareas: List<TareaItem>) {
    LazyColumn(modifier = modifier.fillMaxSize(),
               contentPadding = PaddingValues(16.dp),
               verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Mis Calificaciones", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
        
        val tareasEvaluadas = tareas.filter { it.estado != EstadoEvidencia.Pendiente }
        if (tareasEvaluadas.isEmpty()) {
            item { Text("Aún no tienes calificaciones.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(tareasEvaluadas) { item -> TareaCard(item.tarea, item.estado, onClick = {}) }
        }
    }
}
