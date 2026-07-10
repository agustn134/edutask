package com.pmlp.edutask.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pmlp.edutask.model.RolUsuario
import com.pmlp.edutask.ui.alumno.HomeAlumnoScreen
import com.pmlp.edutask.ui.login.LoginScreen
import com.pmlp.edutask.ui.profesor.HomeProfesorScreen
import com.pmlp.edutask.ui.profesor.CrearTareaScreen
import com.pmlp.edutask.ui.profesor.EvaluarTareaScreen
import com.pmlp.edutask.ui.profesor.AlumnosClaseScreen
import com.pmlp.edutask.ui.profesor.EstadisticasTareaScreen

object EduTaskRoutes {
    const val LOGIN         = "login"
    const val HOME_ALUMNO   = "home_alumno/{idUsuario}/{nombre}/{carrera}"
    const val HOME_PROFESOR = "home_profesor/{idUsuario}/{nombre}/{clase}"
    const val CREAR_TAREA   = "crear_tarea/{idUsuario}?idTarea={idTarea}"
    const val EVALUAR_TAREA = "evaluar_tarea/{idEvidencia}/{idUsuario}"
    const val ALUMNOS_CLASE = "alumnos_clase/{idClase}/{nombreClase}"
    const val ESTADISTICAS_TAREA = "estadisticas_tarea/{idTarea}/{tituloTarea}"

    fun homeAlumno(idUsuario: String, nombre: String, carrera: String)  = "home_alumno/${enc(idUsuario)}/${enc(nombre)}/${enc(carrera)}"
    fun homeProfesor(idUsuario: String, nombre: String, clase: String)  = "home_profesor/${enc(idUsuario)}/${enc(nombre)}/${enc(clase)}"
    fun crearTarea(idUsuario: String, idTarea: String? = null)  = "crear_tarea/${enc(idUsuario)}" + if (idTarea != null) "?idTarea=${enc(idTarea)}" else ""
    fun evaluarTarea(idEvidencia: String, idUsuario: String)  = "evaluar_tarea/${enc(idEvidencia)}/${enc(idUsuario)}"
    fun alumnosClase(idClase: String, nombreClase: String)  = "alumnos_clase/${enc(idClase)}/${enc(nombreClase)}"
    fun estadisticasTarea(idTarea: String, tituloTarea: String)  = "estadisticas_tarea/${enc(idTarea)}/${enc(tituloTarea)}"
    private fun enc(s: String): String {
        return try {
            java.net.URLEncoder.encode(s, "UTF-8")
        } catch (e: Exception) {
            s
        }
    }
    fun dec(s: String?): String {
        if (s == null) return ""
        return try {
            java.net.URLDecoder.decode(s, "UTF-8")
        } catch (e: Exception) {
            s
        }
    }
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
            HomeAlumnoScreen(
                idUsuario        = EduTaskRoutes.dec(back.arguments?.getString("idUsuario")),
                nombreAlumno     = EduTaskRoutes.dec(back.arguments?.getString("nombre")),
                carrera          = EduTaskRoutes.dec(back.arguments?.getString("carrera")),
                onSubirEvidencia = {},
                onLogout         = { navController.navigate(EduTaskRoutes.LOGIN) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(EduTaskRoutes.HOME_PROFESOR) { back ->
            val idUsuario = EduTaskRoutes.dec(back.arguments?.getString("idUsuario"))
            HomeProfesorScreen(
                idUsuario      = idUsuario,
                nombreProfesor = EduTaskRoutes.dec(back.arguments?.getString("nombre")),
                claseActual    = EduTaskRoutes.dec(back.arguments?.getString("clase")),
                onCrearTarea   = { idUser, idTar ->
                    navController.navigate(EduTaskRoutes.crearTarea(idUser, idTar))
                },
                onVerEvidencia = { idEvidencia ->
                    navController.navigate(EduTaskRoutes.evaluarTarea(idEvidencia, idUsuario))
                },
                onVerAlumnos = { idClase, nombreClase ->
                    navController.navigate(EduTaskRoutes.alumnosClase(idClase, nombreClase))
                },
                onVerEstadisticas = { idTarea, titulo ->
                    navController.navigate(EduTaskRoutes.estadisticasTarea(idTarea, titulo))
                },
                onLogout       = { navController.navigate(EduTaskRoutes.LOGIN) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(
            route = EduTaskRoutes.CREAR_TAREA,
            arguments = listOf(
                androidx.navigation.navArgument("idTarea") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { back ->
            val idUsuario = EduTaskRoutes.dec(back.arguments?.getString("idUsuario"))
            val idTarea = back.arguments?.getString("idTarea")?.let { EduTaskRoutes.dec(it) }
            CrearTareaScreen(
                idUsuario = idUsuario,
                idTarea = idTarea,
                onTareaCreadaExitosa = {
                    navController.popBackStack()
                }
            )
        }

        composable(EduTaskRoutes.EVALUAR_TAREA) { back ->
            val idEvidencia = EduTaskRoutes.dec(back.arguments?.getString("idEvidencia"))
            val idUsuario = EduTaskRoutes.dec(back.arguments?.getString("idUsuario"))
            EvaluarTareaScreen(
                idEvidencia = idEvidencia,
                idUsuario = idUsuario,
                onEvaluadoExitoso = {
                    navController.popBackStack()
                }
            )
        }

        composable(EduTaskRoutes.ALUMNOS_CLASE) { back ->
            val idClase = EduTaskRoutes.dec(back.arguments?.getString("idClase"))
            val nombreClase = EduTaskRoutes.dec(back.arguments?.getString("nombreClase"))
            AlumnosClaseScreen(
                idClase = idClase,
                nombreClase = nombreClase,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(EduTaskRoutes.ESTADISTICAS_TAREA) { back ->
            val idTarea = EduTaskRoutes.dec(back.arguments?.getString("idTarea"))
            val tituloTarea = EduTaskRoutes.dec(back.arguments?.getString("tituloTarea"))
            EstadisticasTareaScreen(
                idTarea = idTarea,
                tituloTarea = tituloTarea,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}