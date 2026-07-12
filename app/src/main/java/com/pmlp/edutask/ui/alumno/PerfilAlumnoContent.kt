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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Grade
import com.pmlp.edutask.model.EstadoEvidencia
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun PerfilContent(
    modifier: Modifier = Modifier, 
    nombre: String, 
    carrera: String, 
    tareas: List<TareaItem>, 
    correo: String = "",
    onGuardarCambios: (String, String) -> Unit = { _, _ -> }
) {
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



        // Materias Section
        Text(
            text = "Mis Materias",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        val materias = tareas.groupBy { it.tarea.nombreClase }
        if (materias.isEmpty()) {
            Text("Aún no tienes materias asignadas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                materias.forEach { (nombreClase, tareasClase) ->
                    ClaseStatAccordion(nombreClase = nombreClase, tareas = tareasClase)
                }
            }
        }

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
        AjustesCuentaAccordion(correoActual = correo, onGuardarCambios = onGuardarCambios)
        Spacer(Modifier.height(12.dp))
        ShortcutItem(icon = Icons.AutoMirrored.Filled.HelpOutline, title = "Soporte Técnico", subtitle = "Reportar un problema con la app")
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaseStatAccordion(nombreClase: String, tareas: List<TareaItem>) {
    var expanded by remember { mutableStateOf(false) }
    val tareasEntregadas = tareas.filter { it.estado != EstadoEvidencia.Pendiente }.size
    val tareasPendientes = tareas.filter { it.estado == EstadoEvidencia.Pendiente }.size
    val calificaciones = tareas.mapNotNull { it.calificacion }
    val calificacionPromedio = if (calificaciones.isNotEmpty()) calificaciones.average() else Double.NaN
    val promedioStr = if (calificacionPromedio.isNaN()) "-" else String.format(java.util.Locale.getDefault(), "%.1f", calificacionPromedio)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { expanded = !expanded },
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = nombreClase.firstOrNull()?.toString()?.uppercase() ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = nombreClase, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(text = "${tareas.size} tarea(s) en total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$promedioStr / 10", 
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text("Promedio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(tareasEntregadas.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Text("Entregadas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(tareasPendientes.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text("Pendientes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesCuentaAccordion(
    correoActual: String = "",
    onGuardarCambios: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    var correo by remember(correoActual) { mutableStateOf(correoActual) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { expanded = !expanded },
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Ajustes de Cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(text = "Cambiar contraseña y correo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Nueva Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword
                    )
                    
                    if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text(
                            text = "Las contraseñas no coinciden",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Button(
                        onClick = {
                            onGuardarCambios(correo, password)
                            expanded = false
                            password = ""
                            confirmPassword = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        enabled = correo.isNotBlank() && (password.isEmpty() || password == confirmPassword)
                    ) {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutItem(icon: ImageVector, title: String, subtitle: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = {},
        shape = MaterialTheme.shapes.large
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
