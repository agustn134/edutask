package com.pmlp.edutask.ui.profesor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class StudentGradeReport(
    val idAlumno: String,
    val nombre: String,
    val matricula: String,
    val status: String, // "Calificada", "Pendiente", "Sin entregar"
    val score: Int?,
    val comments: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasTareaScreen(
    idTarea: String,
    tituloTarea: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }

    var reportList by remember { mutableStateOf<List<StudentGradeReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Load reports
    LaunchedEffect(idTarea) {
        if (idTarea.isNotBlank()) {
            try {
                // 1. Fetch assignments
                val assignmentsSnapshot = db.collection("asignaciones_tarea")
                    .whereEqualTo("idTarea", idTarea)
                    .get()
                    .await()

                val tempReports = mutableListOf<StudentGradeReport>()

                for (asigDoc in assignmentsSnapshot.documents) {
                    val idAlumno = asigDoc.getString("idUsuario") ?: continue
                    val idAsignacion = asigDoc.id

                    // Fetch student name
                    val studentDoc = db.collection("usuarios").document(idAlumno).get().await()
                    val nombre = studentDoc.getString("nombre") ?: "Alumno Sin Nombre"
                    val matricula = studentDoc.getString("matricula") ?: ""

                    // Fetch evidence
                    val evidencesSnapshot = db.collection("evidencias_tarea")
                        .whereEqualTo("idAsignacion", idAsignacion)
                        .get()
                        .await()

                    if (!evidencesSnapshot.isEmpty) {
                        val evDoc = evidencesSnapshot.documents[0]
                        val idEvidencia = evDoc.id

                        // Fetch grade
                        val gradesSnapshot = db.collection("calificaciones")
                            .whereEqualTo("idEvidencia", idEvidencia)
                            .get()
                            .await()

                        if (!gradesSnapshot.isEmpty) {
                            val gradeDoc = gradesSnapshot.documents[0]
                            val esBorrador = gradeDoc.getBoolean("esBorrador") ?: false

                            if (!esBorrador) {
                                val scoreVal = gradeDoc.getLong("valor")?.toInt()
                                val comment = gradeDoc.getString("comentario") ?: ""
                                tempReports.add(
                                    StudentGradeReport(
                                        idAlumno = idAlumno,
                                        nombre = nombre,
                                        matricula = matricula,
                                        status = "Calificada",
                                        score = scoreVal,
                                        comments = comment
                                    )
                                )
                            } else {
                                // It is a draft, so it's technically still pending approval
                                tempReports.add(
                                    StudentGradeReport(
                                        idAlumno = idAlumno,
                                        nombre = nombre,
                                        matricula = matricula,
                                        status = "Pendiente",
                                        score = null,
                                        comments = null
                                    )
                                )
                            }
                        } else {
                            tempReports.add(
                                StudentGradeReport(
                                    idAlumno = idAlumno,
                                    nombre = nombre,
                                    matricula = matricula,
                                    status = "Pendiente",
                                    score = null,
                                    comments = null
                                )
                            )
                        }
                    } else {
                        tempReports.add(
                            StudentGradeReport(
                                idAlumno = idAlumno,
                                        nombre = nombre,
                                        matricula = matricula,
                                        status = "Sin entregar",
                                        score = null,
                                        comments = null
                            )
                        )
                    }
                }

                reportList = tempAlumnosList(tempReports)
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar("Error al cargar reportes: ${e.localizedMessage}")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(tituloTarea, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            // Calculate Statistics
            val gradedList = reportList.filter { it.status == "Calificada" && it.score != null }
            val average = if (gradedList.isNotEmpty()) gradedList.map { it.score!! }.average() else 0.0
            val maxScore = if (gradedList.isNotEmpty()) gradedList.maxOf { it.score!! } else 0
            val minScore = if (gradedList.isNotEmpty()) gradedList.minOf { it.score!! } else 0
            
            val totalAssigned = reportList.size
            val gradedCount = gradedList.size

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Statistics Cards Header
                item {
                    Text(
                        text = "Estadísticas del Grupo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Promedio",
                            value = String.format(Locale.getDefault(), "%.1f", average),
                            icon = Icons.Default.Analytics,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                        StatCard(
                            title = "Avance",
                            value = "$gradedCount / $totalAssigned",
                            icon = Icons.Default.DoneAll,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Nota Alta",
                            value = maxScore.toString(),
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                        StatCard(
                            title = "Nota Baja",
                            value = minScore.toString(),
                            icon = Icons.Default.TrendingDown,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.errorContainer
                        )
                    }
                }

                // Student Grades List
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reporte de Calificaciones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (reportList.isEmpty()) {
                    item {
                        Text(
                            text = "No hay alumnos asignados a esta tarea.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(reportList) { report ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = report.nombre,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Matrícula: ${report.matricula}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Status Badge / Score
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (report.status) {
                                            "Calificada" -> MaterialTheme.colorScheme.primaryContainer
                                            "Pendiente" -> MaterialTheme.colorScheme.tertiaryContainer
                                            else -> MaterialTheme.colorScheme.errorContainer
                                        }
                                    ) {
                                        Text(
                                            text = when (report.status) {
                                                "Calificada" -> "${report.score} / 10"
                                                "Pendiente" -> "Pendiente"
                                                else -> "Sin entregar"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (report.status) {
                                                "Calificada" -> MaterialTheme.colorScheme.onPrimaryContainer
                                                "Pendiente" -> MaterialTheme.colorScheme.onTertiaryContainer
                                                else -> MaterialTheme.colorScheme.onErrorContainer
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (report.status == "Calificada" && !report.comments.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Retroalimentación: ${report.comments}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

private fun tempAlumnosList(list: List<StudentGradeReport>): List<StudentGradeReport> {
    return list.sortedBy { it.nombre }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
