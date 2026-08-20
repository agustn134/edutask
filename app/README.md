# Módulo Móvil (App) — Documentación Técnica Completa

> **Proyecto:** EduTask — Plataforma de Gestión Académica
> **Módulo:** `:app` — Aplicación Móvil para Teléfonos y Tablets (Android)
> **Propósito:** Proporcionar una plataforma integral con interfaces dedicadas para **Alumnos**, **Profesores** y **Coordinadores**, permitiendo la gestión de clases, asignación y entrega de tareas, evaluación continua, generación de boletas/gradebook, sincronización con smartwatches y notificaciones locales.

---

## Tabla de Contenidos

1. [Dependencias y Librerías (build.gradle.kts)](#1-dependencias-y-librerías)
2. [Arquitectura General y Flujo por Roles](#2-arquitectura-general-y-flujo-por-roles)
3. [Navegación y Punto de Entrada](#3-navegación-y-punto-de-entrada)
   - [MainActivity.kt](#mainactivitykt)
   - [EduTaskNavGraph.kt](#edutasknavgraphkt)
4. [Modelos de Datos](#4-modelos-de-datos)
   - [Usuario.kt](#usuariokt)
   - [ClaseInfo.kt](#claseinfokt)
   - [Tarea.kt](#tareakt)
   - [EvidenciaTarea.kt](#evidenciatareakt)
5. [Módulo de Autenticación (Login)](#5-módulo-de-autenticación-login)
   - [LoginScreen.kt](#loginscreenkt)
6. [Módulo del Alumno](#6-módulo-del-alumno)
   - [HomeAlumnoScreen.kt](#homealumnoscreenkt)
   - [HomeAlumnoViewModel.kt](#homealumnoviewmodelkt)
   - [InicioAlumnoContent.kt](#inicioalumnocontentkt)
   - [TareasAlumnoContent.kt](#tareasalumnocontentkt)
   - [CalificacionesAlumnoContent.kt](#calificacionesalumnocontentkt)
   - [PerfilAlumnoContent.kt](#perfilalumnocontentkt)
   - [EnviarEvidenciaScreen.kt](#enviarevindenciascreenkt)
   - [EnviarEvidenciaViewModel.kt](#enviarevindenciaviewmodelkt)
7. [Módulo del Profesor](#7-módulo-del-profesor)
   - [HomeProfesorScreen.kt](#homeprofesorscreenkt)
   - [CrearTareaScreen.kt](#creartareaskt)
   - [AlumnosClaseScreen.kt](#alumnosclasescreenkt)
   - [EvaluarTareaScreen.kt](#evaluartareaskt)
   - [EstadisticasTareaScreen.kt](#estadisticastareaskt)
   - [GradebookScreen.kt](#gradebookscreentkt)
   - [GradebookViewModel.kt](#gradebookviewmodelkt)
8. [Módulo del Coordinador](#8-módulo-del-coordinador)
   - [HomeCoordinadorScreen.kt](#homecoordinadorscreenkt)
   - [CoordinadorViewModel.kt](#coordinadorviewmodelkt)
   - [FormularioEventoScreen.kt](#formularioeventoscreenkt)
   - [ListaEventosScreen.kt](#listaeventosscreenkt)
   - [FormularioUsuarioScreen.kt](#formulariousuarioscreenkt)
   - [ListaUsuariosScreen.kt](#listausuariosscreenkt)
9. [Componentes Reutilizables de Interfaz](#9-componentes-reutilizables-de-interfaz)
   - [VisorArchivoDialog.kt](#visorarchivodialogkt)
   - [SkeletonLoader.kt](#skeletonloaderkt)
   - [EmptyStateIllustration.kt](#emptystateillustrationkt)
10. [Tema y Estilos](#10-tema-y-estilos)
    - [Color.kt](#colorkt)
    - [Theme.kt](#themekt)
    - [Type.kt](#typekt)
11. [Workers y Utilidades](#11-workers-y-utilidades)
    - [TareaReminderWorker.kt](#tareareminderworkerkt)
    - [FirestoreUtils.kt](#firestoreutilskt)

---

## 1. Dependencias y Librerías

Archivo `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.pmlp.edutask"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.pmlp.edutask"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    implementation(libs.androidx.compose.material3.phone)
    implementation(libs.androidx.compose.material3.windowsize)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation(libs.play.services.wearable)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
```

### Explicación de librerías utilizadas:
- **`androidx.compose.material3`**: Componentes visuales modernos de Material Design 3 (Botones, Tarjetas, Scaffolds, Floating Action Buttons).
- **`androidx.navigation:navigation-compose`**: Controla el cambio de pantallas y paso de parámetros mediante rutas URI declarativas.
- **`com.google.firebase:firebase-auth`**: Autenticación segura de usuarios mediante credenciales en la nube.
- **`com.google.firebase:firebase-firestore`**: Base de datos NoSQL reactiva para lectura y sincronización instantánea de clases, tareas, evidencias y calificaciones.
- **`com.google.firebase:firebase-storage`**: Almacenamiento en la nube para documentos PDF, imágenes y archivos adjuntos.
- **`androidx.work:work-runtime-ktx`**: WorkManager para ejecutar tareas en segundo plano periódicamente (recordatorios de entregas próximas a vencer).
- **`com.google.android.gms:play-services-wearable`**: Comunicación con el smartwatch Wear OS para enviar la sesión activa del profesor.
- **`io.coil-kt:coil-compose`**: Renderizado asíncrono y cache de fotos de perfil y adjuntos de evidencias.

---

## 2. Arquitectura General y Flujo por Roles

La aplicación móvil implementa el patrón de arquitectura **MVVM (Model-View-ViewModel)** recomendado por Google junto con **Jetpack Compose**.

```
                           ┌────────────────────────┐
                           │    EduTaskNavGraph     │
                           └───────────┬────────────┘
                                       │
                                       ▼
                           ┌────────────────────────┐
                           │      LoginScreen       │
                           └───────────┬────────────┘
                                       │ (Autenticación y consulta de rol)
             ┌─────────────────────────┼─────────────────────────┐
             ▼                         ▼                         ▼
   ┌───────────────────┐     ┌───────────────────┐     ┌───────────────────┐
   │    Rol Alumno     │     │   Rol Profesor    │     │  Rol Coordinador  │
   │                   │     │                   │     │                   │
   │ - Inicio          │     │ - Clases activas  │     │ - Avisos TV/Móvil │
   │ - Tareas          │     │ - Crear Tarea     │     │ - Gestión Usuar.  │
   │ - Enviar Evidencia│     │ - Evaluar entrega │     │ - Reportes        │
   │ - Calificaciones  │     │ - Gradebook       │     └───────────────────┘
   │ - Perfil          │     │ - Sync Smartwatch │
   └───────────────────┘     └───────────────────┘
```

---

## 3. Navegación y Punto de Entrada

### `MainActivity.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/MainActivity.kt`

**¿Qué hace?** Es la actividad principal de la aplicación. Configura el tema `EdutaskTheme`, inicializa el contexto de la ventana y monta el grafo de navegación.

```kotlin
/**
 * Actividad principal de la aplicacion movil EduTask.
 * Configura el tema general de la app, inicializa el grafo de navegacion (EduTaskNavGraph)
 * y gestiona el flujo de autenticacion y arranque inicial.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.navigation.EduTaskNavGraph
import com.pmlp.edutask.ui.theme.EduTaskTheme
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MainActivity : ComponentActivity() {
    /**
     * Manejador de evento para la accion onCreate.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        createNotificationChannel()

        val db = FirebaseFirestore.getInstance()

        db.collection("tareas")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("TestFirebase", "Lectura exitosa: ${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TestFirebase", "Error de conexión: ", exception)
            }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduTaskTheme(darkTheme = false, dynamicColor = false) {
                EduTaskNavGraph()
            }
        }
    }

    /**
     * Metodo principal que ejecuta la operacion: createNotificationChannel.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios EduTask"
            val descriptionText = "Canal para recordar entregas de tareas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("edutask_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
```

**Explicación:**
- Hereda de `ComponentActivity`.
- En `onCreate()`, llama a `setContent { EdutaskTheme { EduTaskNavGraph() } }` para activar Jetpack Compose y la navegación global.

---

### `EduTaskNavGraph.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/navigation/EduTaskNavGraph.kt`

**¿Qué hace?** Define todas las rutas, argumentos y transiciones entre pantallas de la app.

```kotlin
/**
 * Grafo de navegacion principal de la aplicacion movil con Jetpack Compose Navigation.
 * Define las rutas, argumentos y transiciones entre pantallas para los diferentes roles
 * (Alumno, Profesor, Coordinador y Login).
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
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
import com.pmlp.edutask.ui.profesor.CrearTareaScreen
import com.pmlp.edutask.ui.profesor.EvaluarTareaScreen
import com.pmlp.edutask.ui.profesor.AlumnosClaseScreen
import com.pmlp.edutask.ui.profesor.EstadisticasTareaScreen
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
        "enviar_evidencia/{idAsignacion}/{idTarea}/{titulo}/{descripcion}/{fechaLimite}/{nombreClase}/{nombreAlumno}?idEvidencia={idEvidencia}"
    const val CREAR_TAREA   = "crear_tarea/{idUsuario}?idTarea={idTarea}"
    const val EVALUAR_TAREA = "evaluar_tarea/{idEvidencia}/{idUsuario}"
    const val ALUMNOS_CLASE = "alumnos_clase/{idClase}/{nombreClase}"
    const val ESTADISTICAS_TAREA = "estadisticas_tarea/{idTarea}/{tituloTarea}"
    const val GRADEBOOK = "gradebook/{idClase}"

    /**
     * Metodo principal que ejecuta la operacion: homeAlumno.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun homeAlumno(idUsuario: String, nombre: String, carrera: String) =
        "home_alumno/${enc(idUsuario)}/${enc(nombre)}/${enc(carrera)}"

    /**
     * Metodo principal que ejecuta la operacion: homeProfesor.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun homeProfesor(idUsuario: String, nombre: String, clase: String) =
        "home_profesor/${enc(idUsuario)}/${enc(nombre)}/${enc(clase)}"

    /**
     * Metodo principal que ejecuta la operacion: homeCoordinador.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun homeCoordinador(idUsuario: String, nombre: String) =
        "home_coordinador/${enc(idUsuario)}/${enc(nombre)}"

    /**
     * Metodo principal que ejecuta la operacion: listaUsuarios.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun listaUsuarios(filtro: String) = "lista_usuarios/${enc(filtro)}"

    /**
     * Metodo principal que ejecuta la operacion: formularioUsuario.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun formularioUsuario(idUsuario: String? = null) =
        if (idUsuario != null) "formulario_usuario?idUsuario=${enc(idUsuario)}" else "formulario_usuario"

    /**
     * Metodo principal que ejecuta la operacion: formularioEvento.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun formularioEvento(idEvento: String? = null) =
        if (idEvento != null) "formulario_evento?idEvento=${enc(idEvento)}" else "formulario_evento"

    /**
     * Metodo principal que ejecuta la operacion: enviarEvidencia.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun enviarEvidencia(
        idAsignacion: String,
        tarea:        Tarea,
        nombreAlumno: String,
        idEvidencia:  String? = null
    ) = "enviar_evidencia/${enc(idAsignacion)}/${enc(tarea.idTarea)}/${enc(tarea.titulo)}" +
        "/${enc(tarea.descripcion)}/${tarea.fechaLimite?.time ?: 0L}" +
        "/${enc(tarea.nombreClase)}/${enc(nombreAlumno)}" +
        if (idEvidencia != null) "?idEvidencia=${enc(idEvidencia)}" else ""

    /**
     * Metodo principal que ejecuta la operacion: crearTarea.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun crearTarea(idUsuario: String, idTarea: String? = null)  = "crear_tarea/${enc(idUsuario)}" + if (idTarea != null) "?idTarea=${enc(idTarea)}" else ""
    /**
     * Metodo principal que ejecuta la operacion: evaluarTarea.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun evaluarTarea(idEvidencia: String, idUsuario: String)  = "evaluar_tarea/${enc(idEvidencia)}/${enc(idUsuario)}"
    /**
     * Metodo principal que ejecuta la operacion: alumnosClase.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun alumnosClase(idClase: String, nombreClase: String)  = "alumnos_clase/${enc(idClase)}/${enc(nombreClase)}"
    /**
     * Metodo principal que ejecuta la operacion: estadisticasTarea.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun estadisticasTarea(idTarea: String, tituloTarea: String)  = "estadisticas_tarea/${enc(idTarea)}/${enc(tituloTarea)}"
    /**
     * Metodo principal que ejecuta la operacion: gradebook.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun gradebook(idClase: String) = "gradebook/${enc(idClase)}"

    /**
     * Metodo principal que ejecuta la operacion: enc.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun enc(s: String): String = java.net.URLEncoder.encode(s, "UTF-8")
    /**
     * Metodo principal que ejecuta la operacion: dec.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun dec(s: String?): String = if (s != null) java.net.URLDecoder.decode(s, "UTF-8") else ""
}

@Composable
/**
 * Metodo principal que ejecuta la operacion: EduTaskNavGraph.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
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
                        EduTaskRoutes.enviarEvidencia(item.idAsignacion, item.tarea, nombreAlumno, item.idEvidencia)
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
            route = EduTaskRoutes.ENVIAR_EVIDENCIA,
            arguments = listOf(
                navArgument("idEvidencia") { nullable = true; defaultValue = null }
            )
        ) { back ->
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
                idEvidenciaRecibida = args?.getString("idEvidencia")?.let { EduTaskRoutes.dec(it) },
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

        composable(
            route = EduTaskRoutes.CREAR_TAREA,
            arguments = listOf(
                navArgument("idTarea") {
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
                },
                onBack = {
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
                },
                onVerLibreta = {
                    navController.navigate(EduTaskRoutes.gradebook(idClase))
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

        composable(EduTaskRoutes.GRADEBOOK) { back ->
            val idClase = EduTaskRoutes.dec(back.arguments?.getString("idClase"))
            val viewModel: com.pmlp.edutask.ui.profesor.GradebookViewModel = viewModel()
            com.pmlp.edutask.ui.profesor.GradebookScreen(
                idClase = idClase,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

**Explicación de rutas clave:**
- `"login"`: Pantalla inicial de autenticación.
- `"home_alumno/{idUsuario}"`: Panel principal del estudiante.
- `"home_profesor/{idUsuario}"`: Panel de control del docente.
- `"home_coordinador/{idUsuario}"`: Panel de administración institucional.
- `"crear_tarea/{idClase}"`: Formulario para nueva asignación.
- `"evaluar_tarea/{idTarea}"`: Interfaz para revisar y calificar entregas de alumnos.
- `"enviar_evidencia/{idTarea}"`: Interfaz para que el alumno suba archivos y fotos.
- `"gradebook/{idClase}"`: Matriz completa de calificaciones por grupo.

---

## 4. Modelos de Datos

### `Usuario.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/model/Usuario.kt`

```kotlin
/**
 * Modelo de datos representativo de los usuarios de la plataforma (Alumno, Profesor, Coordinador),
 * incluyendo nombre, correo, rol y datos de autenticacion.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
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
```

### `ClaseInfo.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/model/ClaseInfo.kt`

```kotlin
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
```

### `Tarea.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/model/Tarea.kt`

```kotlin
/**
 * Modelo de datos representativo de una tarea escolar,
 * con titulo, descripcion, fecha de entrega, clase asociada y ponderacion.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Tarea(
    val idTarea: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    @ServerTimestamp
    val fechaLimite: Date? = null,
    val idClase: String = "",
    val nombreClase: String = ""
)
```

### `EvidenciaTarea.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/model/EvidenciaTarea.kt`

```kotlin
/**
 * Modelo de datos para las evidencias entregadas por los alumnos,
 * almacenando enlaces de archivos, comentarios, fechas de entrega y estado de revision.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.model

import java.util.Date

enum class EstadoEvidencia { Pendiente, Aprobada, Rechazada }

data class EvidenciaTarea(
    val idEvidencia: String = "",
    val tituloTarea: String = "",
    val fotoBase64: String = "",
    val fechaEnvio: Date = Date(),
    val estado: EstadoEvidencia = EstadoEvidencia.Pendiente,
    val idAsignacion: String = "",
    val nombreAlumno: String = "",
    // Campos legacy para compatibilidad
    val nombreArchivo: String? = null,
    val textoEvidencia: String? = null,
    // Nuevos campos
    val archivos: List<Map<String, String>> = emptyList(), // lista de { "nombre": "...", "url": "..." }
    val vinculos: List<String> = emptyList(),
    val calificacion: Int? = null
)
```

---

## 5. Módulo de Autenticación (Login)

### `LoginScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/login/LoginScreen.kt`

**¿Qué hace?** Maneja el inicio de sesión con correo y contraseña, validación de campos, animaciones de carga y redirección según el rol del usuario en Firestore.

```kotlin
/**
 * Pantalla de autenticacion de EduTask que permite el ingreso mediante correo y contrasena,
 * validando credenciales y redirigiendo segun el rol del usuario.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.login

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.RolUsuario
import com.pmlp.edutask.ui.theme.EduTaskTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla LoginScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun LoginScreen(onLoginSuccess: (String, String, RolUsuario) -> Unit = { _, _, _ -> }) {
    val context   = LocalContext.current
    val winSize   = calculateWindowSizeClass(activity = context as Activity)
    val isCompact = winSize.widthSizeClass == WindowWidthSizeClass.Compact

    var matricula       by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passVisible     by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMsg        by remember { mutableStateOf<String?>(null) }
    val focusMgr        = LocalFocusManager.current
    val scroll          = rememberScrollState()
    val scope           = rememberCoroutineScope()
    val db              = remember { FirebaseFirestore.getInstance() }

    /**
     * Metodo principal que ejecuta la operacion: doLogin.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun doLogin() {
        if (matricula.isBlank() || password.isBlank()) return

        scope.launch {
            isLoading = true
            errorMsg = null

            try {
                Log.d("LOGIN_DEBUG", "Intentando login con matricula: $matricula")
                // Buscar usuario por matricula en Firestore
                val snapshot = db.collection("usuarios")
                    .whereEqualTo("matricula", matricula.trim())
                    .get()
                    .await()

                if (snapshot.isEmpty) {
                    errorMsg = "No se encontro un usuario con esa matricula."
                    isLoading = false
                    return@launch
                }

                val doc = snapshot.documents[0]
                val contrasenaFirestore = doc.getString("contrasena") ?: ""

                if (password != contrasenaFirestore) {
                    errorMsg = "Contrasena incorrecta."
                    isLoading = false
                    return@launch
                }

                // Mapear el rol del documento al enum
                val rolString = doc.getString("rol") ?: "Alumno"
                val rol = when (rolString) {
                    "Profesor"     -> RolUsuario.Profesor
                    "Coordinador"  -> RolUsuario.Coordinador
                    else           -> RolUsuario.Alumno
                }

                val idUsuario = doc.id
                val nombre    = doc.getString("nombre") ?: matricula

                Log.d("LOGIN_DEBUG", "Login exitoso. Usuario: $nombre, Rol: $rol")

                // Sync session to wear device on successful login
                if (rol == RolUsuario.Profesor) {
                    try {
                        val dataClient = Wearable.getDataClient(context)
                        val putDataReq = PutDataMapRequest.create("/usuario_logueado").run {
                            dataMap.putString("idUsuario", idUsuario)
                            dataMap.putString("nombre", nombre)
                            asPutDataRequest()
                        }
                        dataClient.putDataItem(putDataReq)
                            .addOnSuccessListener { Log.d("WearSync", "Session synced successfully: $idUsuario") }
                            .addOnFailureListener { Log.e("WearSync", "Failed to sync session with Wear OS", it) }
                    } catch (e: Exception) {
                        Log.e("WearSync", "Wearable API error", e)
                    }

                    // Cloud fallback
                    try {
                        db.collection("sesion_wear").document("default")
                            .set(hashMapOf(
                                "idUsuario" to idUsuario,
                                "nombre" to nombre,
                                "timestamp" to com.google.firebase.Timestamp.now()
                            ))
                    } catch (e: Exception) {
                        Log.e("WearSync", "Firestore fallback sync error on Login", e)
                    }
                }

                isLoading = false
                onLoginSuccess(idUsuario, nombre, rol)

            } catch (e: Exception) {
                isLoading = false
                errorMsg = "Error de conexion. Verifica tu internet."
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor   = MaterialTheme.colorScheme.onBackground
    ) { pad ->
        Box(
            modifier         = Modifier.fillMaxSize().padding(pad).verticalScroll(scroll),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .then(if (isCompact) Modifier.fillMaxWidth() else Modifier.widthIn(max = 480.dp))
                    .padding(horizontal = if (isCompact) 24.dp else 0.dp)
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    color    = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.School, null, Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("EduTask", style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text("Plataforma Academica", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(40.dp))

                ElevatedCard(
                    modifier  = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    colors    = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Iniciar Sesion", style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface)

                        OutlinedTextField(
                            value = matricula, onValueChange = { matricula = it.uppercase(); errorMsg = null },
                            modifier = Modifier.fillMaxWidth(), label = { Text("Matricula") },
                            placeholder = { Text("Ej. A12345") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Matricula") },
                            isError = errorMsg != null, singleLine = true, enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusMgr.moveFocus(FocusDirection.Down) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor    = MaterialTheme.colorScheme.primary,
                                cursorColor          = MaterialTheme.colorScheme.primary
                            ), shape = MaterialTheme.shapes.medium
                        )

                        OutlinedTextField(
                            value = password, onValueChange = { password = it; errorMsg = null },
                            modifier = Modifier.fillMaxWidth(), label = { Text("Contrasena") },
                            leadingIcon  = { Icon(Icons.Default.Lock, contentDescription = "Contrasena") },
                            enabled = !isLoading,
                            trailingIcon = {
                                IconButton(onClick = { passVisible = !passVisible }) {
                                    Icon(if (passVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passVisible) "Ocultar" else "Mostrar")
                                }
                            },
                            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = errorMsg != null, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus(); doLogin() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor    = MaterialTheme.colorScheme.primary,
                                cursorColor          = MaterialTheme.colorScheme.primary
                            ), shape = MaterialTheme.shapes.medium
                        )

                        AnimatedVisibility(visible = errorMsg != null, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.ErrorOutline, "Error", Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error)
                                Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick  = { focusMgr.clearFocus(); doLogin() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled  = !isLoading && matricula.isNotBlank() && password.isNotBlank(),
                            colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape    = MaterialTheme.shapes.large
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(Modifier.size(20.dp), MaterialTheme.colorScheme.onPrimary, 2.dp)
                                Spacer(Modifier.width(10.dp))
                                Text("Validando...", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Text("Iniciar Sesion", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TextButton(onClick = {}) {
                                Text("Olvidaste tu contrasena?", style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("El rol se asigna automaticamente segun tus credenciales institucionales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

@Preview(name = "Login Movil", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
/**
 * Metodo principal que ejecuta la operacion: LoginPreviewMovil.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun LoginPreviewMovil() {
    EduTaskTheme(darkTheme = false, dynamicColor = false) { LoginScreen() }
}

@Preview(name = "Login Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
/**
 * Metodo principal que ejecuta la operacion: LoginPreviewTablet.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun LoginPreviewTablet() {
    EduTaskTheme(darkTheme = false, dynamicColor = false) { LoginScreen() }
}
```

**Explicación de componentes y botones:**
- **`OutlinedTextField` de Correo y Contraseña:** Campos de texto con validación en tiempo real y soporte para ocultar/mostrar contraseña.
- **Botón "Iniciar Sesión" (`Button`):** Ejecuta `FirebaseAuth.signInWithEmailAndPassword`, consulta el documento del usuario en Firestore y navega a la pantalla correspondiente según el rol (`Alumno`, `Profesor`, `Coordinador`).

---

## 6. Módulo del Alumno

### `HomeAlumnoScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/alumno/HomeAlumnoScreen.kt`

```kotlin
/**
 * Pantalla principal del rol Alumno.
 * Integra la barra de navegacion inferior (Bottom Navigation) y coordina las vistas de
 * Inicio, Tareas, Calificaciones y Perfil.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.alumno

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.ui.theme.EduTaskTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.edutask.ui.EventosUiState
import java.text.SimpleDateFormat

private data class NavItem(val label: String, val icon: ImageVector)
private val NAV_ITEMS = listOf(
    NavItem("Inicio",         Icons.Default.Home),
    NavItem("Tareas",         Icons.AutoMirrored.Filled.Assignment),
    NavItem("Calificaciones", Icons.Default.Grade),
    NavItem("Perfil",         Icons.Default.Person)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla HomeAlumnoScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun HomeAlumnoScreen(
    idUsuario: String    = "",
    nombreAlumno: String = "Juan Ramirez",
    carrera: String      = "Ingenieria de Software",
    viewModel: HomeAlumnoViewModel = viewModel(),
    eventosViewModel: EventosSharedViewModel = viewModel(),
    onVerTarea: (TareaItem) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context    = LocalContext.current
    val winSize    = calculateWindowSizeClass(activity = context as Activity)
    val isCompact  = winSize.widthSizeClass == WindowWidthSizeClass.Compact

    var selectedNav       by rememberSaveable { mutableIntStateOf(0) }
    var claseSelected     by rememberSaveable { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val correoAlumno by viewModel.correo.collectAsState()
    val eventosState by eventosViewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, el WorkManager podrá enviar notificaciones
        }
    }

    LaunchedEffect(idUsuario) {
        viewModel.fetchUserData(idUsuario)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        eventosViewModel.fetchEventos()
    }
    
    // Agendar recordatorios si los datos se cargaron
    LaunchedEffect(uiState) {
        if (uiState is HomeAlumnoState.Success) {
            viewModel.scheduleReminders(context, (uiState as HomeAlumnoState.Success).tareas)
        }
    }

    val now = java.util.Date()
    val pendienteCount = if (uiState is HomeAlumnoState.Success) {
        (uiState as HomeAlumnoState.Success).tareas.count { it.estado == EstadoEvidencia.Pendiente && it.idEvidencia == null && (it.tarea.fechaLimite == null || !now.after(it.tarea.fechaLimite)) }
    } else 0

    val initials = nombreAlumno.split(" ").take(2).joinToString("") { it.first().toString().uppercase() }

    if (isCompact) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor   = MaterialTheme.colorScheme.onBackground,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        Surface(Modifier.padding(start = 12.dp, end = 12.dp).size(40.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.primaryContainer) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(initials, style = MaterialTheme.typography.labelLarge,
                                     color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    },
                    title = {
                        Column {
                            Text("Hola, ${nombreAlumno.substringBefore(" ")}!", style = MaterialTheme.typography.titleMedium,
                                 maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(carrera, style = MaterialTheme.typography.bodySmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant,
                              contentColor   = MaterialTheme.colorScheme.onSurfaceVariant) {
                    NAV_ITEMS.forEachIndexed { i, item ->
                        NavigationBarItem(
                            selected = selectedNav == i, onClick = { selectedNav = i },
                            icon = {
                                if (i == 1 && pendienteCount > 0)
                                    BadgedBox(badge = { Badge { Text(pendienteCount.toString()) } }) { Icon(item.icon, null) }
                                else Icon(item.icon, null)
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            },

        ) { pad ->
            when (uiState) {
                is HomeAlumnoState.Loading -> {
                    Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HomeAlumnoState.Error -> {
                    Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                        Text("Error al cargar datos.", color = MaterialTheme.colorScheme.error)
                    }
                }
                is HomeAlumnoState.Success -> {
                    val data = uiState as HomeAlumnoState.Success
                    val tareasFiltradas = if (claseSelected == null) data.tareas
                                          else data.tareas.filter { it.tarea.nombreClase == claseSelected }
                    
                    val eventos = if (eventosState is EventosUiState.Success) {
                        (eventosState as EventosUiState.Success).eventos
                    } else emptyList()
                    
                    Crossfade(targetState = selectedNav, label = "TabSwitch") { nav ->
                        when (nav) {
                            0 -> InicioContent(Modifier.padding(pad), pendienteCount, data.tareas, eventos, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea) { codigo ->
                                viewModel.unirseAClase(codigo, idUsuario)
                            }
                            1 -> TareasContent(Modifier.padding(pad), claseSelected, { claseSelected = if (claseSelected == it) null else it },
                                          tareasFiltradas, pendienteCount, data.clases, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea)
                            2 -> CalificacionesContent(Modifier.padding(pad), data.tareas, data.promedios, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea)
                            3 -> PerfilContent(Modifier.padding(pad), nombreAlumno, carrera, data.tareas, correoAlumno) { nuevoCorreo, nuevaContrasena ->
                                viewModel.updateAccount(idUsuario, nuevoCorreo, nuevaContrasena) { success ->
                                    if (success) {
                                        android.widget.Toast.makeText(context, "Cuenta actualizada", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Error al actualizar", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Row(Modifier.fillMaxSize()) {
            NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceVariant,
                           contentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
                           header = {
                               Spacer(Modifier.height(8.dp))
                               Surface(Modifier.size(40.dp), shape = MaterialTheme.shapes.extraLarge,
                                       color = MaterialTheme.colorScheme.primaryContainer) {
                                   Box(contentAlignment = Alignment.Center) {
                                       Text(initials, style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                                   }
                               }
                               Spacer(Modifier.height(8.dp))
                           }) {
                NAV_ITEMS.forEachIndexed { i, item ->
                    NavigationRailItem(selected = selectedNav == i, onClick = { selectedNav = i },
                        icon = { Icon(item.icon, null) }, label = { Text(item.label) })
                }
            }
            Scaffold(Modifier.weight(1f), containerColor = MaterialTheme.colorScheme.background,
                     topBar = {
                         TopAppBar(
                             title = {
                                 Column {
                                     Text("Hola, $nombreAlumno!", style = MaterialTheme.typography.titleLarge)
                                     Text(carrera, style = MaterialTheme.typography.bodyMedium,
                                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                                 }
                             },
                             actions = {
                                 IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar sesión") }
                             },
                             colors = TopAppBarDefaults.topAppBarColors(
                                 containerColor = MaterialTheme.colorScheme.surface,
                                 scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                             )
                         )
                     }) { pad ->
                when (uiState) {
                    is HomeAlumnoState.Loading -> {
                        Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is HomeAlumnoState.Error -> {
                        Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                            Text("Error al cargar datos.", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is HomeAlumnoState.Success -> {
                        val data = uiState as HomeAlumnoState.Success
                        val tareasFiltradas = if (claseSelected == null) data.tareas
                                              else data.tareas.filter { it.tarea.nombreClase == claseSelected }
                        
                        val eventos = if (eventosState is EventosUiState.Success) {
                            (eventosState as EventosUiState.Success).eventos
                        } else emptyList()
                        
                        Crossfade(targetState = selectedNav, label = "TabSwitchTablet") { nav ->
                            when (nav) {
                                0 -> InicioContent(Modifier.padding(pad), pendienteCount, data.tareas, eventos, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea) { codigo ->
                                    viewModel.unirseAClase(codigo, idUsuario)
                                }
                                1 -> TareasContent(Modifier.padding(pad), claseSelected, { claseSelected = if (claseSelected == it) null else it },
                                              tareasFiltradas, pendienteCount, data.clases, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea)
                                2 -> CalificacionesContent(Modifier.padding(pad), data.tareas, data.promedios, isRefreshing, { viewModel.refresh(idUsuario) }, onVerTarea)
                                3 -> PerfilContent(Modifier.padding(pad), nombreAlumno, carrera, data.tareas, correoAlumno) { nuevoCorreo, nuevaContrasena ->
                                    viewModel.updateAccount(idUsuario, nuevoCorreo, nuevaContrasena) { success ->
                                        if (success) {
                                            android.widget.Toast.makeText(context, "Cuenta actualizada", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Error al actualizar", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(name = "Home Alumno Movil", showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 800)
@Composable
/**
 * Metodo principal que ejecuta la operacion: PreviewAlumnoMovil.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun PreviewAlumnoMovil() { EduTaskTheme(darkTheme = false, dynamicColor = false) { HomeAlumnoScreen() } }

@Preview(name = "Home Alumno Tablet", showBackground = true, showSystemUi = true, widthDp = 800, heightDp = 1280)
@Composable
/**
 * Metodo principal que ejecuta la operacion: PreviewAlumnoTablet.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun PreviewAlumnoTablet() { EduTaskTheme(darkTheme = false, dynamicColor = false) { HomeAlumnoScreen() } }
```

### `HomeAlumnoViewModel.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/alumno/HomeAlumnoViewModel.kt`

```kotlin
/**
 * ViewModel que administra el estado global del alumno (clases inscritas, tareas pendientes,
 * resumen academico y datos de sesion).
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.alumno

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.Tarea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import java.util.Date
import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import java.util.concurrent.TimeUnit
import com.pmlp.edutask.worker.TareaReminderWorker
import com.pmlp.edutask.utils.getSafeDate

data class TareaItem(
    val tarea: Tarea,
    val estado: EstadoEvidencia,
    val idAsignacion: String,
    val calificacion: Int? = null,
    val comentario: String? = null,
    val idEvidencia: String? = null
)

sealed class HomeAlumnoState {
    object Loading : HomeAlumnoState()
    data class Success(
        val clases: List<String>,
        val tareas: List<TareaItem>,
        val promedios: Map<String, Double> = emptyMap()
    ) : HomeAlumnoState()
    data class Error(val message: String) : HomeAlumnoState()
}

class HomeAlumnoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeAlumnoState>(HomeAlumnoState.Loading)
    val uiState: StateFlow<HomeAlumnoState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _correo = MutableStateFlow("")
    val correo: StateFlow<String> = _correo.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    private var asignacionesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private val evidenciasListeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
    
    private var inscripcionesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var tareasListener: com.google.firebase.firestore.ListenerRegistration? = null

    private var enrolledClassesNames = mutableListOf<String>()

    /**
     * Manejador de evento para la accion onCleared.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override fun onCleared() {
        super.onCleared()
        asignacionesListener?.remove()
        evidenciasListeners.forEach { it.remove() }
        inscripcionesListener?.remove()
        tareasListener?.remove()
    }

    /**
     * Obtiene o recupera datos asociados a fetchUserData desde la base de datos o API.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun fetchUserData(idUsuario: String) {
        if (idUsuario.isBlank()) {
            _uiState.value = HomeAlumnoState.Error("ID de usuario no válido")
            return
        }

        // Limpiar listeners anteriores si se recarga
        asignacionesListener?.remove()
        evidenciasListeners.forEach { it.remove() }
        evidenciasListeners.clear()

        db.collection("usuarios").document(idUsuario).get().addOnSuccessListener { doc ->
            _correo.value = doc.getString("correo") ?: ""
        }

        _uiState.value = HomeAlumnoState.Loading
        loadData(idUsuario)
    }

    /**
     * Metodo principal que ejecuta la operacion: refresh.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun refresh(idUsuario: String) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(1000) // Simulación UX
            loadData(idUsuario)
            _isRefreshing.value = false
        }
    }

    /**
     * Guarda o actualiza los datos de updateAccount en la base de datos.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun updateAccount(idUsuario: String, nuevoCorreo: String, nuevaContrasena: String, onComplete: (Boolean) -> Unit) {
        val updates = mutableMapOf<String, Any>("correo" to nuevoCorreo)
        if (nuevaContrasena.isNotBlank()) {
            updates["contrasena"] = nuevaContrasena
        }
        db.collection("usuarios").document(idUsuario).update(updates)
            .addOnSuccessListener {
                _correo.value = nuevoCorreo
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    /**
     * Metodo principal que ejecuta la operacion: scheduleReminders.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun scheduleReminders(context: Context, tareas: List<TareaItem>) {
        val now = System.currentTimeMillis()
        val twoHoursInMillis = 2 * 60 * 60 * 1000L

        tareas.filter { it.estado == EstadoEvidencia.Pendiente && it.idEvidencia == null }
            .forEach { item ->
                val timeRemaining = (item.tarea.fechaLimite?.time ?: now) - now
                if (timeRemaining > twoHoursInMillis) {
                    val delay = timeRemaining - twoHoursInMillis
                    
                    val inputData = Data.Builder()
                        .putString("TAREA_NOMBRE", item.tarea.titulo)
                        .putString("TAREA_ID", item.tarea.idTarea)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<TareaReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .build()

                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "reminder_${item.tarea.idTarea}",
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                }
            }
    }

    /**
     * Obtiene o recupera datos asociados a loadData desde la base de datos o API.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private fun loadData(idUsuario: String) {
        // Iniciar la sincronización automática de nuevas tareas
        startAutoSync(idUsuario)

        asignacionesListener = db.collection("asignaciones_tarea")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = HomeAlumnoState.Error(error.message ?: "Error al cargar tareas")
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    _uiState.value = HomeAlumnoState.Success(enrolledClassesNames.toList(), emptyList())
                    return@addSnapshotListener
                }

                viewModelScope.launch {
                    try {
                        val paresTareasMap = mutableMapOf<String, TareaItem>()
                        val clasesSet = mutableSetOf<String>()

                        val clasesCache = mutableMapOf<String, String>()
                        val tareasCache = mutableMapOf<String, Tarea>()

                        // Limpiar listeners de evidencias anteriores para esta nueva carga
                        evidenciasListeners.forEach { it.remove() }
                        evidenciasListeners.clear()

                        val asignacionesSnapshot = snapshot.documents
                        
                        // 2. Fetch all Tareas and Clases efficiently using async map
                        val tareasMapDeferred = asignacionesSnapshot.map { asignacionDoc ->
                            val idTarea = asignacionDoc.getString("idTarea") ?: return@map null
                            if (tareasCache.containsKey(idTarea)) return@map tareasCache[idTarea]
                            viewModelScope.async {
                                val tareaDoc = db.collection("tareas").document(idTarea).get().await()
                                if (!tareaDoc.exists()) return@async null
                                val idClase = tareaDoc.getString("idClase") ?: ""
                                val titulo = tareaDoc.getString("titulo") ?: ""
                                val desc = tareaDoc.getString("descripcion") ?: ""
                                val fecha = tareaDoc.getSafeDate("fechaLimite") ?: java.util.Date()
                                val nombreClase = clasesCache.getOrPut(idClase) {
                                    db.collection("clases").document(idClase).get().await().getString("nombre") ?: "Sin Clase"
                                }
                                val t = Tarea(idTarea, titulo, desc, fecha, idClase, nombreClase)
                                tareasCache[idTarea] = t
                                t
                            }
                        }
                        
                        val tareasList = tareasMapDeferred.mapNotNull { if (it is kotlinx.coroutines.Deferred<*>) (it as kotlinx.coroutines.Deferred<Tarea?>).await() else it as Tarea? }
                        
                        // Populate classes set
                        tareasList.forEach { clasesSet.add(it.nombreClase) }

                        // Map Asignacion -> Tarea
                        val idAsignacionToTarea = asignacionesSnapshot.mapNotNull { doc -> 
                            val idTarea = doc.getString("idTarea") ?: return@mapNotNull null
                            val tarea = tareasCache[idTarea] ?: return@mapNotNull null
                            doc.id to tarea
                        }.toMap()

                        // 3. Fetch all Evidencias using whereIn in chunks of 30
                        val idAsignacionesList = idAsignacionToTarea.keys.toList()
                        val evidenciasResult = mutableMapOf<String, com.google.firebase.firestore.DocumentSnapshot>()
                        
                        if (idAsignacionesList.isNotEmpty()) {
                            val chunks = idAsignacionesList.chunked(30)
                            for (chunk in chunks) {
                                val evSnap = db.collection("evidencias_tarea").whereIn("idAsignacion", chunk).get().await()
                                for (doc in evSnap.documents) {
                                    val idAsig = doc.getString("idAsignacion")
                                    if (idAsig != null) evidenciasResult[idAsig] = doc
                                }
                            }
                        }

                        // 4. Fetch all Calificaciones for evaluated evidences
                        val evaluatedEvidenciaIds = evidenciasResult.values.filter { it.getString("estado") != "Pendiente" }
                            .mapNotNull { 
                                val idRaw = it.get("idEvidencia")
                                when (idRaw) {
                                    is Number -> idRaw.toLong().toString()
                                    else -> idRaw?.toString() ?: it.id
                                }
                            }
                        
                        val calificacionesResult = mutableMapOf<String, com.google.firebase.firestore.DocumentSnapshot>()
                        if (evaluatedEvidenciaIds.isNotEmpty()) {
                            val chunks = evaluatedEvidenciaIds.chunked(30)
                            for (chunk in chunks) {
                                val califSnap = db.collection("calificaciones").whereIn("idEvidencia", chunk).get().await()
                                for (doc in califSnap.documents) {
                                    val idEvid = doc.getString("idEvidencia")
                                    if (idEvid != null) calificacionesResult[idEvid] = doc
                                }
                            }
                        }

                        // 5. Build Final TareaItems
                        idAsignacionToTarea.forEach { (idAsignacion, tarea) ->
                            val evidenciaDoc = evidenciasResult[idAsignacion]
                            if (evidenciaDoc != null) {
                                val estadoStr = evidenciaDoc.getString("estado")
                                val idEvidenciaRaw = evidenciaDoc.get("idEvidencia")
                                val idEvidencia = when (idEvidenciaRaw) {
                                    is Number -> idEvidenciaRaw.toLong().toString()
                                    else -> idEvidenciaRaw?.toString() ?: evidenciaDoc.id
                                }
                                val estadoEvidencia = when (estadoStr) {
                                    "Aprobada" -> EstadoEvidencia.Aprobada
                                    "Rechazada" -> EstadoEvidencia.Rechazada
                                    else -> EstadoEvidencia.Pendiente
                                }
                                
                                if (estadoEvidencia != EstadoEvidencia.Pendiente) {
                                    val califDoc = calificacionesResult[idEvidencia]
                                    var calificacion: Int? = null
                                    var comentario: String? = null
                                    if (califDoc != null) {
                                        val valorRaw = califDoc.get("valor")
                                        calificacion = when (valorRaw) {
                                            is Number -> valorRaw.toInt()
                                            is String -> valorRaw.toIntOrNull()
                                            else -> null
                                        }
                                        comentario = califDoc.getString("comentario")
                                    }
                                    if (calificacion == null) {
                                        val califRaw = evidenciaDoc.get("calificacion")
                                        calificacion = when (califRaw) {
                                            is Number -> califRaw.toInt()
                                            is String -> califRaw.toIntOrNull()
                                            else -> null
                                        }
                                    }
                                    paresTareasMap[idAsignacion] = TareaItem(tarea, estadoEvidencia, idAsignacion, calificacion, comentario, idEvidencia)
                                } else {
                                    paresTareasMap[idAsignacion] = TareaItem(tarea, estadoEvidencia, idAsignacion, null, null, idEvidencia)
                                }
                            } else {
                                paresTareasMap[idAsignacion] = TareaItem(tarea, EstadoEvidencia.Pendiente, idAsignacion, null, null, null)
                            }
                        }

                        val promediosMap = mutableMapOf<String, Double>()
                        val tareasPorClase = paresTareasMap.values.groupBy { it.tarea.nombreClase }
                        
                        for ((nombreClase, items) in tareasPorClase) {
                            val calificaciones = items.mapNotNull { it.calificacion }
                            if (calificaciones.isNotEmpty()) {
                                promediosMap[nombreClase] = calificaciones.average()
                            }
                        }

                        _uiState.value = HomeAlumnoState.Success(
                            clases = enrolledClassesNames.ifEmpty { clasesSet.toList().sorted() },
                            tareas = paresTareasMap.values.distinctBy { it.tarea.idTarea }.sortedBy { it.tarea.fechaLimite },
                            promedios = promediosMap
                        )

                    } catch (e: Exception) {
                        _uiState.value = HomeAlumnoState.Error(e.message ?: "Error desconocido")
                    }
                }
            }
    }

    /**
     * Metodo principal que ejecuta la operacion: startAutoSync.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private fun startAutoSync(idUsuario: String) {
        inscripcionesListener?.remove()
        tareasListener?.remove()

        inscripcionesListener = db.collection("clase_alumno")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                val clasesIds = snapshot.documents.mapNotNull { it.getString("idClase") }
                if (clasesIds.isEmpty()) {
                    enrolledClassesNames.clear()
                    val curr = _uiState.value
                    if (curr is HomeAlumnoState.Success) {
                        _uiState.value = curr.copy(clases = emptyList())
                    }
                    return@addSnapshotListener
                }

                // Cargar nombres de clases para mostrarlos en UI independientemente de si hay tareas
                viewModelScope.launch {
                    try {
                        val nombres = mutableListOf<String>()
                        for (id in clasesIds) {
                            val doc = db.collection("clases").document(id).get().await()
                            doc.getString("nombre")?.let { nombres.add(it) }
                        }
                        enrolledClassesNames = nombres.sorted().toMutableList()
                        val curr = _uiState.value
                        if (curr is HomeAlumnoState.Success) {
                            _uiState.value = curr.copy(clases = enrolledClassesNames.toList())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                tareasListener?.remove()
                // Firestore whereIn solo soporta hasta 30 elementos en una consulta.
                val clasesChunk = clasesIds.take(30)
                
                tareasListener = db.collection("tareas")
                    .whereIn("idClase", clasesChunk)
                    .addSnapshotListener { tareasSnapshot, err ->
                        if (err != null || tareasSnapshot == null) return@addSnapshotListener

                        viewModelScope.launch {
                            try {
                                val asignacionesSnapshot = db.collection("asignaciones_tarea")
                                    .whereEqualTo("idUsuario", idUsuario)
                                    .get().await()

                                val tareasAsignadasIds = asignacionesSnapshot.documents
                                    .mapNotNull { it.getString("idTarea") }.toSet()

                                for (tareaDoc in tareasSnapshot.documents) {
                                    val idTarea = tareaDoc.id
                                    if (!tareasAsignadasIds.contains(idTarea)) {
                                        // Generar la asignación faltante automáticamente
                                        val nuevaAsignacion = hashMapOf(
                                            "idUsuario" to idUsuario,
                                            "idTarea" to idTarea,
                                            "fechaAsignacion" to java.util.Date()
                                        )
                                        db.collection("asignaciones_tarea").add(nuevaAsignacion).await()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            }
    }

    /**
     * Metodo principal que ejecuta la operacion: unirseAClase.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun unirseAClase(codigoClase: String, idUsuario: String) {
        if (codigoClase.isBlank() || idUsuario.isBlank()) return

        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = HomeAlumnoState.Loading
            try {
                // 1. Normalizar y verificar si la clase existe
                val codeNormalized = codigoClase.trim().uppercase()
                val claseDoc = db.collection("clases").document(codeNormalized).get().await()

                if (!claseDoc.exists()) {
                    _uiState.value = HomeAlumnoState.Error("El código de clase no existe o es incorrecto.")
                    return@launch
                }

                // 2. Verificar si el alumno ya está inscrito
                val inscripciones = db.collection("clase_alumno")
                    .whereEqualTo("idClase", codeNormalized)
                    .whereEqualTo("idUsuario", idUsuario)
                    .get()
                    .await()

                if (!inscripciones.isEmpty) {
                    _uiState.value = HomeAlumnoState.Error("Ya estás inscrito en esta clase.")
                    return@launch
                }

                // 3. Inscribir al alumno
                val nuevaInscripcion = hashMapOf(
                    "idClase" to codeNormalized,
                    "idUsuario" to idUsuario
                )
                db.collection("clase_alumno").add(nuevaInscripcion).await()

                // 4. Buscar tareas de la clase y crear asignaciones retroactivamente
                val tareasSnapshot = db.collection("tareas")
                    .whereEqualTo("idClase", codeNormalized)
                    .get()
                    .await()

                for (tareaDoc in tareasSnapshot.documents) {
                    val idTarea = tareaDoc.id
                    val nuevaAsignacion = hashMapOf(
                        "idUsuario" to idUsuario,
                        "idTarea" to idTarea,
                        "fechaAsignacion" to Date()
                    )
                    db.collection("asignaciones_tarea").add(nuevaAsignacion).await()
                }

                // 5. Recargar la pantalla para mostrar la nueva clase y tareas
                fetchUserData(idUsuario)
                
            } catch (e: Exception) {
                _uiState.value = HomeAlumnoState.Error(e.message ?: "Error al unirse a la clase.")
            }
        }
    }
}
```

### `InicioAlumnoContent.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/alumno/InicioAlumnoContent.kt`

```kotlin
/**
 * Pestana de inicio del alumno que presenta un resumen de bienvenida, proximas entregas,
 * avisos importantes y accesos rapidos a sus clases.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.alumno

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.EstadoEvidencia
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import com.pmlp.edutask.model.Evento
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.style.TextOverflow
import com.pmlp.edutask.ui.components.EmptyStateIllustration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente interno que renderiza el contenido de InicioContent.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun InicioContent(
    modifier: Modifier = Modifier, 
    pendientes: Int, 
    tareas: List<TareaItem>,
    eventos: List<Evento> = emptyList(),
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onVerTarea: (TareaItem) -> Unit,
    onUnirseAClase: (String) -> Unit = {}
) {
    var codigoClase by remember { mutableStateOf("") }
    
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        
        if (eventos.isNotEmpty()) {
            item {
                Text("Anuncios Recientes", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(eventos, key = { it.idEvento }) { evento ->
                        EventoCarouselCard(evento)
                    }
                }
            }
        }
        
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Mi Progreso", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text(if (pendientes > 0) "Tienes $pendientes tareas pendientes." else "¡Felicidades! Estás al día con tus tareas.",
                             style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Inscribirse a una clase", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    OutlinedTextField(
                        value = codigoClase,
                        onValueChange = { codigoClase = it },
                        label = { Text("Código de clase.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (codigoClase.isNotBlank()) {
                                onUnirseAClase(codigoClase)
                                codigoClase = "" // Limpiar después de intentar
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Unirme")
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Próximas Entregas", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (pendientes > 0) Badge(containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError) { Text(pendientes.toString()) }
            }
        }
        
        val now = java.util.Date()
        val proximasTareas = tareas.filter { it.estado == EstadoEvidencia.Pendiente && it.idEvidencia == null && (it.tarea.fechaLimite == null || !now.after(it.tarea.fechaLimite)) }.take(3)
        if (proximasTareas.isEmpty()) {
            item {
                EmptyStateIllustration(
                    icon = Icons.Default.TaskAlt,
                    title = "¡Todo al día!",
                    subtitle = "Puedes relajarte, no hay tareas próximas.",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            items(proximasTareas, key = { it.idAsignacion }) { item -> TareaCard(item.tarea, item.estado, onClick = { onVerTarea(item) }) }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
    }
}

@Composable
/**
 * Componente visual reutilizable para renderizar EventoCarouselCard.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EventoCarouselCard(evento: Evento) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val fechaFormat = dateFormat.format(Date(evento.fechaPublicacion))
    var showDialog by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.width(260.dp).height(120.dp).clickable { showDialog = true },
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(evento.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(evento.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text(fechaFormat, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(evento.titulo, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Publicado: $fechaFormat",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(evento.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
```

### `TareasAlumnoContent.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/alumno/TareasAlumnoContent.kt`

```kotlin
/**
 * Pestana de tareas del alumno que organiza y filtra las tareas por estado
 * (pendientes, entregadas y calificadas) con acceso directo al detalle de cada una.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.alumno

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.ui.components.EmptyStateIllustration
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente interno que renderiza el contenido de TareasContent.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun TareasContent(modifier: Modifier, claseSelected: String?, onClaseSelected: (String) -> Unit,
                  tareas: List<TareaItem>, pendienteCount: Int, clases: List<String>,
                  isRefreshing: Boolean, onRefresh: () -> Unit,
                  onVerTarea: (TareaItem) -> Unit = {}) {
    val now = java.util.Date()
    val tareasPendientes = tareas.filter { it.estado == EstadoEvidencia.Pendiente && it.idEvidencia == null && (it.tarea.fechaLimite == null || !now.after(it.tarea.fechaLimite)) }
    val tareasVencidas = tareas.filter { it.estado == EstadoEvidencia.Pendiente && it.idEvidencia == null && (it.tarea.fechaLimite != null && now.after(it.tarea.fechaLimite)) }
    val tareasEntregadas = tareas.filter { it.estado == EstadoEvidencia.Pendiente && it.idEvidencia != null }
    val tareasEvaluadas = tareas.filter { it.estado == EstadoEvidencia.Aprobada || it.estado == EstadoEvidencia.Rechazada }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pendientes", "Entregadas", "Evaluadas")

    val currentTareas = when (selectedTabIndex) {
        0 -> tareasPendientes
        1 -> tareasEntregadas
        else -> tareasEvaluadas
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de Filtros de Clase
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text("Filtrar por Clase", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                    val allItems = listOf("Todas") + clases
                    val chunked = allItems.chunked(2)
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        chunked.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClaseGridItem(
                                            nombre = item,
                                            isSelected = if (item == "Todas") claseSelected == null else claseSelected == item,
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = {
                                                if (item == "Todas") onClaseSelected(claseSelected ?: "")
                                                else onClaseSelected(item)
                                            }
                                        )
                                    }
                                }
                                if (rowItems.size < 2) {
                                    Spacer(modifier = Modifier.weight((2 - rowItems.size).toFloat()))
                                }
                            }
                        }
                    }
                }
            }

            // Título de la lista y Tabs Kanban
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Text("Lista de Tareas", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.weight(1f))
                        if (pendienteCount > 0 && claseSelected == null) {
                            Badge(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError) { 
                                Text("$pendienteCount pendientes") 
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, title ->
                            val count = when (index) {
                                0 -> tareasPendientes.size
                                1 -> tareasEntregadas.size
                                else -> tareasEvaluadas.size
                            }
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text("$title ($count)", style = MaterialTheme.typography.titleSmall) }
                            )
                        }
                    }
                }
            }

            // Lista de Tareas o Estado Vacío
            if (currentTareas.isEmpty()) {
                item {
                    val (icon, title, subtitle) = when (selectedTabIndex) {
                        0 -> Triple(Icons.Default.TaskAlt, "Sin Tareas Pendientes", "¡Estás al día con tus entregas!")
                        1 -> Triple(Icons.Default.PendingActions, "Aún No Hay Entregas", "Cuando entregues tus tareas, aparecerán aquí.")
                        else -> Triple(Icons.Default.FactCheck, "Sin Tareas Evaluadas", "Tus calificaciones aparecerán aquí cuando el profesor revise.")
                    }
                    EmptyStateIllustration(
                        icon = icon,
                        title = title,
                        subtitle = subtitle,
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
            } else {
                items(currentTareas, key = { it.idAsignacion }) { item -> 
                    TareaCard(item.tarea, item.estado, onClick = { onVerTarea(item) }) 
                }
            }

            if (tareasVencidas.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                        Text(
                            "Tareas Fuera de Límite", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(tareasVencidas, key = { it.idAsignacion }) { item -> 
                    TareaCard(item.tarea, item.estado, isVencida = true, onClick = { onVerTarea(item) }) 
                }
            }
            
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
/**
 * Componente visual reutilizable para renderizar TareaCard.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun TareaCard(tarea: Tarea, estado: EstadoEvidencia, isVencida: Boolean = false, onClick: () -> Unit = {}) {
    val fmt = SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp, pressedElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Icono de estado
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = when (estado) {
                        EstadoEvidencia.Aprobada -> MaterialTheme.colorScheme.tertiaryContainer
                        EstadoEvidencia.Rechazada -> MaterialTheme.colorScheme.errorContainer
                        EstadoEvidencia.Pendiente -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (estado) {
                                EstadoEvidencia.Aprobada -> Icons.Default.CheckCircle
                                EstadoEvidencia.Rechazada -> Icons.Default.Cancel
                                EstadoEvidencia.Pendiente -> Icons.Default.HourglassEmpty
                            },
                            contentDescription = "Estado: ${estado.name}",
                            modifier = Modifier.size(20.dp),
                            tint = when (estado) {
                                EstadoEvidencia.Aprobada -> MaterialTheme.colorScheme.onTertiaryContainer
                                EstadoEvidencia.Rechazada -> MaterialTheme.colorScheme.onErrorContainer
                                EstadoEvidencia.Pendiente -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                }
                
                // Textos
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = tarea.titulo, 
                        style = MaterialTheme.typography.titleSmall, 
                        maxLines = 2, 
                        overflow = TextOverflow.Ellipsis,
                        color = if (isVencida) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(tarea.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            
            // Fila inferior con Chips
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = { Text(tarea.nombreClase, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.School, null, Modifier.size(16.dp)) },
                        border = null,
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(tarea.fechaLimite?.let { fmt.format(it) } ?: "Sin límite", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                EstadoChip(estado)
            }
        }
    }
}

@Composable
/**
 * Metodo principal que ejecuta la operacion: EstadoChip.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EstadoChip(estado: EstadoEvidencia) {
    val label = when (estado) { EstadoEvidencia.Pendiente -> "Pend."; EstadoEvidencia.Aprobada -> "Aprob."; EstadoEvidencia.Rechazada -> "Rec." }
    val icon  = when (estado) { EstadoEvidencia.Pendiente -> Icons.Default.Schedule; EstadoEvidencia.Aprobada -> Icons.Default.CheckCircle; EstadoEvidencia.Rechazada -> Icons.Default.Cancel }
    val container = when (estado) { EstadoEvidencia.Pendiente -> MaterialTheme.colorScheme.secondaryContainer; EstadoEvidencia.Aprobada -> MaterialTheme.colorScheme.tertiaryContainer; EstadoEvidencia.Rechazada -> MaterialTheme.colorScheme.errorContainer }
    val content   = when (estado) { EstadoEvidencia.Pendiente -> MaterialTheme.colorScheme.onSecondaryContainer; EstadoEvidencia.Aprobada -> MaterialTheme.colorScheme.onTertiaryContainer; EstadoEvidencia.Rechazada -> MaterialTheme.colorScheme.onErrorContainer }
    SuggestionChip(onClick = {}, label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                   icon = { Icon(icon, null, Modifier.size(AssistChipDefaults.IconSize)) },
                   colors = SuggestionChipDefaults.suggestionChipColors(containerColor = container, labelColor = content, iconContentColor = content),
                   border = null)
}

@Composable
/**
 * Componente visual reutilizable para renderizar ClaseGridItem.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun ClaseGridItem(nombre: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
```

### `CalificacionesAlumnoContent.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/alumno/CalificacionesAlumnoContent.kt`

```kotlin
/**
 * Componente de interfaz para la consulta de calificaciones del alumno.
 * Muestra el listado de materias, promedios acumulados y desglose de notas por tarea.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.alumno

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.ui.components.EmptyStateIllustration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.ui.Alignment
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateContentSize
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente interno que renderiza el contenido de CalificacionesContent.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun CalificacionesContent(
    modifier: Modifier = Modifier, 
    tareas: List<TareaItem>, 
    promedios: Map<String, Double>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onVerTarea: (TareaItem) -> Unit = {}
) {
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(),
                   contentPadding = PaddingValues(16.dp),
                   verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Mis Calificaciones", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
            
            val tareasEvaluadas = tareas.filter { it.estado != EstadoEvidencia.Pendiente }
            val tareasPorClase = tareasEvaluadas.groupBy { it.tarea.nombreClase }

            if (tareasPorClase.isEmpty()) {
                item {
                    EmptyStateIllustration(
                        icon = Icons.Default.FactCheck,
                        title = "Sin Calificaciones",
                        subtitle = "Aún no tienes tareas calificadas. ¡Sigue entregando tus trabajos!",
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
            } else {
                tareasPorClase.forEach { (nombreClase, tareasClase) ->
                    val promedioClase = promedios[nombreClase]
                    item(key = nombreClase) {
                        ClaseGradesAccordion(nombreClase, tareasClase, promedioClase, onVerTarea)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente visual reutilizable para renderizar ClaseGradesAccordion.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun ClaseGradesAccordion(nombreClase: String, tareas: List<TareaItem>, promedio: Double?, onVerTarea: (TareaItem) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    val promedioStr = if (promedio == null || promedio.isNaN()) "-" else String.format(Locale.getDefault(), "%.1f", promedio)
    val colorPromedio = when {
        promedio == null || promedio.isNaN() -> androidx.compose.ui.graphics.Color.Gray
        promedio >= 8.0 -> androidx.compose.ui.graphics.Color(0xFF388E3C) // Verde
        promedio >= 6.0 -> androidx.compose.ui.graphics.Color(0xFFFBC02D) // Amarillo
        else -> androidx.compose.ui.graphics.Color.Red
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { expanded = !expanded },
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = nombreClase.firstOrNull()?.toString()?.uppercase() ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = nombreClase, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "${tareas.size} tarea(s) evaluada(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$promedioStr / 10", 
                        style = MaterialTheme.typography.titleMedium,
                        color = colorPromedio,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Promedio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tareas.forEach { item ->
                        CalificacionCard(item = item, onClick = onVerTarea)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente visual reutilizable para renderizar CalificacionCard.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun CalificacionCard(item: TareaItem, onClick: (TareaItem) -> Unit) {
    val calificacionStr = item.calificacion?.toString() ?: "-"
    
    val isAprobada = item.estado == EstadoEvidencia.Aprobada
    val iconColor = if (isAprobada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = { onClick(item) },
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.tarea.titulo, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.SemiBold, 
                    maxLines = 2, 
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!item.comentario.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.comentario,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = calificacionStr,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
                Text(
                    text = "/ 10",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### `PerfilAlumnoContent.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/alumno/PerfilAlumnoContent.kt`

```kotlin
/**
 * Pestana de perfil del alumno que muestra su informacion personal, matricula, correo
 * y opciones de configuracion de cuenta / cierre de sesion.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.alumno

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Grade
import com.pmlp.edutask.model.EstadoEvidencia
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
/**
 * Componente interno que renderiza el contenido de PerfilContent.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun PerfilContent(
    modifier: Modifier = Modifier, 
    nombre: String, 
    carrera: String, 
    tareas: List<TareaItem>, 
    correo: String = "",
    onGuardarCambios: (String, String) -> Unit = { _, _ -> }
) {
    val scrollState = rememberScrollState()
    
    val tareasEntregadas = tareas.filter { it.estado != EstadoEvidencia.Pendiente }.size
    val tareasPendientes = tareas.filter { it.estado == EstadoEvidencia.Pendiente }.size
    val calificaciones = tareas.mapNotNull { it.calificacion }
    val promedio = if (calificaciones.isNotEmpty()) calificaciones.average() else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Surface(
            Modifier.size(100.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    nombre.split(" ").take(2).joinToString("") { it.first().toString().uppercase() },
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text(carrera, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Rol: Alumno", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        
        Spacer(Modifier.height(32.dp))
        
        // Statistics Section
        Text(
            text = "Mis Estadísticas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                title = "Entregadas",
                value = tareasEntregadas.toString(),
                icon = Icons.Default.TaskAlt,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pendientes",
                value = tareasPendientes.toString(),
                icon = Icons.Default.PendingActions,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        StatCard(
            title = "Promedio General",
            value = String.format(java.util.Locale.getDefault(), "%.1f / 10", promedio),
            icon = Icons.Default.Grade,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))



        // Materias Section
        Text(
            text = "Mis Materias",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        val materias = tareas.groupBy { it.tarea.nombreClase }
        if (materias.isEmpty()) {
            Text("Aún no tienes materias asignadas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                materias.forEach { (nombreClase, tareasClase) ->
                    ClaseStatAccordion(nombreClase = nombreClase, tareas = tareasClase)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Shortcuts / Contact Info
        Text(
            text = "Información y Atajos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        val clasesUnicas = tareas.map { it.tarea.nombreClase }.distinct()
        VideoConferenciasAccordion(clases = clasesUnicas)
        Spacer(Modifier.height(12.dp))
        AjustesCuentaAccordion(correoActual = correo, onGuardarCambios = onGuardarCambios)
        Spacer(Modifier.height(12.dp))
        ShortcutItem(icon = Icons.AutoMirrored.Filled.HelpOutline, title = "Soporte Técnico", subtitle = "Reportar un problema con la app")
    }
}

@Composable
/**
 * Componente visual reutilizable para renderizar StatCard.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun StatCard(title: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente visual reutilizable para renderizar ClaseStatAccordion.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun ClaseStatAccordion(nombreClase: String, tareas: List<TareaItem>) {
    var expanded by remember { mutableStateOf(false) }
    val tareasEntregadas = tareas.filter { it.estado != EstadoEvidencia.Pendiente }.size
    val tareasPendientes = tareas.filter { it.estado == EstadoEvidencia.Pendiente }.size
    val calificaciones = tareas.mapNotNull { it.calificacion }
    val calificacionPromedio = if (calificaciones.isNotEmpty()) calificaciones.average() else Double.NaN
    val promedioStr = if (calificacionPromedio.isNaN()) "-" else String.format(java.util.Locale.getDefault(), "%.1f", calificacionPromedio)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { expanded = !expanded },
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = nombreClase.firstOrNull()?.toString()?.uppercase() ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = nombreClase, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(text = "${tareas.size} tarea(s) en total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$promedioStr / 10", 
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text("Promedio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(tareasEntregadas.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Text("Entregadas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(tareasPendientes.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text("Pendientes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente visual reutilizable para renderizar AjustesCuentaAccordion.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun AjustesCuentaAccordion(
    correoActual: String = "",
    onGuardarCambios: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    var correo by remember(correoActual) { mutableStateOf(correoActual) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { expanded = !expanded },
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Ajustes de Cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(text = "Cambiar contraseña y correo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Nueva Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword
                    )
                    
                    if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text(
                            text = "Las contraseñas no coinciden",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Button(
                        onClick = {
                            onGuardarCambios(correo, password)
                            expanded = false
                            password = ""
                            confirmPassword = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        enabled = correo.isNotBlank() && (password.isEmpty() || password == confirmPassword)
                    ) {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente visual reutilizable para renderizar ShortcutItem.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun ShortcutItem(icon: ImageVector, title: String, subtitle: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = {},
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente visual reutilizable para renderizar VideoConferenciasAccordion.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun VideoConferenciasAccordion(clases: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { expanded = !expanded },
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Video Conferencias de clases", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(text = "Enlaces a tus clases virtuales", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (clases.isEmpty()) {
                        Text(
                            text = "No tienes clases asignadas por el momento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        clases.forEach { clase ->
                            // Generar un link estático basado en el nombre de la clase para demostración
                            val link = "https://meet.google.com/${clase.lowercase(java.util.Locale.getDefault()).replace(" ", "-").take(10)}"
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                onClick = { 
                                    try {
                                        uriHandler.openUri(link)
                                    } catch (e: Exception) { }
                                },
                                shape = MaterialTheme.shapes.medium,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = clase,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        Text(
                                            text = link,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### `EnviarEvidenciaScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/alumno/EnviarEvidenciaScreen.kt`

```kotlin
/**
 * Pantalla para que el alumno suba y envie la evidencia de una tarea asignada,
 * permitiendo adjuntar documentos/imagenes, redactar notas y confirmar la entrega.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.alumno

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.ui.theme.EduTaskTheme
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.provider.OpenableColumns
import com.pmlp.edutask.ui.components.VisorArchivoDialog

// ── Pantalla principal ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla EnviarEvidenciaScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EnviarEvidenciaScreen(
    tarea:        Tarea,
    idAsignacion: String,
    nombreAlumno: String,
    idEvidenciaRecibida: String? = null,
    viewModel:    EnviarEvidenciaViewModel = viewModel(),
    onBack:       () -> Unit = {}
) {
    val context  = LocalContext.current
    val uiState  by viewModel.uiState.collectAsState()
    val evidenciaEnviada by viewModel.evidenciaEnviada.collectAsState()
    val isLoadingEvidencia by viewModel.isLoadingEvidencia.collectAsState()

    val now = java.util.Date()
    val isVencida = evidenciaEnviada == null && (tarea.fechaLimite != null && now.after(tarea.fechaLimite))
    val isReadOnlyMode = evidenciaEnviada != null

    var visorData by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(idEvidenciaRecibida) {
        if (idEvidenciaRecibida != null) {
            viewModel.cargarEvidenciaEnviada(idEvidenciaRecibida)
        }
    }

    var archivosTarea by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    LaunchedEffect(tarea.idTarea) {
        if (tarea.idTarea.isNotBlank()) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("tareas").document(tarea.idTarea).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val rawArchivos = doc.get("archivos") as? List<*>
                        val list = mutableListOf<Map<String, Any>>()
                        rawArchivos?.forEach { item ->
                            if (item is Map<*, *>) {
                                val nombre = item["nombre"]?.toString() ?: ""
                                val base64 = item["base64"]?.toString() ?: ""
                                val esLink = item["esLink"] as? Boolean ?: false
                                list.add(mapOf("nombre" to nombre, "base64" to base64, "esLink" to esLink))
                            }
                        }
                        archivosTarea = list
                    }
                }
        }
    }

    // ── Estados para Múltiples Archivos y Vínculos ───────────────────────────
    var archivosSubir by remember { mutableStateOf(listOf<ArchivoSubir>()) }
    var vinculos by remember { mutableStateOf(listOf<String>()) }
    var nuevoVinculo by remember { mutableStateOf("") }
    var textoEvidencia by remember { mutableStateOf("") }

    // Rellenar estados si estamos en modo lectura
    val actualTexto = if (isReadOnlyMode) evidenciaEnviada?.textoEvidencia ?: "" else textoEvidencia
    val actualVinculos = if (isReadOnlyMode) evidenciaEnviada?.vinculos ?: emptyList() else vinculos
    val actualArchivosEnviados = if (isReadOnlyMode) evidenciaEnviada?.archivos ?: emptyList() else emptyList()
    
    // Bitmap en modo lectura (decodificado de forma asíncrona para no bloquear la UI) - LEGACY
    val base64Foto = evidenciaEnviada?.fotoBase64
    val actualNombreArchivoLegacy = evidenciaEnviada?.nombreArchivo
    val bitmapLectura by produceState<Bitmap?>(initialValue = null, key1 = base64Foto) {
        if (base64Foto != null) {
            value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                decodeBase64ToBitmap(base64Foto)
            }
        } else {
            value = null
        }
    }

    /**
     * Metodo principal que ejecuta la operacion: addArchivo.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun addArchivo(archivo: ArchivoSubir) {
        if (archivosSubir.size < 3) {
            archivosSubir = archivosSubir + archivo
        } else {
            Toast.makeText(context, "Máximo 3 archivos permitidos", Toast.LENGTH_SHORT).show()
        }
    }

    // URI temporal para escribir la foto de la cámara
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val getFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            addArchivo(ArchivoSubir(uri = uri, nombre = getFileName(context, uri) ?: "documento"))
        }
    }
    
    // ── Launcher para la galería (con fallback para compatibilidad) ─────────────
    // PickVisualMedia es el selector moderno (Android 11+). En dispositivos que
    // no lo soporten (algunos ROMs como ColorOS), usamos GetContent como respaldo.
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { 
            val bitmap = decodeUriToSafeBitmap(context, it)
            if (bitmap != null) {
                addArchivo(ArchivoSubir(bitmap = bitmap, nombre = getFileName(context, it) ?: "imagen.jpg"))
            }
        }
    }

    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            val bitmap = decodeUriToSafeBitmap(context, it)
            if (bitmap != null) {
                addArchivo(ArchivoSubir(bitmap = bitmap, nombre = getFileName(context, it) ?: "imagen.jpg"))
            }
        }
    }

    // ── Dialogo de éxito ─────────────────────────────────────────────────────
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is EnviarEvidenciaUiState.Success) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon    = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary) },
            title   = { Text("¡Evidencia Enviada!", style = MaterialTheme.typography.headlineSmall) },
            text    = {
                Text(
                    "Tu evidencia ha sido enviada correctamente y está en revisión por el profesor.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    viewModel.resetState()
                    onBack()
                }) { Text("Aceptar") }
            }
        )
    }

    // ── Launcher para la cámara ──────────────────────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            fotoUri?.let { uri ->
                val bitmap = decodeUriToSafeBitmap(context, uri)
                if (bitmap != null) {
                    addArchivo(ArchivoSubir(bitmap = bitmap, nombre = "foto_camara.jpg"))
                }
            }
        }
    }


    // ── Launchers para Permisos Nativos ──────────────────────────────────────
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = crearUriParaFoto(context)
            fotoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // En Android 12 (API 32) y anterior se requiere READ_EXTERNAL_STORAGE
    // En Android 13+ (API 33) el Photo Picker no necesita ningún permiso
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getContentLauncher.launch("image/*")
        }
    }

    /**
     * Abre el recurso o vista abrirGaleria para la interaccion del usuario.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun abrirGaleria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: PickVisualMedia no necesita permisos, lanzar directo
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
            // Android 11-12 con Photo Picker disponible (via Play Services)
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            // Fallback: Android 12 sin Photo Picker — necesita permiso READ_EXTERNAL_STORAGE
            val permiso = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED) {
                getContentLauncher.launch("image/*")
            } else {
                storagePermissionLauncher.launch(permiso)
            }
        }
    }

    // ── Snackbars de UI ────────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is EnviarEvidenciaUiState.Error) {
            snackbarHostState.showSnackbar(
                message     = (uiState as EnviarEvidenciaUiState.Error).mensaje,
                actionLabel = "OK"
            )
            viewModel.resetState()
        }
    }

    // ── Scaffold principal ───────────────────────────────────────────────────
    Scaffold(
        containerColor  = MaterialTheme.colorScheme.background,
        snackbarHost    = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                title = {
                    Column {
                        Text(
                            "Entregar Evidencia",
                            style    = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            tarea.nombreClase,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // ── Botones de acción ─────────────────────────────────────────
            Surface(
                color       = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isReadOnlyMode) {
                        if (evidenciaEnviada?.estado == "Pendiente") {
                            OutlinedButton(
                                onClick = { 
                                    evidenciaEnviada?.idEvidencia?.let {
                                        viewModel.anularEvidencia(it) { onBack() }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(12.dp),
                                border   = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Anular Entrega")
                            }
                        } else {
                            // Está evaluada
                            Button(
                                onClick = { onBack() },
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(12.dp)
                            ) {
                                Text("Regresar")
                            }
                        }
                    } else {
                        if (!isVencida) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Botón Tomar Foto
                                OutlinedButton(
                                    onClick = {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                            val uri = crearUriParaFoto(context)
                                            fotoUri = uri
                                            cameraLauncher.launch(uri)
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape    = RoundedCornerShape(12.dp),
                                    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Cámara")
                                }

                                // Botón Subir Galería (verifica permisos según versión Android)
                                OutlinedButton(
                                    onClick = { abrirGaleria() },
                                    modifier = Modifier.weight(1f),
                                    shape    = RoundedCornerShape(12.dp),
                                    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Galería")
                                }
                            }

                            // Botón Archivo (nuevo)
                            OutlinedButton(
                                onClick = { getFileLauncher.launch(arrayOf("*/*")) },
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(12.dp),
                                border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Adjuntar Documento")
                            }
                        }

                        // Botón Enviar Evidencia
                        Button(
                            onClick = {
                                viewModel.enviarEvidencia(
                                    context = context,
                                    idAsignacion = idAsignacion,
                                    nombreAlumno = nombreAlumno,
                                    tituloTarea  = tarea.titulo,
                                    archivosSubir = if (isVencida) emptyList() else archivosSubir,
                                    vinculos = if (isVencida) emptyList() else vinculos,
                                    textoEvidencia = textoEvidencia
                                )
                            },
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(12.dp),
                            enabled   = (archivosSubir.isNotEmpty() || vinculos.isNotEmpty() || textoEvidencia.isNotBlank()) && uiState !is EnviarEvidenciaUiState.Uploading,
                            colors    = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (uiState is EnviarEvidenciaUiState.Uploading) {
                                CircularProgressIndicator(
                                    modifier  = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color       = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Enviando…", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Enviar Evidencia", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    } // Fin de if (!isReadOnlyMode)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Card de detalle de la tarea ───────────────────────────────
                ElevatedCard(
                    modifier  = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    colors    = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Encabezado
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape    = MaterialTheme.shapes.medium,
                                color    = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Assignment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    tarea.titulo,
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines   = 2,
                                    overflow   = TextOverflow.Ellipsis
                                )
                                Text(
                                    tarea.nombreClase,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Descripción
                        if (tarea.descripcion.isNotBlank()) {
                            Text(
                                tarea.descripcion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Material complementario de la tarea
                        if (archivosTarea.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                "Material Adjunto del Profesor:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                            archivosTarea.forEach { archivo ->
                                val nombre = archivo["nombre"] as? String ?: "archivo"
                                val base64 = archivo["base64"] as? String ?: ""
                                val esLink = archivo["esLink"] as? Boolean ?: false
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (esLink || base64.startsWith("http")) {
                                        IconButton(
                                            onClick = { try { uriHandler.openUri(base64) } catch(e: Exception) {} }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = "Abrir enlace",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = { visorData = base64 to nombre }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = "Ver material",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Fecha límite
                        val fmt = SimpleDateFormat("dd 'de' MMMM 'a las' HH:mm", Locale("es", "MX"))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint     = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "Fecha límite: ${tarea.fechaLimite?.let { fmt.format(it) } ?: "Sin fecha"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        // Alumno
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                nombreAlumno,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                val currentEvidencia = evidenciaEnviada
                if (isReadOnlyMode && currentEvidencia?.comentarioProfesor?.isNotBlank() == true) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Feedback, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                Text("Comentario del profesor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                            Text(currentEvidencia.comentarioProfesor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            if (currentEvidencia.calificacionProfesor != null) {
                                Text("Calificación: ${currentEvidencia.calificacionProfesor}/10", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                    }
                }

                if (isVencida) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Esta tarea está vencida. Solo puedes entregar un comentario.", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ── Zona de texto de evidencia ─────────────────────────────────────
                OutlinedTextField(
                    value = actualTexto,
                    onValueChange = { if (!isReadOnlyMode) textoEvidencia = it },
                    label = { Text("Texto o Enlace (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5,
                    readOnly = isReadOnlyMode
                )

                // ── Zona de Vínculos ───────────────────────────────────────────────────────
                if (!isReadOnlyMode && !isVencida) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nuevoVinculo,
                            onValueChange = { nuevoVinculo = it },
                            label = { Text("Añadir un enlace") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (nuevoVinculo.isNotBlank()) {
                                    vinculos = vinculos + nuevoVinculo
                                    nuevoVinculo = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Añadir")
                        }
                    }
                }

                if (actualVinculos.isNotEmpty()) {
                    Text(
                        "Enlaces añadidos:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    actualVinculos.forEach { link ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val url = if (!link.startsWith("http://") && !link.startsWith("https://")) {
                                        "https://$link"
                                    } else link
                                    try {
                                        uriHandler.openUri(url)
                                    } catch (e: Exception) {
                                        // Ignore or show toast if needed
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                link, 
                                style = MaterialTheme.typography.bodyMedium, 
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        }
                    }
                }

                // ── Zona de Archivos ───────────────────────────────────────────────────────
                if (!isVencida || isReadOnlyMode) {
                    Text(
                        "Archivos Adjuntos (${if (isReadOnlyMode) actualArchivosEnviados.size + (if(base64Foto!=null) 1 else 0) else archivosSubir.size}/3)",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Mostrar archivos en modo escritura
                if (!isReadOnlyMode && archivosSubir.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        archivosSubir.forEachIndexed { index, archivo ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (archivo.bitmap != null) Icons.Default.Image else Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(archivo.nombre, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(200.dp))
                                    }
                                    IconButton(onClick = { 
                                        val mList = archivosSubir.toMutableList()
                                        mList.removeAt(index)
                                        archivosSubir = mList
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // Mostrar archivos en modo lectura
                if (isReadOnlyMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Archivos guardados (Base64)
                        actualArchivosEnviados.forEach { archivoMap ->
                            val base64Data = archivoMap["base64"] ?: ""
                            val nombre = archivoMap["nombre"] ?: "Archivo"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(nombre, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(200.dp))
                                    }
                                    IconButton(onClick = { 
                                        if (base64Data.isNotBlank()) {
                                            visorData = Pair(base64Data, nombre)
                                        }
                                    }) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = "Abrir", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        // Archivo Legacy Base64
                        if (base64Foto != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(actualNombreArchivoLegacy ?: "Archivo antiguo", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(200.dp))
                                    }
                                    IconButton(onClick = { 
                                        visorData = Pair(base64Foto, actualNombreArchivoLegacy ?: "Archivo antiguo") 
                                    }) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = "Abrir", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            
                            // Vista previa de imagen si es bitmap
                            if (bitmapLectura != null) {
                                Image(
                                    bitmap = bitmapLectura!!.asImageBitmap(),
                                    contentDescription = "Foto de evidencia",
                                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // ── Nota informativa ──────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp),
                            tint     = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            if (isReadOnlyMode) {
                                if (evidenciaEnviada?.estado == "Pendiente") "Esta evidencia ha sido enviada y está pendiente de revisión."
                                else "Esta evidencia ya fue evaluada: ${evidenciaEnviada?.estado}."
                            } else {
                                "Asegúrate de que los archivos o el enlace " +
                                "sean correctos antes de enviar."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Espacio extra para el bottom bar
                Spacer(Modifier.height(8.dp))
            }

            // Overlay Loading 
            if (isLoadingEvidencia) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (uiState is EnviarEvidenciaUiState.Uploading) {
                val progress = (uiState as EnviarEvidenciaUiState.Uploading).progress
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                            .padding(24.dp)
                    ) {
                        if (progress > 0f) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Subiendo evidencia... ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    visorData?.let { (base64, nombre) ->
        VisorArchivoDialog(
            base64String = base64,
            nombreArchivo = nombre,
            onDismissRequest = { visorData = null }
        )
    }
}

// ── Helper: crear URI segura para FileProvider ───────────────────────────────
/**
 * Metodo principal que ejecuta la operacion: crearUriParaFoto.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun crearUriParaFoto(context: Context): Uri {
    // Usar estrictamente caché interna para evitar el error de "Tarjeta SD" en emuladores
    val cacheDir = File(context.cacheDir, "camera").also { it.mkdirs() }
    val archivo  = File.createTempFile("evidencia_", ".jpg", cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )
}

// ── Helper: Decodificar Bitmap previniendo OutOfMemoryError (Crashes) ──────────
/**
 * Realiza el procesamiento y conversion de archivos (decodeUriToSafeBitmap).
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun decodeUriToSafeBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        // 1. Decodificar solo los límites para obtener las dimensiones reales
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        
        // 2. Calcular inSampleSize para reducir la imagen (ej: a max 1024x1024)
        val reqWidth = 1024
        val reqHeight = 1024
        var inSampleSize = 1
        
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            val halfHeight: Int = options.outHeight / 2
            val halfWidth: Int = options.outWidth / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        // 3. Decodificar la imagen real escalada
        val finalOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, finalOptions)
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

// ── Helper: Obtener nombre de archivo de la URI ──────────────────────────────
/**
 * Obtiene o recupera datos asociados a getFileName desde la base de datos o API.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "archivo"
}

// ── Helper: Decodificar Base64 a Bitmap ──────────────────────────────────────
/**
 * Realiza el procesamiento y conversion de archivos (decodeBase64ToBitmap).
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val bytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}

/**
 * Abre el recurso o vista abrirArchivoBase64 para la interaccion del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun abrirArchivoBase64(context: Context, base64Str: String, fileName: String?) {
    try {
        val cleanString = if (base64Str.contains(",")) {
            base64Str.substring(base64Str.indexOf(",") + 1)
        } else {
            base64Str
        }
        val bytes = android.util.Base64.decode(cleanString, android.util.Base64.DEFAULT)
        val safeFileName = fileName ?: "documento.pdf"
        val file = File(context.cacheDir, safeFileName)
        FileOutputStream(file).use { it.write(bytes) }
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Abrir archivo con"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al abrir el archivo", Toast.LENGTH_SHORT).show()
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────
@Preview(name = "Enviar Evidencia", showBackground = true, showSystemUi = true,
         widthDp = 360, heightDp = 800)
@Composable
/**
 * Metodo principal que ejecuta la operacion: PreviewEnviarEvidencia.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun PreviewEnviarEvidencia() {
    EduTaskTheme(darkTheme = false, dynamicColor = false) {
        EnviarEvidenciaScreen(
            tarea = Tarea(
                idTarea     = "1",
                titulo      = "Actividad 3 — Diagramas UML",
                descripcion = "Fotografía los diagramas de clases y de secuencia de tu libreta.",
                fechaLimite = Date(),
                idClase     = "c1",
                nombreClase = "Programación Móvil PMLP"
            ),
            idAsignacion = "asig_001",
            nombreAlumno = "Juan Ramírez"
        )
    }
}
```

### `EnviarEvidenciaViewModel.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/alumno/EnviarEvidenciaViewModel.kt`

```kotlin
/**
 * ViewModel encargado de gestionar la logica de subida y registro de evidencias en Firestore,
 * controlando los estados de carga, progreso y manejo de errores.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.alumno

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

// ── Estados de la UI ────────────────────────────────────────────────────────
sealed class EnviarEvidenciaUiState {
    object Idle      : EnviarEvidenciaUiState()
    data class Uploading(val progress: Float) : EnviarEvidenciaUiState()
    object Success   : EnviarEvidenciaUiState()
    data class Error(val mensaje: String) : EnviarEvidenciaUiState()
}

data class EvidenciaEnviadaData(
    val idEvidencia: String,
    val estado: String,
    val nombreArchivo: String?,
    val textoEvidencia: String?,
    val fotoBase64: String?,
    val archivos: List<Map<String, String>>,
    val vinculos: List<String>,
    val comentarioProfesor: String? = null,
    val calificacionProfesor: Int? = null
)

data class ArchivoSubir(
    val uri: Uri? = null,
    val bitmap: Bitmap? = null,
    val nombre: String
)

class EnviarEvidenciaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EnviarEvidenciaUiState>(EnviarEvidenciaUiState.Idle)
    val uiState: StateFlow<EnviarEvidenciaUiState> = _uiState.asStateFlow()

    private val _evidenciaEnviada = MutableStateFlow<EvidenciaEnviadaData?>(null)
    val evidenciaEnviada: StateFlow<EvidenciaEnviadaData?> = _evidenciaEnviada.asStateFlow()

    private val _isLoadingEvidencia = MutableStateFlow(false)
    val isLoadingEvidencia: StateFlow<Boolean> = _isLoadingEvidencia.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    /**
     * Metodo principal que ejecuta la operacion: cargarEvidenciaEnviada.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun cargarEvidenciaEnviada(idEvidencia: String) {
        if (idEvidencia.isBlank()) return
        viewModelScope.launch {
            _isLoadingEvidencia.value = true
            try {
                val doc = db.collection("evidencias_tarea").document(idEvidencia).get().await()
                if (doc.exists()) {
                    val estado = doc.getString("estado") ?: "Pendiente"
                    var comentarioProfesor: String? = null
                    var calificacionProfesor: Int? = null

                    if (estado != "Pendiente") {
                        val califSnapshot = db.collection("calificaciones")
                            .whereEqualTo("idEvidencia", idEvidencia)
                            .get().await()
                        if (!califSnapshot.isEmpty) {
                            val califDoc = califSnapshot.documents[0]
                            comentarioProfesor = califDoc.getString("comentario")
                            calificacionProfesor = califDoc.getLong("valor")?.toInt()
                        }
                    }

                    _evidenciaEnviada.value = EvidenciaEnviadaData(
                        idEvidencia = doc.id,
                        estado = estado,
                        nombreArchivo = doc.getString("nombreArchivo"),
                        textoEvidencia = doc.getString("textoEvidencia"),
                        fotoBase64 = doc.getString("fotoBase64"),
                        archivos = (doc.get("archivos") as? List<Map<String, String>>) ?: emptyList(),
                        vinculos = (doc.get("vinculos") as? List<String>) ?: emptyList(),
                        comentarioProfesor = comentarioProfesor,
                        calificacionProfesor = calificacionProfesor
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingEvidencia.value = false
            }
        }
    }

    /**
     * Metodo principal que ejecuta la operacion: anularEvidencia.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun anularEvidencia(idEvidencia: String, onSuccess: () -> Unit) {
        if (idEvidencia.isBlank()) return
        viewModelScope.launch {
            _isLoadingEvidencia.value = true
            try {
                db.collection("evidencias_tarea").document(idEvidencia).delete().await()
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = EnviarEvidenciaUiState.Error("Error al anular la entrega: ${e.message}")
            } finally {
                _isLoadingEvidencia.value = false
            }
        }
    }

    // ── Enviar evidencia ─────────────────────────────────────────────────────
    /**
     * Metodo principal que ejecuta la operacion: enviarEvidencia.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun enviarEvidencia(
        context: android.content.Context,
        idAsignacion: String,
        nombreAlumno: String,
        tituloTarea:  String,
        archivosSubir: List<ArchivoSubir>,
        vinculos: List<String>,
        textoEvidencia: String
    ) {
        if (idAsignacion.isBlank()) {
            _uiState.value = EnviarEvidenciaUiState.Error("ID de asignación inválido.")
            return
        }

        if (textoEvidencia.isBlank() && archivosSubir.isEmpty() && vinculos.isEmpty()) {
            _uiState.value = EnviarEvidenciaUiState.Error("Debes enviar un texto, enlace o un archivo adjunto.")
            return
        }

        if (archivosSubir.size > 3) {
            _uiState.value = EnviarEvidenciaUiState.Error("Máximo 3 archivos permitidos.")
            return
        }

        viewModelScope.launch {
            _uiState.value = EnviarEvidenciaUiState.Uploading(0f)
            try {
                val subidos = mutableListOf<Map<String, String>>()
                val totalSteps = archivosSubir.size + 1
                var currentStep = 0

                // Convertir cada archivo a Base64
                for (archivo in archivosSubir) {
                    _uiState.value = EnviarEvidenciaUiState.Uploading(currentStep.toFloat() / totalSteps)
                    val base64String = withContext(Dispatchers.IO) {
                        try {
                            if (archivo.bitmap != null) {
                                val baos = ByteArrayOutputStream()
                                archivo.bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                                val bytes = baos.toByteArray()
                                Base64.encodeToString(bytes, Base64.NO_WRAP)
                            } else if (archivo.uri != null) {
                                context.contentResolver.openInputStream(archivo.uri)?.use { input ->
                                    val bytes = input.readBytes()
                                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                                } ?: ""
                            } else {
                                ""
                            }
                        } catch (e: Exception) {
                            ""
                        }
                    }
                    if (base64String.isNotBlank()) {
                        subidos.add(mapOf("nombre" to archivo.nombre, "base64" to base64String))
                    }
                    currentStep++
                }

                _uiState.value = EnviarEvidenciaUiState.Uploading(currentStep.toFloat() / totalSteps)

                // Guardar en Firestore colección "evidencias_tarea"
                val evidenciaData = hashMapOf<String, Any>(
                    "idAsignacion" to idAsignacion,
                    "tituloTarea"  to tituloTarea,
                    "nombreAlumno" to nombreAlumno,
                    "fechaEnvio"   to FieldValue.serverTimestamp(),
                    "estado"       to "Pendiente",
                    "archivos"     to subidos,
                    "vinculos"     to vinculos
                )

                if (textoEvidencia.isNotBlank()) {
                    evidenciaData["textoEvidencia"] = textoEvidencia
                }

                db.collection("evidencias_tarea")
                    .add(evidenciaData)
                    .await()

                _uiState.value = EnviarEvidenciaUiState.Success

            } catch (e: Exception) {
                _uiState.value = EnviarEvidenciaUiState.Error(
                    e.message ?: "Error desconocido al subir la evidencia."
                )
            }
        }
    }

    // ── Resetear estado (para re-intentar o navegar atrás) ──────────────────
    /**
     * Metodo principal que ejecuta la operacion: resetState.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun resetState() {
        _uiState.value = EnviarEvidenciaUiState.Idle
    }

    // ── Helper privado: Bitmap → Base64 String (máx. 800×800, 60% JPEG) ──────
    /**
     * Metodo principal que ejecuta la operacion: bitmapToBase64.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val scaled = if (bitmap.width > 800 || bitmap.height > 800) {
            val ratio = minOf(800f / bitmap.width, 800f / bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else bitmap

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        if (scaled != bitmap) scaled.recycle() 
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    // ── Helper privado: ByteArray → Base64 String ────────────────────────────
    /**
     * Metodo principal que ejecuta la operacion: bytesToBase64.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private fun bytesToBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
```

---

## 7. Módulo del Profesor

### `HomeProfesorScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/profesor/HomeProfesorScreen.kt`

```kotlin
/**
 * Pantalla principal del rol Profesor que administra las clases activas, tareas asignadas,
 * revisiones pendientes y sincronizacion con dispositivos Wear OS.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.profesor

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.pmlp.edutask.ui.components.ShimmerPlaceholder
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.EvidenciaTarea
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.model.ClaseInfo
import com.pmlp.edutask.ui.theme.EduTaskTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.edutask.ui.EventosUiState
import com.pmlp.edutask.model.Evento
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.text.input.PasswordVisualTransformation
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import com.pmlp.edutask.utils.getSafeDate

private val EVIDENCIAS = listOf(
    EvidenciaTarea("1", "Evidencia Act. 3 PMLP", "", Date(System.currentTimeMillis() - 2 * 3600000),  EstadoEvidencia.Pendiente, "1", "Juan Ramirez"),
    EvidenciaTarea("2", "Diagrama ER BD",         "", Date(System.currentTimeMillis() - 5 * 3600000),  EstadoEvidencia.Pendiente, "2", "Maria Lopez"),
    EvidenciaTarea("3", "Casos de Uso IS",        "", Date(System.currentTimeMillis() - 10 * 3600000), EstadoEvidencia.Pendiente, "3", "Carlos Torres"),
    EvidenciaTarea("4", "App mockup PMLP",        "", Date(System.currentTimeMillis() - 24 * 3600000), EstadoEvidencia.Aprobada,  "4", "Ana Garcia")
)

private data class AccesoRapido(val label: String, val icon: ImageVector)
private val ACCESOS = listOf(
    AccesoRapido("Mis Clases",     Icons.Default.School),
    AccesoRapido("Calificaciones", Icons.Default.Grade),
    AccesoRapido("Estadísticas",   Icons.Default.BarChart),
    AccesoRapido("Configuración",  Icons.Default.Settings)
)

private data class ProfNavItem(val label: String, val icon: ImageVector)
private val PROF_NAV = listOf(
    ProfNavItem("Inicio",  Icons.Default.Home),
    ProfNavItem("Tareas",  Icons.Default.Assignment),
    ProfNavItem("Clases",  Icons.Default.School),
    ProfNavItem("Perfil",  Icons.Default.Person)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla HomeProfesorScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun HomeProfesorScreen(
    idUsuario: String        = "",
    nombreProfesor: String   = "Mtro. Perez",
    claseActual: String      = "Programacion Movil PMLP",
    eventosViewModel: EventosSharedViewModel = viewModel(),
    onCrearTarea: (String, String?) -> Unit = { _, _ -> },
    onVerEvidencia: (String) -> Unit = {},
    onVerAlumnos: (String, String) -> Unit = { _, _ -> },
    onVerEstadisticas: (String, String) -> Unit = { _, _ -> },
    onLogout: () -> Unit     = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val winSize   = calculateWindowSizeClass(activity = context as Activity)
    val isCompact = winSize.widthSizeClass == WindowWidthSizeClass.Compact

    var selectedNav   by remember { mutableIntStateOf(0) }
    var showClassDialog by remember { mutableStateOf(false) }
    var nuevaClaseNombre by remember { mutableStateOf("") }
    var nuevaClaseDesc by remember { mutableStateOf("") }
    var nuevaClaseEnlace by remember { mutableStateOf("") }
    var editingClaseId by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    var inscripcionesMap by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }

    // Estado para manejar las evidencias reales traídas de Firebase
    var listaEvidencias by remember { mutableStateOf<List<EvidenciaTarea>>(emptyList()) }
    val db = remember { FirebaseFirestore.getInstance() }
    
    var showDeleteTareaDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteClaseDialog by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    var correoProfesor by remember { mutableStateOf("") }
    LaunchedEffect(idUsuario) {
        if (idUsuario.isNotBlank()) {
            db.collection("usuarios").document(idUsuario).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        correoProfesor = doc.getString("correo") ?: ""
                    }
                }
        }
    }

    // Real-time listener for classes of this professor
    var listaClases by remember { mutableStateOf<List<ClaseInfo>>(emptyList()) }
    DisposableEffect(idUsuario) {
        if (idUsuario.isBlank()) return@DisposableEffect onDispose {}
        val listener = db.collection("clases")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                listaClases = snapshot.documents.map { doc ->
                    ClaseInfo(
                        idClase = doc.id,
                        nombre = doc.getString("nombre") ?: "Sin Nombre",
                        descripcion = doc.getString("descripcion") ?: "Sin Descripción",
                        enlace = doc.getString("enlace") ?: ""
                    )
                }
            }
        onDispose { listener.remove() }
    }

    // Real-time listener for enrolled students
    DisposableEffect(listaClases) {
        if (listaClases.isEmpty()) return@DisposableEffect onDispose {}
        val clasesIds = listaClases.map { it.idClase }
        val listener = db.collection("clase_alumno")
            .whereIn("idClase", clasesIds)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    val tempMap = mutableMapOf<String, MutableList<String>>()
                    snapshot.documents.forEach { doc ->
                        val idClase = doc.getString("idClase") ?: return@forEach
                        val idAlumno = doc.getString("idUsuario") ?: return@forEach
                        
                        val userDoc = db.collection("usuarios").document(idAlumno).get().await()
                        val name = userDoc.getString("nombre") ?: "Alumno Sin Nombre"
                        
                        val list = tempMap.getOrPut(idClase) { mutableListOf() }
                        if (!list.contains(name)) {
                            list.add(name)
                        }
                    }
                    inscripcionesMap = tempMap
                }
            }
        onDispose { listener.remove() }
    }

    var listaTareas by remember { mutableStateOf<List<Tarea>>(emptyList()) }
    // Real-time listener for tasks of this professor's classes
    DisposableEffect(listaClases) {
        if (listaClases.isEmpty()) return@DisposableEffect onDispose {}
        val clasesIds = listaClases.map { it.idClase }
        val listener = db.collection("tareas")
            .whereIn("idClase", clasesIds)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                listaTareas = snapshot.documents.map { doc ->
                    val date = doc.getSafeDate("fechaLimite") ?: Date()
                    Tarea(
                        idTarea = doc.id,
                        titulo = doc.getString("titulo") ?: "Sin Título",
                        descripcion = doc.getString("descripcion") ?: "",
                        fechaLimite = date,
                        idClase = doc.getString("idClase") ?: "",
                        nombreClase = doc.getString("nombreClase") ?: ""
                    )
                }
            }
        onDispose { listener.remove() }
    }

    val tareasIds = remember(listaTareas) { listaTareas.map { it.idTarea } }
    var asignacionesIdsDelProfesor by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Listener for assignments related to this professor's tasks
    DisposableEffect(tareasIds) {
        if (tareasIds.isEmpty()) {
            asignacionesIdsDelProfesor = emptySet()
            onDispose {}
        } else {
            val listener = db.collection("asignaciones_tarea")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val matchingIds = snapshot.documents
                        .filter { doc ->
                            val idTarea = doc.getString("idTarea") ?: ""
                            idTarea in tareasIds
                        }
                        .map { it.id }
                        .toSet()
                    asignacionesIdsDelProfesor = matchingIds
                }
            onDispose { listener.remove() }
        }
    }

    // Listener for evidence submissions, filtered in memory by professor assignment IDs
    DisposableEffect(asignacionesIdsDelProfesor) {
        if (asignacionesIdsDelProfesor.isEmpty()) {
            listaEvidencias = emptyList()
            onDispose {}
        } else {
            val listener = db.collection("evidencias_tarea")
                .orderBy("fechaEnvio", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val evidenciasMapeadas = snapshot.documents
                        .filter { doc ->
                            val idAsignacion = doc.getString("idAsignacion") ?: ""
                            idAsignacion in asignacionesIdsDelProfesor
                        }
                        .map { doc ->
                            val estadoStr = doc.getString("estado") ?: "Pendiente"
                            val estadoEnum = when (estadoStr.lowercase()) {
                                "aprobada" -> EstadoEvidencia.Aprobada
                                "rechazada" -> EstadoEvidencia.Rechazada
                                else -> EstadoEvidencia.Pendiente
                            }

                            val idEvidenciaRaw = doc.get("idEvidencia")
                            val idEvidenciaStr = when (idEvidenciaRaw) {
                                is Number -> idEvidenciaRaw.toLong().toString()
                                else -> idEvidenciaRaw?.toString() ?: doc.id
                            }
                            val idAsignacionRaw = doc.get("idAsignacion")
                            val idAsignacionStr = when (idAsignacionRaw) {
                                is Number -> idAsignacionRaw.toLong().toString()
                                else -> idAsignacionRaw?.toString() ?: ""
                            }
                            EvidenciaTarea(
                                idEvidencia = idEvidenciaStr,
                                tituloTarea = doc.getString("tituloTarea") ?: "Sin Título",
                                fotoBase64 = doc.getString("fotoBase64") ?: doc.getString("fotoUrl") ?: "",
                                fechaEnvio = doc.getSafeDate("fechaEnvio") ?: Date(),
                                estado = estadoEnum,
                                idAsignacion = idAsignacionStr,
                                nombreAlumno = doc.getString("nombreAlumno") ?: "Alumno Anónimo"
                            )
                        }
                    listaEvidencias = evidenciasMapeadas
                }

            onDispose { listener.remove() }
        }
    }

    val pendientes = listaEvidencias.count { it.estado == EstadoEvidencia.Pendiente }
    val initials   = nombreProfesor.split(" ").filter { it.length > 2 }.take(2)
        .joinToString("") { it.first().toString().uppercase() }.ifBlank { "P" }

    val eventosState by eventosViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        eventosViewModel.fetchEventos()
    }

    LaunchedEffect(idUsuario, nombreProfesor) {
        if (idUsuario.isNotBlank()) {
            // 1. Sincronización local vía Wearable API
            try {
                val dataClient = com.google.android.gms.wearable.Wearable.getDataClient(context)
                val putDataReq = com.google.android.gms.wearable.PutDataMapRequest.create("/usuario_logueado").run {
                    dataMap.putString("idUsuario", idUsuario)
                    dataMap.putString("nombre", nombreProfesor)
                    asPutDataRequest()
                }
                dataClient.putDataItem(putDataReq)
                    .addOnSuccessListener { android.util.Log.d("WearSync", "Session synced from Home: $idUsuario") }
                    .addOnFailureListener { android.util.Log.e("WearSync", "Failed to sync session from Home", it) }
            } catch (e: java.lang.Exception) {
                android.util.Log.e("WearSync", "Wearable API error on Home", e)
            }

            // 2. Sincronización en la nube vía Firestore (Fallback)
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("sesion_wear").document("default")
                    .set(hashMapOf(
                        "idUsuario" to idUsuario,
                        "nombre" to nombreProfesor,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    ))
                    .addOnSuccessListener { android.util.Log.d("WearSync", "Session synced to Firestore fallback: $idUsuario") }
            } catch (e: java.lang.Exception) {
                android.util.Log.e("WearSync", "Firestore fallback error on Home", e)
            }
        }
    }

    // Diálogo para Crear Clase Nueva
    if (showClassDialog) {
        AlertDialog(
            onDismissRequest = { 
                showClassDialog = false 
                nuevaClaseNombre = ""
                nuevaClaseDesc = ""
                nuevaClaseEnlace = ""
                editingClaseId = null
            },
            icon    = { Icon(Icons.Default.Class, null) },
            title   = { Text(if (editingClaseId == null) "Crear Nueva Clase" else "Editar Clase", style = MaterialTheme.typography.headlineSmall) },
            text    = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa los detalles de la asignatura académica.", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = nuevaClaseNombre,
                        onValueChange = { nuevaClaseNombre = it },
                        label = { Text("Nombre de la Clase") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevaClaseDesc,
                        onValueChange = { nuevaClaseDesc = it },
                        label = { Text("Descripción de la Clase") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevaClaseEnlace,
                        onValueChange = { nuevaClaseEnlace = it },
                        label = { Text("Enlace de videoconferencia") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevaClaseNombre.isNotBlank()) {
                            val nuevaClase = hashMapOf(
                                "nombre" to nuevaClaseNombre.trim(),
                                "descripcion" to nuevaClaseDesc.trim(),
                                "enlace" to nuevaClaseEnlace.trim(),
                                "idUsuario" to idUsuario
                            )
                            val classId = editingClaseId ?: generateShortCode()
                            db.collection("clases").document(classId).set(nuevaClase)
                            
                            showClassDialog = false
                            nuevaClaseNombre = ""
                            nuevaClaseDesc = ""
                            nuevaClaseEnlace = ""
                            editingClaseId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { 
                    showClassDialog = false 
                    nuevaClaseNombre = ""
                    nuevaClaseDesc = ""
                    nuevaClaseEnlace = ""
                    editingClaseId = null
                }) {
                    Text("Cancelar", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }



    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor   = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Surface(Modifier.padding(start = 12.dp, end = 12.dp).size(40.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.secondaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(initials, style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                },
                title = {
                    Column {
                        Text("Hola, ${nombreProfesor.substringBefore(" ")}!", style = MaterialTheme.typography.titleMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if(selectedNav == 2) "Gestión Académica" else claseActual, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            if (selectedNav == 1) {
                ExtendedFloatingActionButton(
                    onClick = { onCrearTarea(idUsuario, null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.AddTask, null) },
                    text = { Text("Nueva Tarea") }
                )
            } else if (selectedNav == 2) {
                ExtendedFloatingActionButton(
                    onClick = { showClassDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.AddHomeWork, null) },
                    text = { Text("Nueva Clase") }
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor   = MaterialTheme.colorScheme.onSurfaceVariant) {
                PROF_NAV.forEachIndexed { i, item ->
                    NavigationBarItem(selected = selectedNav == i, onClick = { selectedNav = i },
                        icon = {
                            if (i == 0 && pendientes > 0)
                                BadgedBox(badge = { Badge { Text(pendientes.toString()) } }) { Icon(item.icon, null) }
                            else Icon(item.icon, null)
                        },
                        label = { Text(item.label, style = MaterialTheme.typography.labelMedium) })
                }
            }
        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            val eventos = if (eventosState is EventosUiState.Success) {
                (eventosState as EventosUiState.Success).eventos
            } else emptyList()

            @OptIn(ExperimentalMaterial3Api::class)
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    scope.launch {
                        kotlinx.coroutines.delay(800)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                if (isRefreshing) {
                    when (selectedNav) {
                        0 -> InicioSkeleton()
                        1 -> TareasSkeleton()
                        2 -> ClasesSkeleton()
                        3 -> PerfilContent(
                            nombre = nombreProfesor,
                            clasesCount = listaClases.size,
                            tareasCount = listaTareas.size,
                            evaluacionesCount = listaEvidencias.count { it.estado != EstadoEvidencia.Pendiente },
                            correo = correoProfesor,
                            onGuardarCambios = { nuevoCorreo, nuevaContrasena ->
                                val updates = mutableMapOf<String, Any>("correo" to nuevoCorreo)
                                if (nuevaContrasena.isNotBlank()) {
                                    updates["contrasena"] = nuevaContrasena
                                }
                                db.collection("usuarios").document(idUsuario).update(updates)
                                    .addOnSuccessListener {
                                        correoProfesor = nuevoCorreo
                                        android.widget.Toast.makeText(context, "Cuenta actualizada", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        android.widget.Toast.makeText(context, "Error al actualizar", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            },
                            onLogout = onLogout
                        )
                    }
                } else {
                    when (selectedNav) {
                        0 -> InicioContent(
                            pendientes = pendientes,
                            claseActual = claseActual,
                            eventos = eventos,
                            evidencias = listaEvidencias,
                            onCrearTarea = { onCrearTarea(idUsuario, null) },
                            onVerClick = { onVerEvidencia(it.idEvidencia) }
                        )
                        1 -> TareasContent(
                            tareas = listaTareas,
                            clases = listaClases,
                            idUsuario = idUsuario,
                            onEditTarea = onCrearTarea,
                            onDeleteTarea = { idTarea ->
                                showDeleteTareaDialog = idTarea
                            },
                            onVerEstadisticas = onVerEstadisticas
                        )
                        2 -> ClasesContent(
                            clases = listaClases,
                            inscripciones = inscripcionesMap,
                            onEditClase = { clase ->
                                nuevaClaseNombre = clase.nombre
                                nuevaClaseDesc = clase.descripcion
                                nuevaClaseEnlace = clase.enlace
                                editingClaseId = clase.idClase
                                showClassDialog = true
                            },
                            onDeleteClase = { idClase ->
                                showDeleteClaseDialog = idClase
                            },
                            onVerAlumnos = onVerAlumnos
                        )
                        3 -> PerfilContent(
                            nombre = nombreProfesor,
                            clasesCount = listaClases.size,
                            tareasCount = listaTareas.size,
                            evaluacionesCount = listaEvidencias.count { it.estado != EstadoEvidencia.Pendiente },
                            correo = correoProfesor,
                            onGuardarCambios = { nuevoCorreo, nuevaContrasena ->
                                val updates = mutableMapOf<String, Any>("correo" to nuevoCorreo)
                                if (nuevaContrasena.isNotBlank()) {
                                    updates["contrasena"] = nuevaContrasena
                                }
                                db.collection("usuarios").document(idUsuario).update(updates)
                                    .addOnSuccessListener {
                                        correoProfesor = nuevoCorreo
                                        android.widget.Toast.makeText(context, "Cuenta actualizada", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        android.widget.Toast.makeText(context, "Error al actualizar", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            },
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }

    showDeleteTareaDialog?.let { idTarea ->
        val tarea = listaTareas.find { it.idTarea == idTarea }
        AlertDialog(
            onDismissRequest = { showDeleteTareaDialog = null },
            title = { Text("Eliminar Actividad", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar la actividad \"${tarea?.titulo ?: ""}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("tareas").document(idTarea).delete()
                        showDeleteTareaDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteTareaDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    showDeleteClaseDialog?.let { idClase ->
        val clase = listaClases.find { it.idClase == idClase }
        AlertDialog(
            onDismissRequest = { showDeleteClaseDialog = null },
            title = { Text("Eliminar Clase", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar la clase \"${clase?.nombre ?: ""}\"? Se perderán todos los datos asociados en cascada. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("clases").document(idClase).delete()
                        showDeleteClaseDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteClaseDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
/**
 * Componente interno que renderiza el contenido de InicioContent.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun InicioContent(
    pendientes: Int,
    claseActual: String,
    eventos: List<Evento>,
    evidencias: List<EvidenciaTarea>,
    onCrearTarea: () -> Unit,
    onVerClick: (EvidenciaTarea) -> Unit
) {
    var selectedStatusFilter by remember { mutableStateOf<EstadoEvidencia?>(null) }

    val filteredEvidencias = remember(evidencias, selectedStatusFilter) {
        if (selectedStatusFilter == null) {
            evidencias
        } else {
            evidencias.filter { it.estado == selectedStatusFilter }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        
        if (eventos.isNotEmpty()) {
            item {
                Text("Anuncios Recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(eventos) { evento ->
                        EventoCarouselCardProfesor(evento)
                    }
                }
            }
        }
        
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Estatus de Aula", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        Text(if (pendientes > 0) "Tienes $pendientes actividades pendientes por evaluar." else "Al corriente con tus evaluaciones académicas.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Evidencias Recientes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (pendientes > 0) Badge(containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor   = MaterialTheme.colorScheme.onTertiary) { Text(pendientes.toString()) }
            }
        }

        // Status Filter Chips LazyRow
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("Todas", style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {
                            if (selectedStatusFilter == null) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == EstadoEvidencia.Pendiente,
                        onClick = { selectedStatusFilter = EstadoEvidencia.Pendiente },
                        label = { Text("Pendientes", style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {
                            if (selectedStatusFilter == EstadoEvidencia.Pendiente) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == EstadoEvidencia.Aprobada,
                        onClick = { selectedStatusFilter = EstadoEvidencia.Aprobada },
                        label = { Text("Aprobadas", style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {
                            if (selectedStatusFilter == EstadoEvidencia.Aprobada) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == EstadoEvidencia.Rechazada,
                        onClick = { selectedStatusFilter = EstadoEvidencia.Rechazada },
                        label = { Text("Rechazadas", style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {
                            if (selectedStatusFilter == EstadoEvidencia.Rechazada) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
            }
        }

        if (filteredEvidencias.isEmpty()) {
            item {
                Text("No hay entregas registradas con este estado.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
            }
        } else {
            items(filteredEvidencias) { ev -> EvidenciaListItem(ev, onVerClick) }
        }
    }
}

@Composable
/**
 * Componente interno que renderiza el contenido de TareasContent.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun TareasContent(
    tareas: List<Tarea>,
    clases: List<ClaseInfo>,
    idUsuario: String,
    onEditTarea: (String, String?) -> Unit,
    onDeleteTarea: (String) -> Unit,
    onVerEstadisticas: (String, String) -> Unit
) {
    var selectedClaseFilterId by remember { mutableStateOf<String?>(null) }

    val filteredTareas = remember(tareas, selectedClaseFilterId) {
        if (selectedClaseFilterId == null) {
            tareas
        } else {
            tareas.filter { it.idClase == selectedClaseFilterId }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Panel de Actividades", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { Text("Lista de tareas vigentes asignadas a tus clases.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        
        // Sección de Filtros de Clase
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text("Filtrar por Clase", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    // Chip para "Todas"
                    item {
                        FilterChip(
                            selected = selectedClaseFilterId == null,
                            onClick = { selectedClaseFilterId = null },
                            label = { Text("Todas", style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = {
                                if (selectedClaseFilterId == null) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    }
                    // Chips de cada clase
                    items(clases) { clase ->
                        FilterChip(
                            selected = selectedClaseFilterId == clase.idClase,
                            onClick = { selectedClaseFilterId = clase.idClase },
                            label = { Text(clase.nombre, style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = {
                                if (selectedClaseFilterId == clase.idClase) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        if (filteredTareas.isEmpty()) {
            item {
                Text("No hay tareas creadas para tus clases.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(filteredTareas) { tarea ->
                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tarea.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            
                            IconButton(onClick = { onVerEstadisticas(tarea.idTarea, tarea.titulo) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.BarChart, "Estadísticas", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onEditTarea(idUsuario, tarea.idTarea) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onDeleteTarea(tarea.idTarea) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(tarea.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tarea.nombreClase) },
                                icon = { Icon(Icons.Default.Class, null, Modifier.size(16.dp)) }
                            )
                            Spacer(Modifier.weight(1f))
                            val fmt = SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
                            Text("Límite: " + tarea.fechaLimite?.let { fmt.format(it) }.orEmpty(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
/**
 * Componente interno que renderiza el contenido de ClasesContent.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun ClasesContent(
    clases: List<ClaseInfo>,
    inscripciones: Map<String, List<String>>,
    onEditClase: (ClaseInfo) -> Unit,
    onDeleteClase: (String) -> Unit,
    onVerAlumnos: (String, String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Mis Asignaturas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (clases.isEmpty()) {
            item {
                Text("No tienes asignaturas registradas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(clases) { clase ->
                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(clase.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            
                            IconButton(onClick = { onVerAlumnos(clase.idClase, clase.nombre) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.People, "Ver Alumnos", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onEditClase(clase) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onDeleteClase(clase.idClase) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(clase.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        if (clase.enlace.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Link, "Enlace", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(clase.enlace, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        
                        // Enrolled Students list
                        val alumnos = inscripciones.getOrDefault(clase.idClase, emptyList())
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Alumnos Inscritos (${alumnos.size}):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (alumnos.isEmpty()) {
                            Text("Sin alumnos inscritos aún.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(alumnos.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Código de unión: ${clase.idClase}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
/**
 * Componente interno que renderiza el contenido de PerfilContent.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun PerfilContent(
    nombre: String,
    clasesCount: Int,
    tareasCount: Int,
    evaluacionesCount: Int,
    correo: String,
    onGuardarCambios: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Surface(
            Modifier.size(100.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    nombre.split(" ").take(2).joinToString("") { it.first().toString().uppercase() },
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Departamento Académico", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Rol: Profesor", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        
        Spacer(Modifier.height(32.dp))
        
        // Statistics Section
        Text(
            text = "Mis Estadísticas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                title = "Asignaturas",
                value = clasesCount.toString(),
                icon = Icons.Default.School,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Tareas",
                value = tareasCount.toString(),
                icon = Icons.Default.Assignment,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        StatCard(
            title = "Evidencias Evaluadas",
            value = evaluacionesCount.toString(),
            icon = Icons.Default.Grade,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        // Shortcuts / Contact Info
        Text(
            text = "Información y Atajos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        ShortcutItem(icon = Icons.Default.Email, title = "Contacto Administrativo", subtitle = "soporte.docentes@edutask.edu")
        Spacer(Modifier.height(12.dp))
        AjustesCuentaAccordion(correoActual = correo, onGuardarCambios = onGuardarCambios)
        Spacer(Modifier.height(12.dp))
        ShortcutItem(icon = Icons.Default.HelpOutline, title = "Soporte Técnico", subtitle = "Reportar un problema con la app")
        Spacer(Modifier.height(12.dp))
        ShortcutItem(
            icon = Icons.Default.Logout,
            title = "Cerrar Sesión",
            subtitle = "Salir de la cuenta de forma segura",
            onClick = onLogout
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente visual reutilizable para renderizar AjustesCuentaAccordion.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun AjustesCuentaAccordion(
    correoActual: String = "",
    onGuardarCambios: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    var correo by remember(correoActual) { mutableStateOf(correoActual) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { expanded = !expanded },
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Ajustes de Cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Cambiar contraseña y correo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Nueva Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword
                    )
                    
                    if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text(
                            text = "Las contraseñas no coinciden",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Button(
                        onClick = {
                            onGuardarCambios(correo, password)
                            expanded = false
                            password = ""
                            confirmPassword = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        enabled = correo.isNotBlank() && (password.isEmpty() || password == confirmPassword)
                    ) {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}

@Composable
/**
 * Componente visual reutilizable para renderizar StatCard.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun StatCard(title: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            Spacer(Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
/**
 * Componente visual reutilizable para renderizar ShortcutItem.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun ShortcutItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
/**
 * Componente visual reutilizable para renderizar EvidenciaListItem.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EvidenciaListItem(
    evidencia: EvidenciaTarea,
    onVerClick: (EvidenciaTarea) -> Unit
) {
    val fmt = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
    val isPend = evidencia.estado == EstadoEvidencia.Pendiente
    OutlinedCard(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        ListItem(
            headlineContent   = { Text(evidencia.nombreAlumno, style = MaterialTheme.typography.titleSmall) },
            supportingContent = { Text(evidencia.tituloTarea, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingContent    = {
                Surface(Modifier.size(12.dp), shape = MaterialTheme.shapes.extraLarge,
                    color = if (isPend) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary) {}
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(fmt.format(evidencia.fechaEnvio), style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(onClick = { onVerClick(evidencia) }, modifier = Modifier.height(28.dp)) {
                        Text("Ver", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        )
    }
}

@Composable
/**
 * Componente visual reutilizable para renderizar AccesoRapidoCard.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun AccesoRapidoCard(acceso: AccesoRapido, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(Modifier.size(48.dp), shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.secondaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(acceso.icon, contentDescription = acceso.label, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Text(acceso.label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/**
 * Metodo principal que ejecuta la operacion: generateShortCode.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun generateShortCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..6).map { chars.random() }.joinToString("")
}

@Composable
/**
 * Metodo principal que ejecuta la operacion: EventoCarouselCardProfesor.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EventoCarouselCardProfesor(evento: Evento) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val fechaFormat = dateFormat.format(Date(evento.fechaPublicacion))
    var showDialog by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.width(260.dp).height(120.dp).clickable { showDialog = true },
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(evento.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(evento.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text(fechaFormat, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(evento.titulo, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Publicado: $fechaFormat",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(evento.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
/**
 * Metodo principal que ejecuta la operacion: InicioSkeleton.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun InicioSkeleton() {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(180.dp).height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                OutlinedCard(
                    modifier = androidx.compose.ui.Modifier.width(260.dp).height(120.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = androidx.compose.ui.Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(160.dp).height(20.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(220.dp).height(16.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(60.dp).height(12.dp))
                    }
                }
            }
        }
        
        Card(modifier = androidx.compose.ui.Modifier.fillMaxWidth().height(80.dp)) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.CenterStart) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(140.dp).height(18.dp))
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(200.dp).height(14.dp))
                }
            }
        }
        
        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(160.dp).height(24.dp))
        repeat(2) {
            OutlinedCard(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = androidx.compose.ui.Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = androidx.compose.ui.Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(180.dp).height(16.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(120.dp).height(14.dp))
                    }
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(60.dp).height(28.dp), shape = RoundedCornerShape(14.dp))
                }
            }
        }
    }
}

@Composable
/**
 * Metodo principal que ejecuta la operacion: TareasSkeleton.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun TareasSkeleton() {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(70.dp).height(32.dp), shape = RoundedCornerShape(16.dp))
            }
        }
        repeat(3) {
            OutlinedCard(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = androidx.compose.ui.Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(200.dp).height(20.dp))
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(280.dp).height(16.dp))
                    Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(100.dp).height(24.dp), shape = RoundedCornerShape(12.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(120.dp).height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
/**
 * Metodo principal que ejecuta la operacion: ClasesSkeleton.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun ClasesSkeleton() {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(150.dp).height(24.dp))
        repeat(3) {
            OutlinedCard(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = androidx.compose.ui.Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(160.dp).height(20.dp).weight(1f))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.size(24.dp), shape = androidx.compose.foundation.shape.CircleShape)
                        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.size(24.dp), shape = androidx.compose.foundation.shape.CircleShape)
                        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.size(24.dp), shape = androidx.compose.foundation.shape.CircleShape)
                    }
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(260.dp).height(16.dp))
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(180.dp).height(14.dp))
                    ShimmerPlaceholder(modifier = androidx.compose.ui.Modifier.width(140.dp).height(24.dp), shape = RoundedCornerShape(6.dp))
                }
            }
        }
    }
}
```

### `CrearTareaScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/profesor/CrearTareaScreen.kt`

```kotlin
/**
 * Pantalla para que el profesor cree o modifique asignaciones academicas,
 * definiendo instrucciones, fechas limite, ponderaciones y archivos adjuntos.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.profesor

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.model.ClaseInfo
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat
import com.pmlp.edutask.utils.getSafeDate

data class ArchivoAdjunto(
    val uri: Uri? = null,
    val nombre: String,
    val base64: String? = null,
    val esLink: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla CrearTareaScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun CrearTareaScreen(
    idUsuario: String = "",
    idTarea: String? = null,
    onTareaCreadaExitosa: () -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var limitTimestamp by remember {
        mutableStateOf(
            java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        )
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var archivosAdjuntos by remember { mutableStateOf(listOf<ArchivoAdjunto>()) }
    val selectFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris != null) {
            val nuevosArchivos = uris.map { uri ->
                val nombre = getFileName(context, uri)
                ArchivoAdjunto(uri = uri, nombre = nombre)
            }
            archivosAdjuntos = (archivosAdjuntos + nuevosArchivos).distinctBy { it.uri?.toString() ?: it.nombre }
        }
    }
    
    var isLoading by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }

    // Dynamic Class selector states
    var clasesList by remember { mutableStateOf<List<ClaseInfo>>(emptyList()) }
    var selectedClaseId by remember { mutableStateOf("") }
    var selectedClaseNombre by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    var showLinkDialog by remember { mutableStateOf(false) }
    var linkUrl by remember { mutableStateOf("") }
    var linkTitle by remember { mutableStateOf("") }

    // Load professor classes
    LaunchedEffect(idUsuario) {
        if (idUsuario.isNotBlank()) {
            db.collection("clases")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .addOnSuccessListener { snapshot ->
                    val clases = snapshot.documents.map { doc ->
                        ClaseInfo(
                            idClase = doc.id,
                            nombre = doc.getString("nombre") ?: "Sin Nombre"
                        )
                    }
                    clasesList = clases
                    // Default selection
                    if (clases.isNotEmpty() && selectedClaseId.isBlank()) {
                        selectedClaseId = clases[0].idClase
                        selectedClaseNombre = clases[0].nombre
                    }
                }
        }
    }

    // Load task details if editing
    LaunchedEffect(idTarea) {
        if (!idTarea.isNullOrBlank()) {
            isLoading = true
            db.collection("tareas").document(idTarea).get()
                .addOnSuccessListener { doc ->
                    isLoading = false
                    if (doc.exists()) {
                        titulo = doc.getString("titulo") ?: ""
                        descripcion = doc.getString("descripcion") ?: ""
                        
                        val date = doc.getSafeDate("fechaLimite") ?: Date()
                        limitTimestamp = date.time
                        
                        selectedClaseId = doc.getString("idClase") ?: ""
                        selectedClaseNombre = doc.getString("nombreClase") ?: ""

                        val rawArchivos = doc.get("archivos") as? List<*>
                        val cargados = mutableListOf<ArchivoAdjunto>()
                        rawArchivos?.forEach { item ->
                            if (item is Map<*, *>) {
                                val nombre = item["nombre"]?.toString() ?: ""
                                val base64 = item["base64"]?.toString() ?: ""
                                val esLink = item["esLink"] as? Boolean ?: false
                                cargados.add(ArchivoAdjunto(nombre = nombre, base64 = base64, esLink = esLink))
                            }
                        }
                        archivosAdjuntos = cargados
                    }
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (idTarea.isNullOrBlank()) "Asignar Nueva Actividad" else "Editar Actividad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdown Selector for classes list
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedClaseNombre.ifBlank { "Selecciona una clase" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Asignar a Clase") },
                    trailingIcon = {
                        IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                            Icon(
                                imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Expandir"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { dropdownExpanded = !dropdownExpanded },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    clasesList.forEach { clase ->
                        DropdownMenuItem(
                            text = { Text(clase.nombre) },
                            onClick = {
                                selectedClaseId = clase.idClase
                                selectedClaseNombre = clase.nombre
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título de la tarea") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp) // Diseño institucional redondeado sin emojis
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción larga de la actividad") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )

            val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
            val timeFormat = remember { SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
            
            val fechaFormateada = remember(limitTimestamp) { dateFormat.format(Date(limitTimestamp)) }
            val horaFormateada = remember(limitTimestamp) { timeFormat.format(Date(limitTimestamp)) }

            Text(
                text = "Fecha y Hora Límite:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { if (!isLoading) showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(fechaFormateada)
                }

                OutlinedButton(
                    onClick = { if (!isLoading) showTimePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(horaFormateada)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Adjuntar archivos o imágenes
            if (archivosAdjuntos.isNotEmpty()) {
                Text(
                    text = "Material Complementario:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                archivosAdjuntos.forEach { archivo ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (archivo.esLink) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = archivo.nombre,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            archivosAdjuntos = archivosAdjuntos.filter { it != archivo }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { selectFilesLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(imageVector = Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Archivo", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
                
                OutlinedButton(
                    onClick = { showLinkDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(imageVector = Icons.Default.Link, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enlace", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (titulo.isNotBlank() && descripcion.isNotBlank() && selectedClaseId.isNotBlank()) {
                        isLoading = true
                        val parsedDate = Date(limitTimestamp)

                        scope.launch {
                            try {
                                val subidos = mutableListOf<Map<String, Any>>()
                                
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    for (archivo in archivosAdjuntos) {
                                        if (archivo.esLink && archivo.base64 != null) {
                                            subidos.add(mapOf("nombre" to archivo.nombre, "base64" to archivo.base64, "esLink" to true))
                                        } else if (archivo.base64 != null) {
                                            subidos.add(mapOf("nombre" to archivo.nombre, "base64" to archivo.base64))
                                        } else if (archivo.uri != null) {
                                            try {
                                                context.contentResolver.openInputStream(archivo.uri)?.use { input ->
                                                    val bytes = input.readBytes()
                                                    val base64Str = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                                                    if (base64Str.isNotBlank()) {
                                                        subidos.add(mapOf("nombre" to archivo.nombre, "base64" to base64Str))
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }

                                val nuevaTarea = hashMapOf(
                                    "titulo" to titulo.trim(),
                                    "descripcion" to descripcion.trim(),
                                    "fechaLimite" to com.google.firebase.Timestamp(parsedDate),
                                    "idClase" to selectedClaseId,
                                    "nombreClase" to selectedClaseNombre,
                                    "archivos" to subidos
                                )

                                val docRef = if (idTarea.isNullOrBlank()) {
                                    db.collection("tareas").document()
                                } else {
                                    db.collection("tareas").document(idTarea)
                                }

                                docRef.set(nuevaTarea)
                                    .addOnSuccessListener {
                                        if (idTarea.isNullOrBlank()) {
                                            val newTaskId = docRef.id
                                            db.collection("clase_alumno")
                                                .whereEqualTo("idClase", selectedClaseId)
                                                .get()
                                                .addOnSuccessListener { snapshot ->
                                                    val batch = db.batch()
                                                    for (doc in snapshot.documents) {
                                                        val idAlumno = doc.getString("idUsuario") ?: continue
                                                        val asigRef = db.collection("asignaciones_tarea").document()
                                                        val asigDoc = hashMapOf(
                                                            "idTarea" to newTaskId,
                                                            "idUsuario" to idAlumno,
                                                            "fechaAsignacion" to com.google.firebase.Timestamp.now()
                                                        )
                                                        batch.set(asigRef, asigDoc)
                                                    }
                                                    batch.commit()
                                                        .addOnSuccessListener {
                                                            isLoading = false
                                                            onTareaCreadaExitosa()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            isLoading = false
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Error al crear asignaciones: ${e.localizedMessage}")
                                                            }
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Error al buscar alumnos: ${e.localizedMessage}")
                                                    }
                                                }
                                        } else {
                                            isLoading = false
                                            onTareaCreadaExitosa()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Error al guardar: ${e.localizedMessage}")
                                        }
                                    }
                            } catch (e: Exception) {
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al guardar: ${e.localizedMessage}")
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading && titulo.isNotBlank() && descripcion.isNotBlank() && selectedClaseId.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (idTarea.isNullOrBlank()) "Publicar Actividad" else "Guardar Cambios", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = limitTimestamp
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = selectedMillis
                            }
                            val newCal = java.util.Calendar.getInstance().apply {
                                timeInMillis = limitTimestamp
                                set(java.util.Calendar.YEAR, utcCal.get(java.util.Calendar.YEAR))
                                set(java.util.Calendar.MONTH, utcCal.get(java.util.Calendar.MONTH))
                                set(java.util.Calendar.DAY_OF_MONTH, utcCal.get(java.util.Calendar.DAY_OF_MONTH))
                            }
                            limitTimestamp = newCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val calendar = remember(limitTimestamp) {
            java.util.Calendar.getInstance().apply { timeInMillis = limitTimestamp }
        }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(java.util.Calendar.MINUTE),
            is24Hour = true
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedCal = java.util.Calendar.getInstance().apply {
                            timeInMillis = limitTimestamp
                            set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(java.util.Calendar.MINUTE, timePickerState.minute)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                        limitTimestamp = updatedCal.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text("Agregar Enlace Externo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it },
                        label = { Text("URL del enlace (https://...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = linkTitle,
                        onValueChange = { linkTitle = it },
                        label = { Text("Título (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (linkUrl.isNotBlank()) {
                            val urlFixed = if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) "https://$linkUrl" else linkUrl
                            val finalTitle = if (linkTitle.isNotBlank()) linkTitle else urlFixed
                            archivosAdjuntos = archivosAdjuntos + ArchivoAdjunto(
                                uri = null,
                                nombre = "Enlace: $finalTitle",
                                base64 = urlFixed,
                                esLink = true
                            )
                        }
                        showLinkDialog = false
                        linkUrl = ""
                        linkTitle = ""
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLinkDialog = false
                        linkUrl = ""
                        linkTitle = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Obtiene o recupera datos asociados a getFileName desde la base de datos o API.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun getFileName(context: android.content.Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "archivo"
}
```

### `AlumnosClaseScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/profesor/AlumnosClaseScreen.kt`

```kotlin
/**
 * Pantalla que muestra el listado de alumnos inscritos en una clase especifica,
 * permitiendo al profesor gestionar inscripciones y revisar el progreso individual.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.profesor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.ui.components.ShimmerPlaceholder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AlumnoInfo(
    val idAlumno: String,
    val nombre: String,
    val matricula: String,
    val correo: String,
    val idClaseAlumno: String // Document ID inside clase_alumno
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla AlumnosClaseScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun AlumnosClaseScreen(
    idClase: String,
    nombreClase: String,
    onBack: () -> Unit,
    onVerLibreta: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    
    var listAlumnos by remember { mutableStateOf<List<AlumnoInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedAlumnoForRemove by remember { mutableStateOf<AlumnoInfo?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Real-time listener for enrolled students
    DisposableEffect(idClase) {
        if (idClase.isBlank()) return@DisposableEffect onDispose {}
        
        val listener = db.collection("clase_alumno")
            .whereEqualTo("idClase", idClase)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                
                scope.launch {
                    val tempAlumnos = mutableListOf<AlumnoInfo>()
                    for (doc in snapshot.documents) {
                        val idAlumno = doc.getString("idUsuario") ?: continue
                        val idClaseAlumno = doc.id
                        
                        try {
                            val userDoc = db.collection("usuarios").document(idAlumno).get().await()
                            if (userDoc.exists()) {
                                tempAlumnos.add(
                                    AlumnoInfo(
                                        idAlumno = idAlumno,
                                        nombre = userDoc.getString("nombre") ?: "Alumno Sin Nombre",
                                        matricula = userDoc.getString("matricula") ?: "",
                                        correo = userDoc.getString("correo") ?: "",
                                        idClaseAlumno = idClaseAlumno
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            // Ignore specific failures and load others
                        }
                    }
                    listAlumnos = tempAlumnos.sortedBy { it.nombre }
                    isLoading = false
                }
            }
            
        onDispose { listener.remove() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(nombreClase, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onVerLibreta) {
                        Icon(Icons.Default.Grade, contentDescription = "Ver Libreta de Calificaciones")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerPlaceholder(modifier = Modifier.width(180.dp).height(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                repeat(4) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                ShimmerPlaceholder(modifier = Modifier.width(150.dp).height(20.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ShimmerPlaceholder(modifier = Modifier.size(16.dp), shape = androidx.compose.foundation.shape.CircleShape)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ShimmerPlaceholder(modifier = Modifier.width(110.dp).height(16.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ShimmerPlaceholder(modifier = Modifier.size(16.dp), shape = androidx.compose.foundation.shape.CircleShape)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ShimmerPlaceholder(modifier = Modifier.width(170.dp).height(16.dp))
                                }
                            }
                            ShimmerPlaceholder(modifier = Modifier.size(24.dp), shape = androidx.compose.foundation.shape.CircleShape)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Alumnos Inscritos (${listAlumnos.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (listAlumnos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay alumnos inscritos en esta clase.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(listAlumnos) { alumno ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alumno.nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Matrícula: ${alumno.matricula}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Mail,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = alumno.correo,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(onClick = { selectedAlumnoForRemove = alumno }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Quitar alumno",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Cascade Deletion Confirmation Dialog
    selectedAlumnoForRemove?.let { alumno ->
        AlertDialog(
            onDismissRequest = { selectedAlumnoForRemove = null },
            icon = { Icon(Icons.Default.Person, null) },
            title = { Text("Quitar de la Clase", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas quitar a ${alumno.nombre} de la clase? Se borrarán de forma permanente sus entregas, calificaciones, asignaciones y smartwatch alertas asociadas en cascada."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isLoading = true
                        selectedAlumnoForRemove = null
                        scope.launch {
                            try {
                                // 1. Delete enrollment from clase_alumno
                                db.collection("clase_alumno").document(alumno.idClaseAlumno).delete().await()

                                // 2. Query all tasks of this class
                                val tasksSnapshot = db.collection("tareas")
                                    .whereEqualTo("idClase", idClase)
                                    .get()
                                    .await()
                                
                                val taskIds = tasksSnapshot.documents.map { it.id }

                                if (taskIds.isNotEmpty()) {
                                    // 3. Query all student assignments for these tasks
                                    for (taskId in taskIds) {
                                        val assignmentsSnapshot = db.collection("asignaciones_tarea")
                                            .whereEqualTo("idUsuario", alumno.idAlumno)
                                            .whereEqualTo("idTarea", taskId)
                                            .get()
                                            .await()
                                        
                                        for (asigDoc in assignmentsSnapshot.documents) {
                                            val idAsignacion = asigDoc.id
                                            
                                            // Delete evidence and dependencies
                                            val evidencesSnapshot = db.collection("evidencias_tarea")
                                                .whereEqualTo("idAsignacion", idAsignacion)
                                                .get()
                                                .await()
                                            
                                            for (evDoc in evidencesSnapshot.documents) {
                                                val idEvidencia = evDoc.id
                                                
                                                // Delete smartwatch alerts
                                                val notifsSnapshot = db.collection("notificaciones_reloj")
                                                    .whereEqualTo("idEvidencia", idEvidencia)
                                                    .get()
                                                    .await()
                                                for (notifDoc in notifsSnapshot.documents) {
                                                    db.collection("notificaciones_reloj").document(notifDoc.id).delete().await()
                                                }
                                                
                                                // Delete grades
                                                val gradesSnapshot = db.collection("calificaciones")
                                                    .whereEqualTo("idEvidencia", idEvidencia)
                                                    .get()
                                                    .await()
                                                for (gradeDoc in gradesSnapshot.documents) {
                                                    db.collection("calificaciones").document(gradeDoc.id).delete().await()
                                                }
                                                
                                                // Delete evidence document
                                                db.collection("evidencias_tarea").document(idEvidencia).delete().await()
                                            }
                                            
                                            // Delete assignment document
                                            db.collection("asignaciones_tarea").document(idAsignacion).delete().await()
                                        }
                                    }
                                }
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Alumno y sus datos eliminados en cascada con éxito.")
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al quitar: ${e.localizedMessage}")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Quitar Alumno", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedAlumnoForRemove = null }) {
                    Text("Cancelar", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}
```

### `EvaluarTareaScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/profesor/EvaluarTareaScreen.kt`

```kotlin
/**
 * Pantalla donde el profesor revisa las evidencias enviadas por los alumnos,
 * asigna calificaciones numericas y anade retroalimentacion personalizada.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.profesor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.pmlp.edutask.ui.components.ShimmerPlaceholder
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.model.EstadoEvidencia
import com.pmlp.edutask.model.EvidenciaTarea
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.pmlp.edutask.utils.getSafeDate
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import android.util.Base64
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla EvaluarTareaScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EvaluarTareaScreen(
    idEvidencia: String,
    idUsuario: String, // Profesor ID
    onEvaluadoExitoso: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }

    var evidencia by remember { mutableStateOf<EvidenciaTarea?>(null) }
    var idCalificacion by remember { mutableStateOf<String?>(null) }
    
    // Evaluation Form States
    var puntaje by remember { mutableStateOf("") }
    var comentarios by remember { mutableStateOf("") }
    var esBorrador by remember { mutableStateOf(false) }
    
    // Edit & Load States
    var isEditMode by remember { mutableStateOf(true) } // If graded, defaults to false
    var isAlreadyGraded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Load Evidence Info
    LaunchedEffect(idEvidencia) {
        if (idEvidencia.isNotBlank()) {
            db.collection("evidencias_tarea").document(idEvidencia).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val estadoStr = doc.getString("estado") ?: "Pendiente"
                        val estadoEnum = when (estadoStr.lowercase()) {
                            "aprobada" -> EstadoEvidencia.Aprobada
                            "rechazada" -> EstadoEvidencia.Rechazada
                            else -> EstadoEvidencia.Pendiente
                        }
                        
                        val idEvidenciaRaw = doc.get("idEvidencia")
                        val idEvidenciaStr = when (idEvidenciaRaw) {
                            is Number -> idEvidenciaRaw.toLong().toString()
                            else -> idEvidenciaRaw?.toString() ?: doc.id
                        }
                        val idAsignacionRaw = doc.get("idAsignacion")
                        val idAsignacionStr = when (idAsignacionRaw) {
                            is Number -> idAsignacionRaw.toLong().toString()
                            else -> idAsignacionRaw?.toString() ?: ""
                        }

                        val archivosList = mutableListOf<Map<String, String>>()
                        val rawArchivos = doc.get("archivos") as? List<*>
                        rawArchivos?.forEach { item ->
                            if (item is Map<*, *>) {
                                val nombre = item["nombre"]?.toString() ?: ""
                                val base64 = item["base64"]?.toString() ?: ""
                                archivosList.add(mapOf("nombre" to nombre, "base64" to base64))
                            }
                        }

                        val vinculosList = mutableListOf<String>()
                        val rawVinculos = doc.get("vinculos") as? List<*>
                        rawVinculos?.forEach { item ->
                            if (item != null) {
                                vinculosList.add(item.toString())
                            }
                        }

                        evidencia = EvidenciaTarea(
                            idEvidencia = idEvidenciaStr,
                            tituloTarea = doc.getString("tituloTarea") ?: "Sin Título",
                            fotoBase64 = doc.getString("fotoBase64") ?: doc.getString("fotoUrl") ?: "",
                            fechaEnvio = doc.getSafeDate("fechaEnvio") ?: Date(),
                            estado = estadoEnum,
                            idAsignacion = idAsignacionStr,
                            nombreAlumno = doc.getString("nombreAlumno") ?: "Alumno Anónimo",
                            nombreArchivo = doc.getString("nombreArchivo"),
                            textoEvidencia = doc.getString("textoEvidencia"),
                            archivos = archivosList,
                            vinculos = vinculosList
                        )
                    } else {
                        Toast.makeText(context, "Esta entrega ha sido anulada por el alumno.", Toast.LENGTH_LONG).show()
                        onEvaluadoExitoso()
                    }
                }
        }
    }

    // Load Existing Calificacion if any
    LaunchedEffect(idEvidencia) {
        if (idEvidencia.isNotBlank()) {
            db.collection("calificaciones")
                .whereEqualTo("idEvidencia", idEvidencia)
                .get()
                .addOnSuccessListener { snapshot ->
                    isLoading = false
                    if (!snapshot.isEmpty) {
                        val doc = snapshot.documents[0]
                        idCalificacion = doc.id
                        val valorNum = doc.get("valor")
                        puntaje = valorNum?.toString() ?: ""
                        comentarios = doc.getString("comentario") ?: ""
                        esBorrador = doc.getBoolean("esBorrador") ?: false
                        
                        isAlreadyGraded = true
                        // If it's a saved draft, keep it in edit mode. If officially graded, set read-only.
                        isEditMode = esBorrador
                    }
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Evaluar Actividad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onEvaluadoExitoso) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card Details Skeleton
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ShimmerPlaceholder(modifier = Modifier.width(220.dp).height(24.dp))
                        ShimmerPlaceholder(modifier = Modifier.width(160.dp).height(18.dp))
                        ShimmerPlaceholder(modifier = Modifier.width(180.dp).height(16.dp))
                    }
                }
                
                // Uploaded Files / Evidence Section Skeleton
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerPlaceholder(modifier = Modifier.width(140.dp).height(20.dp))
                
                repeat(2) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShimmerPlaceholder(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(6.dp))
                            Spacer(Modifier.width(12.dp))
                            ShimmerPlaceholder(modifier = Modifier.width(180.dp).height(16.dp))
                        }
                    }
                }
                
                // Evaluation form Skeleton
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerPlaceholder(modifier = Modifier.width(120.dp).height(20.dp))
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(8.dp))
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(8.dp))
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(8.dp))
            }
        } else {
            val ev = evidencia
            if (ev == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontró la entrega de evidencia.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left/Top Section: Student Delivery Details
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = ev.tituloTarea,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Alumno: ${ev.nombreAlumno}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                val fmt = SimpleDateFormat("dd 'de' MMMM, HH:mm", Locale.getDefault())
                                Text(
                                    text = "Entregado: " + fmt.format(ev.fechaEnvio),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Attachment / Evidence image link
                            Text(
                                text = "Evidencia:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            if (!ev.textoEvidencia.isNullOrBlank()) {
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = ev.textoEvidencia,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            // ── Zona de Vínculos ───────────────────────────────────────────────────────
                            if (ev.vinculos.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Enlaces añadidos:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                ev.vinculos.forEach { link ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                        Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        val ctx = LocalContext.current
                                        TextButton(onClick = {
                                            val url = if (!link.startsWith("http://") && !link.startsWith("https://")) {
                                                "https://$link"
                                            } else link
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                ctx.startActivity(intent)
                                            } catch (e: Exception) {
                                                // Ignore or log
                                            }
                                        }, contentPadding = PaddingValues(0.dp)) {
                                            Text(link, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            // ── Zona de Archivos ───────────────────────────────────────────────────────
                            Spacer(modifier = Modifier.height(12.dp))
                            val totalArchivos = ev.archivos.size + (if (ev.fotoBase64.isNotBlank()) 1 else 0)
                            if (totalArchivos > 0) {
                                Text(
                                    "Archivos Adjuntos ($totalArchivos):",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Archivos de Firestore (Base64)
                                ev.archivos.forEach { archivoMap ->
                                    val base64Data = archivoMap["base64"] ?: ""
                                    val nombre = archivoMap["nombre"] ?: "Archivo"
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(nombre, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                                val ctx = LocalContext.current
                                                IconButton(onClick = { 
                                                    if (base64Data.isNotBlank()) {
                                                        abrirArchivoBase64(ctx, base64Data, nombre)
                                                    }
                                                }) {
                                                    Icon(Icons.Default.OpenInNew, contentDescription = "Abrir", tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }

                                            val isImage = nombre.lowercase().run {
                                                endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") || endsWith(".webp") || endsWith(".gif")
                                            }
                                            if (isImage && base64Data.isNotBlank()) {
                                                val bitmap = remember(base64Data) { decodeBase64ToBitmap(base64Data) }
                                                if (bitmap != null) {
                                                    Image(
                                                        bitmap = bitmap.asImageBitmap(),
                                                        contentDescription = "Vista previa de $nombre",
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(200.dp)
                                                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                                                            .clip(RoundedCornerShape(8.dp)),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Archivo Legacy
                                if (ev.fotoBase64.isNotBlank()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(ev.nombreArchivo ?: "Archivo antiguo", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(200.dp))
                                            }
                                            val ctx = LocalContext.current
                                            IconButton(onClick = { 
                                                abrirArchivoBase64(ctx, ev.fotoBase64, ev.nombreArchivo) 
                                            }) {
                                                Icon(Icons.Default.OpenInNew, contentDescription = "Abrir", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }

                                    val bitmap = remember(ev.fotoBase64) { decodeBase64ToBitmap(ev.fotoBase64) }
                                    if (bitmap != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Evidencia adjunta",
                                            modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            } else if (ev.textoEvidencia.isNullOrBlank() && ev.vinculos.isEmpty()) {
                                Text(
                                    text = "No se subieron archivos adjuntos ni texto.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Right/Bottom Section: Grade & Feedback Card
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Calificación y Retroalimentación",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                if (isAlreadyGraded && !isEditMode) {
                                    IconButton(onClick = { isEditMode = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar Calificación", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            // Score Field
                            OutlinedTextField(
                                value = puntaje,
                                onValueChange = { puntaje = it },
                                label = { Text("Puntaje (1 - 10)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isEditMode,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Comments Field
                            OutlinedTextField(
                                value = comentarios,
                                onValueChange = { comentarios = it },
                                label = { Text("Comentarios al alumno") },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                enabled = isEditMode,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Submit Actions
                            if (isEditMode) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val scoreInt = puntaje.toIntOrNull()
                                            if (scoreInt != null && scoreInt in 1..10) {
                                                // Save official grade
                                                isLoading = true
                                                val califDoc = hashMapOf(
                                                    "idEvidencia" to idEvidencia,
                                                    "idUsuario" to idUsuario,
                                                    "valor" to scoreInt,
                                                    "comentario" to comentarios.trim(),
                                                    "esBorrador" to false,
                                                    "fechaCalificacion" to Timestamp.now()
                                                )

                                                val gradeRef = if (idCalificacion.isNullOrBlank()) {
                                                    db.collection("calificaciones").document()
                                                } else {
                                                    db.collection("calificaciones").document(idCalificacion!!)
                                                }

                                                gradeRef.set(califDoc)
                                                    .addOnSuccessListener {
                                                        // Update evidence status: Approved if >= 6, Rejected if < 6
                                                        val nuevoEstado = if (scoreInt >= 6) "Aprobada" else "Rechazada"
                                                        db.collection("evidencias_tarea").document(idEvidencia)
                                                            .update(
                                                                "estado", nuevoEstado,
                                                                "calificacion", scoreInt
                                                            )
                                                            .addOnSuccessListener {
                                                                isLoading = false
                                                                onEvaluadoExitoso()
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        isLoading = false
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Error: ${e.localizedMessage}")
                                                        }
                                                    }
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Por favor ingresa un puntaje válido entre 1 y 10.")
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (isAlreadyGraded) "Actualizar Calificación" else "Enviar Calificación",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    if (!isAlreadyGraded || esBorrador) {
                                        OutlinedButton(
                                            onClick = {
                                                val scoreInt = puntaje.toIntOrNull()
                                                if (scoreInt != null && scoreInt in 1..10) {
                                                    // Save draft grade
                                                    isLoading = true
                                                    val califDoc = hashMapOf(
                                                        "idEvidencia" to idEvidencia,
                                                        "idUsuario" to idUsuario,
                                                        "valor" to scoreInt,
                                                        "comentario" to comentarios.trim(),
                                                        "esBorrador" to true,
                                                        "fechaCalificacion" to Timestamp.now()
                                                    )

                                                    val gradeRef = if (idCalificacion.isNullOrBlank()) {
                                                        db.collection("calificaciones").document()
                                                    } else {
                                                        db.collection("calificaciones").document(idCalificacion!!)
                                                    }

                                                    gradeRef.set(califDoc)
                                                        .addOnSuccessListener {
                                                            db.collection("evidencias_tarea").document(idEvidencia)
                                                                .update("calificacion", scoreInt)
                                                                .addOnSuccessListener {
                                                                    isLoading = false
                                                                    isAlreadyGraded = true
                                                                    esBorrador = true
                                                                    isEditMode = true
                                                                    scope.launch {
                                                                        snackbarHostState.showSnackbar("Borrador guardado exitosamente.")
                                                                    }
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            isLoading = false
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Error: ${e.localizedMessage}")
                                                            }
                                                        }
                                                } else {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Por favor ingresa un puntaje válido entre 1 y 10.")
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "Guardar Borrador",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Graded (Read-Only) status alert
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Esta entrega ya está calificada oficialmente.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Realiza el procesamiento y conversion de archivos (decodeBase64ToBitmap).
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun decodeBase64ToBitmap(base64Str: String): android.graphics.Bitmap? {
    return try {
        val cleanString = if (base64Str.contains(",")) {
            base64Str.substring(base64Str.indexOf(",") + 1)
        } else {
            base64Str
        }
        val decodedBytes = android.util.Base64.decode(cleanString, android.util.Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

/**
 * Abre el recurso o vista abrirArchivoBase64 para la interaccion del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun abrirArchivoBase64(context: Context, base64Str: String, fileName: String?) {
    try {
        val cleanString = if (base64Str.contains(",")) {
            base64Str.substring(base64Str.indexOf(",") + 1)
        } else {
            base64Str
        }
        val bytes = android.util.Base64.decode(cleanString, android.util.Base64.DEFAULT)
        val safeFileName = fileName ?: "documento.pdf"
        val file = File(context.cacheDir, safeFileName)
        FileOutputStream(file).use { it.write(bytes) }
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Abrir archivo con"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al abrir el archivo", Toast.LENGTH_SHORT).show()
    }
}
```

### `EstadisticasTareaScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/profesor/EstadisticasTareaScreen.kt`

```kotlin
/**
 * Pantalla de metricas y estadisticas de una tarea en particular (promedio de calificaciones,
 * tasa de entrega, alumnos evaluados y pendientes).
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.profesor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.ui.components.ShimmerPlaceholder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class StudentGradeReport(
    val idAlumno: String,
    val nombre: String,
    val matricula: String,
    val status: String, // "Calificada", "Pendiente", "Sin entregar"
    val score: Int?,
    val comments: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla EstadisticasTareaScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EstadisticasTareaScreen(
    idTarea: String,
    tituloTarea: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }

    var reportList by remember { mutableStateOf<List<StudentGradeReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Load reports
    LaunchedEffect(idTarea) {
        if (idTarea.isNotBlank()) {
            try {
                // 1. Fetch assignments
                val assignmentsSnapshot = db.collection("asignaciones_tarea")
                    .whereEqualTo("idTarea", idTarea)
                    .get()
                    .await()

                val tempReports = mutableListOf<StudentGradeReport>()

                for (asigDoc in assignmentsSnapshot.documents) {
                    val idAlumno = asigDoc.getString("idUsuario") ?: continue
                    val idAsignacion = asigDoc.id

                    // Fetch student name
                    val studentDoc = db.collection("usuarios").document(idAlumno).get().await()
                    val nombre = studentDoc.getString("nombre") ?: "Alumno Sin Nombre"
                    val matricula = studentDoc.getString("matricula") ?: ""

                    // Fetch evidence
                    val evidencesSnapshot = db.collection("evidencias_tarea")
                        .whereEqualTo("idAsignacion", idAsignacion)
                        .get()
                        .await()

                    if (!evidencesSnapshot.isEmpty) {
                        val evDoc = evidencesSnapshot.documents[0]
                        val idEvidencia = evDoc.id

                        // Fetch grade
                        val gradesSnapshot = db.collection("calificaciones")
                            .whereEqualTo("idEvidencia", idEvidencia)
                            .get()
                            .await()

                        if (!gradesSnapshot.isEmpty) {
                            val gradeDoc = gradesSnapshot.documents[0]
                            val esBorrador = gradeDoc.getBoolean("esBorrador") ?: false

                            if (!esBorrador) {
                                val scoreVal = gradeDoc.getLong("valor")?.toInt()
                                val comment = gradeDoc.getString("comentario") ?: ""
                                tempReports.add(
                                    StudentGradeReport(
                                        idAlumno = idAlumno,
                                        nombre = nombre,
                                        matricula = matricula,
                                        status = "Calificada",
                                        score = scoreVal,
                                        comments = comment
                                    )
                                )
                            } else {
                                // It is a draft, so it's technically still pending approval
                                tempReports.add(
                                    StudentGradeReport(
                                        idAlumno = idAlumno,
                                        nombre = nombre,
                                        matricula = matricula,
                                        status = "Pendiente",
                                        score = null,
                                        comments = null
                                    )
                                )
                            }
                        } else {
                            tempReports.add(
                                StudentGradeReport(
                                    idAlumno = idAlumno,
                                    nombre = nombre,
                                    matricula = matricula,
                                    status = "Pendiente",
                                    score = null,
                                    comments = null
                                )
                            )
                        }
                    } else {
                        tempReports.add(
                            StudentGradeReport(
                                idAlumno = idAlumno,
                                        nombre = nombre,
                                        matricula = matricula,
                                        status = "Sin entregar",
                                        score = null,
                                        comments = null
                            )
                        )
                    }
                }

                reportList = tempAlumnosList(tempReports)
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar("Error al cargar reportes: ${e.localizedMessage}")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(tituloTarea, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Statistics Summary Row Skeleton
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                ShimmerPlaceholder(modifier = Modifier.size(24.dp), shape = androidx.compose.foundation.shape.CircleShape)
                                Spacer(Modifier.height(8.dp))
                                ShimmerPlaceholder(modifier = Modifier.width(50.dp).height(14.dp))
                                Spacer(Modifier.height(4.dp))
                                ShimmerPlaceholder(modifier = Modifier.width(30.dp).height(18.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerPlaceholder(modifier = Modifier.width(180.dp).height(24.dp))
                
                // Student grades reports skeleton
                repeat(3) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                ShimmerPlaceholder(modifier = Modifier.width(160.dp).height(18.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                ShimmerPlaceholder(modifier = Modifier.width(90.dp).height(14.dp))
                            }
                            ShimmerPlaceholder(modifier = Modifier.width(80.dp).height(24.dp), shape = RoundedCornerShape(12.dp))
                        }
                    }
                }
            }
        } else {
            // Calculate Statistics
            val gradedList = reportList.filter { it.status == "Calificada" && it.score != null }
            val average = if (gradedList.isNotEmpty()) gradedList.map { it.score!! }.average() else 0.0
            val maxScore = if (gradedList.isNotEmpty()) gradedList.maxOf { it.score!! } else 0
            val minScore = if (gradedList.isNotEmpty()) gradedList.minOf { it.score!! } else 0
            
            val totalAssigned = reportList.size
            val gradedCount = gradedList.size

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Statistics Cards Header
                item {
                    Text(
                        text = "Estadísticas del Grupo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Promedio",
                            value = String.format(Locale.getDefault(), "%.1f", average),
                            icon = Icons.Default.Analytics,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                        StatCard(
                            title = "Avance",
                            value = "$gradedCount / $totalAssigned",
                            icon = Icons.Default.DoneAll,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Nota Alta",
                            value = maxScore.toString(),
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                        StatCard(
                            title = "Nota Baja",
                            value = minScore.toString(),
                            icon = Icons.Default.TrendingDown,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.errorContainer
                        )
                    }
                }

                // Student Grades List
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reporte de Calificaciones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (reportList.isEmpty()) {
                    item {
                        Text(
                            text = "No hay alumnos asignados a esta tarea.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(reportList) { report ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = report.nombre,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Matrícula: ${report.matricula}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Status Badge / Score
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (report.status) {
                                            "Calificada" -> MaterialTheme.colorScheme.primaryContainer
                                            "Pendiente" -> MaterialTheme.colorScheme.tertiaryContainer
                                            else -> MaterialTheme.colorScheme.errorContainer
                                        }
                                    ) {
                                        Text(
                                            text = when (report.status) {
                                                "Calificada" -> "${report.score} / 10"
                                                "Pendiente" -> "Pendiente"
                                                else -> "Sin entregar"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (report.status) {
                                                "Calificada" -> MaterialTheme.colorScheme.onPrimaryContainer
                                                "Pendiente" -> MaterialTheme.colorScheme.onTertiaryContainer
                                                else -> MaterialTheme.colorScheme.onErrorContainer
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (report.status == "Calificada" && !report.comments.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Retroalimentación: ${report.comments}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Metodo principal que ejecuta la operacion: tempAlumnosList.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun tempAlumnosList(list: List<StudentGradeReport>): List<StudentGradeReport> {
    return list.sortedBy { it.nombre }
}

@Composable
/**
 * Componente visual reutilizable para renderizar StatCard.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
```

### `GradebookScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/profesor/GradebookScreen.kt`

```kotlin
/**
 * Pantalla de libro de calificaciones (Gradebook) del profesor,
 * presentando una matriz completa de alumnos versus tareas con calculo automatico de promedios.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.profesor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla GradebookScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun GradebookScreen(
    idClase: String,
    viewModel: GradebookViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(idClase) {
        viewModel.loadGradebook(idClase)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Libreta de Calificaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (!state.isLoading && state.error == null && state.alumnos.isNotEmpty()) {
                        IconButton(onClick = {
                            val uri = viewModel.exportarAExcel(context)
                            if (uri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Exportar Calificaciones"))
                            }
                        }) {
                            Icon(Icons.Filled.Download, contentDescription = "Exportar a Excel")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                
                val columnWidth = 120.dp
                val nameColumnWidth = 150.dp
                val horizontalScrollState = rememberScrollState()

                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    // Fixed Name Header
                    Box(
                        modifier = Modifier
                            .width(nameColumnWidth)
                            .padding(8.dp)
                    ) {
                        Text("Alumno", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    
                    // Scrollable Tasks + Average Header
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        state.tareas.forEach { tarea ->
                            Box(
                                modifier = Modifier
                                    .width(columnWidth)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tarea.titulo,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        // Average Header
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Promedio", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                
                Divider()

                // Data Rows
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.alumnos) { alumno ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Fixed Name Column
                            Box(
                                modifier = Modifier
                                    .width(nameColumnWidth)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = alumno.nombre.ifBlank { alumno.matricula },
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 14.sp
                                )
                            }
                            
                            // Scrollable Tasks + Average Columns
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(horizontalScrollState) // Sync scroll with header
                            ) {
                                state.tareas.forEach { tarea ->
                                    val grade = state.calificaciones["${alumno.idUsuario}_${tarea.idTarea}"]
                                    Box(
                                        modifier = Modifier
                                            .width(columnWidth)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = grade?.toString() ?: "-",
                                            textAlign = TextAlign.Center,
                                            fontSize = 14.sp,
                                            color = if (grade == null) Color.Gray else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                
                                // Average Column
                                val average = state.promedios[alumno.idUsuario]
                                val avgColor = when {
                                    average == null -> Color.Gray
                                    average >= 8.0 -> Color(0xFF388E3C) // Green
                                    average >= 6.0 -> Color(0xFFFBC02D) // Yellow
                                    else -> Color.Red
                                }
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (average != null) String.format("%.1f", average) else "-",
                                        fontWeight = FontWeight.Bold,
                                        color = avgColor,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### `GradebookViewModel.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/profesor/GradebookViewModel.kt`

```kotlin
/**
 * ViewModel que calcula de forma reactiva las calificaciones y promedios generales
 * por alumno, grupo y tarea para la vista de Gradebook.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.profesor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.model.Tarea
import com.pmlp.edutask.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.net.Uri

import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
data class GradebookState(
    val isLoading: Boolean = false,
    val alumnos: List<Usuario> = emptyList(),
    val tareas: List<Tarea> = emptyList(),
    // Map of (alumnoId + "_" + tareaId) to Calificacion (Int)
    val calificaciones: Map<String, Int> = emptyMap(),
    // Map of alumnoId to Average
    val promedios: Map<String, Double> = emptyMap(),
    val error: String? = null
)

class GradebookViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(GradebookState())
    val uiState: StateFlow<GradebookState> = _uiState

    /**
     * Obtiene o recupera datos asociados a loadGradebook desde la base de datos o API.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun loadGradebook(idClase: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 1. Fetch Students in the class
                val alumnosRefs = db.collection("clase_alumno")
                    .whereEqualTo("idClase", idClase)
                    .get().await()
                val alumnoIds = alumnosRefs.documents.mapNotNull { it.getString("idUsuario") }

                val alumnos = mutableListOf<Usuario>()
                if (alumnoIds.isNotEmpty()) {
                    // Fetch student details
                    // Firestore 'whereIn' supports up to 10 items, so we chunk it if necessary
                    val chunks = alumnoIds.chunked(10)
                    for (chunk in chunks) {
                        val usersSnap = db.collection("usuarios")
                            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                            .get().await()
                        alumnos.addAll(usersSnap.documents.mapNotNull { 
                            Usuario(
                                idUsuario = it.id,
                                nombre = it.getString("nombre") ?: "",
                                matricula = it.getString("matricula") ?: ""
                            )
                        })
                    }
                }

                // 2. Fetch Tasks in the class
                val tareasSnap = db.collection("tareas")
                    .whereEqualTo("idClase", idClase)
                    .get().await()
                    
                val tareas = tareasSnap.documents.mapNotNull { doc ->
                    Tarea(
                        idTarea = doc.id,
                        titulo = doc.getString("titulo") ?: "Sin Título",
                        idClase = idClase
                    )
                }
                
                val tareaIds = tareas.map { it.idTarea }

                // 3. Fetch Evidencias (with denormalized calificacion) for these tasks
                val matrix = mutableMapOf<String, Int>()
                if (tareaIds.isNotEmpty()) {
                    val taskChunks = tareaIds.chunked(10)
                    for (chunk in taskChunks) {
                        // Get asignaciones for these tasks
                        val asignacionesSnap = db.collection("asignaciones_tarea")
                            .whereIn("idTarea", chunk)
                            .get().await()

                        // Map idAsignacion to (idAlumno, idTarea)
                        val asigMap = mutableMapOf<String, Pair<String, String>>()
                        val asignacionIds = mutableListOf<String>()
                        
                        for (doc in asignacionesSnap.documents) {
                            val idAsig = doc.id
                            val idAlumno = doc.getString("idUsuario") ?: continue
                            val idTarea = doc.getString("idTarea") ?: continue
                            asigMap[idAsig] = Pair(idAlumno, idTarea)
                            asignacionIds.add(idAsig)
                        }

                        if (asignacionIds.isNotEmpty()) {
                            val asigChunks = asignacionIds.chunked(10)
                            for (asigChunk in asigChunks) {
                                val evidenciasSnap = db.collection("evidencias_tarea")
                                    .whereIn("idAsignacion", asigChunk)
                                    .get().await()
                                    
                                for (ev in evidenciasSnap.documents) {
                                    val califRaw = ev.get("calificacion")
                                    val calificacion = when (califRaw) {
                                        is Number -> califRaw.toInt()
                                        is String -> califRaw.toIntOrNull()
                                        else -> null
                                    }
                                    
                                    val idAsig = ev.getString("idAsignacion")
                                    val pair = asigMap[idAsig]
                                    if (pair != null && calificacion != null) {
                                        matrix["${pair.first}_${pair.second}"] = calificacion
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Calculate averages
                val promedios = mutableMapOf<String, Double>()
                for (alumno in alumnos) {
                    var sum = 0
                    var count = 0
                    for (tarea in tareas) {
                        val grade = matrix["${alumno.idUsuario}_${tarea.idTarea}"]
                        if (grade != null) {
                            sum += grade
                            count++
                        }
                    }
                    if (count > 0) {
                        promedios[alumno.idUsuario] = sum.toDouble() / count.toDouble()
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    alumnos = alumnos.sortedBy { it.nombre },
                    tareas = tareas,
                    calificaciones = matrix,
                    promedios = promedios
                )

            } catch (e: Exception) {
                Log.e("GradebookVM", "Error loading gradebook", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar libreta de calificaciones"
                )
            }
        }
    }

    /**
     * Metodo principal que ejecuta la operacion: exportarAExcel.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun exportarAExcel(context: Context): Uri? {
        val state = _uiState.value
        if (state.alumnos.isEmpty() || state.tareas.isEmpty()) return null

        return try {
            val file = File(context.cacheDir, "calificaciones.csv")
            val fos = FileOutputStream(file)
            

            // CSV Header
            val header = mutableListOf("Alumno")
            state.tareas.forEach { header.add(it.titulo) }
            header.add("Promedio General")
            fos.write((header.joinToString(",") + "\n").toByteArray(Charsets.ISO_8859_1))

            // Rows
            state.alumnos.forEach { alumno ->
                val row = mutableListOf(alumno.nombre)
                state.tareas.forEach { tarea ->
                    val grade = state.calificaciones["${alumno.idUsuario}_${tarea.idTarea}"]
                    row.add(grade?.toString() ?: "-")
                }
                val promedio = state.promedios[alumno.idUsuario]
                row.add(if (promedio != null) java.util.Locale.US.let { String.format(it, "%.1f", promedio) } else "-")
                fos.write((row.joinToString(",") + "\n").toByteArray(Charsets.ISO_8859_1))
            }
            
            fos.flush()
            fos.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Log.e("GradebookVM", "Error exporting CSV", e)
            null
        }
    }
}
```

---

## 8. Módulo del Coordinador

### `HomeCoordinadorScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/coordinador/HomeCoordinadorScreen.kt`

```kotlin
/**
 * Pantalla principal del rol Coordinador con acceso a la gestion de eventos institucionales,
 * administracion de usuarios y metricas del sistema.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.coordinador

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla HomeCoordinadorScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun HomeCoordinadorScreen(
    idUsuario: String,
    nombreCoordinador: String,
    onNavigateToLista: (String) -> Unit,
    onNavigateToEventos: () -> Unit,
    onLogout: () -> Unit
) {
    val initials = nombreCoordinador.split(" ").take(2).joinToString("") { it.first().toString().uppercase() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Surface(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp).size(40.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Hola, ${nombreCoordinador.substringBefore(" ")}!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Coordinador",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Panel de Gestión",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            CoordinadorCard(
                title = "Gestionar Alumnos",
                subtitle = "Inscribir, ver o editar perfiles de alumnos.",
                icon = Icons.Filled.School,
                onClick = { onNavigateToLista("Alumno") }
            )

            CoordinadorCard(
                title = "Gestionar Profesores",
                subtitle = "Ver, editar o registrar docentes.",
                icon = Icons.Filled.Person,
                onClick = { onNavigateToLista("Profesor") }
            )

            CoordinadorCard(
                title = "Eventos Escolares",
                subtitle = "Publicar anuncios o eventos opcionales.",
                icon = Icons.Filled.Event,
                onClick = { onNavigateToEventos() }
            )
        }
    }
}

@Composable
/**
 * Componente visual reutilizable para renderizar CoordinadorCard.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun CoordinadorCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
```

### `CoordinadorViewModel.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/coordinador/CoordinadorViewModel.kt`

```kotlin
/**
 * ViewModel del modulo de Coordinador que gestiona la creacion, edicion y eliminacion
 * de eventos institucionales y el catalogo general de usuarios.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.coordinador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.pmlp.edutask.model.RolUsuario
import com.pmlp.edutask.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class CoordinadorUiState {
    object Idle : CoordinadorUiState()
    object Loading : CoordinadorUiState()
    data class Success(val usuarios: List<Usuario>) : CoordinadorUiState()
    data class Error(val message: String) : CoordinadorUiState()
}

class CoordinadorViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<CoordinadorUiState>(CoordinadorUiState.Idle)
    val uiState: StateFlow<CoordinadorUiState> = _uiState

    init {
        fetchUsuarios()
    }

    /**
     * Obtiene o recupera datos asociados a fetchUsuarios desde la base de datos o API.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun fetchUsuarios() {
        viewModelScope.launch {
            _uiState.value = CoordinadorUiState.Loading
            try {
                val snapshot = db.collection("usuarios").get().await()
                val list = snapshot.documents.mapNotNull { doc ->
                    val rolString = doc.getString("rol") ?: "Alumno"
                    val rolEnum = try {
                        RolUsuario.valueOf(rolString)
                    } catch (e: Exception) {
                        RolUsuario.Alumno
                    }
                    Usuario(
                        idUsuario = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        matricula = doc.getString("matricula") ?: "",
                        correo = doc.getString("correo") ?: "",
                        contrasena = doc.getString("contrasena") ?: "",
                        rol = rolEnum
                    )
                }
                _uiState.value = CoordinadorUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = CoordinadorUiState.Error(e.message ?: "Error al obtener usuarios")
            }
        }
    }

    /**
     * Guarda o actualiza los datos de saveUsuario en la base de datos.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun saveUsuario(usuario: Usuario, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                var finalIdUsuario = usuario.idUsuario
                var finalMatricula = usuario.matricula

                if (finalIdUsuario.isEmpty()) {
                    val initials = usuario.nombre.trim().split("\\s+".toRegex())
                        .filter { it.isNotEmpty() }
                        .map { it.first().uppercaseChar() }
                        .joinToString("")
                    val prefix = when (usuario.rol) {
                        RolUsuario.Alumno -> "A-"
                        RolUsuario.Profesor -> "P-"
                        RolUsuario.Coordinador -> "C-"
                    }
                    val baseId = "$prefix$initials"
                    var idToTry = baseId
                    var counter = 1
                    while (db.collection("usuarios").document(idToTry).get().await().exists()) {
                        idToTry = "$baseId-$counter"
                        counter++
                    }
                    finalIdUsuario = idToTry
                    finalMatricula = idToTry
                }

                val map = mapOf(
                    "nombre" to usuario.nombre,
                    "matricula" to finalMatricula,
                    "correo" to usuario.correo,
                    "contrasena" to usuario.contrasena,
                    "rol" to usuario.rol.name
                )

                db.collection("usuarios").document(finalIdUsuario).set(map).await()
                fetchUsuarios()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al guardar usuario")
            }
        }
    }

    /**
     * Elimina el registro correspondiente a deleteUsuario del sistema.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun deleteUsuario(idUsuario: String) {
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(idUsuario).delete().await()
                fetchUsuarios()
            } catch (e: Exception) {
                _uiState.value = CoordinadorUiState.Error(e.message ?: "Error al eliminar usuario")
            }
        }
    }
}
```

### `FormularioEventoScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/coordinador/FormularioEventoScreen.kt`

```kotlin
/**
 * Pantalla con formulario para que el coordinador publique o actualice eventos y avisos
 * institucionales con titulo, fecha, descripcion, lugar e imagen.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.coordinador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.pmlp.edutask.model.Evento
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.edutask.ui.EventosUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla FormularioEventoScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun FormularioEventoScreen(
    viewModel: EventosSharedViewModel,
    idEvento: String?,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = idEvento != null

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf<String?>(null) }
    
    var hasCustomDate by remember { mutableStateOf(false) }
    var customDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idEvento, uiState) {
        if (isEdit && uiState is EventosUiState.Success) {
            val evento = (uiState as EventosUiState.Success).eventos.find { it.idEvento == idEvento }
            if (evento != null) {
                titulo = evento.titulo
                descripcion = evento.descripcion
                lugar = evento.lugar
                imagenUrl = evento.imagenUrl
                customDate = evento.fechaPublicacion
                hasCustomDate = true
            }
        }
    }
    
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val compressedBase64 = compressImageToBase64(context, it)
            if (compressedBase64 != null) {
                imagenUrl = compressedBase64
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Editar Evento o Anuncio" else "Nuevo Evento o Anuncio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMsg != null) {
                Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del Evento o Anuncio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5
            )

            OutlinedTextField(
                value = lugar,
                onValueChange = { lugar = it },
                label = { Text("Lugar del Evento (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Selector de Imagen
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Imagen de Fondo (Opcional)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                if (imagenUrl != null) {
                    val imageModel = remember(imagenUrl) {
                        try {
                            val base64Str = if (imagenUrl!!.startsWith("data:image")) imagenUrl!!.substringAfter("base64,") else imagenUrl!!
                            if (base64Str.length > 500 && !base64Str.startsWith("http")) {
                                val bytes = Base64.decode(base64Str, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } else {
                                imagenUrl
                            }
                        } catch (e: Exception) {
                            imagenUrl
                        }
                    }
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Vista previa",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (imagenUrl == null) "Seleccionar Foto" else "Cambiar Foto")
                }
                if (imagenUrl != null) {
                    TextButton(onClick = { imagenUrl = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("Quitar Foto", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Añadir fecha al evento",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = hasCustomDate,
                    onCheckedChange = { hasCustomDate = it }
                )
            }

            if (hasCustomDate) {
                val dateFormat = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
                val timeFormat = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
                
                val fechaFormateada = remember(customDate) { dateFormat.format(java.util.Date(customDate)) }
                val horaFormateada = remember(customDate) { timeFormat.format(java.util.Date(customDate)) }
                
                var showDatePicker by remember { mutableStateOf(false) }
                var showTimePicker by remember { mutableStateOf(false) }

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = customDate
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { selectedMillis ->
                                        val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                            timeInMillis = selectedMillis
                                        }
                                        val newCal = java.util.Calendar.getInstance().apply {
                                            timeInMillis = customDate
                                            set(java.util.Calendar.YEAR, utcCal.get(java.util.Calendar.YEAR))
                                            set(java.util.Calendar.MONTH, utcCal.get(java.util.Calendar.MONTH))
                                            set(java.util.Calendar.DAY_OF_MONTH, utcCal.get(java.util.Calendar.DAY_OF_MONTH))
                                        }
                                        customDate = newCal.timeInMillis
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                if (showTimePicker) {
                    val calendar = remember(customDate) {
                        java.util.Calendar.getInstance().apply { timeInMillis = customDate }
                    }
                    val timePickerState = rememberTimePickerState(
                        initialHour = calendar.get(java.util.Calendar.HOUR_OF_DAY),
                        initialMinute = calendar.get(java.util.Calendar.MINUTE),
                        is24Hour = true
                    )
                    
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val updatedCal = java.util.Calendar.getInstance().apply {
                                        timeInMillis = customDate
                                        set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                                        set(java.util.Calendar.MINUTE, timePickerState.minute)
                                        set(java.util.Calendar.SECOND, 0)
                                        set(java.util.Calendar.MILLISECOND, 0)
                                    }
                                    customDate = updatedCal.timeInMillis
                                    showTimePicker = false
                                }
                            ) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text("Cancelar")
                            }
                        },
                        text = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                TimePicker(state = timePickerState)
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("Fecha: $fechaFormateada")
                    }

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("Hora: $horaFormateada")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (titulo.isBlank() || descripcion.isBlank()) {
                        errorMsg = "Todos los campos son obligatorios"
                        return@Button
                    }
                    isLoading = true
                    errorMsg = null
                    
                    val finalFecha = if (hasCustomDate) customDate else System.currentTimeMillis()
                    
                    val evento = Evento(
                        idEvento = idEvento ?: "",
                        titulo = titulo,
                        descripcion = descripcion,
                        lugar = lugar,
                        fechaPublicacion = finalFecha,
                        imagenUrl = imagenUrl
                    )
                    viewModel.saveEvento(
                        evento = evento,
                        onSuccess = {
                            isLoading = false
                            onBack()
                        },
                        onError = {
                            isLoading = false
                            errorMsg = it
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (isEdit) "Guardar Cambios" else "Publicar Evento o Anuncio")
                }
            }
        }
    }
}

/**
 * Realiza el procesamiento y conversion de archivos (compressImageToBase64).
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun compressImageToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Reducir la imagen a máximo 800px de ancho/alto manteniendo la proporción
        val maxDimension = 800
        val ratio = Math.min(
            maxDimension.toFloat() / originalBitmap.width,
            maxDimension.toFloat() / originalBitmap.height
        )
        val width = Math.round(ratio * originalBitmap.width)
        val height = Math.round(ratio * originalBitmap.height)

        val scaledBitmap = if (ratio < 1f) {
            Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        } else {
            originalBitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
```

### `ListaEventosScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/coordinador/ListaEventosScreen.kt`

```kotlin
/**
 * Pantalla que despliega el listado completo de eventos y noticias escolares registradas,
 * con opciones para crear, editar o eliminar publicaciones.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.coordinador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.Evento
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.edutask.ui.EventosUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla ListaEventosScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun ListaEventosScreen(
    viewModel: EventosSharedViewModel,
    onBack: () -> Unit,
    onNavigateToFormulario: (String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchEventos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos Escolares", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToFormulario(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar Evento")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is EventosUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is EventosUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is EventosUiState.Success -> {
                    val eventos = state.eventos
                    if (eventos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay eventos registrados.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(eventos) { evento ->
                                EventoItem(
                                    evento = evento,
                                    onEdit = { onNavigateToFormulario(evento.idEvento) },
                                    onDelete = { viewModel.deleteEvento(evento.idEvento) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
/**
 * Componente visual reutilizable para renderizar EventoItem.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EventoItem(
    evento: Evento,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val fechaFormat = dateFormat.format(Date(evento.fechaPublicacion))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = evento.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = evento.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Publicado: $fechaFormat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
```

### `FormularioUsuarioScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/coordinador/FormularioUsuarioScreen.kt`

```kotlin
/**
 * Pantalla con formulario para registrar o actualizar datos de usuarios (alumnos, profesores,
 * coordinadores), asignando roles y credenciales.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.coordinador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.RolUsuario
import com.pmlp.edutask.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla FormularioUsuarioScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun FormularioUsuarioScreen(
    viewModel: CoordinadorViewModel,
    idUsuario: String?,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = idUsuario != null

    var nombre by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.Alumno) }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idUsuario, uiState) {
        if (isEdit && uiState is CoordinadorUiState.Success) {
            val user = (uiState as CoordinadorUiState.Success).usuarios.find { it.idUsuario == idUsuario }
            if (user != null) {
                nombre = user.nombre
                matricula = user.matricula
                correo = user.correo
                contrasena = user.contrasena
                rolSeleccionado = user.rol
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Editar Usuario" else "Nuevo Usuario", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMsg != null) {
                Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo Electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = rolSeleccionado.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    RolUsuario.entries.forEach { rol ->
                        DropdownMenuItem(
                            text = { Text(rol.name) },
                            onClick = {
                                rolSeleccionado = rol
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                        errorMsg = "Todos los campos son obligatorios"
                        return@Button
                    }
                    isLoading = true
                    errorMsg = null
                    val usuario = Usuario(
                        idUsuario = idUsuario ?: "",
                        nombre = nombre,
                        matricula = matricula,
                        correo = correo,
                        contrasena = contrasena,
                        rol = rolSeleccionado
                    )
                    viewModel.saveUsuario(
                        usuario = usuario,
                        onSuccess = {
                            isLoading = false
                            onBack()
                        },
                        onError = {
                            isLoading = false
                            errorMsg = it
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (isEdit) "Guardar Cambios" else "Crear Usuario")
                }
            }
        }
    }
}
```

### `ListaUsuariosScreen.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/coordinador/ListaUsuariosScreen.kt`

```kotlin
/**
 * Pantalla con el directorio de usuarios del sistema, permitiendo busquedas, filtros por rol
 * y gestion de cuentas de usuario.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.coordinador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmlp.edutask.model.RolUsuario
import com.pmlp.edutask.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla ListaUsuariosScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun ListaUsuariosScreen(
    viewModel: CoordinadorViewModel,
    filtroInicial: String, // "Alumno" o "Profesor"
    onBack: () -> Unit,
    onNavigateToFormulario: (String?) -> Unit // idUsuario or null for create
) {
    var tabIndex by remember { mutableStateOf(if (filtroInicial == "Profesor") 1 else 0) }
    val tabs = listOf("Alumnos", "Profesores")

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUsuarios()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToFormulario(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar Usuario")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (val state = uiState) {
                is CoordinadorUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CoordinadorUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is CoordinadorUiState.Success -> {
                    val currentRol = if (tabIndex == 0) RolUsuario.Alumno else RolUsuario.Profesor
                    val filteredUsers = state.usuarios.filter { it.rol == currentRol }

                    if (filteredUsers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay usuarios registrados con este rol.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredUsers) { usuario ->
                                UsuarioItem(
                                    usuario = usuario,
                                    onEdit = { onNavigateToFormulario(usuario.idUsuario) },
                                    onDelete = { viewModel.deleteUsuario(usuario.idUsuario) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
/**
 * Componente visual reutilizable para renderizar UsuarioItem.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun UsuarioItem(
    usuario: Usuario,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = usuario.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Matrícula: ${usuario.matricula}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Correo: ${usuario.correo}", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
```

---

## 9. Componentes Reutilizables de Interfaz

### `VisorArchivoDialog.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/components/VisorArchivoDialog.kt`

```kotlin
/**
 * Dialogo modal reutilizable para previsualizar archivos, documentos e imagenes adjuntas
 * directamente dentro de la aplicacion.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Metodo principal que ejecuta la operacion: VisorArchivoDialog.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun VisorArchivoDialog(
    base64String: String,
    nombreArchivo: String,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Para imágenes
    var bitmapImage by remember { mutableStateOf<Bitmap?>(null) }
    // Para PDFs
    var pdfPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    val isPdf = nombreArchivo.endsWith(".pdf", ignoreCase = true) || base64String.startsWith("JVBERi0") // Comienzo común de PDF en base64

    LaunchedEffect(base64String) {
        isLoading = true
        error = null
        try {
            withContext(Dispatchers.IO) {
                // Limpiar string de Base64 si contiene prefijos (ej: data:image/png;base64,)
                val cleanString = if (base64String.contains(",")) {
                    base64String.substring(base64String.indexOf(",") + 1)
                } else {
                    base64String
                }
                
                val bytes = Base64.decode(cleanString, Base64.DEFAULT)
                
                if (isPdf) {
                    // Procesar PDF
                    val tempFile = File.createTempFile("visor_temp", ".pdf", context.cacheDir)
                    FileOutputStream(tempFile).use { it.write(bytes) }
                    
                    val fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    val pdfRenderer = PdfRenderer(fileDescriptor)
                    val pages = mutableListOf<Bitmap>()
                    
                    val screenWidth = context.resources.displayMetrics.widthPixels
                    
                    for (i in 0 until pdfRenderer.pageCount) {
                        val page = pdfRenderer.openPage(i)
                        // Escalar el PDF para mantener calidad
                        val scale = screenWidth.toFloat() / page.width.toFloat()
                        val bmp = Bitmap.createBitmap(
                            (page.width * scale).toInt(),
                            (page.height * scale).toInt(),
                            Bitmap.Config.ARGB_8888
                        )
                        // Fondo blanco para el PDF
                        bmp.eraseColor(android.graphics.Color.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        pages.add(bmp)
                        page.close()
                    }
                    pdfRenderer.close()
                    fileDescriptor.close()
                    tempFile.delete()
                    pdfPages = pages
                } else {
                    // Procesar Imagen
                    val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bmp != null) {
                        bitmapImage = bmp
                    } else {
                        error = "No se pudo decodificar la imagen"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            error = "Error al abrir archivo: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                TopAppBar(
                    title = { Text(nombreArchivo, style = MaterialTheme.typography.titleMedium, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar visor")
                        }
                    }
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text("Cargando archivo...", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else if (error != null) {
                        Text(
                            text = error ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        if (isPdf && pdfPages.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(pdfPages.size) { index ->
                                    Image(
                                        bitmap = pdfPages[index].asImageBitmap(),
                                        contentDescription = "Página ${index + 1} de PDF",
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                            }
                        } else if (bitmapImage != null) {
                            Image(
                                bitmap = bitmapImage!!.asImageBitmap(),
                                contentDescription = "Imagen de evidencia",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text("No se pudo cargar el contenido", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
```

### `SkeletonLoader.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/components/SkeletonLoader.kt`

```kotlin
/**
 * Componente de carga visual (Skeleton / Shimmer) que muestra marcadores de posicion animados
 * mientras se cargan datos asincronos desde la red o base de datos.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
/**
 * Metodo principal que ejecuta la operacion: ShimmerPlaceholder.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                shape = shape
            )
    )
}
```

### `EmptyStateIllustration.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/components/EmptyStateIllustration.kt`

```kotlin
/**
 * Componente reutilizable de interfaz que renderiza ilustraciones y mensajes informativos
 * cuando una lista o seccion no cuenta con datos para mostrar.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
/**
 * Metodo principal que ejecuta la operacion: EmptyStateIllustration.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EmptyStateIllustration(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
```

---

## 10. Tema y Estilos

### `Color.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/theme/Color.kt`

```kotlin
/**
 * Definicion de la paleta de colores del tema Material 3 para la aplicacion movil EduTask.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.theme

import androidx.compose.ui.graphics.Color

// Light palette
val EduPrimary             = Color(0xFF4355B9)
val EduOnPrimary           = Color.White
val EduPrimaryContainer    = Color(0xFFDEE0FF)
val EduOnPrimaryContainer  = Color(0xFF00105C)
val EduSecondary           = Color(0xFF5B5D72)
val EduOnSecondary         = Color.White
val EduSecondaryContainer  = Color(0xFFE0E1F9)
val EduOnSecondaryContainer = Color(0xFF181A2C)
val EduTertiary            = Color(0xFF77536D)
val EduOnTertiary          = Color.White
val EduTertiaryContainer   = Color(0xFFFFD7F1)
val EduOnTertiaryContainer = Color(0xFF2D1228)
val EduError               = Color(0xFFBA1A1A)
val EduOnError             = Color.White
val EduErrorContainer      = Color(0xFFFFDAD6)
val EduOnErrorContainer    = Color(0xFF93000A)
val EduBackground          = Color(0xFFFEFBFF)
val EduOnBackground        = Color(0xFF1B1B1F)
val EduSurface             = Color(0xFFFEFBFF)
val EduOnSurface           = Color(0xFF1B1B1F)
val EduSurfaceVariant      = Color(0xFFE3E1EC)
val EduOnSurfaceVariant    = Color(0xFF46464F)
val EduOutline             = Color(0xFF777680)
val EduOutlineVariant      = Color(0xFFC7C5D0)
val EduScrim               = Color(0xFF000000)
val EduInverseSurface      = Color(0xFF313034)
val EduInverseOnSurface    = Color(0xFFF3EFF4)
val EduInversePrimary      = Color(0xFFBBC2FF)
val EduSurfaceTint         = Color(0xFF4355B9)
val EduSurfaceContainerLowest  = Color(0xFFFFFBFF)
val EduSurfaceContainerLow     = Color(0xFFF5F2FB)
val EduSurfaceContainer        = Color(0xFFEFECF5)
val EduSurfaceContainerHigh    = Color(0xFFE9E7EF)
val EduSurfaceContainerHighest = Color(0xFFE4E1EA)

// Dark palette
val EduPrimaryDark             = Color(0xFFBBC2FF)
val EduOnPrimaryDark           = Color(0xFF08218A)
val EduPrimaryContainerDark    = Color(0xFF293CA0)
val EduOnPrimaryContainerDark  = Color(0xFFDEE0FF)
val EduSecondaryDark           = Color(0xFFC4C5DD)
val EduOnSecondaryDark         = Color(0xFF2D2F42)
val EduSecondaryContainerDark  = Color(0xFF434659)
val EduOnSecondaryContainerDark = Color(0xFFE0E1F9)
val EduTertiaryDark            = Color(0xFFE5B9D5)
val EduOnTertiaryDark          = Color(0xFF44263D)
val EduTertiaryContainerDark   = Color(0xFF5D3C54)
val EduOnTertiaryContainerDark = Color(0xFFFFD7F1)
val EduErrorDark               = Color(0xFFFFB4AB)
val EduOnErrorDark             = Color(0xFF690005)
val EduErrorContainerDark      = Color(0xFF93000A)
val EduOnErrorContainerDark    = Color(0xFFFFDAD6)
val EduBackgroundDark          = Color(0xFF1B1B1F)
val EduOnBackgroundDark        = Color(0xFFE5E1E6)
val EduSurfaceDark             = Color(0xFF1B1B1F)
val EduOnSurfaceDark           = Color(0xFFE5E1E6)
val EduSurfaceVariantDark      = Color(0xFF46464F)
val EduOnSurfaceVariantDark    = Color(0xFFC7C5D0)
val EduOutlineDark             = Color(0xFF918F9A)
val EduOutlineVariantDark      = Color(0xFF46464F)
val EduInverseSurfaceDark      = Color(0xFFE5E1E6)
val EduInverseOnSurfaceDark    = Color(0xFF313034)
val EduInversePrimaryDark      = Color(0xFF4355B9)
val EduSurfaceContainerLowestDark  = Color(0xFF0F0F14)
val EduSurfaceContainerLowDark     = Color(0xFF1B1B1F)
val EduSurfaceContainerDark        = Color(0xFF201F23)
val EduSurfaceContainerHighDark    = Color(0xFF2B2930)
val EduSurfaceContainerHighestDark = Color(0xFF36343B)
```

### `Theme.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/theme/Theme.kt`

```kotlin
/**
 * Configuracion del tema visual de Compose para la app movil, soportando modo claro/oscuro
 * y esquemas de color personalizados.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary              = EduPrimary,
    onPrimary            = EduOnPrimary,
    primaryContainer     = EduPrimaryContainer,
    onPrimaryContainer   = EduOnPrimaryContainer,
    secondary            = EduSecondary,
    onSecondary          = EduOnSecondary,
    secondaryContainer   = EduSecondaryContainer,
    onSecondaryContainer = EduOnSecondaryContainer,
    tertiary             = EduTertiary,
    onTertiary           = EduOnTertiary,
    tertiaryContainer    = EduTertiaryContainer,
    onTertiaryContainer  = EduOnTertiaryContainer,
    error                = EduError,
    onError              = EduOnError,
    errorContainer       = EduErrorContainer,
    onErrorContainer     = EduOnErrorContainer,
    background           = EduBackground,
    onBackground         = EduOnBackground,
    surface              = EduSurface,
    onSurface            = EduOnSurface,
    surfaceVariant       = EduSurfaceVariant,
    onSurfaceVariant     = EduOnSurfaceVariant,
    outline              = EduOutline,
    outlineVariant       = EduOutlineVariant,
    scrim                = EduScrim,
    inverseSurface       = EduInverseSurface,
    inverseOnSurface     = EduInverseOnSurface,
    inversePrimary       = EduInversePrimary,
    surfaceTint          = EduSurfaceTint,
    surfaceContainerLowest  = EduSurfaceContainerLowest,
    surfaceContainerLow     = EduSurfaceContainerLow,
    surfaceContainer        = EduSurfaceContainer,
    surfaceContainerHigh    = EduSurfaceContainerHigh,
    surfaceContainerHighest = EduSurfaceContainerHighest
)

private val DarkColorScheme = darkColorScheme(
    primary              = EduPrimaryDark,
    onPrimary            = EduOnPrimaryDark,
    primaryContainer     = EduPrimaryContainerDark,
    onPrimaryContainer   = EduOnPrimaryContainerDark,
    secondary            = EduSecondaryDark,
    onSecondary          = EduOnSecondaryDark,
    secondaryContainer   = EduSecondaryContainerDark,
    onSecondaryContainer = EduOnSecondaryContainerDark,
    tertiary             = EduTertiaryDark,
    onTertiary           = EduOnTertiaryDark,
    tertiaryContainer    = EduTertiaryContainerDark,
    onTertiaryContainer  = EduOnTertiaryContainerDark,
    error                = EduErrorDark,
    onError              = EduOnErrorDark,
    errorContainer       = EduErrorContainerDark,
    onErrorContainer     = EduOnErrorContainerDark,
    background           = EduBackgroundDark,
    onBackground         = EduOnBackgroundDark,
    surface              = EduSurfaceDark,
    onSurface            = EduOnSurfaceDark,
    surfaceVariant       = EduSurfaceVariantDark,
    onSurfaceVariant     = EduOnSurfaceVariantDark,
    outline              = EduOutlineDark,
    outlineVariant       = EduOutlineVariantDark,
    inverseSurface       = EduInverseSurfaceDark,
    inverseOnSurface     = EduInverseOnSurfaceDark,
    inversePrimary       = EduInversePrimaryDark,
    surfaceContainerLowest  = EduSurfaceContainerLowestDark,
    surfaceContainerLow     = EduSurfaceContainerLowDark,
    surfaceContainer        = EduSurfaceContainerDark,
    surfaceContainerHigh    = EduSurfaceContainerHighDark,
    surfaceContainerHighest = EduSurfaceContainerHighestDark
)

@Composable
/**
 * Metodo principal que ejecuta la operacion: EduTaskTheme.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EduTaskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, typography = EduTaskTypography, content = content)
}
```

### `Type.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/ui/theme/Type.kt`

```kotlin
/**
 * Configuracion de estilos tipograficos (Typography) para la aplicacion movil EduTask.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pmlp.edutask.R

val PlayfairDisplay = FontFamily(Font(R.font.playfair_display, FontWeight.Bold))
val RobotoFamily    = FontFamily(Font(R.font.roboto, FontWeight.Normal))

val EduTaskTypography = Typography(
    displayMedium = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,   fontSize = 36.sp, lineHeight = 44.sp),
    displaySmall  = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,   fontSize = 28.sp, lineHeight = 36.sp),
    headlineLarge = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Bold,      fontSize = 28.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge   = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall   = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp)
)
```

---

## 11. Workers y Utilidades

### `TareaReminderWorker.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/worker/TareaReminderWorker.kt`

```kotlin
/**
 * Worker en segundo plano (WorkManager) para enviar recordatorios y notificaciones
 * sobre tareas proximas a vencer a los estudiantes.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pmlp.edutask.R

class TareaReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    /**
     * Metodo principal que ejecuta la operacion: doWork.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override suspend fun doWork(): Result {
        val tareaNombre = inputData.getString("TAREA_NOMBRE") ?: "Tarea pendiente"
        val notifId = inputData.getString("TAREA_ID")?.hashCode() ?: System.currentTimeMillis().toInt()

        mostrarNotificacion(tareaNombre, notifId)

        return Result.success()
    }

    /**
     * Metodo principal que ejecuta la operacion: mostrarNotificacion.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private fun mostrarNotificacion(tareaNombre: String, notifId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, "edutask_reminders")
            .setSmallIcon(R.mipmap.ic_launcher_round) // Asegurar que exista
            .setContentTitle("¡Recordatorio de Entrega!")
            .setContentText("Tu tarea '$tareaNombre' vence en menos de 2 horas. ¡No olvides entregarla!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notifId, builder.build())
    }
}
```

### `FirestoreUtils.kt`
**Ruta:** `app/src/main/java/com/pmlp/edutask/utils/FirestoreUtils.kt`

```kotlin
/**
 * Funciones de utilidad y extensiones para operaciones con Firebase Firestore
 * (conversion de datos, queries seguras y manejo de colecciones).
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

/**
 * Extrae una fecha de manera segura, soportando:
 * - Timestamp de Firebase
 * - Map (exportado como {_seconds, _nanoseconds})
 * - String (ISO-8601 o numérico)
 * - Number (milisegundos o segundos)
 */
fun DocumentSnapshot.getSafeDate(field: String): Date? {
    val fechaRaw = this.get(field) ?: return null
    return try {
        when (fechaRaw) {
            is Timestamp -> fechaRaw.toDate()
            is Date -> fechaRaw
            is String -> {
                try {
                    Date(java.time.Instant.parse(fechaRaw).toEpochMilli())
                } catch (_: Exception) {
                    fechaRaw.toLongOrNull()?.let { Date(it) }
                }
            }
            is Number -> {
                // Asumimos milisegundos. Si es demasiado pequeño, podría ser segundos.
                val value = fechaRaw.toLong()
                if (value < 100000000000L) Date(value * 1000) else Date(value)
            }
            is Map<*, *> -> {
                val seconds = (fechaRaw["_seconds"] as? Number)?.toLong() ?: 0L
                Date(seconds * 1000)
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
```

---

## Guía de Compilación y Ejecución

```bash
# Compilar APK de depuración
./gradlew :app:assembleDebug

# Instalar en dispositivo Android o emulador conectado
adb install app/build/outputs/apk/debug/app-debug.apk
```
