package com.pmlp.edutask.ui.alumno

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
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
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.ui.theme.EduTaskTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat

private data class NavItem(val label: String, val icon: ImageVector)
private val NAV_ITEMS = listOf(
    NavItem("Inicio",         Icons.Default.Home),
    NavItem("Tareas",         Icons.Default.Assignment),
    NavItem("Calificaciones", Icons.Default.Grade),
    NavItem("Perfil",         Icons.Default.Person)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeAlumnoScreen(
    idUsuario: String    = "",
    nombreAlumno: String = "Juan Ramirez",
    carrera: String      = "Ingenieria de Software",
    viewModel: HomeAlumnoViewModel = viewModel(),
    onSubirEvidencia: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context    = LocalContext.current
    val winSize    = calculateWindowSizeClass(activity = context as Activity)
    val isCompact  = winSize.widthSizeClass == WindowWidthSizeClass.Compact

    var selectedNav       by remember { mutableIntStateOf(0) }
    var claseSelected     by remember { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idUsuario) {
        viewModel.fetchUserData(idUsuario)
    }

    val pendienteCount = if (uiState is HomeAlumnoState.Success) {
        (uiState as HomeAlumnoState.Success).tareas.count { (_, e) -> e == EstadoEvidencia.Pendiente }
    } else 0

    val initials = nombreAlumno.split(" ").take(2).joinToString("") { it.first().toString().uppercase() }

    if (isCompact) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor   = MaterialTheme.colorScheme.onBackground,
            topBar = {
                MediumTopAppBar(
                    navigationIcon = {
                        Surface(Modifier.padding(start = 12.dp).size(40.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.primaryContainer) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(initials, style = MaterialTheme.typography.labelLarge,
                                     color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    },
                    title = {
                        Column {
                            Text("Hola, ${nombreAlumno.substringBefore(" ")}!", style = MaterialTheme.typography.titleLarge,
                                 maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(carrera, style = MaterialTheme.typography.bodySmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        BadgedBox(badge = { if (pendienteCount > 0) Badge { Text(pendienteCount.toString()) } }) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant,
                              contentColor   = MaterialTheme.colorScheme.onSurfaceVariant) {
                    NAV_ITEMS.forEachIndexed { i, item ->
                        NavigationBarItem(
                            selected = selectedNav == i, onClick = { selectedNav = i },
                            icon = {
                                if (i == 1 && pendienteCount > 0)
                                    BadgedBox(badge = { Badge { Text(pendienteCount.toString()) } }) { Icon(item.icon, null) }
                                else Icon(item.icon, null)
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onSubirEvidencia,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Subir Evidencia") },
                    text = { Text("Subir Evidencia", style = MaterialTheme.typography.labelLarge) }
                )
            }
        ) { pad ->
            when (uiState) {
                is HomeAlumnoState.Loading -> {
                    Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HomeAlumnoState.Error -> {
                    Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                        Text("Error al cargar datos.", color = MaterialTheme.colorScheme.error)
                    }
                }
                is HomeAlumnoState.Success -> {
                    val data = uiState as HomeAlumnoState.Success
                    val tareasFiltradas = if (claseSelected == null) data.tareas
                                          else data.tareas.filter { (t, _) -> t.nombreClase == claseSelected }
                    TareasContent(Modifier.padding(pad), claseSelected, { claseSelected = if (claseSelected == it) null else it },
                                  tareasFiltradas, pendienteCount, data.clases)
                }
            }
        }
    } else {
        Row(Modifier.fillMaxSize()) {
            NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceVariant,
                           contentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
                           header = {
                               Spacer(Modifier.height(8.dp))
                               Surface(Modifier.size(40.dp), shape = MaterialTheme.shapes.extraLarge,
                                       color = MaterialTheme.colorScheme.primaryContainer) {
                                   Box(contentAlignment = Alignment.Center) {
                                       Text(initials, style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                                   }
                               }
                               Spacer(Modifier.height(8.dp))
                               ExtendedFloatingActionButton(onClick = onSubirEvidencia,
                                   containerColor = MaterialTheme.colorScheme.primaryContainer,
                                   contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                                   icon = { Icon(Icons.Default.CameraAlt, "Subir Evidencia") },
                                   text = { Text("Evidencia", style = MaterialTheme.typography.labelMedium) })
                           }) {
                NAV_ITEMS.forEachIndexed { i, item ->
                    NavigationRailItem(selected = selectedNav == i, onClick = { selectedNav = i },
                        icon = { Icon(item.icon, null) }, label = { Text(item.label) })
                }
            }
            Scaffold(Modifier.weight(1f), containerColor = MaterialTheme.colorScheme.background,
                     topBar = {
                         LargeTopAppBar(
                             title = {
                                 Column {
                                     Text("Hola, $nombreAlumno!", style = MaterialTheme.typography.headlineMedium)
                                     Text(carrera, style = MaterialTheme.typography.bodyMedium,
                                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                                 }
                             },
                             actions = {
                                 BadgedBox(badge = { if (pendienteCount > 0) Badge { Text(pendienteCount.toString()) } }) {
                                     IconButton(onClick = {}) { Icon(Icons.Default.Notifications, "Notificaciones") }
                                 }
                             },
                             colors = TopAppBarDefaults.largeTopAppBarColors(
                                 containerColor = MaterialTheme.colorScheme.surface,
                                 scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                             )
                         )
                     }) { pad ->
                when (uiState) {
                    is HomeAlumnoState.Loading -> {
                        Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is HomeAlumnoState.Error -> {
                        Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                            Text("Error al cargar datos.", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is HomeAlumnoState.Success -> {
                        val data = uiState as HomeAlumnoState.Success
                        val tareasFiltradas = if (claseSelected == null) data.tareas
                                              else data.tareas.filter { (t, _) -> t.nombreClase == claseSelected }
                        TareasContent(Modifier.padding(pad), claseSelected, { claseSelected = if (claseSelected == it) null else it },
                                      tareasFiltradas, pendienteCount, data.clases)
                    }
                }
            }
        }
    }
}

@Composable
private fun TareasContent(modifier: Modifier, claseSelected: String?, onClaseSelected: (String) -> Unit,
                          tareas: List<Pair<Tarea, EstadoEvidencia>>, pendienteCount: Int, clases: List<String>) {
    LazyColumn(modifier = modifier.fillMaxSize(),
               contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
               verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Mis Clases", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp)) {
                items(clases) { clase ->
                    val chipColor by animateColorAsState(
                        targetValue = if (claseSelected == clase) MaterialTheme.colorScheme.primaryContainer
                                      else MaterialTheme.colorScheme.surface,
                        animationSpec = tween(300), label = "chipColor"
                    )
                    FilterChip(selected = claseSelected == clase, onClick = { onClaseSelected(clase) },
                               label = { Text(clase, style = MaterialTheme.typography.labelMedium) },
                               leadingIcon = { if (claseSelected == clase) Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) })
                }
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tareas Pendientes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (pendienteCount > 0) Badge(containerColor = MaterialTheme.colorScheme.error,
                                              contentColor   = MaterialTheme.colorScheme.onError) { Text(pendienteCount.toString()) }
            }
        }
        items(tareas) { (tarea, estado) -> TareaCard(tarea, estado) }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
fun TareaCard(tarea: Tarea, estado: EstadoEvidencia) {
    val fmt = SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
    ElevatedCard(modifier = Modifier.fillMaxWidth(),
                 elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                 colors    = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        ListItem(
            headlineContent  = { Text(tarea.titulo, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis) },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(tarea.descripcion, style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(onClick = {}, label = { Text(tarea.nombreClase, style = MaterialTheme.typography.labelSmall) },
                                   leadingIcon = { Icon(Icons.Default.School, null, Modifier.size(AssistChipDefaults.IconSize)) },
                                   border = AssistChipDefaults.assistChipBorder(enabled = true))
                        Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(fmt.format(tarea.fechaLimite), style = MaterialTheme.typography.labelSmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            leadingContent  = {
                Icon(when (estado) {
                         EstadoEvidencia.Aprobada  -> Icons.Default.CheckCircle
                         EstadoEvidencia.Rechazada -> Icons.Default.Cancel
                         EstadoEvidencia.Pendiente -> Icons.Default.HourglassEmpty
                     },
                     contentDescription = "Estado: ${estado.name}",
                     tint = when (estado) {
                         EstadoEvidencia.Aprobada  -> MaterialTheme.colorScheme.tertiary
                         EstadoEvidencia.Rechazada -> MaterialTheme.colorScheme.error
                         EstadoEvidencia.Pendiente -> MaterialTheme.colorScheme.primary
                     })
            },
            trailingContent = { EstadoChip(estado) },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.clickable {}
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun EstadoChip(estado: EstadoEvidencia) {
    val label = when (estado) { EstadoEvidencia.Pendiente -> "Pendiente"; EstadoEvidencia.Aprobada -> "Aprobada"; EstadoEvidencia.Rechazada -> "Rechazada" }
    val icon  = when (estado) { EstadoEvidencia.Pendiente -> Icons.Default.Schedule; EstadoEvidencia.Aprobada -> Icons.Default.CheckCircle; EstadoEvidencia.Rechazada -> Icons.Default.Cancel }
    val container = when (estado) { EstadoEvidencia.Pendiente -> MaterialTheme.colorScheme.secondaryContainer; EstadoEvidencia.Aprobada -> MaterialTheme.colorScheme.tertiaryContainer; EstadoEvidencia.Rechazada -> MaterialTheme.colorScheme.errorContainer }
    val content   = when (estado) { EstadoEvidencia.Pendiente -> MaterialTheme.colorScheme.onSecondaryContainer; EstadoEvidencia.Aprobada -> MaterialTheme.colorScheme.onTertiaryContainer; EstadoEvidencia.Rechazada -> MaterialTheme.colorScheme.onErrorContainer }
    SuggestionChip(onClick = {}, label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                   icon = { Icon(icon, null, Modifier.size(AssistChipDefaults.IconSize)) },
                   colors = SuggestionChipDefaults.suggestionChipColors(containerColor = container, labelColor = content, iconContentColor = content),
                   border = null)
}

@Preview(name = "Home Alumno Movil", showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewAlumnoMovil() { EduTaskTheme(darkTheme = false, dynamicColor = false) { HomeAlumnoScreen() } }

@Preview(name = "Home Alumno Tablet", showBackground = true, showSystemUi = true, widthDp = 800, heightDp = 1280)
@Composable
private fun PreviewAlumnoTablet() { EduTaskTheme(darkTheme = false, dynamicColor = false) { HomeAlumnoScreen() } }