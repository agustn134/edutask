package com.pmlp.edutask.model

import java.util.Date

enum class EstadoEvidencia { Pendiente, Aprobada, Rechazada }

data class EvidenciaTarea(
    val idEvidencia: String = "",
    val tituloTarea: String = "",
    val fotoBase64: String = "",
    val fechaEnvio: Date = Date(),
    val estado: EstadoEvidencia = EstadoEvidencia.Pendiente,
    val idAsignacion: String = "",
    val nombreAlumno: String = ""
)