package com.pmlp.edutask

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.navigation.EduTaskNavGraph
import com.pmlp.edutask.ui.theme.EduTaskTheme
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        createNotificationChannel()

        val db = FirebaseFirestore.getInstance()

        db.collection("tareas")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("TestFirebase", "Lectura exitosa: ${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TestFirebase", "Error de conexión: ", exception)
            }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduTaskTheme(darkTheme = false, dynamicColor = false) {
                EduTaskNavGraph()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios EduTask"
            val descriptionText = "Canal para recordar entregas de tareas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("edutask_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}