package com.pmlp.edutask.ui.profesor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.ui.components.ShimmerPlaceholder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AlumnoInfo(
    val idAlumno: String,
    val nombre: String,
    val matricula: String,
    val correo: String,
    val idClaseAlumno: String // Document ID inside clase_alumno
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnosClaseScreen(
    idClase: String,
    nombreClase: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    
    var listAlumnos by remember { mutableStateOf<List<AlumnoInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedAlumnoForRemove by remember { mutableStateOf<AlumnoInfo?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Real-time listener for enrolled students
    DisposableEffect(idClase) {
        if (idClase.isBlank()) return@DisposableEffect onDispose {}
        
        val listener = db.collection("clase_alumno")
            .whereEqualTo("idClase", idClase)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                
                scope.launch {
                    val tempAlumnos = mutableListOf<AlumnoInfo>()
                    for (doc in snapshot.documents) {
                        val idAlumno = doc.getString("idUsuario") ?: continue
                        val idClaseAlumno = doc.id
                        
                        try {
                            val userDoc = db.collection("usuarios").document(idAlumno).get().await()
                            if (userDoc.exists()) {
                                tempAlumnos.add(
                                    AlumnoInfo(
                                        idAlumno = idAlumno,
                                        nombre = userDoc.getString("nombre") ?: "Alumno Sin Nombre",
                                        matricula = userDoc.getString("matricula") ?: "",
                                        correo = userDoc.getString("correo") ?: "",
                                        idClaseAlumno = idClaseAlumno
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            // Ignore specific failures and load others
                        }
                    }
                    listAlumnos = tempAlumnos.sortedBy { it.nombre }
                    isLoading = false
                }
            }
            
        onDispose { listener.remove() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(nombreClase, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerPlaceholder(modifier = Modifier.width(180.dp).height(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                repeat(4) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                ShimmerPlaceholder(modifier = Modifier.width(150.dp).height(20.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ShimmerPlaceholder(modifier = Modifier.size(16.dp), shape = androidx.compose.foundation.shape.CircleShape)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ShimmerPlaceholder(modifier = Modifier.width(110.dp).height(16.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ShimmerPlaceholder(modifier = Modifier.size(16.dp), shape = androidx.compose.foundation.shape.CircleShape)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ShimmerPlaceholder(modifier = Modifier.width(170.dp).height(16.dp))
                                }
                            }
                            ShimmerPlaceholder(modifier = Modifier.size(24.dp), shape = androidx.compose.foundation.shape.CircleShape)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Alumnos Inscritos (${listAlumnos.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (listAlumnos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay alumnos inscritos en esta clase.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(listAlumnos) { alumno ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alumno.nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Matrícula: ${alumno.matricula}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Mail,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = alumno.correo,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(onClick = { selectedAlumnoForRemove = alumno }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Quitar alumno",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Cascade Deletion Confirmation Dialog
    selectedAlumnoForRemove?.let { alumno ->
        AlertDialog(
            onDismissRequest = { selectedAlumnoForRemove = null },
            icon = { Icon(Icons.Default.Person, null) },
            title = { Text("Quitar de la Clase", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas quitar a ${alumno.nombre} de la clase? Se borrarán de forma permanente sus entregas, calificaciones, asignaciones y smartwatch alertas asociadas en cascada."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isLoading = true
                        selectedAlumnoForRemove = null
                        scope.launch {
                            try {
                                // 1. Delete enrollment from clase_alumno
                                db.collection("clase_alumno").document(alumno.idClaseAlumno).delete().await()

                                // 2. Query all tasks of this class
                                val tasksSnapshot = db.collection("tareas")
                                    .whereEqualTo("idClase", idClase)
                                    .get()
                                    .await()
                                
                                val taskIds = tasksSnapshot.documents.map { it.id }

                                if (taskIds.isNotEmpty()) {
                                    // 3. Query all student assignments for these tasks
                                    for (taskId in taskIds) {
                                        val assignmentsSnapshot = db.collection("asignaciones_tarea")
                                            .whereEqualTo("idUsuario", alumno.idAlumno)
                                            .whereEqualTo("idTarea", taskId)
                                            .get()
                                            .await()
                                        
                                        for (asigDoc in assignmentsSnapshot.documents) {
                                            val idAsignacion = asigDoc.id
                                            
                                            // Delete evidence and dependencies
                                            val evidencesSnapshot = db.collection("evidencias_tarea")
                                                .whereEqualTo("idAsignacion", idAsignacion)
                                                .get()
                                                .await()
                                            
                                            for (evDoc in evidencesSnapshot.documents) {
                                                val idEvidencia = evDoc.id
                                                
                                                // Delete smartwatch alerts
                                                val notifsSnapshot = db.collection("notificaciones_reloj")
                                                    .whereEqualTo("idEvidencia", idEvidencia)
                                                    .get()
                                                    .await()
                                                for (notifDoc in notifsSnapshot.documents) {
                                                    db.collection("notificaciones_reloj").document(notifDoc.id).delete().await()
                                                }
                                                
                                                // Delete grades
                                                val gradesSnapshot = db.collection("calificaciones")
                                                    .whereEqualTo("idEvidencia", idEvidencia)
                                                    .get()
                                                    .await()
                                                for (gradeDoc in gradesSnapshot.documents) {
                                                    db.collection("calificaciones").document(gradeDoc.id).delete().await()
                                                }
                                                
                                                // Delete evidence document
                                                db.collection("evidencias_tarea").document(idEvidencia).delete().await()
                                            }
                                            
                                            // Delete assignment document
                                            db.collection("asignaciones_tarea").document(idAsignacion).delete().await()
                                        }
                                    }
                                }
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Alumno y sus datos eliminados en cascada con éxito.")
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al quitar: ${e.localizedMessage}")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Quitar Alumno", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedAlumnoForRemove = null }) {
                    Text("Cancelar", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}
