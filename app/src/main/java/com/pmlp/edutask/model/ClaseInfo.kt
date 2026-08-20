/**
 * Modelo de datos que representa una clase o asignatura en el sistema EduTask,
 * incluyendo su identificador, nombre, descripcion y enlace.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.model

data class ClaseInfo(
    val idClase: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val enlace: String = ""
)
