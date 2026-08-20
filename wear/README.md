# Módulo Wear OS — Documentación Técnica Completa

> **Proyecto:** EduTask — Plataforma de Gestión Académica
> **Módulo:** `:wear` — Aplicación para Smartwatch (Wear OS)
> **Propósito:** Permitir al profesor recibir alertas en tiempo real cuando un alumno envía una tarea, previsualizar la evidencia fotográfica y calificar de forma rápida directamente desde el reloj inteligente.

---

## Tabla de Contenidos

1. [Dependencias y Librerías](#1-dependencias-y-librerías)
2. [Arquitectura General](#2-arquitectura-general)
3. [Archivo 1: Theme.kt — Tema Visual](#3-archivo-1-themekt--tema-visual-de-wear-os)
4. [Archivo 2: CalificarViewModel.kt — Lógica de Negocio](#4-archivo-2-calificarviewmodelkt--lógica-de-negocio)
5. [Archivo 3: MainActivityWear.kt — Actividad Principal y Navegación](#5-archivo-3-mainactivitywearkt--actividad-principal-y-navegación)
6. [Archivo 4: PendientesScreen.kt — Lista de Evidencias Pendientes](#6-archivo-4-pendientesscreenkt--lista-de-evidencias-pendientes)
7. [Archivo 5: CalificarScreen.kt — Pantalla de Calificación Rápida](#7-archivo-5-calificarscreenkt--pantalla-de-calificación-rápida)
8. [Archivo 6: SessionListenerService.kt — Sincronización con Móvil](#8-archivo-6-sessionlistenerservicekt--sincronización-con-móvil)
9. [Archivo 7: TaskNotificationService.kt — Notificaciones en Segundo Plano](#9-archivo-7-tasknotificationservicekt--notificaciones-en-segundo-plano)

---

## 1. Dependencias y Librerías

Archivo de configuración de Gradle donde se declaran todas las librerías que utiliza este módulo:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.pmlp.wear"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.pmlp.edutask"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.compose.ui.tooling)
    implementation(libs.play.services.wearable)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    
    // Wear OS Compose Fundamental
    implementation("androidx.wear.compose:compose-material3:1.0.0-alpha23")
    implementation("androidx.wear.compose:compose-foundation:1.3.1")

    // Firebase para Wear OS
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Carga de Imágenes desde la nube
    implementation("io.coil-kt:coil-compose:2.6.0")
}
```

**Explicación de cada dependencia:**

| Línea | Librería | Para qué se usa |
|---|---|---|
| `compose-material3` (línea 57) | Componentes Material 3 para Wear OS | Botones (`Button`), textos (`Text`), temas (`MaterialTheme`), scaffold (`AppScaffold`, `ScreenScaffold`) optimizados para pantallas circulares |
| `compose-foundation` (línea 58) | Contenedores scroll | `TransformingLazyColumn` que ajusta automáticamente los elementos visibles en pantallas redondas |
| `firebase-firestore` (línea 62) | Base de datos en la nube | Leer evidencias pendientes, guardar calificaciones, escuchar cambios en tiempo real |
| `play-services-wearable` (línea 50) | Comunicación reloj ↔ móvil | Data Layer API para recibir la sesión del profesor desde el smartphone |
| `coil-compose` (línea 67) | Carga de imágenes | Mostrar fotos de evidencia que vienen como URLs remotas |
| `coroutines-play-services` (línea 64) | Extensión `await()` | Convertir las llamadas asíncronas de Firebase (`Task<T>`) en código secuencial usando corrutinas |

---

## 2. Arquitectura General

```
┌────────────────────────────────────────────────────────────────┐
│                     SMARTWATCH (Wear OS)                       │
│                                                                │
│  ┌──────────────────┐    ┌─────────────────────────────────┐  │
│  │ SessionListener  │    │  TaskNotificationService         │  │
│  │ Service          │    │  (Servicio en segundo plano)     │  │
│  │                  │    │                                   │  │
│  │ Recibe sesión    │    │  Escucha evidencias_tarea         │  │
│  │ del móvil vía    │    │  en Firestore y muestra           │  │
│  │ Data Layer API   │    │  notificaciones con vibración     │  │
│  └───────┬──────────┘    └──────────┬──────────────────────┘  │
│          │ SharedPreferences         │                         │
│          ▼                           ▼                         │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              MainActivityWear                             │ │
│  │  ┌──────────────────────────────────────────────────────┐│ │
│  │  │           EduTaskWearApp() [Composable raíz]         ││ │
│  │  │                                                       ││ │
│  │  │  Máquina de estados (WearDestino):                    ││ │
│  │  │                                                       ││ │
│  │  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ││ │
│  │  │  │ Pendientes  │→ │ NuevaEntrega │→ │ Calificar  │  ││ │
│  │  │  │ Screen      │  │ Screen       │  │ Screen     │  ││ │
│  │  │  └─────────────┘  └──────┬───────┘  └─────┬──────┘  ││ │
│  │  │                          │                  │         ││ │
│  │  │                          ▼                  │         ││ │
│  │  │                   ┌────────────┐            │         ││ │
│  │  │                   │ VerFoto    │            │         ││ │
│  │  │                   │ Screen     │            │         ││ │
│  │  │                   └────────────┘            │         ││ │
│  │  │                                             ▼         ││ │
│  │  │                                   CalificarViewModel  ││ │
│  │  │                                   (Firebase Firestore)││ │
│  │  └───────────────────────────────────────────────────────┘│ │
│  └──────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────┘
```

---

## 3. Archivo 1: `Theme.kt` — Tema Visual de Wear OS

**Ruta:** `wear/src/main/java/com/pmlp/wear/presentation/theme/Theme.kt`

**¿Qué hace?** Define el tema visual global para toda la interfaz del smartwatch. Utiliza `MaterialTheme` de `androidx.wear.compose.material3`, que viene preconfigurado con colores, tipografías y formas optimizadas para pantallas pequeñas circulares.

```kotlin
/**
 * Configuracion del tema visual de Compose para Wear OS optimizado para pantallas circulares/rectangulares.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
/**
 * Metodo principal que ejecuta la operacion: EdutaskTheme.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EdutaskTheme(
    content: @Composable () -> Unit
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        content = content
    )
}
```

**Explicación paso a paso:**
- **Línea 10:** La función `EdutaskTheme` es un composable envolvente que recibe como parámetro `content` (todo el contenido visual de la app).
- **Líneas 17-19:** Aplica el `MaterialTheme` de Wear OS Material 3. Al no especificar `colorScheme` ni `typography` personalizados, utiliza los valores predeterminados del sistema que se adaptan al modo oscuro del reloj.

---

## 4. Archivo 2: `CalificarViewModel.kt` — Lógica de Negocio

**Ruta:** `wear/src/main/java/com/pmlp/wear/presentation/CalificarViewModel.kt`

**¿Qué hace?** Contiene toda la lógica de negocio del smartwatch:
1. Define el modelo de datos `EvidenciaPendiente` (una entrega de tarea sin calificar).
2. Define los estados de la interfaz (`CalificarUiState`).
3. Carga las evidencias pendientes del profesor autenticado desde Firestore.
4. Guarda las calificaciones en Firestore cuando el profesor elige una nota.

```kotlin
/**
 * ViewModel para Wear OS que procesa la calificacion de evidencias y actualiza Firestore.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.wear.presentation

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── Modelo de datos ──────────────────────────────────────────────────────────
data class EvidenciaPendiente(
    val id:           String = "",
    val nombreAlumno: String = "",
    val tituloTarea:  String = "",
    val idAsignacion: String = "",
    val fotos:        List<String> = emptyList(),
    val tieneArchivosNoImagen: Boolean = false
)

// ── Estados UI ───────────────────────────────────────────────────────────────
sealed class CalificarUiState {
    object Cargando    : CalificarUiState()
    object Idle        : CalificarUiState()
    object Exito       : CalificarUiState()
    data class Error(val msg: String) : CalificarUiState()
    data class ListaLista(val items: List<EvidenciaPendiente>) : CalificarUiState()
}

class CalificarViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<CalificarUiState>(CalificarUiState.Cargando)
    val uiState: StateFlow<CalificarUiState> = _uiState.asStateFlow()

    // ── Cargar evidencias pendientes ─────────────────────────────────────────
    init { cargarPendientes() }

    /**
     * Metodo principal que ejecuta la operacion: cargarPendientes.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun cargarPendientes() {
        viewModelScope.launch {
            _uiState.value = CalificarUiState.Cargando
            try {
                // Retrieve synchronized professor ID from SharedPreferences (default: "profesor_001")
                val prefs = getApplication<Application>().getSharedPreferences("edutask_wear_prefs", Context.MODE_PRIVATE)
                val idProfesor = prefs.getString("idUsuario", "profesor_001") ?: "profesor_001"

                // 1. Fetch classes of this professor
                val classesSnap = db.collection("clases")
                    .whereEqualTo("idUsuario", idProfesor)
                    .get()
                    .await()
                val classIds = classesSnap.documents.map { it.id }

                if (classIds.isEmpty()) {
                    _uiState.value = CalificarUiState.ListaLista(emptyList())
                    return@launch
                }

                // 2. Fetch tasks for these classes
                val tasksSnap = db.collection("tareas")
                    .whereIn("idClase", classIds)
                    .get()
                    .await()
                val taskIds = tasksSnap.documents.map { it.id }

                if (taskIds.isEmpty()) {
                    _uiState.value = CalificarUiState.ListaLista(emptyList())
                    return@launch
                }

                // 3. Fetch assignments matching these tasks
                val assignmentsSnap = db.collection("asignaciones_tarea")
                    .get()
                    .await()
                val assignmentIds = assignmentsSnap.documents
                    .filter { doc -> (doc.getString("idTarea") ?: "") in taskIds }
                    .map { doc -> doc.id }

                if (assignmentIds.isEmpty()) {
                    _uiState.value = CalificarUiState.ListaLista(emptyList())
                    return@launch
                }

                // 4. Fetch pending evidence matching these assignments
                val evidencesSnap = db.collection("evidencias_tarea")
                    .whereEqualTo("estado", "Pendiente")
                    .get()
                    .await()

                val lista = evidencesSnap.documents
                    .filter { doc -> (doc.getString("idAsignacion") ?: "") in assignmentIds }
                    .map { doc ->
                        val fotos = mutableListOf<String>()
                        val legacyFoto = doc.getString("fotoBase64") ?: doc.getString("fotoUrl") ?: ""
                        if (legacyFoto.isNotEmpty()) {
                            fotos.add(legacyFoto)
                        }
                        var tieneArchivosNoImagen = false
                        val archivosRaw = doc.get("archivos") as? List<*>
                        archivosRaw?.forEach { item ->
                            if (item is Map<*, *>) {
                                val nombre = item["nombre"]?.toString() ?: ""
                                val base64 = item["base64"]?.toString() ?: ""
                                val isImage = nombre.lowercase().run {
                                    endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") || endsWith(".webp") || endsWith(".gif")
                                }
                                if (isImage && base64.isNotEmpty()) {
                                    fotos.add(base64)
                                }
                                if (!isImage && base64.isNotEmpty()) {
                                    tieneArchivosNoImagen = true
                                }
                            }
                        }
                        EvidenciaPendiente(
                            id           = doc.id,
                            nombreAlumno = doc.getString("nombreAlumno") ?: "Alumno",
                            tituloTarea  = doc.getString("tituloTarea")  ?: "Sin título",
                            idAsignacion = doc.getString("idAsignacion") ?: "",
                            fotos        = fotos,
                            tieneArchivosNoImagen = tieneArchivosNoImagen
                        )
                    }

                _uiState.value = CalificarUiState.ListaLista(lista)

            } catch (e: Exception) {
                _uiState.value = CalificarUiState.Error(e.message ?: "Error de red")
            }
        }
    }

    // ── Guardar calificación ─────────────────────────────────────────────────
    /**
     * Metodo principal que ejecuta la operacion: calificar.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun calificar(idEvidencia: String, nota: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = CalificarUiState.Cargando
            try {
                // Update evidencias_tarea
                db.collection("evidencias_tarea")
                    .document(idEvidencia)
                    .update(
                        "estado",          if (nota > 0) "Aprobada" else "Rechazada",
                        "calificacion",    nota,
                        "fechaCalificada", FieldValue.serverTimestamp()
                    )
                    .await()

                // Fetch evidence's idUsuario (student ID) if possible
                val evDoc = db.collection("evidencias_tarea").document(idEvidencia).get().await()
                val idUsuario = evDoc.getString("idUsuario") ?: ""

                // Add to calificaciones collection
                val califDoc = hashMapOf(
                    "idEvidencia" to idEvidencia,
                    "idUsuario" to idUsuario,
                    "valor" to nota,
                    "comentario" to "Calificado desde Wear OS",
                    "esBorrador" to false,
                    "fechaCalificacion" to FieldValue.serverTimestamp()
                )
                db.collection("calificaciones").add(califDoc).await()

                _uiState.value = CalificarUiState.Exito
                onDone()

            } catch (e: Exception) {
                _uiState.value = CalificarUiState.Error(e.message ?: "Error al guardar")
            }
        }
    }

    /**
     * Metodo principal que ejecuta la operacion: resetEstado.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun resetEstado() { _uiState.value = CalificarUiState.Idle }
}
```

**Explicación paso a paso:**

### Modelo de datos (líneas 19-26)
```kotlin
data class EvidenciaPendiente(
    val id: String,           // ID del documento en Firestore
    val nombreAlumno: String, // Nombre del estudiante que entregó
    val tituloTarea: String,  // Título de la tarea
    val idAsignacion: String, // Referencia a la asignación original
    val fotos: List<String>,  // Lista de imágenes (Base64 o URLs)
    val tieneArchivosNoImagen: Boolean // true si subió PDFs u otros docs
)
```

### Estados de la UI (líneas 29-35)
- `Cargando`: Se están consultando datos de Firestore.
- `Idle`: Estado neutro, sin actividad.
- `Exito`: La calificación se guardó correctamente.
- `Error(msg)`: Hubo un error de red o datos.
- `ListaLista(items)`: Se tienen las evidencias pendientes listas para mostrar.

### Función `cargarPendientes()` (líneas 47-138)
Realiza una **consulta relacional en cadena** a Firestore:
1. **Paso 1** (línea 56): Obtiene las clases del profesor logueado → `classIds`.
2. **Paso 2** (línea 68): Obtiene las tareas que pertenecen a esas clases → `taskIds`.
3. **Paso 3** (línea 80): Obtiene las asignaciones que corresponden a esas tareas → `assignmentIds`.
4. **Paso 4** (línea 93): Obtiene las evidencias en estado `"Pendiente"` que correspondan a esas asignaciones.
5. Para cada evidencia, parsea las fotos adjuntas (Base64 de imágenes, URLs) y detecta si tiene archivos que no son imágenes.

### Función `calificar()` (líneas 142-178)
1. Actualiza el documento en `evidencias_tarea` con:
   - `estado`: `"Aprobada"` si nota > 0, `"Rechazada"` si nota = 0
   - `calificacion`: El valor numérico asignado
   - `fechaCalificada`: Marca de tiempo del servidor
2. Crea un nuevo documento en la colección `calificaciones` con el comentario `"Calificado desde Wear OS"`.

---

## 5. Archivo 3: `MainActivityWear.kt` — Actividad Principal y Navegación

**Ruta:** `wear/src/main/java/com/pmlp/wear/presentation/MainActivityWear.kt`

**¿Qué hace?** Es el punto de entrada de la aplicación en el smartwatch. Contiene:
- La actividad Android (`MainActivity`)
- La máquina de estados de navegación (`WearDestino`)
- El composable raíz (`EduTaskWearApp`)
- Las pantallas auxiliares (`NuevaEntregaScreen`, `VerFotoScreen`)
- La función de decodificación Base64 a Bitmap

```kotlin
/**
 * Actividad principal para Wear OS que configura la navegacion y flujo en smartwatches.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.wear.presentation

import android.content.Context
import android.content.Intent
import android.os.Build
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import coil.compose.AsyncImage
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.pmlp.wear.presentation.theme.EdutaskTheme

class MainActivity : ComponentActivity() {
    /**
     * Manejador de evento para la accion onCreate.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notifications permissions for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Start background task notification service
        try {
            val serviceIntent = Intent(this, TaskNotificationService::class.java)
            startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            EdutaskTheme {
                EduTaskWearApp()
            }
        }
    }
}

// ── Navegación simple entre pantallas ────────────────────────────────────────
private sealed class WearDestino {
    object Pendientes                          : WearDestino()
    data class Calificar(val e: EvidenciaPendiente) : WearDestino()
    data class NuevaEntrega(
        val idEvidencia: String,
        val nombreAlumno: String,
        val tituloTarea: String,
        val fotos: List<String>,
        val tieneArchivosNoImagen: Boolean
    ) : WearDestino()
    data class VerFoto(val fotos: List<String>, val prevDestino: WearDestino) : WearDestino()
}

@Composable
/**
 * Metodo principal que ejecuta la operacion: EduTaskWearApp.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EduTaskWearApp() {
    val vm: CalificarViewModel = viewModel()
    val uiState by vm.uiState.collectAsState()

    var destino: WearDestino by remember { mutableStateOf(WearDestino.Pendientes) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    var isFirstLoad by remember { mutableStateOf(true) }

    val prefs = remember(context) { context.getSharedPreferences("edutask_wear_prefs", Context.MODE_PRIVATE) }
    var nombreProfesor by remember { mutableStateOf(prefs.getString("nombre", "Sin sincronizar") ?: "Sin sincronizar") }
    var idProfesor by remember { mutableStateOf(prefs.getString("idUsuario", "profesor_001") ?: "profesor_001") }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "nombre") {
                nombreProfesor = sharedPreferences.getString("nombre", "Sin sincronizar") ?: "Sin sincronizar"
            }
            if (key == "idUsuario") {
                idProfesor = sharedPreferences.getString("idUsuario", "profesor_001") ?: "profesor_001"
                vm.cargarPendientes()
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    // Sincronización en la nube vía Firestore (Fallback)
    DisposableEffect(Unit) {
        val listener = db.collection("sesion_wear").document("default")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val idUsuarioVal = snapshot.getString("idUsuario") ?: ""
                    val nombreVal = snapshot.getString("nombre") ?: ""
                    if (idUsuarioVal.isNotEmpty() && nombreVal.isNotEmpty()) {
                        prefs.edit()
                            .putString("idUsuario", idUsuarioVal)
                            .putString("nombre", nombreVal)
                            .apply()
                        
                        idProfesor = idUsuarioVal
                        nombreProfesor = nombreVal
                        vm.cargarPendientes()
                    }
                }
            }
        onDispose {
            listener.remove()
        }
    }

    DisposableEffect(idProfesor) {
        val listener = db.collection("evidencias_tarea")
            .orderBy("fechaEnvio", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                if (isFirstLoad) {
                    isFirstLoad = false
                    return@addSnapshotListener
                }

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val estado = doc.getString("estado") ?: "Pendiente"
                    val idAsignacion = doc.getString("idAsignacion") ?: ""

                    if (estado == "Pendiente") {
                        // Check if this assignment belongs to this professor in a coroutine
                        scope.launch {
                            try {
                                val assignDoc = db.collection("asignaciones_tarea").document(idAsignacion).get().await()
                                val idTarea = assignDoc.getString("idTarea") ?: ""
                                if (idTarea.isNotEmpty()) {
                                    val taskDoc = db.collection("tareas").document(idTarea).get().await()
                                    val idClase = taskDoc.getString("idClase") ?: ""
                                    if (idClase.isNotEmpty()) {
                                        val classDoc = db.collection("clases").document(idClase).get().await()
                                        val classProfId = classDoc.getString("idUsuario") ?: ""
                                        if (classProfId == idProfesor) {
                                            val nombreAlumno = doc.getString("nombreAlumno") ?: "Alumno"
                                            val tituloTarea = doc.getString("tituloTarea") ?: "Tarea"
                                            val fotos = mutableListOf<String>()
                                            val legacyFoto = doc.getString("fotoBase64") ?: doc.getString("fotoUrl") ?: ""
                                            if (legacyFoto.isNotEmpty()) {
                                                fotos.add(legacyFoto)
                                            }
                                            var tieneArchivosNoImagen = false
                                            val archivosRaw = doc.get("archivos") as? List<*>
                                            archivosRaw?.forEach { item ->
                                                if (item is Map<*, *>) {
                                                    val nombre = item["nombre"]?.toString() ?: ""
                                                    val base64 = item["base64"]?.toString() ?: ""
                                                    val isImage = nombre.lowercase().run {
                                                        endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") || endsWith(".webp") || endsWith(".gif")
                                                    }
                                                    if (isImage && base64.isNotEmpty()) {
                                                        fotos.add(base64)
                                                    }
                                                    if (!isImage && base64.isNotEmpty()) {
                                                        tieneArchivosNoImagen = true
                                                    }
                                                }
                                            }
                                            val idEvidencia = doc.id

                                            // Trigger haptic physical motor vibration (400ms duration)
                                            try {
                                                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    vibrator.vibrate(400)
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }

                                            // Shift to NuevaEntrega screen
                                            destino = WearDestino.NuevaEntrega(
                                                idEvidencia = idEvidencia,
                                                nombreAlumno = nombreAlumno,
                                                tituloTarea = tituloTarea,
                                                fotos = fotos,
                                                tieneArchivosNoImagen = tieneArchivosNoImagen
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        onDispose { listener.remove() }
    }

    when (val dest = destino) {

        // ── Lista de evidencias pendientes ────────────────────────────────
        is WearDestino.Pendientes -> {
            val items = when (val s = uiState) {
                is CalificarUiState.ListaLista -> s.items
                else                           -> emptyList()
            }
            PendientesScreen(
                nombreProfesor = nombreProfesor,
                items         = items,
                onSeleccionar = { evidencia ->
                    destino = WearDestino.NuevaEntrega(
                        idEvidencia = evidencia.id,
                        nombreAlumno = evidencia.nombreAlumno,
                        tituloTarea = evidencia.tituloTarea,
                        fotos = evidencia.fotos,
                        tieneArchivosNoImagen = evidencia.tieneArchivosNoImagen
                    )
                },
                onRefrescar   = { vm.cargarPendientes() }
            )
        }

        // ── Pantalla de calificación rápida ───────────────────────────────
        is WearDestino.Calificar -> {
            val cargando = uiState is CalificarUiState.Cargando

            CalificarScreen(
                evidencia   = dest.e,
                esCargando  = cargando,
                onCalificar = { nota ->
                    vm.calificar(dest.e.id, nota) {
                        // Tras guardar: volver a la lista actualizada
                        vm.cargarPendientes()
                        destino = WearDestino.Pendientes
                    }
                },
                onVolver    = { destino = WearDestino.Pendientes }
            )
        }

        // ── Nueva Entrega (Smart Stack Detail) ─────────────────────────────
        is WearDestino.NuevaEntrega -> {
            NuevaEntregaScreen(
                nombreAlumno = dest.nombreAlumno,
                tituloTarea = dest.tituloTarea,
                tieneArchivosNoImagen = dest.tieneArchivosNoImagen,
                tieneFoto = dest.fotos.isNotEmpty(),
                onVerFoto = {
                    destino = WearDestino.VerFoto(fotos = dest.fotos, prevDestino = dest)
                },
                onCalificar = {
                    destino = WearDestino.Calificar(
                        EvidenciaPendiente(
                            id = dest.idEvidencia,
                            nombreAlumno = dest.nombreAlumno,
                            tituloTarea = dest.tituloTarea
                        )
                    )
                },
                onVolver = {
                    destino = WearDestino.Pendientes
                }
            )
        }

        // ── Visualización de Evidencia Fullscreen ─────────────────────────
        is WearDestino.VerFoto -> {
            VerFotoScreen(
                fotos = dest.fotos,
                onVolver = {
                    destino = dest.prevDestino
                }
            )
        }
    }
}

@Composable
/**
 * Componente de interfaz de usuario para la pantalla NuevaEntregaScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun NuevaEntregaScreen(
    nombreAlumno: String,
    tituloTarea: String,
    tieneArchivosNoImagen: Boolean,
    tieneFoto: Boolean,
    onVerFoto: () -> Unit,
    onCalificar: () -> Unit,
    onVolver: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Nueva Entrega",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "$nombreAlumno entregó:\n$tituloTarea",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (tieneArchivosNoImagen) {
                Text(
                    text = "Subió archivos. Ver en el móvil.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (!tieneFoto) {
                Text(
                    text = "Debe evaluar esta entrega en el móvil.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onVerFoto,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Ver Foto", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = onCalificar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Calificar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            TextButton(
                onClick = onVolver,
                modifier = Modifier.height(24.dp)
            ) {
                Text("Descartar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
/**
 * Componente de interfaz de usuario para la pantalla VerFotoScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun VerFotoScreen(fotos: List<String>, onVolver: () -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    val foto = remember(fotos, currentIndex) { fotos.getOrNull(currentIndex) ?: "" }
    val isUrl = remember(foto) { foto.startsWith("http://") || foto.startsWith("https://") }
    val bitmap = remember(foto) {
        if (!isUrl && foto.isNotEmpty()) decodeBase64ToBitmap(foto) else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Evidencia Foto ${currentIndex + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else if (isUrl) {
                AsyncImage(
                    model = foto,
                    contentDescription = "Evidencia Foto ${currentIndex + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = "Sin evidencia fotográfica",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Overlay elements: Page Indicator and Navigation buttons
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (fotos.size > 1) {
                Text(
                    text = "${currentIndex + 1} / ${fotos.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                if (fotos.size > 1) {
                    Button(
                        onClick = { currentIndex = if (currentIndex > 0) currentIndex - 1 else fotos.size - 1 },
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("<", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onVolver,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Volver", style = MaterialTheme.typography.labelSmall)
                }

                if (fotos.size > 1) {
                    Button(
                        onClick = { currentIndex = (currentIndex + 1) % fotos.size },
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(">", fontWeight = FontWeight.Bold)
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
private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val cleanString = if (base64Str.contains(",")) {
            base64Str.substring(base64Str.indexOf(",") + 1)
        } else {
            base64Str
        }
        val decodedBytes = Base64.decode(cleanString, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}
```

**Explicación paso a paso:**

### Clase `MainActivity` (líneas 43-68)
- **Líneas 48-51:** Si el dispositivo tiene Android 13+, solicita el permiso `POST_NOTIFICATIONS` necesario para mostrar alertas.
- **Líneas 55-60:** Inicia el servicio en segundo plano `TaskNotificationService` para que las notificaciones funcionen incluso cuando la app no está en primer plano.
- **Líneas 62-66:** Establece el contenido visual usando Compose con el tema `EdutaskTheme` y el composable raíz `EduTaskWearApp()`.

### Máquina de estados `WearDestino` (líneas 71-82)
Define las 4 pantallas posibles del reloj:
- `Pendientes`: Lista de entregas por calificar.
- `Calificar(e)`: Pantalla de asignación de nota para una evidencia específica.
- `NuevaEntrega(...)`: Vista de alerta cuando llega una nueva entrega.
- `VerFoto(fotos, prevDestino)`: Visor de imágenes a pantalla completa.

### Composable `EduTaskWearApp()` (líneas 84-305)
- **Líneas 96-98:** Lee el ID y nombre del profesor desde `SharedPreferences`.
- **Líneas 100-114:** Escucha cambios en SharedPreferences (cuando `SessionListenerService` actualiza las credenciales).
- **Líneas 117-138:** Listener de Firestore sobre `sesion_wear/default` como respaldo de sincronización.
- **Líneas 140-227:** Listener en tiempo real sobre `evidencias_tarea` que:
  1. Detecta nuevas entregas en estado `"Pendiente"`
  2. Verifica que la tarea pertenezca a una clase del profesor
  3. Hace vibrar el reloj (400ms) como alerta háptica
  4. Navega automáticamente a `NuevaEntregaScreen`
- **Líneas 229-305:** Bloque `when` que renderiza la pantalla según el estado actual.

### Composable `NuevaEntregaScreen()` (líneas 308-401)
Muestra una vista compacta con:
- Nombre del alumno y título de la tarea
- Advertencia si contiene archivos no visualizables (PDFs)
- Dos botones: "Ver Foto" y "Calificar" (o solo "Descartar" si no hay fotos)

### Composable `VerFotoScreen()` (líneas 403-507)
- Visor de imágenes a pantalla completa con fondo negro
- Soporta Base64 (decodificación local con `decodeBase64ToBitmap`) y URLs (Coil `AsyncImage`)
- Botones `<` y `>` para navegar entre múltiples fotos
- Contador de fotos ("1 / 3")

### Función `decodeBase64ToBitmap()` (líneas 510-522)
Convierte una cadena Base64 (con o sin prefijo `data:image/...;base64,`) en un objeto `Bitmap` de Android.

---

## 6. Archivo 4: `PendientesScreen.kt` — Lista de Evidencias Pendientes

**Ruta:** `wear/src/main/java/com/pmlp/wear/presentation/PendientesScreen.kt`

**¿Qué hace?** Muestra la lista desplazable de todas las evidencias pendientes de calificar, optimizada para la pantalla circular del reloj.

```kotlin
/**
 * Pantalla para Wear OS que lista las tareas y evidencias pendientes de calificar.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.wear.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices

// ── Lista de evidencias pendientes ───────────────────────────────────────────
@Composable
/**
 * Componente de interfaz de usuario para la pantalla PendientesScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun PendientesScreen(
    nombreProfesor:    String,
    items:             List<EvidenciaPendiente>,
    onSeleccionar:     (EvidenciaPendiente) -> Unit,
    onRefrescar:       () -> Unit
) {
    val listState        = rememberTransformingLazyColumnState()
    val transformSpec    = rememberTransformationSpec()

    AppScaffold {
        ScreenScaffold(
            scrollState = listState,
            edgeButton  = {
                EdgeButton(
                    onClick = onRefrescar,
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor   = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) { Text("↻ Actualizar") }
            }
        ) { padding ->
            TransformingLazyColumn(
                contentPadding = padding,
                state          = listState
            ) {
                item {
                    ListHeader(
                        modifier       = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformSpec),
                        transformation = SurfaceTransformation(transformSpec)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Prof: $nombreProfesor",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text      = if (items.isEmpty()) "Sin pendientes" else "Pendientes (${items.size})",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (items.isEmpty()) {
                    item {
                        Box(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .transformedHeight(this, transformSpec),
                            contentAlignment    = Alignment.Center
                        ) {
                            Text(
                                "Todo calificado",
                                style     = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(items) { evidencia ->
                    Button(
                        onClick    = { onSeleccionar(evidencia) },
                        modifier   = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformSpec),
                        transformation = SurfaceTransformation(transformSpec),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor   = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column(
                            modifier              = Modifier.fillMaxWidth(),
                            verticalArrangement   = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text     = evidencia.nombreAlumno,
                                style    = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text     = evidencia.tituloTarea,
                                style    = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (evidencia.tieneArchivosNoImagen) {
                                Text(
                                    text     = "Contiene archivos. Ver en móvil.",
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = MaterialTheme.colorScheme.error,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────
@WearPreviewDevices
@Composable
/**
 * Metodo principal que ejecuta la operacion: PreviewPendientes.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun PreviewPendientes() {
    val mocks = listOf(
        EvidenciaPendiente("1", "María López",  "Ensayo Revolución"),
        EvidenciaPendiente("2", "Juan Ramírez", "Diagramas UML"),
        EvidenciaPendiente("3", "Ana Torres",   "Actividad 5")
    )
    PendientesScreen(nombreProfesor = "Martha Elena", items = mocks, onSeleccionar = {}, onRefrescar = {})
}
```

**Explicación paso a paso:**

- **Líneas 30-31:** Inicializa `TransformingLazyColumnState` y `TransformationSpec`, que permiten que los elementos se escalen y transformen suavemente al desplazarse en pantallas redondas.
- **Líneas 33-44:** `AppScaffold` + `ScreenScaffold` con un `EdgeButton` inferior que sirve como botón de "Actualizar" para recargar las evidencias manualmente.
- **Líneas 50-74:** El encabezado de la lista que muestra `"Prof: [nombre]"` y el conteo de pendientes.
- **Líneas 77-93:** Si la lista está vacía, muestra `"Todo calificado"`.
- **Líneas 95-134:** Para cada `EvidenciaPendiente`, renderiza un `Button` que al tocarse navega a la pantalla de detalle. Cada botón muestra:
  - `evidencia.nombreAlumno` (en negrita)
  - `evidencia.tituloTarea` (texto secundario)
  - Advertencia roja si contiene archivos no visualizables

---

## 7. Archivo 5: `CalificarScreen.kt` — Pantalla de Calificación Rápida

**Ruta:** `wear/src/main/java/com/pmlp/wear/presentation/CalificarScreen.kt`

**¿Qué hace?** Presenta una interfaz táctil de calificación rápida con 4 botones grandes y un paso de confirmación antes de guardar.

```kotlin
/**
 * Pantalla para Wear OS que permite al profesor calificar evidencias de tareas de forma rapida
 * directamente desde el reloj inteligente.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.wear.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.*
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices

// ── Pantalla de calificación rápida ──────────────────────────────────────────
@Composable
/**
 * Componente de interfaz de usuario para la pantalla CalificarScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun CalificarScreen(
    evidencia:   EvidenciaPendiente,
    esCargando:  Boolean,
    onCalificar: (nota: Int) -> Unit,
    onVolver:    () -> Unit
) {
    var confirmando by remember { mutableStateOf<Int?>(null) }

    AppScaffold {
        // ScreenScaffold sin scroll state (pantalla estática — sin lista)
        ScreenScaffold { padding ->
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // ── Encabezado ────────────────────────────────────────
                    Text(
                        text       = evidencia.nombreAlumno,
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text      = evidencia.tituloTarea,
                        style     = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines  = 2,
                        overflow  = TextOverflow.Ellipsis,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(4.dp))

                    // ── Contenido central ─────────────────────────────────
                    if (esCargando) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    } else if (confirmando != null) {
                        ConfirmacionCalificacion(
                            nota       = confirmando!!,
                            onConfirmar = {
                                onCalificar(confirmando!!)
                                confirmando = null
                            },
                            onCancelar = { confirmando = null }
                        )
                    } else {
                        // ── Cuadrícula 2×2 de botones grandes ────────────
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BotonNota(
                                    label    = "10",
                                    colors   = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor   = MaterialTheme.colorScheme.onTertiary
                                    ),
                                    onClick  = { confirmando = 10 },
                                    modifier = Modifier.weight(1f)
                                )
                                BotonNota(
                                    label    = "9",
                                    colors   = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor   = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    onClick  = { confirmando = 9 },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BotonNota(
                                    label    = "8",
                                    colors   = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor   = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    onClick  = { confirmando = 8 },
                                    modifier = Modifier.weight(1f)
                                )
                                BotonNota(
                                    label    = "0",
                                    colors   = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor   = MaterialTheme.colorScheme.onError
                                    ),
                                    onClick  = { confirmando = 0 },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // ── Botón Volver ──────────────────────────────
                            Button(
                                onClick  = onVolver,
                                modifier = Modifier.fillMaxWidth(),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor   = MaterialTheme.colorScheme.onSurface
                                )
                            ) { Text("← Volver", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            }
        }
    }
}

// ── Botón de calificación grande ─────────────────────────────────────────────
@Composable
/**
 * Metodo principal que ejecuta la operacion: BotonNota.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun BotonNota(
    label:    String,
    colors:   ButtonColors,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick  = onClick,
        modifier = modifier.height(44.dp),
        colors   = colors
    ) {
        Text(
            text       = label,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
    }
}

// ── Paso de confirmación ──────────────────────────────────────────────────────
@Composable
/**
 * Metodo principal que ejecuta la operacion: ConfirmacionCalificacion.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun ConfirmacionCalificacion(
    nota:       Int,
    onConfirmar: () -> Unit,
    onCancelar:  () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "¿Guardar nota?",
            style     = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
        Text(
            nota.toString(),
            fontSize   = 32.sp,
            fontWeight = FontWeight.Bold,
            color      = if (nota > 0) MaterialTheme.colorScheme.tertiary
                         else MaterialTheme.colorScheme.error,
            textAlign  = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick  = onCancelar,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor   = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            ) { Text("No") }
            Button(
                onClick  = onConfirmar,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Sí") }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@WearPreviewDevices
@Composable
/**
 * Metodo principal que ejecuta la operacion: PreviewCalificar.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun PreviewCalificar() {
    CalificarScreen(
        evidencia   = EvidenciaPendiente("1", "María López", "Ensayo Revolución"),
        esCargando  = false,
        onCalificar = {},
        onVolver    = {}
    )
}
```

**Explicación paso a paso:**

### Botonera 2×2 (líneas 78-125)
Cuatro botones grandes diseñados para que el profesor pueda tocarlos fácilmente en la pantalla pequeña:
- **`10`** (verde terciario): Calificación perfecta
- **`9`** (azul primario): Calificación alta
- **`8`** (secundario): Calificación buena
- **`0`** (rojo error): Tarea rechazada / No calificable

### Paso de confirmación `ConfirmacionCalificacion` (líneas 167-210)
Antes de guardar, muestra:
- El texto `"¿Guardar nota?"`
- El número en grande (32sp) con color verde si > 0 o rojo si = 0
- Botones "Sí" y "No"

### Composable `BotonNota` (líneas 146-164)
Botón reutilizable de 44dp de altura con texto en negrita centrado. Recibe colores personalizados para cada nota.

---

## 8. Archivo 6: `SessionListenerService.kt` — Sincronización con Móvil

**Ruta:** `wear/src/main/java/com/pmlp/wear/presentation/SessionListenerService.kt`

**¿Qué hace?** Escucha los datos que envía la app móvil al reloj a través de la Data Layer API de Google Play Services, para sincronizar la sesión del profesor sin necesidad de internet.

```kotlin
/**
 * Servicio en segundo plano para Wear OS que escucha y sincroniza la sesion del profesor con la app movil.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.wear.presentation

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class SessionListenerService : WearableListenerService() {
    /**
     * Manejador de evento para la accion onDataChanged.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("SessionListenerService", "Data changed event received on wear")
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/usuario_logueado") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val idUsuario = dataMap.getString("idUsuario") ?: ""
                val nombre = dataMap.getString("nombre") ?: ""

                Log.d("SessionListenerService", "Synchronized session on Wear OS: idUsuario=$idUsuario, nombre=$nombre")

                // Save locally on the watch SharedPreferences
                val prefs = getSharedPreferences("edutask_wear_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("idUsuario", idUsuario)
                    .putString("nombre", nombre)
                    .apply()
            }
        }
    }
}
```

**Explicación paso a paso:**
- **Línea 13:** Extiende `WearableListenerService`, un servicio del sistema que se activa automáticamente cuando la app móvil envía datos.
- **Línea 17:** Filtra solo eventos de tipo `TYPE_CHANGED` en la ruta `/usuario_logueado`.
- **Líneas 18-20:** Extrae `idUsuario` y `nombre` del `DataMap`.
- **Líneas 25-29:** Guarda los datos en `SharedPreferences` locales. Esto dispara los listeners de `MainActivityWear` y `TaskNotificationService`.

---

## 9. Archivo 7: `TaskNotificationService.kt` — Notificaciones en Segundo Plano

**Ruta:** `wear/src/main/java/com/pmlp/wear/presentation/TaskNotificationService.kt`

**¿Qué hace?** Servicio persistente que mantiene un listener activo de Firestore para emitir notificaciones locales del sistema (con vibración) cuando un alumno envía una tarea, incluso si la app no está abierta.

```kotlin
/**
 * Servicio de notificaciones en Wear OS que alerta al profesor sobre nuevas evidencias recibidas.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.wear.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TaskNotificationService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null
    private var isFirstLoad = true
    private var prefListener: android.content.SharedPreferences.OnSharedPreferenceChangeListener? = null

    /**
     * Manejador de evento para la accion onBind.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Manejador de evento para la accion onCreate.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d("TaskNotificationService", "Service created")
        createNotificationChannel()
        startListening()
    }

    /**
     * Metodo principal que ejecuta la operacion: createNotificationChannel.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "wear_task_notifications",
                "Nuevas Evidencias",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevas tareas entregadas por calificar"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Metodo principal que ejecuta la operacion: startListening.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private fun startListening() {
        val prefs = getSharedPreferences("edutask_wear_prefs", Context.MODE_PRIVATE)
        val idProfesor = prefs.getString("idUsuario", "profesor_001") ?: "profesor_001"

        prefListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "idUsuario") {
                Log.d("TaskNotificationService", "Professor ID changed in prefs, restarting listener")
                listener?.remove()
                isFirstLoad = true
                startListening()
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(prefListener)

        listener = db.collection("evidencias_tarea")
            .orderBy("fechaEnvio", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("TaskNotificationService", "Firestore listener error", error)
                    return@addSnapshotListener
                }
                if (isFirstLoad) {
                    isFirstLoad = false
                    Log.d("TaskNotificationService", "Initial load skipped")
                    return@addSnapshotListener
                }

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val estado = doc.getString("estado") ?: "Pendiente"
                    val idAsignacion = doc.getString("idAsignacion") ?: ""

                    if (estado == "Pendiente") {
                        scope.launch {
                            try {
                                val assignDoc = db.collection("asignaciones_tarea").document(idAsignacion).get().await()
                                val idTarea = assignDoc.getString("idTarea") ?: ""
                                if (idTarea.isNotEmpty()) {
                                    val taskDoc = db.collection("tareas").document(idTarea).get().await()
                                    val idClase = taskDoc.getString("idClase") ?: ""
                                    if (idClase.isNotEmpty()) {
                                        val classDoc = db.collection("clases").document(idClase).get().await()
                                        val classProfId = classDoc.getString("idUsuario") ?: ""
                                        
                                        val currentIdProfesor = prefs.getString("idUsuario", "profesor_001") ?: "profesor_001"
                                        if (classProfId == currentIdProfesor) {
                                             val nombreAlumno = doc.getString("nombreAlumno") ?: "Alumno"
                                             val tituloTarea = doc.getString("tituloTarea") ?: "Tarea"
                                             var tieneArchivosNoImagen = false
                                             val archivosRaw = doc.get("archivos") as? List<*>
                                             archivosRaw?.forEach { item ->
                                                 if (item is Map<*, *>) {
                                                     val nombre = item["nombre"]?.toString() ?: ""
                                                     val base64 = item["base64"]?.toString() ?: ""
                                                     val isImage = nombre.lowercase().run {
                                                         endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") || endsWith(".webp") || endsWith(".gif")
                                                     }
                                                     if (!isImage && base64.isNotEmpty()) {
                                                         tieneArchivosNoImagen = true
                                                     }
                                                 }
                                             }
                                             
                                             showNotification(nombreAlumno, tituloTarea, tieneArchivosNoImagen)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("TaskNotificationService", "Error resolving task metadata", e)
                            }
                        }
                    }
                }
            }
    }

    /**
     * Metodo principal que ejecuta la operacion: showNotification.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private fun showNotification(alumno: String, tarea: String, tieneArchivosNoImagen: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (tieneArchivosNoImagen) {
            "Subió archivos. Ver en el móvil. (Califica aquí)"
        } else {
            tarea
        }

        val notification = NotificationCompat.Builder(this, "wear_task_notifications")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Entrega de $alumno")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
        Log.d("TaskNotificationService", "Notification posted: $alumno - $contentText")
    }

    /**
     * Manejador de evento para la accion onDestroy.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
        val prefs = getSharedPreferences("edutask_wear_prefs", Context.MODE_PRIVATE)
        prefListener?.let { prefs.unregisterOnSharedPreferenceChangeListener(it) }
        job.cancel()
        Log.d("TaskNotificationService", "Service destroyed")
    }
}
```

**Explicación paso a paso:**

### Ciclo de vida del servicio
- **`onCreate()`** (líneas 36-41): Crea el canal de notificación y comienza a escuchar Firestore.
- **`onDestroy()`** (líneas 164-171): Limpia el listener y cancela las corrutinas.

### `createNotificationChannel()` (líneas 43-55)
Crea un canal de notificación Android con prioridad `IMPORTANCE_HIGH` para que las alertas se muestren de forma prominente.

### `startListening()` (líneas 57-132)
1. Lee el ID del profesor desde SharedPreferences.
2. Registra un listener de SharedPreferences para reiniciar la escucha si cambia el profesor.
3. Establece un `addSnapshotListener` sobre la última evidencia de `evidencias_tarea`:
   - Salta la carga inicial (`isFirstLoad = true`).
   - Solo procesa evidencias con estado `"Pendiente"`.
   - Verifica que la tarea pertenezca al profesor actual (cadena: `asignaciones_tarea` → `tareas` → `clases`).
   - Si coincide, llama a `showNotification()`.

### `showNotification()` (líneas 134-162)
Construye y muestra una notificación Android con:
- Título: `"Entrega de [alumno]"`
- Texto: nombre de la tarea o advertencia de archivos
- Vibración doble: `[0, 400, 200, 400]` ms
- Al tocar: abre `MainActivity` del smartwatch

---

## Guía de Compilación y Ejecución

```bash
# Compilar el APK de depuración del módulo Wear
./gradlew :wear:assembleDebug

# Instalar en el smartwatch conectado por ADB o Wi-Fi
adb -s <device-id> install wear/build/outputs/apk/debug/wear-debug.apk
```
