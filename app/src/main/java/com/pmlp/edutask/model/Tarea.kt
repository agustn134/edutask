package com.pmlp.edutask.model

import java.time.LocalDateTime

data class Tarea(
    val idTarea: Int,
    val titulo: String,
    val descripcion: String,
    val fechaLimite: LocalDateTime,
    val idClase: Int,
    val nombreClase: String = ""
)