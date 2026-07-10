package com.pmlp.edutask.ui.profesor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.EvidenciaTarea
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import android.util.Base64
import android.graphics.BitmapFactory
import android.graphics.Bitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluarTareaScreen(
    idEvidencia: String,
    idUsuario: String, // Profesor ID
    onEvaluadoExitoso: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }

    var evidencia by remember { mutableStateOf<EvidenciaTarea?>(null) }
    var idCalificacion by remember { mutableStateOf<String?>(null) }
    
    // Evaluation Form States
    var puntaje by remember { mutableStateOf("") }
    var comentarios by remember { mutableStateOf("") }
    var esBorrador by remember { mutableStateOf(false) }
    
    // Edit & Load States
    var isEditMode by remember { mutableStateOf(true) } // If graded, defaults to false
    var isAlreadyGraded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Load Evidence Info
    LaunchedEffect(idEvidencia) {
        if (idEvidencia.isNotBlank()) {
            db.collection("evidencias_tarea").document(idEvidencia).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
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

                        evidencia = EvidenciaTarea(
                            idEvidencia = idEvidenciaStr,
                            tituloTarea = doc.getString("tituloTarea") ?: "Sin Título",
                            fotoBase64 = doc.getString("fotoBase64") ?: doc.getString("fotoUrl") ?: "",
                            fechaEnvio = doc.getDate("fechaEnvio") ?: Date(),
                            estado = estadoEnum,
                            idAsignacion = idAsignacionStr,
                            nombreAlumno = doc.getString("nombreAlumno") ?: "Alumno Anónimo"
                        )
                    }
                }
        }
    }

    // Load Existing Calificacion if any
    LaunchedEffect(idEvidencia) {
        if (idEvidencia.isNotBlank()) {
            db.collection("calificaciones")
                .whereEqualTo("idEvidencia", idEvidencia)
                .get()
                .addOnSuccessListener { snapshot ->
                    isLoading = false
                    if (!snapshot.isEmpty) {
                        val doc = snapshot.documents[0]
                        idCalificacion = doc.id
                        val valorNum = doc.get("valor")
                        puntaje = valorNum?.toString() ?: ""
                        comentarios = doc.getString("comentario") ?: ""
                        esBorrador = doc.getBoolean("esBorrador") ?: false
                        
                        isAlreadyGraded = true
                        // If it's a saved draft, keep it in edit mode. If officially graded, set read-only.
                        isEditMode = esBorrador
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
                title = { Text("Evaluar Actividad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onEvaluadoExitoso) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val ev = evidencia
            if (ev == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontró la entrega de evidencia.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left/Top Section: Student Delivery Details
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = ev.tituloTarea,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Alumno: ${ev.nombreAlumno}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                val fmt = SimpleDateFormat("dd 'de' MMMM, HH:mm", Locale.getDefault())
                                Text(
                                    text = "Entregado: " + fmt.format(ev.fechaEnvio),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Attachment / Evidence image link
                            Text(
                                text = "Archivos Adjuntos / Evidencia:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val bitmap = remember(ev.fotoBase64) {
                                if (ev.fotoBase64.isNotBlank()) {
                                    decodeBase64ToBitmap(ev.fotoBase64)
                                } else {
                                    null
                                }
                            }

                            if (bitmap != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth().height(220.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Evidencia adjunta",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                }
                            } else if (ev.fotoBase64.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.BrokenImage, null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Error al decodificar la evidencia.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else {
                                Text(
                                    text = "No se subieron archivos adjuntos.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Right/Bottom Section: Grade & Feedback Card
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Calificación y Retroalimentación",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                if (isAlreadyGraded && !isEditMode) {
                                    IconButton(onClick = { isEditMode = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar Calificación", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            // Score Field
                            OutlinedTextField(
                                value = puntaje,
                                onValueChange = { puntaje = it },
                                label = { Text("Puntaje (0 - 100)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isEditMode,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Comments Field
                            OutlinedTextField(
                                value = comentarios,
                                onValueChange = { comentarios = it },
                                label = { Text("Comentarios al alumno") },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                enabled = isEditMode,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Submit Actions
                            if (isEditMode) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val scoreInt = puntaje.toIntOrNull()
                                            if (scoreInt != null && scoreInt in 0..100) {
                                                // Save official grade
                                                isLoading = true
                                                val califDoc = hashMapOf(
                                                    "idEvidencia" to idEvidencia,
                                                    "idUsuario" to idUsuario,
                                                    "valor" to scoreInt,
                                                    "comentario" to comentarios.trim(),
                                                    "esBorrador" to false,
                                                    "fechaCalificacion" to Timestamp.now()
                                                )

                                                val gradeRef = if (idCalificacion.isNullOrBlank()) {
                                                    db.collection("calificaciones").document()
                                                } else {
                                                    db.collection("calificaciones").document(idCalificacion!!)
                                                }

                                                gradeRef.set(califDoc)
                                                    .addOnSuccessListener {
                                                        // Update evidence status: Approved if >= 60, Rejected if < 60
                                                        val nuevoEstado = if (scoreInt >= 60) "Aprobada" else "Rechazada"
                                                        db.collection("evidencias_tarea").document(idEvidencia)
                                                            .update("estado", nuevoEstado)
                                                            .addOnSuccessListener {
                                                                isLoading = false
                                                                onEvaluadoExitoso()
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        isLoading = false
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Error: ${e.localizedMessage}")
                                                        }
                                                    }
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Por favor ingresa un puntaje válido entre 0 y 100.")
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (isAlreadyGraded) "Actualizar Calificación" else "Enviar Calificación",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    if (!isAlreadyGraded || esBorrador) {
                                        OutlinedButton(
                                            onClick = {
                                                val scoreInt = puntaje.toIntOrNull()
                                                if (scoreInt != null && scoreInt in 0..100) {
                                                    // Save draft grade
                                                    isLoading = true
                                                    val califDoc = hashMapOf(
                                                        "idEvidencia" to idEvidencia,
                                                        "idUsuario" to idUsuario,
                                                        "valor" to scoreInt,
                                                        "comentario" to comentarios.trim(),
                                                        "esBorrador" to true,
                                                        "fechaCalificacion" to Timestamp.now()
                                                    )

                                                    val gradeRef = if (idCalificacion.isNullOrBlank()) {
                                                        db.collection("calificaciones").document()
                                                    } else {
                                                        db.collection("calificaciones").document(idCalificacion!!)
                                                    }

                                                    gradeRef.set(califDoc)
                                                        .addOnSuccessListener {
                                                            isLoading = false
                                                            isAlreadyGraded = true
                                                            esBorrador = true
                                                            isEditMode = true
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Borrador guardado exitosamente.")
                                                            }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            isLoading = false
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Error: ${e.localizedMessage}")
                                                            }
                                                        }
                                                } else {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Por favor ingresa un puntaje válido entre 0 y 100.")
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "Guardar Borrador",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Graded (Read-Only) status alert
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Esta entrega ya está calificada oficialmente.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
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

private fun decodeBase64ToBitmap(base64Str: String): android.graphics.Bitmap? {
    return try {
        val cleanString = if (base64Str.contains(",")) {
            base64Str.substring(base64Str.indexOf(",") + 1)
        } else {
            base64Str
        }
        val decodedBytes = android.util.Base64.decode(cleanString, android.util.Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}
