package com.pmlp.edutask.ui.profesor

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.EvidenciaTarea
import com.pmlp.edutask.ui.theme.EduTaskTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val EVIDENCIAS = listOf(
    EvidenciaTarea(1, "Evidencia Act. 3 PMLP", "", LocalDateTime.now().minusHours(2),  EstadoEvidencia.Pendiente, 1, "Juan Ramirez"),
    EvidenciaTarea(2, "Diagrama ER BD",         "", LocalDateTime.now().minusHours(5),  EstadoEvidencia.Pendiente, 2, "Maria Lopez"),
    EvidenciaTarea(3, "Casos de Uso IS",        "", LocalDateTime.now().minusHours(10), EstadoEvidencia.Pendiente, 3, "Carlos Torres"),
    EvidenciaTarea(4, "App mockup PMLP",        "", LocalDateTime.now().minusDays(1),   EstadoEvidencia.Aprobada,  4, "Ana Garcia")
)

private data class AccesoRapido(val label: String, val icon: ImageVector)
private val ACCESOS = listOf(
    AccesoRapido("Mis Clases",     Icons.Default.School),
    AccesoRapido("Calificaciones", Icons.Default.Grade),
    AccesoRapido("Estadisticas",   Icons.Default.BarChart),
    AccesoRapido("Configuracion",  Icons.Default.Settings)
)

