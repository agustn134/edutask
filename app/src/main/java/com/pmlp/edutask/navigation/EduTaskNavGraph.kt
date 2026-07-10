package com.pmlp.edutask.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import com.pmlp.edutask.model.RolUsuario
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.edutask.ui.alumno.HomeAlumnoScreen
import com.pmlp.edutask.ui.alumno.EnviarEvidenciaScreen
import com.pmlp.edutask.ui.coordinador.CoordinadorViewModel
import com.pmlp.edutask.ui.coordinador.FormularioUsuarioScreen
import com.pmlp.edutask.ui.coordinador.HomeCoordinadorScreen
import com.pmlp.edutask.ui.coordinador.ListaUsuariosScreen
import com.pmlp.edutask.ui.coordinador.FormularioEventoScreen
import com.pmlp.edutask.ui.coordinador.ListaEventosScreen
import com.pmlp.edutask.ui.login.LoginScreen
import com.pmlp.edutask.ui.profesor.HomeProfesorScreen
import java.util.Date

object EduTaskRoutes {
    const val LOGIN           = "login"
    const val HOME_ALUMNO     = "home_alumno/{idUsuario}/{nombre}/{carrera}"
    const val HOME_PROFESOR   = "home_profesor/{idUsuario}/{nombre}/{clase}"
    const val HOME_COORDINADOR = "home_coordinador/{idUsuario}/{nombre}"
    const val LISTA_USUARIOS  = "lista_usuarios/{filtro}"
    const val FORMULARIO_USUARIO = "formulario_usuario?idUsuario={idUsuario}"
    const val LISTA_EVENTOS = "lista_eventos"
    const val FORMULARIO_EVENTO = "formulario_evento?idEvento={idEvento}"
    const val ENVIAR_EVIDENCIA =
        "enviar_evidencia/{idAsignacion}/{idTarea}/{titulo}/{descripcion}/{fechaLimite}/{nombreClase}/{nombreAlumno}"

    fun homeAlumno(idUsuario: String, nombre: String, carrera: String) =
        "home_alumno/${enc(idUsuario)}/${enc(nombre)}/${enc(carrera)}"

    fun homeProfesor(idUsuario: String, nombre: String, clase: String) =
        "home_profesor/${enc(idUsuario)}/${enc(nombre)}/${enc(clase)}"

    fun homeCoordinador(idUsuario: String, nombre: String) =
        "home_coordinador/${enc(idUsuario)}/${enc(nombre)}"

    fun listaUsuarios(filtro: String) = "lista_usuarios/${enc(filtro)}"

    fun formularioUsuario(idUsuario: String? = null) =
        if (idUsuario != null) "formulario_usuario?idUsuario=${enc(idUsuario)}" else "formulario_usuario"

    fun formularioEvento(idEvento: String? = null) =
        if (idEvento != null) "formulario_evento?idEvento=${enc(idEvento)}" else "formulario_evento"

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
                    RolUsuario.Coordinador -> EduTaskRoutes.homeCoordinador(idUsuario, nombre)
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

        composable(EduTaskRoutes.HOME_COORDINADOR) { back ->
            HomeCoordinadorScreen(
                idUsuario = EduTaskRoutes.dec(back.arguments?.getString("idUsuario")),
                nombreCoordinador = EduTaskRoutes.dec(back.arguments?.getString("nombre")),
                onNavigateToLista = { filtro -> navController.navigate(EduTaskRoutes.listaUsuarios(filtro)) },
                onNavigateToEventos = { navController.navigate(EduTaskRoutes.LISTA_EVENTOS) },
                onLogout = { navController.navigate(EduTaskRoutes.LOGIN) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(EduTaskRoutes.LISTA_USUARIOS) { back ->
            val viewModel: CoordinadorViewModel = viewModel()
            ListaUsuariosScreen(
                viewModel = viewModel,
                filtroInicial = EduTaskRoutes.dec(back.arguments?.getString("filtro")),
                onBack = { navController.popBackStack() },
                onNavigateToFormulario = { id -> navController.navigate(EduTaskRoutes.formularioUsuario(id)) }
            )
        }

        composable(
            route = EduTaskRoutes.FORMULARIO_USUARIO,
            arguments = listOf(navArgument("idUsuario") { nullable = true; defaultValue = null })
        ) { back ->
            val viewModel: CoordinadorViewModel = viewModel()
            val idUsuarioStr = back.arguments?.getString("idUsuario")
            val idUsuario = if (!idUsuarioStr.isNullOrEmpty()) EduTaskRoutes.dec(idUsuarioStr) else null
            FormularioUsuarioScreen(
                viewModel = viewModel,
                idUsuario = idUsuario,
                onBack = { navController.popBackStack() }
            )
        }

        composable(EduTaskRoutes.LISTA_EVENTOS) {
            val viewModel: EventosSharedViewModel = viewModel()
            ListaEventosScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToFormulario = { id -> navController.navigate(EduTaskRoutes.formularioEvento(id)) }
            )
        }

        composable(
            route = EduTaskRoutes.FORMULARIO_EVENTO,
            arguments = listOf(navArgument("idEvento") { nullable = true; defaultValue = null })
        ) { back ->
            val viewModel: EventosSharedViewModel = viewModel()
            val idEventoStr = back.arguments?.getString("idEvento")
            val idEvento = if (!idEventoStr.isNullOrEmpty()) EduTaskRoutes.dec(idEventoStr) else null
            FormularioEventoScreen(
                viewModel = viewModel,
                idEvento = idEvento,
                onBack = { navController.popBackStack() }
            )
        }
    }
}