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

object EduTaskRoutes {
    const val LOGIN         = "login"
    const val HOME_ALUMNO   = "home_alumno/{nombre}/{carrera}"
    const val HOME_PROFESOR = "home_profesor/{nombre}/{clase}"

    fun homeAlumno(nombre: String, carrera: String)  = "home_alumno/${enc(nombre)}/${enc(carrera)}"
    fun homeProfesor(nombre: String, clase: String)  = "home_profesor/${enc(nombre)}/${enc(clase)}"
    private fun enc(s: String) = s.replace(" ", "_").replace(".", "-")
    fun dec(s: String?) = s?.replace("_", " ")?.replace("-", ".") ?: ""
}

@Composable
fun EduTaskNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = EduTaskRoutes.LOGIN) {

        composable(EduTaskRoutes.LOGIN) {
            LoginScreen(onLoginSuccess = { rol ->
                val route = when (rol) {
                    RolUsuario.Alumno      -> EduTaskRoutes.homeAlumno("Juan Ramirez", "Ingenieria de Software")
                    RolUsuario.Profesor    -> EduTaskRoutes.homeProfesor("Mtro Perez", "Programacion Movil PMLP")
                    RolUsuario.Coordinador -> EduTaskRoutes.homeProfesor("Coord Garcia", "Coordinacion Academica")
                }
                navController.navigate(route) { popUpTo(EduTaskRoutes.LOGIN) { inclusive = true } }
            })
        }

        composable(EduTaskRoutes.HOME_ALUMNO) { back ->
            HomeAlumnoScreen(
                nombreAlumno     = EduTaskRoutes.dec(back.arguments?.getString("nombre")),
                carrera          = EduTaskRoutes.dec(back.arguments?.getString("carrera")),
                onSubirEvidencia = {},
                onLogout         = { navController.navigate(EduTaskRoutes.LOGIN) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(EduTaskRoutes.HOME_PROFESOR) { back ->
            HomeProfesorScreen(
                nombreProfesor = EduTaskRoutes.dec(back.arguments?.getString("nombre")),
                claseActual    = EduTaskRoutes.dec(back.arguments?.getString("clase")),
                onCrearTarea   = {},
                onLogout       = { navController.navigate(EduTaskRoutes.LOGIN) { popUpTo(0) { inclusive = true } } }
            )
        }
    }
}