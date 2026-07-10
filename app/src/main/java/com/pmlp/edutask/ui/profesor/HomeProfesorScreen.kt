package com.pmlp.edutask.ui.profesor

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.EvidenciaTarea
import com.pmlp.edutask.ui.theme.EduTaskTheme
import java.util.Date
import java.text.SimpleDateFormat

private val EVIDENCIAS = listOf(
    EvidenciaTarea("1", "Evidencia Act. 3 PMLP", "", Date(System.currentTimeMillis() - 2 * 3600000),  EstadoEvidencia.Pendiente, "1", "Juan Ramirez"),
    EvidenciaTarea("2", "Diagrama ER BD",         "", Date(System.currentTimeMillis() - 5 * 3600000),  EstadoEvidencia.Pendiente, "2", "Maria Lopez"),
    EvidenciaTarea("3", "Casos de Uso IS",        "", Date(System.currentTimeMillis() - 10 * 3600000), EstadoEvidencia.Pendiente, "3", "Carlos Torres"),
    EvidenciaTarea("4", "App mockup PMLP",        "", Date(System.currentTimeMillis() - 24 * 3600000), EstadoEvidencia.Aprobada,  "4", "Ana Garcia")
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
    ProfNavItem("Clases",  Icons.Default.School),
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
    var showClassDialog by remember { mutableStateOf(false) }
    var nuevaClaseNombre by remember { mutableStateOf("") }

    val pendientes    = EVIDENCIAS.count { it.estado == EstadoEvidencia.Pendiente }
    val initials      = nombreProfesor.split(" ").filter { it.length > 2 }.take(2)
        .joinToString("") { it.first().toString().uppercase() }.ifBlank { "P" }

    // Diálogo para Crear Clase Nueva
    if (showClassDialog) {
        AlertDialog(
            onDismissRequest = { showClassDialog = false },
            icon    = { Icon(Icons.Default.Class, null) },
            title   = { Text("Crear Nueva Clase", style = MaterialTheme.typography.headlineSmall) },
            text    = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa el nombre de la asignatura académica.", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = nuevaClaseNombre,
                        onValueChange = { nuevaClaseNombre = it },
                        label = { Text("Nombre de la Clase") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClassDialog = false
                        // Aquí se conectará con Firebase para guardar la clase
                        nuevaClaseNombre = ""
                    },
                    enabled = nuevaClaseNombre.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClassDialog = false }) {
                    Text("Cancelar", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }

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
                        Text(if(selectedNav == 2) "Gestión Académica" else claseActual, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesion") }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            if (selectedNav ==  1) {
                ExtendedFloatingActionButton(
                    onClick = onCrearTarea,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.AddTask, null) },
                    text = { Text("Nueva Tarea") }
                )
            } else if (selectedNav == 2) {
                ExtendedFloatingActionButton(
                    onClick = { showClassDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.AddHomeWork, null) },
                    text = { Text("Nueva Clase") }
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor   = MaterialTheme.colorScheme.onSurfaceVariant) {
                PROF_NAV.forEachIndexed { i, item ->
                    NavigationBarItem(selected = selectedNav == i, onClick = { selectedNav = i },
                        icon = {
                            if (i == 0 && pendientes > 0)
                                BadgedBox(badge = { Badge { Text(pendientes.toString()) } }) { Icon(item.icon, null) }
                            else Icon(item.icon, null)
                        },
                        label = { Text(item.label, style = MaterialTheme.typography.labelMedium) })
                }
            }
        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            when (selectedNav) {
                0 -> InicioContent(pendientes, claseActual, onCrearTarea)
                1 -> TareasContent(claseActual)
                2 -> ClasesContent()
                3 -> PerfilContent(nombreProfesor)
            }
        }
    }
}

@Composable
private fun InicioContent(pendientes: Int, claseActual: String, onCrearTarea: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Estatus de Aula", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        Text("Tienes actividades pendientes por evaluar.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Evidencias Recientes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (pendientes > 0) Badge(containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor   = MaterialTheme.colorScheme.onTertiary) { Text(pendientes.toString()) }
            }
        }
        items(EVIDENCIAS) { ev -> EvidenciaListItem(ev) }
        item { Spacer(Modifier.height(16.dp)) }
        item { Text("Acceso Rápido", style = MaterialTheme.typography.titleLarge) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ACCESOS.chunked(2).forEach { fila ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        fila.forEach { acceso -> AccesoRapidoCard(acceso, Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TareasContent(claseActual: String) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Panel de Actividades", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { Text("Lista de tareas vigentes asignadas a la clase $claseActual.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        // Aquí se iterarán las tareas ligadas desde Firebase en el futuro
    }
}

@Composable
private fun ClasesContent() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Mis Asignaturas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Programación Móvil PMLP", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Código de unión: EDUTASK-6093", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun PerfilContent(nombre: String) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Rol: Docente / Coordinador", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun EvidenciaListItem(evidencia: EvidenciaTarea) {
    val fmt = SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
    val isPend = evidencia.estado == EstadoEvidencia.Pendiente
    OutlinedCard(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        ListItem(
            headlineContent = { Text(evidencia.tituloTarea, style = MaterialTheme.typography.titleSmall) },
            supportingContent = {
                Column {
                    Text(evidencia.nombreAlumno, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Enviado: ${fmt.format(evidencia.fechaEnvio)}", style = MaterialTheme.typography.labelSmall)
                }
            },
            leadingContent = {
                Surface(Modifier.size(12.dp), shape = MaterialTheme.shapes.extraLarge,
                    color = if (isPend) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary) {}
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(onClick = {}, modifier = Modifier.height(28.dp)) {
                        Text("Ver", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        )
    }
}

@Composable
private fun AccesoRapidoCard(acceso: AccesoRapido, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(Modifier.size(48.dp), shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.secondaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(acceso.icon, contentDescription = acceso.label, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Text(acceso.label, style = MaterialTheme.typography.labelMedium)
        }
    }
}