package com.pmlp.edutask.ui.profesor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.model.ClaseInfo
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearTareaScreen(
    idUsuario: String = "",
    idTarea: String? = null,
    onTareaCreadaExitosa: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaLimite by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }

    // Dynamic Class selector states
    var clasesList by remember { mutableStateOf<List<ClaseInfo>>(emptyList()) }
    var selectedClaseId by remember { mutableStateOf("") }
    var selectedClaseNombre by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Load professor classes
    LaunchedEffect(idUsuario) {
        if (idUsuario.isNotBlank()) {
            db.collection("clases")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .addOnSuccessListener { snapshot ->
                    val clases = snapshot.documents.map { doc ->
                        ClaseInfo(
                            idClase = doc.id,
                            nombre = doc.getString("nombre") ?: "Sin Nombre"
                        )
                    }
                    clasesList = clases
                    // Default selection
                    if (clases.isNotEmpty() && selectedClaseId.isBlank()) {
                        selectedClaseId = clases[0].idClase
                        selectedClaseNombre = clases[0].nombre
                    }
                }
        }
    }

    // Load task details if editing
    LaunchedEffect(idTarea) {
        if (!idTarea.isNullOrBlank()) {
            isLoading = true
            db.collection("tareas").document(idTarea).get()
                .addOnSuccessListener { doc ->
                    isLoading = false
                    if (doc.exists()) {
                        titulo = doc.getString("titulo") ?: ""
                        descripcion = doc.getString("descripcion") ?: ""
                        
                        val date = doc.getTimestamp("fechaLimite")?.toDate() ?: Date()
                        fechaLimite = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)
                        
                        selectedClaseId = doc.getString("idClase") ?: ""
                        selectedClaseNombre = doc.getString("nombreClase") ?: ""
                    }
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (idTarea.isNullOrBlank()) "Asignar Nueva Actividad" else "Editar Actividad", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdown Selector for classes list
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedClaseNombre.ifBlank { "Selecciona una clase" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Asignar a Clase") },
                    trailingIcon = {
                        IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                            Icon(
                                imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Expandir"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { dropdownExpanded = !dropdownExpanded },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    clasesList.forEach { clase ->
                        DropdownMenuItem(
                            text = { Text(clase.nombre) },
                            onClick = {
                                selectedClaseId = clase.idClase
                                selectedClaseNombre = clase.nombre
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título de la tarea") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp) // Diseño institucional redondeado sin emojis
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción larga de la actividad") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = fechaLimite,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha Límite") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (titulo.isNotBlank() && descripcion.isNotBlank() && fechaLimite.isNotBlank() && selectedClaseId.isNotBlank()) {
                        isLoading = true

                        val parsedDate = try {
                            SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(fechaLimite.trim()) ?: Date()
                        } catch (e: Exception) {
                            Date()
                        }

                        val nuevaTarea = hashMapOf(
                            "titulo" to titulo.trim(),
                            "descripcion" to descripcion.trim(),
                            "fechaLimite" to com.google.firebase.Timestamp(parsedDate),
                            "idClase" to selectedClaseId,
                            "nombreClase" to selectedClaseNombre
                        )

                        val docRef = if (idTarea.isNullOrBlank()) {
                            db.collection("tareas").document()
                        } else {
                            db.collection("tareas").document(idTarea)
                        }

                        docRef.set(nuevaTarea)
                            .addOnSuccessListener {
                                if (idTarea.isNullOrBlank()) {
                                    val newTaskId = docRef.id
                                    db.collection("clase_alumno")
                                        .whereEqualTo("idClase", selectedClaseId)
                                        .get()
                                        .addOnSuccessListener { snapshot ->
                                            val batch = db.batch()
                                            for (doc in snapshot.documents) {
                                                val idAlumno = doc.getString("idUsuario") ?: continue
                                                val asigRef = db.collection("asignaciones_tarea").document()
                                                val asigDoc = hashMapOf(
                                                    "idTarea" to newTaskId,
                                                    "idUsuario" to idAlumno,
                                                    "fechaAsignacion" to com.google.firebase.Timestamp.now()
                                                )
                                                batch.set(asigRef, asigDoc)
                                            }
                                            batch.commit()
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    onTareaCreadaExitosa()
                                                }
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Error al crear asignaciones: ${e.localizedMessage}")
                                                    }
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Error al buscar alumnos: ${e.localizedMessage}")
                                            }
                                        }
                                } else {
                                    isLoading = false
                                    onTareaCreadaExitosa()
                                }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al guardar: ${e.localizedMessage}")
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading && titulo.isNotBlank() && descripcion.isNotBlank() && fechaLimite.isNotBlank() && selectedClaseId.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (idTarea.isNullOrBlank()) "Publicar Actividad" else "Guardar Cambios", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            fechaLimite = sdf.format(Date(selectedDateMillis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}