private data class ProfNavItem(val label: String, val icon: ImageVector)
private val PROF_NAV = listOf(
    ProfNavItem("Inicio",  Icons.Default.Home),
    ProfNavItem("Tareas",  Icons.Default.Assignment),
    ProfNavItem("Alumnos", Icons.Default.Group),
    ProfNavItem("Perfil",  Icons.Default.Person)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeProfesorScreen(
    idUsuario: String        = "",
    nombreProfesor: String   = "Mtro. Perez",
    claseActual: String      = "Programacion Movil PMLP",
    onCrearTarea: () -> Unit = {},
    onLogout: () -> Unit     = {}
) {
    val context   = LocalContext.current
    val winSize   = calculateWindowSizeClass(activity = context as Activity)
    val isCompact = winSize.widthSizeClass == WindowWidthSizeClass.Compact

    var selectedNav   by remember { mutableIntStateOf(0) }
    var showDialog    by remember { mutableStateOf(false) }
    val pendientes    = EVIDENCIAS.count { it.estado == EstadoEvidencia.Pendiente }
    val initials      = nombreProfesor.split(" ").filter { it.length > 2 }.take(2)
                            .joinToString("") { it.first().toString().uppercase() }.ifBlank { "P" }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon    = { Icon(Icons.Default.AddTask, null) },
            title   = { Text("Nueva Tarea", style = MaterialTheme.typography.headlineSmall) },
            text    = { Text("Crear nueva tarea para $claseActual?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = { showDialog = false; onCrearTarea() },
                       colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Crear", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancelar", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }

    if (isCompact) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor   = MaterialTheme.colorScheme.onBackground,
            topBar = {
                LargeTopAppBar(
                    navigationIcon = {
                        Surface(Modifier.padding(start = 12.dp).size(40.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.secondaryContainer) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(initials, style = MaterialTheme.typography.labelLarge,
                                     color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    },
                    title = {
                        Column {
                            Text("Hola, $nombreProfesor", style = MaterialTheme.typography.headlineSmall,
                                 maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(claseActual, style = MaterialTheme.typography.bodySmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Default.Settings, contentDescription = "Configuracion") }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant,
                              contentColor   = MaterialTheme.colorScheme.onSurfaceVariant) {
                    PROF_NAV.forEachIndexed { i, item ->
                        NavigationBarItem(selected = selectedNav == i, onClick = { selectedNav = i },
                            icon = {
                                if (i == 1 && pendientes > 0)
                                    BadgedBox(badge = { Badge { Text(pendientes.toString()) } }) { Icon(item.icon, null) }
                                else Icon(item.icon, null)
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelMedium) })
                    }
                }
            }
        ) { pad ->
            ProfesorContent(Modifier.padding(pad), pendientes, claseActual, { showDialog = true })
        }
    } else {
        Row(Modifier.fillMaxSize()) {
            NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceVariant,
                           contentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
                           header = {
                               Spacer(Modifier.height(8.dp))
                               Surface(Modifier.size(40.dp), shape = MaterialTheme.shapes.extraLarge,
                                       color = MaterialTheme.colorScheme.secondaryContainer) {
                                   Box(contentAlignment = Alignment.Center) {
                                       Text(initials, style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                                   }
                               }
                               Spacer(Modifier.height(8.dp))
                               FilledTonalButton(onClick = { showDialog = true },
                                   colors = ButtonDefaults.filledTonalButtonColors(
                                       containerColor = MaterialTheme.colorScheme.primaryContainer,
                                       contentColor   = MaterialTheme.colorScheme.onPrimaryContainer)) {
                                   Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                                   Spacer(Modifier.width(4.dp))
                                   Text("Crear", style = MaterialTheme.typography.labelMedium)
                               }
                           }) {
                PROF_NAV.forEachIndexed { i, item ->
                    NavigationRailItem(selected = selectedNav == i, onClick = { selectedNav = i },
                        icon = { Icon(item.icon, null) }, label = { Text(item.label) })
                }
            }
            Scaffold(Modifier.weight(1f), containerColor = MaterialTheme.colorScheme.background,
                     topBar = {
                         LargeTopAppBar(
                             title = {
                                 Column {
                                     Text("Hola, $nombreProfesor", style = MaterialTheme.typography.headlineMedium)
                                     Text(claseActual, style = MaterialTheme.typography.bodyMedium,
                                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                                 }
                             },
                             actions = { IconButton(onClick = {}) { Icon(Icons.Default.Settings, "Configuracion") } },
                             colors = TopAppBarDefaults.largeTopAppBarColors(
                                 containerColor = MaterialTheme.colorScheme.surface,
                                 scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                             )
                         )
                     }) { pad ->
                ProfesorContent(Modifier.padding(pad), pendientes, claseActual, { showDialog = true })
            }
        }
    }
}

@Composable
private fun ProfesorContent(modifier: Modifier, pendientes: Int, claseActual: String, onCrearTarea: () -> Unit) {
    LazyColumn(modifier = modifier.fillMaxSize(),
               contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
               verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth(),
                         elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                         colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Nueva Tarea", style = MaterialTheme.typography.titleMedium,
                             color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Asigna actividades a tus alumnos", style = MaterialTheme.typography.bodySmall,
                             color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    FilledTonalButton(onClick = onCrearTarea,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor   = MaterialTheme.colorScheme.onPrimary)) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Crear", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Evidencias Recientes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (pendientes > 0) Badge(containerColor = MaterialTheme.colorScheme.tertiary,
                                         contentColor   = MaterialTheme.colorScheme.onTertiary) { Text(pendientes.toString()) }
                TextButton(onClick = {}) { Text("Ver todas", style = MaterialTheme.typography.labelMedium) }
            }
            Text("Pendientes de calificacion", style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(EVIDENCIAS) { ev -> EvidenciaListItem(ev) }
        item { HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant) }
        item { Text("Acceso Rapido", style = MaterialTheme.typography.titleLarge) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ACCESOS.chunked(2).forEach { fila ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        fila.forEach { acceso -> AccesoRapidoCard(acceso, Modifier.weight(1f)) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun EvidenciaListItem(evidencia: EvidenciaTarea) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    val isPend = evidencia.estado == EstadoEvidencia.Pendiente
    OutlinedCard(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        ListItem(
            headlineContent   = { Text(evidencia.nombreAlumno, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            supportingContent = { Text(evidencia.tituloTarea, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingContent    = {
                Surface(Modifier.size(12.dp), shape = MaterialTheme.shapes.extraLarge,
                        color = if (isPend) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary) {}
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(evidencia.fechaEnvio.format(fmt), style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(onClick = {}, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                   modifier = Modifier.height(28.dp)) {
                        Text("Ver", style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            colors   = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.clickable {}
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun AccesoRapidoCard(acceso: AccesoRapido, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier, onClick = {},
                 elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                 colors    = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp),
               horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(Modifier.size(48.dp), shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.secondaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(acceso.icon, contentDescription = acceso.label, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Text(acceso.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview(name = "Home Profesor Movil", showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewProfesorMovil() { EduTaskTheme(darkTheme = false, dynamicColor = false) { HomeProfesorScreen() } }

@Preview(name = "Home Profesor Tablet", showBackground = true, showSystemUi = true, widthDp = 800, heightDp = 1280)
@Composable
private fun PreviewProfesorTablet() { EduTaskTheme(darkTheme = false, dynamicColor = false) { HomeProfesorScreen() } }