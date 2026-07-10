package com.pmlp.edutask.model

enum class RolUsuario { Alumno, Profesor, Coordinador }

data class Usuario(
    val idUsuario: Int,
    val nombre: String,
    val correo: String,
    val rol: RolUsuario
)