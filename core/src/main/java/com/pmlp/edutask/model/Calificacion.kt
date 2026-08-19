/**
 * Modelo de datos compartido que representa la calificacion y retroalimentacion
 * asignada a una entrega de tarea.
 */
package com.pmlp.edutask.model

import java.util.Date

data class Calificacion(
    val idCalificacion: String = "",
    val idEvidencia: String = "",
    val idProfesor: String = "",
    val valor: Int = 0,
    val comentario: String = "",
    val fechaCalificacion: Date = Date()
)
