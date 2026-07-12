package com.pmlp.edutask.ui.alumno

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Grade
import com.pmlp.edutask.model.EstadoEvidencia

@Composable
fun PerfilContent(modifier: Modifier = Modifier, nombre: String, carrera: String, tareas: List<TareaItem>) {
    val scrollState = rememberScrollState()
    
    val tareasEntregadas = tareas.filter { it.estado != EstadoEvidencia.Pendiente }.size
    val tareasPendientes = tareas.filter { it.estado == EstadoEvidencia.Pendiente }.size
    val calificaciones = tareas.mapNotNull { it.calificacion }
    val promedio = if (calificaciones.isNotEmpty()) calificaciones.average() else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Surface(
            Modifier.size(100.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    nombre.split(" ").take(2).joinToString("") { it.first().toString().uppercase() },
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text(carrera, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Rol: Alumno", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        
        Spacer(Modifier.height(32.dp))
        
        // Statistics Section
        Text(
            text = "Mis Estadísticas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                title = "Entregadas",
                value = tareasEntregadas.toString(),
                icon = Icons.Default.TaskAlt,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pendientes",
                value = tareasPendientes.toString(),
                icon = Icons.Default.PendingActions,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        StatCard(
            title = "Promedio General",
            value = String.format(java.util.Locale.getDefault(), "%.1f / 10", promedio),
            icon = Icons.Default.Grade,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        // Shortcuts / Contact Info
        Text(
            text = "Información y Atajos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        ShortcutItem(icon = Icons.Default.Email, title = "Contacto Administrativo", subtitle = "control.escolar@edutask.edu")
        Spacer(Modifier.height(12.dp))
        ShortcutItem(icon = Icons.Default.Settings, title = "Ajustes de Cuenta", subtitle = "Cambiar contraseña y preferencias")
        Spacer(Modifier.height(12.dp))
        ShortcutItem(icon = Icons.Default.HelpOutline, title = "Soporte Técnico", subtitle = "Reportar un problema con la app")
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            Spacer(Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}

@Composable
fun ShortcutItem(icon: ImageVector, title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = {}
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
