package com.pmlp.edutask.model

import java.util.Date

data class Tarea(
    val idTarea: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fechaLimite: Date = Date(),
    val idClase: String = "",
    val nombreClase: String = ""
)