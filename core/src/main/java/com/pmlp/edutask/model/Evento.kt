package com.pmlp.edutask.model

data class Evento(
    val idEvento: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val lugar: String = "",
    val fechaPublicacion: Long = System.currentTimeMillis(),
    val imagenUrl: String? = null
)
