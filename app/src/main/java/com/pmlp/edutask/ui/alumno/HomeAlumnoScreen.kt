package com.pmlp.edutask.ui.alumno

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.edutask.ui.EventosUiState
import java.text.SimpleDateFormat

private data class NavItem(val label: String, val icon: ImageVector)
private val NAV_ITEMS = listOf(
    NavItem("Inicio",         Icons.Default.Home),
    NavItem("Tareas",         Icons.AutoMirrored.Filled.Assignment),
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
    eventosViewModel: EventosSharedViewModel = viewModel(),
    onVerTarea: (TareaItem) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context    = LocalContext.current
    val winSize    = calculateWindowSizeClass(activity = context as Activity)
    val isCompact  = winSize.widthSizeClass == WindowWidthSizeClass.Compact

    var selectedNav       by rememberSaveable { mutableIntStateOf(0) }
    var claseSelected     by rememberSaveable { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val correoAlumno by viewModel.correo.collectAsState()
    val eventosState by eventosViewModel.uiState.collectAsState()

    LaunchedEffect(idUsuario) {
        viewModel.fetchUserData(idUsuario)
        eventosViewModel.fetchEventos()
    }

    val now = java.util.Date()
    val pendienteCount = if (uiState is HomeAlumnoState.Success) {
        (uiState as HomeAlumnoState.Success).tareas.count { it.estado == EstadoEvidencia.Pendiente && it.idEvidencia == null && !now.after(it.tarea.fechaLimite) }
    } else 0

    val initials = nombreAlumno.split(" ").take(2).joinToString("") { it.first().toString().uppercase() }

    if (isCompact) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor   = MaterialTheme.colorScheme.onBackground,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        Surface(Modifier.padding(start = 12.dp, end = 12.dp).size(40.dp),
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
                            Text("Hola, ${nombreAlumno.substringBefore(" ")}!", style = MaterialTheme.typography.titleMedium,
                                 maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(carrera, style = MaterialTheme.typography.bodySmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
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
                                          else data.tareas.filter { it.tarea.nombreClase == claseSelected }
                    
                    val eventos = if (eventosState is EventosUiState.Success) {
                        (eventosState as EventosUiState.Success).eventos
                    } else emptyList()
                    
                    Crossfade(targetState = selectedNav, label = "TabSwitch") { nav ->
                        when (nav) {
                            0 -> InicioContent(Modifier.padding(pad), pendienteCount, data.tareas, eventos, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea) { codigo ->
                                viewModel.unirseAClase(codigo, idUsuario)
                            }
                            1 -> TareasContent(Modifier.padding(pad), claseSelected, { claseSelected = if (claseSelected == it) null else it },
                                          tareasFiltradas, pendienteCount, data.clases, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea)
                            2 -> CalificacionesContent(Modifier.padding(pad), data.tareas, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea)
                            3 -> PerfilContent(Modifier.padding(pad), nombreAlumno, carrera, data.tareas, correoAlumno) { nuevoCorreo, nuevaContrasena ->
                                viewModel.updateAccount(idUsuario, nuevoCorreo, nuevaContrasena) { success ->
                                    if (success) {
                                        android.widget.Toast.makeText(context, "Cuenta actualizada", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Error al actualizar", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
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
                           }) {
                NAV_ITEMS.forEachIndexed { i, item ->
                    NavigationRailItem(selected = selectedNav == i, onClick = { selectedNav = i },
                        icon = { Icon(item.icon, null) }, label = { Text(item.label) })
                }
            }
            Scaffold(Modifier.weight(1f), containerColor = MaterialTheme.colorScheme.background,
                     topBar = {
                         TopAppBar(
                             title = {
                                 Column {
                                     Text("Hola, $nombreAlumno!", style = MaterialTheme.typography.titleLarge)
                                     Text(carrera, style = MaterialTheme.typography.bodyMedium,
                                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                                 }
                             },
                             actions = {
                                 IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar sesión") }
                             },
                             colors = TopAppBarDefaults.topAppBarColors(
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
                                              else data.tareas.filter { it.tarea.nombreClase == claseSelected }
                        
                        val eventos = if (eventosState is EventosUiState.Success) {
                            (eventosState as EventosUiState.Success).eventos
                        } else emptyList()
                        
                        Crossfade(targetState = selectedNav, label = "TabSwitchTablet") { nav ->
                            when (nav) {
                                0 -> InicioContent(Modifier.padding(pad), pendienteCount, data.tareas, eventos, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea) { codigo ->
                                    viewModel.unirseAClase(codigo, idUsuario)
                                }
                                1 -> TareasContent(Modifier.padding(pad), claseSelected, { claseSelected = if (claseSelected == it) null else it },
                                              tareasFiltradas, pendienteCount, data.clases, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea)
                                2 -> CalificacionesContent(Modifier.padding(pad), data.tareas, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea)
                                3 -> PerfilContent(Modifier.padding(pad), nombreAlumno, carrera, data.tareas, correoAlumno) { nuevoCorreo, nuevaContrasena ->
                                    viewModel.updateAccount(idUsuario, nuevoCorreo, nuevaContrasena) { success ->
                                        if (success) {
                                            android.widget.Toast.makeText(context, "Cuenta actualizada", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Error al actualizar", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(name = "Home Alumno Movil", showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewAlumnoMovil() { EduTaskTheme(darkTheme = false, dynamicColor = false) { HomeAlumnoScreen() } }

@Preview(name = "Home Alumno Tablet", showBackground = true, showSystemUi = true, widthDp = 800, heightDp = 1280)
@Composable
private fun PreviewAlumnoTablet() { EduTaskTheme(darkTheme = false, dynamicColor = false) { HomeAlumnoScreen() } }