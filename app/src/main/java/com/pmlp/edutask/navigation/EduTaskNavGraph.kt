package com.pmlp.edutask.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pmlp.edutask.model.RolUsuario
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.ui.alumno.HomeAlumnoScreen
import com.pmlp.edutask.ui.alumno.EnviarEvidenciaScreen
import com.pmlp.edutask.ui.login.LoginScreen
import com.pmlp.edutask.ui.profesor.HomeProfesorScreen
import java.util.Date

object EduTaskRoutes {
    const val LOGIN           = "login"
    const val HOME_ALUMNO     = "home_alumno/{idUsuario}/{nombre}/{carrera}"
    const val HOME_PROFESOR   = "home_profesor/{idUsuario}/{nombre}/{clase}"
    const val ENVIAR_EVIDENCIA =
        "enviar_evidencia/{idAsignacion}/{idTarea}/{titulo}/{descripcion}/{fechaLimite}/{nombreClase}/{nombreAlumno}"

    fun homeAlumno(idUsuario: String, nombre: String, carrera: String) =
        "home_alumno/${enc(idUsuario)}/${enc(nombre)}/${enc(carrera)}"

    fun homeProfesor(idUsuario: String, nombre: String, clase: String) =
        "home_profesor/${enc(idUsuario)}/${enc(nombre)}/${enc(clase)}"

    fun enviarEvidencia(
        idAsignacion: String,
        tarea:        Tarea,
        nombreAlumno: String
    ) = "enviar_evidencia/${enc(idAsignacion)}/${enc(tarea.idTarea)}/${enc(tarea.titulo)}" +
        "/${enc(tarea.descripcion)}/${tarea.fechaLimite.time}" +
        "/${enc(tarea.nombreClase)}/${enc(nombreAlumno)}"

    fun enc(s: String): String = java.net.URLEncoder.encode(s, "UTF-8")
    fun dec(s: String?): String = if (s != null) java.net.URLDecoder.decode(s, "UTF-8") else ""
}

@Composable
fun EduTaskNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = EduTaskRoutes.LOGIN) {

        composable(EduTaskRoutes.LOGIN) {
            LoginScreen(onLoginSuccess = { idUsuario, nombre, rol ->
                val route = when (rol) {
                    RolUsuario.Alumno      -> EduTaskRoutes.homeAlumno(idUsuario, nombre, "Ingenieria de Software")
                    RolUsuario.Profesor    -> EduTaskRoutes.homeProfesor(idUsuario, nombre, "Programacion Movil PMLP")
                    RolUsuario.Coordinador -> EduTaskRoutes.homeProfesor(idUsuario, nombre, "Coordinacion Academica")
                }
                navController.navigate(route) { popUpTo(EduTaskRoutes.LOGIN) { inclusive = true } }
            })
        }

        composable(EduTaskRoutes.HOME_ALUMNO) { back ->
            val nombreAlumno = EduTaskRoutes.dec(back.arguments?.getString("nombre"))
            val idUsuario    = EduTaskRoutes.dec(back.arguments?.getString("idUsuario"))
            HomeAlumnoScreen(
                idUsuario    = idUsuario,
                nombreAlumno = nombreAlumno,
                carrera      = EduTaskRoutes.dec(back.arguments?.getString("carrera")),
                onVerTarea   = { item ->
                    navController.navigate(
                        EduTaskRoutes.enviarEvidencia(item.idAsignacion, item.tarea, nombreAlumno)
                    )
                },
                onLogout = {
                    navController.navigate(EduTaskRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(EduTaskRoutes.HOME_PROFESOR) { back ->
            HomeProfesorScreen(
                idUsuario      = EduTaskRoutes.dec(back.arguments?.getString("idUsuario")),
                nombreProfesor = EduTaskRoutes.dec(back.arguments?.getString("nombre")),
                claseActual    = EduTaskRoutes.dec(back.arguments?.getString("clase")),
                onCrearTarea   = {},
                onLogout       = { navController.navigate(EduTaskRoutes.LOGIN) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(EduTaskRoutes.ENVIAR_EVIDENCIA) { back ->
            val args = back.arguments
            val tarea = Tarea(
                idTarea     = EduTaskRoutes.dec(args?.getString("idTarea")),
                titulo      = EduTaskRoutes.dec(args?.getString("titulo")),
                descripcion = EduTaskRoutes.dec(args?.getString("descripcion")),
                fechaLimite = Date(args?.getString("fechaLimite")?.toLongOrNull() ?: System.currentTimeMillis()),
                idClase     = "",
                nombreClase = EduTaskRoutes.dec(args?.getString("nombreClase"))
            )
            EnviarEvidenciaScreen(
                tarea        = tarea,
                idAsignacion = EduTaskRoutes.dec(args?.getString("idAsignacion")),
                nombreAlumno = EduTaskRoutes.dec(args?.getString("nombreAlumno")),
                onBack       = { navController.popBackStack() }
            )
        }
    }
}