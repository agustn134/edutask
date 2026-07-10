package com.pmlp.edutask

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.navigation.EduTaskNavGraph
import com.pmlp.edutask.ui.theme.EduTaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

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
}