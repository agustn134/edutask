package com.pmlp.edutask.ui.profesor

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.pmlp.edutask.ui.components.ShimmerPlaceholder
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.EvidenciaTarea
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.model.ClaseInfo
import com.pmlp.edutask.ui.theme.EduTaskTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.edutask.ui.EventosUiState
import com.pmlp.edutask.model.Evento
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.text.input.PasswordVisualTransformation
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

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
    AccesoRapido("Estadísticas",   Icons.Default.BarChart),
    AccesoRapido("Configuración",  Icons.Default.Settings)
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
    eventosViewModel: EventosSharedViewModel = viewModel(),
    onCrearTarea: (String, String?) -> Unit = { _, _ -> },
    onVerEvidencia: (String) -> Unit = {},
    onVerAlumnos: (String, String) -> Unit = { _, _ -> },
    onVerEstadisticas: (String, String) -> Unit = { _, _ -> },
    onLogout: () -> Unit     = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val winSize   = calculateWindowSizeClass(activity = context as Activity)
    val isCompact = winSize.widthSizeClass == WindowWidthSizeClass.Compact

    var selectedNav   by remember { mutableIntStateOf(0) }
    var showClassDialog by remember { mutableStateOf(false) }
    var nuevaClaseNombre by remember { mutableStateOf("") }
    var nuevaClaseDesc by remember { mutableStateOf("") }
    var nuevaClaseEnlace by remember { mutableStateOf("") }
    var editingClaseId by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    var inscripcionesMap by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }

    // Estado para manejar las evidencias reales traídas de Firebase
    var listaEvidencias by remember { mutableStateOf<List<EvidenciaTarea>>(emptyList()) }
    val db = remember { FirebaseFirestore.getInstance() }
    
    var showDeleteTareaDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteClaseDialog by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    var correoProfesor by remember { mutableStateOf("") }
    LaunchedEffect(idUsuario) {
        if (idUsuario.isNotBlank()) {
            db.collection("usuarios").document(idUsuario).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        correoProfesor = doc.getString("correo") ?: ""
                    }
                }
        }
    }

    // Real-time listener for classes of this professor
    var listaClases by remember { mutableStateOf<List<ClaseInfo>>(emptyList()) }
    DisposableEffect(idUsuario) {
        if (idUsuario.isBlank()) return@DisposableEffect onDispose {}
        val listener = db.collection("clases")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                listaClases = snapshot.documents.map { doc ->
                    ClaseInfo(
                        idClase = doc.id,
                        nombre = doc.getString("nombre") ?: "Sin Nombre",
                        descripcion = doc.getString("descripcion") ?: "Sin Descripción",
                        enlace = doc.getString("enlace") ?: ""
                    )
                }
            }
        onDispose { listener.remove() }
    }

    // Real-time listener for enrolled students
    DisposableEffect(listaClases) {
        if (listaClases.isEmpty()) return@DisposableEffect onDispose {}
        val clasesIds = listaClases.map { it.idClase }
        val listener = db.collection("clase_alumno")
            .whereIn("idClase", clasesIds)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    val tempMap = mutableMapOf<String, MutableList<String>>()
                    snapshot.documents.forEach { doc ->
                        val idClase = doc.getString("idClase") ?: return@forEach
                        val idAlumno = doc.getString("idUsuario") ?: return@forEach
                        
                        val userDoc = db.collection("usuarios").document(idAlumno).get().await()
                        val name = userDoc.getString("nombre") ?: "Alumno Sin Nombre"
                        
                        val list = tempMap.getOrPut(idClase) { mutableListOf() }
                        if (!list.contains(name)) {
                            list.add(name)
                        }
                    }
                    inscripcionesMap = tempMap
                }
            }
        onDispose { listener.remove() }
    }

    var listaTareas by remember { mutableStateOf<List<Tarea>>(emptyList()) }
    // Real-time listener for tasks of this professor's classes
    DisposableEffect(listaClases) {
        if (listaClases.isEmpty()) return@DisposableEffect onDispose {}
        val clasesIds = listaClases.map { it.idClase }
        val listener = db.collection("tareas")
            .whereIn("idClase", clasesIds)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                listaTareas = snapshot.documents.map { doc ->
                    val dateTimestamp = doc.getTimestamp("fechaLimite")
                    val date = dateTimestamp?.toDate() ?: Date()
                    Tarea(
                        idTarea = doc.id,
                        titulo = doc.getString("titulo") ?: "Sin Título",
                        descripcion = doc.getString("descripcion") ?: "",
                        fechaLimite = date,
                        idClase = doc.getString("idClase") ?: "",
                        nombreClase = doc.getString("nombreClase") ?: ""
                    )
                }
            }
        onDispose { listener.remove() }
    }

    val tareasIds = remember(listaTareas) { listaTareas.map { it.idTarea } }
    var asignacionesIdsDelProfesor by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Listener for assignments related to this professor's tasks
    DisposableEffect(tareasIds) {
        if (tareasIds.isEmpty()) {
            asignacionesIdsDelProfesor = emptySet()
            onDispose {}
        } else {
            val listener = db.collection("asignaciones_tarea")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val matchingIds = snapshot.documents
                        .filter { doc ->
                            val idTarea = doc.getString("idTarea") ?: ""
                            idTarea in tareasIds
                        }
                        .map { it.id }
                        .toSet()
                    asignacionesIdsDelProfesor = matchingIds
                }
            onDispose { listener.remove() }
        }
    }

    // Listener for evidence submissions, filtered in memory by professor assignment IDs
    DisposableEffect(asignacionesIdsDelProfesor) {
        if (asignacionesIdsDelProfesor.isEmpty()) {
            listaEvidencias = emptyList()
            onDispose {}
        } else {
            val listener = db.collection("evidencias_tarea")
                .orderBy("fechaEnvio", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val evidenciasMapeadas = snapshot.documents
                        .filter { doc ->
                            val idAsignacion = doc.getString("idAsignacion") ?: ""
                            idAsignacion in asignacionesIdsDelProfesor
                        }
                        .map { doc ->
                            val estadoStr = doc.getString("estado") ?: "Pendiente"
                            val estadoEnum = when (estadoStr.lowercase()) {
                                "aprobada" -> EstadoEvidencia.Aprobada
                                "rechazada" -> EstadoEvidencia.Rechazada
                                else -> EstadoEvidencia.Pendiente
                            }

                            val idEvidenciaRaw = doc.get("idEvidencia")
                            val idEvidenciaStr = when (idEvidenciaRaw) {
                                is Number -> idEvidenciaRaw.toLong().toString()
                                else -> idEvidenciaRaw?.toString() ?: doc.id
                            }
                            val idAsignacionRaw = doc.get("idAsignacion")
                            val idAsignacionStr = when (idAsignacionRaw) {
                                is Number -> idAsignacionRaw.toLong().toString()
                                else -> idAsignacionRaw?.toString() ?: ""
                            }
                            EvidenciaTarea(
                                idEvidencia = idEvidenciaStr,
                                tituloTarea = doc.getString("tituloTarea") ?: "Sin Título",
                                fotoBase64 = doc.getString("fotoBase64") ?: doc.getString("fotoUrl") ?: "",
                                fechaEnvio = doc.getDate("fechaEnvio") ?: Date(),
                                estado = estadoEnum,
                                idAsignacion = idAsignacionStr,
                                nombreAlumno = doc.getString("nombreAlumno") ?: "Alumno Anónimo"
                            )
                        }
                    listaEvidencias = evidenciasMapeadas
                }

            onDispose { listener.remove() }
        }
    }

    val pendientes = listaEvidencias.count { it.estado == EstadoEvidencia.Pendiente }
    val initials   = nombreProfesor.split(" ").filter { it.length > 2 }.take(2)
        .joinToString("") { it.first().toString().uppercase() }.ifBlank { "P" }

    val eventosState by eventosViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        eventosViewModel.fetchEventos()
    }

    LaunchedEffect(idUsuario, nombreProfesor) {
        if (idUsuario.isNotBlank()) {
            // 1. Sincronización local vía Wearable API
            try {
                val dataClient = com.google.android.gms.wearable.Wearable.getDataClient(context)
                val putDataReq = com.google.android.gms.wearable.PutDataMapRequest.create("/usuario_logueado").run {
                    dataMap.putString("idUsuario", idUsuario)
                    dataMap.putString("nombre", nombreProfesor)
                    asPutDataRequest()
                }
                dataClient.putDataItem(putDataReq)
                    .addOnSuccessListener { android.util.Log.d("WearSync", "Session synced from Home: $idUsuario") }
                    .addOnFailureListener { android.util.Log.e("WearSync", "Failed to sync session from Home", it) }
            } catch (e: java.lang.Exception) {
                android.util.Log.e("WearSync", "Wearable API error on Home", e)
            }

            // 2. Sincronización en la nube vía Firestore (Fallback)
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("sesion_wear").document("default")
                    .set(hashMapOf(
                        "idUsuario" to idUsuario,
                        "nombre" to nombreProfesor,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    ))
                    .addOnSuccessListener { android.util.Log.d("WearSync", "Session synced to Firestore fallback: $idUsuario") }
            } catch (e: java.lang.Exception) {
                android.util.Log.e("WearSync", "Firestore fallback error on Home", e)
            }
        }
    }

    // Diálogo para Crear Clase Nueva
    if (showClassDialog) {
        AlertDialog(
            onDismissRequest = { 
                showClassDialog = false 
                nuevaClaseNombre = ""
                nuevaClaseDesc = ""
                nuevaClaseEnlace = ""
                editingClaseId = null
            },
            icon    = { Icon(Icons.Default.Class, null) },
            title   = { Text(if (editingClaseId == null) "Crear Nueva Clase" else "Editar Clase", style = MaterialTheme.typography.headlineSmall) },
            text    = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa los detalles de la asignatura académica.", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = nuevaClaseNombre,
                        onValueChange = { nuevaClaseNombre = it },
                        label = { Text("Nombre de la Clase") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevaClaseDesc,
                        onValueChange = { nuevaClaseDesc = it },
                        label = { Text("Descripción de la Clase") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevaClaseEnlace,
                        onValueChange = { nuevaClaseEnlace = it },
                        label = { Text("Enlace de videoconferencia") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevaClaseNombre.isNotBlank()) {
                            val nuevaClase = hashMapOf(
                                "nombre" to nuevaClaseNombre.trim(),
                                "descripcion" to nuevaClaseDesc.trim(),
                                "enlace" to nuevaClaseEnlace.trim(),
                                "idUsuario" to idUsuario
                            )
                            val classId = editingClaseId ?: generateShortCode()
                            db.collection("clases").document(classId).set(nuevaClase)
                            
                            showClassDialog = false
                            nuevaClaseNombre = ""
                            nuevaClaseDesc = ""
                            nuevaClaseEnlace = ""
                            editingClaseId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { 
                    showClassDialog = false 
                    nuevaClaseNombre = ""
                    nuevaClaseDesc = ""
                    nuevaClaseEnlace = ""
                    editingClaseId = null
                }) {
                    Text("Cancelar", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }



    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor   = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Surface(Modifier.padding(start = 12.dp, end = 12.dp).size(40.dp),
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
                        Text("Hola, ${nombreProfesor.substringBefore(" ")}!", style = MaterialTheme.typography.titleMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if(selectedNav == 2) "Gestión Académica" else claseActual, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            if (selectedNav == 1) {
                ExtendedFloatingActionButton(
                    onClick = { onCrearTarea(idUsuario, null) },
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
            val eventos = if (eventosState is EventosUiState.Success) {
                (eventosState as EventosUiState.Success).eventos
            } else emptyList()

            @OptIn(ExperimentalMaterial3Api::class)
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    scope.launch {
                        kotlinx.coroutines.delay(800)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                if (isRefreshing) {
                    when (selectedNav) {
                        0 -> InicioSkeleton()
                        1 -> TareasSkeleton()
                        2 -> ClasesSkeleton()
                        3 -> PerfilContent(
                            nombre = nombreProfesor,
                            clasesCount = listaClases.size,
                            tareasCount = listaTareas.size,
                            evaluacionesCount = listaEvidencias.count { it.estado != EstadoEvidencia.Pendiente },
                            correo = correoProfesor,
                            onGuardarCambios = { nuevoCorreo, nuevaContrasena ->
                                val updates = mutableMapOf<String, Any>("correo" to nuevoCorreo)
                                if (nuevaContrasena.isNotBlank()) {
                                    updates["contrasena"] = nuevaContrasena
                                }
                                db.collection("usuarios").document(idUsuario).update(updates)
                                    .addOnSuccessListener {
                                        correoProfesor = nuevoCorreo
                                        android.widget.Toast.makeText(context, "Cuenta actualizada", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        android.widget.Toast.makeText(context, "Error al actualizar", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            },
                            onLogout = onLogout
                        )
                    }
                } else {
                    when (selectedNav) {
                        0 -> InicioContent(
                            pendientes = pendientes,
                            claseActual = claseActual,
                            eventos = eventos,
                            evidencias = listaEvidencias,
                            onCrearTarea = { onCrearTarea(idUsuario, null) },
                            onVerClick = { onVerEvidencia(it.idEvidencia) }
                        )
                        1 -> TareasContent(
                            tareas = listaTareas,
                            clases = listaClases,
                            idUsuario = idUsuario,
                            onEditTarea = onCrearTarea,
                            onDeleteTarea = { idTarea ->
                                showDeleteTareaDialog = idTarea
                            },
                            onVerEstadisticas = onVerEstadisticas
                        )
                        2 -> ClasesContent(
                            clases = listaClases,
                            inscripciones = inscripcionesMap,
                            onEditClase = { clase ->
                                nuevaClaseNombre = clase.nombre
                                nuevaClaseDesc = clase.descripcion
                                nuevaClaseEnlace = clase.enlace
                                editingClaseId = clase.idClase
                                showClassDialog = true
                            },
                            onDeleteClase = { idClase ->
                                showDeleteClaseDialog = idClase
                            },
                            onVerAlumnos = onVerAlumnos
                        )
                        3 -> PerfilContent(
                            nombre = nombreProfesor,
                            clasesCount = listaClases.size,
                            tareasCount = listaTareas.size,
                            evaluacionesCount = listaEvidencias.count { it.estado != EstadoEvidencia.Pendiente },
                            correo = correoProfesor,
                            onGuardarCambios = { nuevoCorreo, nuevaContrasena ->
                                val updates = mutableMapOf<String, Any>("correo" to nuevoCorreo)
                                if (nuevaContrasena.isNotBlank()) {
                                    updates["contrasena"] = nuevaContrasena
                                }
                                db.collection("usuarios").document(idUsuario).update(updates)
                                    .addOnSuccessListener {
                                        correoProfesor = nuevoCorreo
                                        android.widget.Toast.makeText(context, "Cuenta actualizada", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        android.widget.Toast.makeText(context, "Error al actualizar", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            },
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }

    showDeleteTareaDialog?.let { idTarea ->
        val tarea = listaTareas.find { it.idTarea == idTarea }
        AlertDialog(
            onDismissRequest = { showDeleteTareaDialog = null },
            title = { Text("Eliminar Actividad", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar la actividad \"${tarea?.titulo ?: ""}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("tareas").document(idTarea).delete()
                        showDeleteTareaDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteTareaDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    showDeleteClaseDialog?.let { idClase ->
        val clase = listaClases.find { it.idClase == idClase }
        AlertDialog(
            onDismissRequest = { showDeleteClaseDialog = null },
            title = { Text("Eliminar Clase", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar la clase \"${clase?.nombre ?: ""}\"? Se perderán todos los datos asociados en cascada. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("clases").document(idClase).delete()
                        showDeleteClaseDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteClaseDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun InicioContent(
    pendientes: Int,
    claseActual: String,
    eventos: List<Evento>,
    evidencias: List<EvidenciaTarea>,
    onCrearTarea: () -> Unit,
    onVerClick: (EvidenciaTarea) -> Unit
) {
    var selectedStatusFilter by remember { mutableStateOf<EstadoEvidencia?>(null) }

    val filteredEvidencias = remember(evidencias, selectedStatusFilter) {
        if (selectedStatusFilter == null) {
            evidencias
        } else {
            evidencias.filter { it.estado == selectedStatusFilter }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        
        if (eventos.isNotEmpty()) {
            item {
                Text("Anuncios Recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(eventos) { evento ->
                        EventoCarouselCardProfesor(evento)
                    }
                }
            }
        }
        
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Estatus de Aula", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        Text(if (pendientes > 0) "Tienes $pendientes actividades pendientes por evaluar." else "Al corriente con tus evaluaciones académicas.", style = MaterialTheme.typography.bodySmall,
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

        // Status Filter Chips LazyRow
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("Todas", style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {
                            if (selectedStatusFilter == null) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == EstadoEvidencia.Pendiente,
                        onClick = { selectedStatusFilter = EstadoEvidencia.Pendiente },
                        label = { Text("Pendientes", style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {
                            if (selectedStatusFilter == EstadoEvidencia.Pendiente) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == EstadoEvidencia.Aprobada,
                        onClick = { selectedStatusFilter = EstadoEvidencia.Aprobada },
                        label = { Text("Aprobadas", style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {
                            if (selectedStatusFilter == EstadoEvidencia.Aprobada) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == EstadoEvidencia.Rechazada,
                        onClick = { selectedStatusFilter = EstadoEvidencia.Rechazada },
                        label = { Text("Rechazadas", style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {
                            if (selectedStatusFilter == EstadoEvidencia.Rechazada) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
            }
        }

        if (filteredEvidencias.isEmpty()) {
            item {
                Text("No hay entregas registradas con este estado.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
            }
        } else {
            items(filteredEvidencias) { ev -> EvidenciaListItem(ev, onVerClick) }
        }
    }
}

@Composable
private fun TareasContent(
    tareas: List<Tarea>,
    clases: List<ClaseInfo>,
    idUsuario: String,
    onEditTarea: (String, String?) -> Unit,
    onDeleteTarea: (String) -> Unit,
    onVerEstadisticas: (String, String) -> Unit
) {
    var selectedClaseFilterId by remember { mutableStateOf<String?>(null) }

    val filteredTareas = remember(tareas, selectedClaseFilterId) {
        if (selectedClaseFilterId == null) {
            tareas
        } else {
            tareas.filter { it.idClase == selectedClaseFilterId }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Panel de Actividades", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { Text("Lista de tareas vigentes asignadas a tus clases.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        
        // Sección de Filtros de Clase
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text("Filtrar por Clase", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    // Chip para "Todas"
                    item {
                        FilterChip(
                            selected = selectedClaseFilterId == null,
                            onClick = { selectedClaseFilterId = null },
                            label = { Text("Todas", style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = {
                                if (selectedClaseFilterId == null) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    }
                    // Chips de cada clase
                    items(clases) { clase ->
                        FilterChip(
                            selected = selectedClaseFilterId == clase.idClase,
                            onClick = { selectedClaseFilterId = clase.idClase },
                            label = { Text(clase.nombre, style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = {
                                if (selectedClaseFilterId == clase.idClase) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        if (filteredTareas.isEmpty()) {
            item {
                Text("No hay tareas creadas para tus clases.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(filteredTareas) { tarea ->
                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tarea.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            
                            IconButton(onClick = { onVerEstadisticas(tarea.idTarea, tarea.titulo) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.BarChart, "Estadísticas", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onEditTarea(idUsuario, tarea.idTarea) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onDeleteTarea(tarea.idTarea) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(tarea.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tarea.nombreClase) },
                                icon = { Icon(Icons.Default.Class, null, Modifier.size(16.dp)) }
                            )
                            Spacer(Modifier.weight(1f))
                            val fmt = SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
                            Text("Límite: " + fmt.format(tarea.fechaLimite), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClasesContent(
    clases: List<ClaseInfo>,
    inscripciones: Map<String, List<String>>,
    onEditClase: (ClaseInfo) -> Unit,
    onDeleteClase: (String) -> Unit,
    onVerAlumnos: (String, String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Mis Asignaturas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (clases.isEmpty()) {
            item {
                Text("No tienes asignaturas registradas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(clases) { clase ->
                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(clase.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            
                            IconButton(onClick = { onVerAlumnos(clase.idClase, clase.nombre) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.People, "Ver Alumnos", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onEditClase(clase) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onDeleteClase(clase.idClase) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(clase.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        if (clase.enlace.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Link, "Enlace", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(clase.enlace, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        
                        // Enrolled Students list
                        val alumnos = inscripciones.getOrDefault(clase.idClase, emptyList())
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Alumnos Inscritos (${alumnos.size}):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (alumnos.isEmpty()) {
                            Text("Sin alumnos inscritos aún.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(alumnos.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Código de unión: ${clase.idClase}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerfilContent(
    nombre: String,
    clasesCount: Int,
    tareasCount: Int,
    evaluacionesCount: Int,
    correo: String,
    onGuardarCambios: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
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
        Text(nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Departamento Académico", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Rol: Profesor", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        
        Spacer(Modifier.height(32.dp))
        
        // Statistics Section
        Text(
            text = "Mis Estadísticas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                title = "Asignaturas",
                value = clasesCount.toString(),
                icon = Icons.Default.School,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Tareas",
                value = tareasCount.toString(),
                icon = Icons.Default.Assignment,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        StatCard(
            title = "Evidencias Evaluadas",
            value = evaluacionesCount.toString(),
            icon = Icons.Default.Grade,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        // Shortcuts / Contact Info
        Text(
            text = "Información y Atajos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        ShortcutItem(icon = Icons.Default.Email, title = "Contacto Administrativo", subtitle = "soporte.docentes@edutask.edu")
        Spacer(Modifier.height(12.dp))
        AjustesCuentaAccordion(correoActual = correo, onGuardarCambios = onGuardarCambios)
        Spacer(Modifier.height(12.dp))
        ShortcutItem(icon = Icons.Default.HelpOutline, title = "Soporte Técnico", subtitle = "Reportar un problema con la app")
        Spacer(Modifier.height(12.dp))
        ShortcutItem(
            icon = Icons.Default.Logout,
            title = "Cerrar Sesión",
            subtitle = "Salir de la cuenta de forma segura",
            onClick = onLogout
        )
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
                    Text(text = "Ajustes de Cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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

@Composable
private fun StatCard(title: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
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
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ShortcutItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
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
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EvidenciaListItem(
    evidencia: EvidenciaTarea,
    onVerClick: (EvidenciaTarea) -> Unit
) {
    val fmt = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
    val isPend = evidencia.estado == EstadoEvidencia.Pendiente
    OutlinedCard(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        ListItem(
            headlineContent   = { Text(evidencia.nombreAlumno, style = MaterialTheme.typography.titleSmall) },
            supportingContent = { Text(evidencia.tituloTarea, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingContent    = {
                Surface(Modifier.size(12.dp), shape = MaterialTheme.shapes.extraLarge,
                    color = if (isPend) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary) {}
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(fmt.format(evidencia.fechaEnvio), style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(onClick = { onVerClick(evidencia) }, modifier = Modifier.height(28.dp)) {
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

private fun generateShortCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..6).map { chars.random() }.joinToString("")
}

@Composable
fun EventoCarouselCardProfesor(evento: Evento) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val fechaFormat = dateFormat.format(Date(evento.fechaPublicacion))
    var showDialog by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.width(260.dp).height(120.dp).clickable { showDialog = true },
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(evento.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(evento.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text(fechaFormat, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(evento.titulo, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Publicado: $fechaFormat",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(evento.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun InicioSkeleton() {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(180.dp).height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                OutlinedCard(
                    modifier = androidx.compose.ui.Modifier.width(260.dp).height(120.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = androidx.compose.ui.Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(160.dp).height(20.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(220.dp).height(16.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(60.dp).height(12.dp))
                    }
                }
            }
        }
        
        Card(modifier = androidx.compose.ui.Modifier.fillMaxWidth().height(80.dp)) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.CenterStart) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(140.dp).height(18.dp))
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(200.dp).height(14.dp))
                }
            }
        }
        
        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(160.dp).height(24.dp))
        repeat(2) {
            OutlinedCard(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = androidx.compose.ui.Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = androidx.compose.ui.Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(180.dp).height(16.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(120.dp).height(14.dp))
                    }
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(60.dp).height(28.dp), shape = RoundedCornerShape(14.dp))
                }
            }
        }
    }
}

@Composable
private fun TareasSkeleton() {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(70.dp).height(32.dp), shape = RoundedCornerShape(16.dp))
            }
        }
        repeat(3) {
            OutlinedCard(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = androidx.compose.ui.Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(200.dp).height(20.dp))
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(280.dp).height(16.dp))
                    Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(100.dp).height(24.dp), shape = RoundedCornerShape(12.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(120.dp).height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClasesSkeleton() {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(150.dp).height(24.dp))
        repeat(3) {
            OutlinedCard(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = androidx.compose.ui.Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(160.dp).height(20.dp).weight(1f))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.size(24.dp), shape = androidx.compose.foundation.shape.CircleShape)
                        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.size(24.dp), shape = androidx.compose.foundation.shape.CircleShape)
                        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.size(24.dp), shape = androidx.compose.foundation.shape.CircleShape)
                    }
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(260.dp).height(16.dp))
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(180.dp).height(14.dp))
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(140.dp).height(24.dp), shape = RoundedCornerShape(6.dp))
                }
            }
        }
    }
}