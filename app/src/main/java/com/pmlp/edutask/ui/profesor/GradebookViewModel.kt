package com.pmlp.edutask.ui.profesor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.net.Uri

import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
data class GradebookState(
    val isLoading: Boolean = false,
    val alumnos: List<Usuario> = emptyList(),
    val tareas: List<Tarea> = emptyList(),
    // Map of (alumnoId + "_" + tareaId) to Calificacion (Int)
    val calificaciones: Map<String, Int> = emptyMap(),
    // Map of alumnoId to Average
    val promedios: Map<String, Double> = emptyMap(),
    val error: String? = null
)

class GradebookViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(GradebookState())
    val uiState: StateFlow<GradebookState> = _uiState

    fun loadGradebook(idClase: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 1. Fetch Students in the class
                val alumnosRefs = db.collection("clase_alumno")
                    .whereEqualTo("idClase", idClase)
                    .get().await()
                val alumnoIds = alumnosRefs.documents.mapNotNull { it.getString("idUsuario") }

                val alumnos = mutableListOf<Usuario>()
                if (alumnoIds.isNotEmpty()) {
                    // Fetch student details
                    // Firestore 'whereIn' supports up to 10 items, so we chunk it if necessary
                    val chunks = alumnoIds.chunked(10)
                    for (chunk in chunks) {
                        val usersSnap = db.collection("usuarios")
                            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                            .get().await()
                        alumnos.addAll(usersSnap.documents.mapNotNull { 
                            Usuario(
                                idUsuario = it.id,
                                nombre = it.getString("nombre") ?: "",
                                matricula = it.getString("matricula") ?: ""
                            )
                        })
                    }
                }

                // 2. Fetch Tasks in the class
                val tareasSnap = db.collection("tareas")
                    .whereEqualTo("idClase", idClase)
                    .get().await()
                    
                val tareas = tareasSnap.documents.mapNotNull { doc ->
                    Tarea(
                        idTarea = doc.id,
                        titulo = doc.getString("titulo") ?: "Sin Título",
                        idClase = idClase
                    )
                }
                
                val tareaIds = tareas.map { it.idTarea }

                // 3. Fetch Evidencias (with denormalized calificacion) for these tasks
                val matrix = mutableMapOf<String, Int>()
                if (tareaIds.isNotEmpty()) {
                    val taskChunks = tareaIds.chunked(10)
                    for (chunk in taskChunks) {
                        // Get asignaciones for these tasks
                        val asignacionesSnap = db.collection("asignaciones_tarea")
                            .whereIn("idTarea", chunk)
                            .get().await()

                        // Map idAsignacion to (idAlumno, idTarea)
                        val asigMap = mutableMapOf<String, Pair<String, String>>()
                        val asignacionIds = mutableListOf<String>()
                        
                        for (doc in asignacionesSnap.documents) {
                            val idAsig = doc.id
                            val idAlumno = doc.getString("idUsuario") ?: continue
                            val idTarea = doc.getString("idTarea") ?: continue
                            asigMap[idAsig] = Pair(idAlumno, idTarea)
                            asignacionIds.add(idAsig)
                        }

                        if (asignacionIds.isNotEmpty()) {
                            val asigChunks = asignacionIds.chunked(10)
                            for (asigChunk in asigChunks) {
                                val evidenciasSnap = db.collection("evidencias_tarea")
                                    .whereIn("idAsignacion", asigChunk)
                                    .get().await()
                                    
                                for (ev in evidenciasSnap.documents) {
                                    val califRaw = ev.get("calificacion")
                                    val calificacion = when (califRaw) {
                                        is Number -> califRaw.toInt()
                                        is String -> califRaw.toIntOrNull()
                                        else -> null
                                    }
                                    
                                    val idAsig = ev.getString("idAsignacion")
                                    val pair = asigMap[idAsig]
                                    if (pair != null && calificacion != null) {
                                        matrix["${pair.first}_${pair.second}"] = calificacion
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Calculate averages
                val promedios = mutableMapOf<String, Double>()
                for (alumno in alumnos) {
                    var sum = 0
                    var count = 0
                    for (tarea in tareas) {
                        val grade = matrix["${alumno.idUsuario}_${tarea.idTarea}"]
                        if (grade != null) {
                            sum += grade
                            count++
                        }
                    }
                    if (count > 0) {
                        promedios[alumno.idUsuario] = sum.toDouble() / count.toDouble()
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    alumnos = alumnos.sortedBy { it.nombre },
                    tareas = tareas,
                    calificaciones = matrix,
                    promedios = promedios
                )

            } catch (e: Exception) {
                Log.e("GradebookVM", "Error loading gradebook", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar libreta de calificaciones"
                )
            }
        }
    }

    fun exportarAExcel(context: Context): Uri? {
        val state = _uiState.value
        if (state.alumnos.isEmpty() || state.tareas.isEmpty()) return null

        return try {
            val file = File(context.cacheDir, "calificaciones.csv")
            val fos = FileOutputStream(file)
            

            // CSV Header
            val header = mutableListOf("Alumno")
            state.tareas.forEach { header.add(it.titulo) }
            header.add("Promedio General")
            fos.write((header.joinToString(",") + "\n").toByteArray(Charsets.ISO_8859_1))

            // Rows
            state.alumnos.forEach { alumno ->
                val row = mutableListOf(alumno.nombre)
                state.tareas.forEach { tarea ->
                    val grade = state.calificaciones["${alumno.idUsuario}_${tarea.idTarea}"]
                    row.add(grade?.toString() ?: "-")
                }
                val promedio = state.promedios[alumno.idUsuario]
                row.add(if (promedio != null) java.util.Locale.US.let { String.format(it, "%.1f", promedio) } else "-")
                fos.write((row.joinToString(",") + "\n").toByteArray(Charsets.ISO_8859_1))
            }
            
            fos.flush()
            fos.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Log.e("GradebookVM", "Error exporting CSV", e)
            null
        }
    }
}
