package com.pmlp.edutask.model

enum class RolUsuario { Alumno, Profesor, Coordinador }

data class Usuario(
    val idUsuario: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: RolUsuario = RolUsuario.Alumno
)