/**
 * Modelo de datos representativo de una tarea escolar,
 * con titulo, descripcion, fecha de entrega, clase asociada y ponderacion.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
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