/**
 * Modelo de datos representativo de los usuarios de la plataforma (Alumno, Profesor, Coordinador),
 * incluyendo nombre, correo, rol y datos de autenticacion.
 */
package com.pmlp.edutask.model

enum class RolUsuario { Alumno, Profesor, Coordinador }

data class Usuario(
    val idUsuario: String = "",
    val nombre: String = "",
    val matricula: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val rol: RolUsuario = RolUsuario.Alumno
)