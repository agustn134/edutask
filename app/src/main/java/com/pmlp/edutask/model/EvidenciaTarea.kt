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
    val nombreAlumno: String = "",
    // Campos legacy para compatibilidad
    val nombreArchivo: String? = null,
    val textoEvidencia: String? = null,
    // Nuevos campos
    val archivos: List<Map<String, String>> = emptyList(), // lista de { "nombre": "...", "url": "..." }
    val vinculos: List<String> = emptyList()
)