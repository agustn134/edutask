package com.pmlp.wear.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TaskNotificationService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null
    private var isFirstLoad = true
    private var prefListener: android.content.SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("TaskNotificationService", "Service created")
        createNotificationChannel()
        startListening()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "wear_task_notifications",
                "Nuevas Evidencias",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevas tareas entregadas por calificar"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startListening() {
        val prefs = getSharedPreferences("edutask_wear_prefs", Context.MODE_PRIVATE)
        val idProfesor = prefs.getString("idUsuario", "profesor_001") ?: "profesor_001"

        prefListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "idUsuario") {
                Log.d("TaskNotificationService", "Professor ID changed in prefs, restarting listener")
                listener?.remove()
                isFirstLoad = true
                startListening()
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(prefListener)

        listener = db.collection("evidencias_tarea")
            .orderBy("fechaEnvio", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("TaskNotificationService", "Firestore listener error", error)
                    return@addSnapshotListener
                }
                if (isFirstLoad) {
                    isFirstLoad = false
                    Log.d("TaskNotificationService", "Initial load skipped")
                    return@addSnapshotListener
                }

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val estado = doc.getString("estado") ?: "Pendiente"
                    val idAsignacion = doc.getString("idAsignacion") ?: ""

                    if (estado == "Pendiente") {
                        scope.launch {
                            try {
                                val assignDoc = db.collection("asignaciones_tarea").document(idAsignacion).get().await()
                                val idTarea = assignDoc.getString("idTarea") ?: ""
                                if (idTarea.isNotEmpty()) {
                                    val taskDoc = db.collection("tareas").document(idTarea).get().await()
                                    val idClase = taskDoc.getString("idClase") ?: ""
                                    if (idClase.isNotEmpty()) {
                                        val classDoc = db.collection("clases").document(idClase).get().await()
                                        val classProfId = classDoc.getString("idUsuario") ?: ""
                                        
                                        val currentIdProfesor = prefs.getString("idUsuario", "profesor_001") ?: "profesor_001"
                                        if (classProfId == currentIdProfesor) {
                                             val nombreAlumno = doc.getString("nombreAlumno") ?: "Alumno"
                                             val tituloTarea = doc.getString("tituloTarea") ?: "Tarea"
                                             var tieneArchivosNoImagen = false
                                             val archivosRaw = doc.get("archivos") as? List<*>
                                             archivosRaw?.forEach { item ->
                                                 if (item is Map<*, *>) {
                                                     val nombre = item["nombre"]?.toString() ?: ""
                                                     val base64 = item["base64"]?.toString() ?: ""
                                                     val isImage = nombre.lowercase().run {
                                                         endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") || endsWith(".webp") || endsWith(".gif")
                                                     }
                                                     if (!isImage && base64.isNotEmpty()) {
                                                         tieneArchivosNoImagen = true
                                                     }
                                                 }
                                             }
                                             
                                             showNotification(nombreAlumno, tituloTarea, tieneArchivosNoImagen)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("TaskNotificationService", "Error resolving task metadata", e)
                            }
                        }
                    }
                }
            }
    }

    private fun showNotification(alumno: String, tarea: String, tieneArchivosNoImagen: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (tieneArchivosNoImagen) {
            "Subió archivos. Ver en el móvil. (Califica aquí)"
        } else {
            tarea
        }

        val notification = NotificationCompat.Builder(this, "wear_task_notifications")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Entrega de $alumno")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
        Log.d("TaskNotificationService", "Notification posted: $alumno - $contentText")
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
        val prefs = getSharedPreferences("edutask_wear_prefs", Context.MODE_PRIVATE)
        prefListener?.let { prefs.unregisterOnSharedPreferenceChangeListener(it) }
        job.cancel()
        Log.d("TaskNotificationService", "Service destroyed")
    }
}
