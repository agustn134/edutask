package com.pmlp.edutask.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Tarea(
    val idTarea: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    @ServerTimestamp
    val fechaLimite: Date? = null,
    val idClase: String = "",
    val nombreClase: String = ""
)