package com.pmlp.edutask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pmlp.edutask.navigation.EduTaskNavGraph
import com.pmlp.edutask.ui.theme.EduTaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduTaskTheme(darkTheme = false, dynamicColor = true) {
                EduTaskNavGraph()
            }
        }
    }
}