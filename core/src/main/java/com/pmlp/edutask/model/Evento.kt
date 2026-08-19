/**
 * Modelo de datos compartido representativo de los eventos y avisos institucionales
 * publicados para la comunidad escolar.
 */
package com.pmlp.edutask.model

data class Evento(
    val idEvento: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val lugar: String = "",
    val fechaPublicacion: Long = System.currentTimeMillis(),
    val imagenUrl: String? = null
)
