package com.pmlp.edutask.model

import java.time.LocalDateTime

enum class EstadoEvidencia { Pendiente, Aprobada, Rechazada }

data class EvidenciaTarea(
    val idEvidencia: Int,
    val tituloTarea: String,
    val fotoUrl: String,
    val fechaEnvio: LocalDateTime,
    val estado: EstadoEvidencia,
    val idAsignacion: Int,
    val nombreAlumno: String = ""
)