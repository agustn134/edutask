# Módulo Android TV — Documentación Técnica Completa

> **Proyecto:** EduTask — Plataforma de Gestión Académica
> **Módulo:** `:tv` — Tablón Inteligente para Pantallas Grandes (Android TV / Google TV)
> **Propósito:** Funcionar como un tablón digital institucional que muestra noticias/eventos escolares y un dashboard de monitoreo académico en tiempo real con gráficas de barras por grupo.

---

## Tabla de Contenidos

1. [Dependencias y Librerías](#1-dependencias-y-librerías)
2. [Arquitectura General](#2-arquitectura-general)
3. [Archivo 1: Color.kt — Paleta de Colores](#3-archivo-1-colorkt--paleta-de-colores)
4. [Archivo 2: Type.kt — Tipografía](#4-archivo-2-typekt--tipografía)
5. [Archivo 3: Theme.kt — Tema Visual TV](#5-archivo-3-themekt--tema-visual-tv)
6. [Archivo 4: MainActivitytv.kt — Actividad Principal y Reproductor](#6-archivo-4-mainactivitytvkt--actividad-principal-y-reproductor)
7. [Archivo 5: TVHomeScreen.kt — Pantalla Principal con Carrusel y Dashboard](#7-archivo-5-tvhomescreenkt--pantalla-principal-con-carrusel-y-dashboard)
8. [Dependencia: EventosSharedViewModel.kt (módulo Core)](#8-dependencia-eventossharedviewmodelkt-módulo-core)

---

## 1. Dependencias y Librerías

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.pmlp.tv"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.pmlp.tv"
        minSdk = 24
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("io.coil-kt:coil-compose:2.5.0")
    // Storage lo agregaremos aquí mismo cuando lleguemos a esa fase
}
```

**Explicación de cada dependencia:**

| Línea | Librería | Para qué se usa |
|---|---|---|
| `project(":core")` (39) | Módulo Core compartido | Modelos `Evento`, `EstadisticaGrupo` y `EventosSharedViewModel` |
| `media3-exoplayer` (40) | Media3 ExoPlayer | Reproduce el himno institucional en bucle como audio de fondo |
| `lifecycle-runtime-compose` (41) | Lifecycle Compose | `collectAsStateWithLifecycle()` para recolectar StateFlows de forma segura |
| `tv-foundation` (51) | TV Foundation | Primitivas de enfoque y navegación para control remoto (D-Pad) |
| `tv-material` (52) | TV Material 3 | Componentes `Surface`, `Text`, `MaterialTheme` optimizados para TV a 10 pies |
| `firebase-firestore` (58) | Firebase Firestore | Conexión en tiempo real a la base de datos |
| `coil-compose` (59) | Coil | Carga asíncrona de imágenes de eventos (URLs e imágenes Base64) |

---

## 2. Arquitectura General

```
                   ┌──────────────────────────────────────┐
                   │        Firebase Firestore             │
                   │  - eventos_escolares                  │
                   │  - evidencias_tarea (SnapshotListener)│
                   │  - clases, tareas, asignaciones_tarea │
                   └──────────────────┬───────────────────┘
                                      │
                                      ▼
                   ┌──────────────────────────────────────┐
                   │      EventosSharedViewModel (Core)   │
                   │  - uiState: StateFlow<EventosUiState>│
                   │  - estadisticasState: StateFlow<...> │
                   └──────────────────┬───────────────────┘
                                      │
                                      ▼
                   ┌──────────────────────────────────────┐
                   │         MainActivitytv               │
                   │  - ExoPlayer (himno en bucle)        │
                   │  - Surface + TVHomeScreen            │
                   └──────────────────┬───────────────────┘
                                      │
                                      ▼
                   ┌──────────────────────────────────────┐
                   │           TVHomeScreen               │
                   │  - Combina List<CarouselPage>        │
                   │  - AutoCarousel (rotación cada 6s)   │
                   └──────────┬────────────────┬──────────┘
                              │                │
                              ▼                ▼
                     ┌─────────────────┐ ┌──────────────────────┐
                     │ EventoHeroCard  │ │ GrupoEstadisticaCard │
                     │ (Noticias)      │ │ (Dashboard + Barras) │
                     └─────────────────┘ └──────────────────────┘
```

---

## 3. Archivo 1: `Color.kt` — Paleta de Colores

**Ruta:** `tv/src/main/java/com/pmlp/tv/ui/theme/Color.kt`

```kotlin
/**
 * Paleta de colores optimizada para interfaces de Android TV.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.tv.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
```

**Explicación:** Define 6 colores base del tema Material. `Purple80`/`PurpleGrey80`/`Pink80` se usan en modo oscuro (el modo por defecto en TV). `Purple40`/`PurpleGrey40`/`Pink40` se usan en modo claro.

---

## 4. Archivo 2: `Type.kt` — Tipografía

**Ruta:** `tv/src/main/java/com/pmlp/tv/ui/theme/Type.kt`

```kotlin
/**
 * Configuracion de tipografia y escalas de texto para pantallas de Android TV.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.tv.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Typography

// Set of Material typography styles to start with
@OptIn(ExperimentalTvMaterial3Api::class)
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
```

**Explicación:** Configura el estilo tipográfico base (`bodyLarge`) con fuente de 16sp. Los demás estilos (`displayMedium`, `headlineSmall`, etc.) usados en `TVHomeScreen` heredan los valores predeterminados del Material 3 para TV.

---

## 5. Archivo 3: `Theme.kt` — Tema Visual TV

**Ruta:** `tv/src/main/java/com/pmlp/tv/ui/theme/Theme.kt`

```kotlin
/**
 * Configuracion del tema de Compose para Android TV (androidx.tv.material3).
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.tv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
/**
 * Metodo principal que ejecuta la operacion: EdutaskTheme.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EdutaskTheme(
    isInDarkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
        background = Color(0xFF0E0B16),
        surface = Color(0xFF1B1528),
        surfaceVariant = Color(0xFF28203B),
        onBackground = Color(0xFFF4EFF4),
        onSurface = Color(0xFFF4EFF4),
        onSurfaceVariant = Color(0xFFCAC4D0)
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**Explicación:** Provee el tema global para toda la interfaz de TV. Si el sistema está en modo oscuro usa `darkColorScheme` con la paleta púrpura; si está en modo claro usa `lightColorScheme`. En la práctica, Android TV siempre usa modo oscuro.

---

## 6. Archivo 4: `MainActivitytv.kt` — Actividad Principal y Reproductor

**Ruta:** `tv/src/main/java/com/pmlp/tv/MainActivitytv.kt`

```kotlin
/**
 * Actividad principal para Android TV que configura la visualizacion en pantalla grande
 * e inicializa la interfaz del tablon inteligente (TVHomeScreen).
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import android.content.Context
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.tv.ui.TVHomeScreen
import com.pmlp.tv.ui.theme.EdutaskTheme
import androidx.media3.common.PlaybackParameters

class MainActivitytv : ComponentActivity() {
    private var exoPlayer: ExoPlayer? = null

    @OptIn(ExperimentalTvMaterial3Api::class)
    /**
     * Manejador de evento para la accion onCreate.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initializePlayer()

        setContent {
            val viewModel: EventosSharedViewModel = viewModel()
            EdutaskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    TVHomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    /**
     * Metodo principal que ejecuta la operacion: initializePlayer.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private fun initializePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(this).build()
        }

        exoPlayer?.let { player ->
            if (player.isPlaying) return

            val uri = "android.resource://${packageName}/${R.raw.himno}"
            player.setMediaItem(MediaItem.fromUri(uri))

            // REMOVER o COMENTAR esta línea para dejar que ExoPlayer gestione la velocidad según el reloj del archivo:
            // player.playbackParameters = PlaybackParameters(1.0f, 1.0f)

            player.repeatMode = Player.REPEAT_MODE_ALL
            player.prepare()
            player.playWhenReady = true
        }
    }

    /**
     * Manejador de evento para la accion onDestroy.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        exoPlayer?.release()
        exoPlayer = null
    }
}
```

**Explicación paso a paso:**

- **Línea 32:** `MainActivitytv` es la Activity principal del módulo TV, registrada en el AndroidManifest.
- **Línea 33:** Declara una referencia al reproductor de audio `ExoPlayer`.
- **Línea 39:** `initializePlayer()` prepara el himno institucional (`res/raw/himno`) para reproducirse en bucle infinito como música ambiente.
- **Líneas 41-51:** Configura Compose:
  1. Crea el `EventosSharedViewModel` con `viewModel()`.
  2. Envuelve todo en `EdutaskTheme`.
  3. Usa una `Surface` a pantalla completa con forma rectangular.
  4. Renderiza `TVHomeScreen(viewModel)`.
- **Líneas 54-71:** `initializePlayer()`:
  - Crea el `ExoPlayer` si no existe.
  - Carga el archivo de audio desde recursos.
  - Configura repetición infinita (`REPEAT_MODE_ALL`).
  - Inicia la reproducción.
- **Líneas 74-80:** `onDestroy()` libera correctamente los recursos del reproductor.

---

## 7. Archivo 5: `TVHomeScreen.kt` — Pantalla Principal con Carrusel y Dashboard

**Ruta:** `tv/src/main/java/com/pmlp/tv/ui/TVHomeScreen.kt`

Este es el archivo más extenso del módulo TV. Contiene el carrusel automático de noticias y las tarjetas de estadísticas por grupo con gráficas de barras.

```kotlin
/**
 * Pantalla principal para Android TV con carrusel automatizado de avisos institucionales
 * y tarjetas de monitoreo academico en tiempo real con graficas de barras por grupo.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.tv.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.pmlp.edutask.model.EstadisticaGrupo
import com.pmlp.edutask.model.Evento
import com.pmlp.edutask.model.PromedioMateria
import com.pmlp.edutask.ui.EstadisticasUiState
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.edutask.ui.EventosUiState
import com.pmlp.tv.ui.theme.EdutaskTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.BorderStroke
import androidx.tv.material3.Border

// ---------------------------------------------------------------------------
// Paleta de calificaciones:
//   Alta  -> Blanco puro  (contrasta en fondo oscuro, sin morado)
//   Media -> Amber
//   Baja  -> Rose
// ---------------------------------------------------------------------------
private val CalifAlta     = Color.White            // antes Purple80 — ahora blanco
private val CalifMedia    = Color(0xFFFFB74D)      // Amber
private val CalifBaja     = Color(0xFFCF6679)      // Rose
private val CalifSinDatos = Color(0xFF9E9E9E)      // Gris

private fun Double?.calColor(): Color = when {
    this == null -> CalifSinDatos
    this >= 8.0  -> CalifAlta
    this >= 6.0  -> CalifMedia
    else         -> CalifBaja
}
private fun Double?.calLabel(): String =
    if (this != null) String.format(Locale.US, "%.1f", this) else "--"

// ---------------------------------------------------------------------------
// Tipos de pagina del carrusel
// ---------------------------------------------------------------------------
sealed class CarouselPage {
    data class EventoPage(val evento: Evento) : CarouselPage()
    data class GrupoPage(val grupo: EstadisticaGrupo) : CarouselPage()
}

// ---------------------------------------------------------------------------
// Pantalla principal TV
// ---------------------------------------------------------------------------
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
/**
 * Componente de interfaz de usuario para la pantalla TVHomeScreen.
 * Muestra los elementos visuales y maneja las interacciones del usuario.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun TVHomeScreen(viewModel: EventosSharedViewModel) {
    EdutaskTheme {
        val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
        val estadisticasState by viewModel.estadisticasState.collectAsStateWithLifecycle()

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                Text(
                    text = "EduTask TV - Tablon de Anuncios",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                val pages: List<CarouselPage> = remember(uiState, estadisticasState) {
                    val eventoPages = when (val s = uiState) {
                        is EventosUiState.Success -> s.eventos.map { CarouselPage.EventoPage(it) }
                        else -> emptyList()
                    }
                    val grupoPages = when (val s = estadisticasState) {
                        is EstadisticasUiState.Success -> s.grupos.map { CarouselPage.GrupoPage(it) }
                        else -> emptyList()
                    }
                    eventoPages + grupoPages
                }

                when {
                    pages.isEmpty() && uiState is EventosUiState.Loading ->
                        CenteredMsg("Cargando...", MaterialTheme.colorScheme.onSurfaceVariant)
                    pages.isEmpty() && uiState is EventosUiState.Error ->
                        CenteredMsg("Error: ${(uiState as EventosUiState.Error).message}", MaterialTheme.colorScheme.error)
                    pages.isEmpty() ->
                        CenteredMsg("Sin contenido", MaterialTheme.colorScheme.onSurfaceVariant)
                    else -> AutoCarousel(pages = pages)
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
/**
 * Metodo principal que ejecuta la operacion: CenteredMsg.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun CenteredMsg(msg: String, color: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(msg, color = color, fontSize = 24.sp)
    }
}

// ---------------------------------------------------------------------------
// Carrusel automatico — avanza cada 6 segundos
// ---------------------------------------------------------------------------
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
/**
 * Metodo principal que ejecuta la operacion: AutoCarousel.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun AutoCarousel(pages: List<CarouselPage>) {
    val pagerState = rememberPagerState(pageCount = { pages.size })

    LaunchedEffect(pages) {
        if (pages.isNotEmpty()) {
            while (true) {
                delay(6000)
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % pages.size)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (val p = pages[page]) {
                    is CarouselPage.EventoPage -> EventoHeroCard(p.evento)
                    is CarouselPage.GrupoPage  -> GrupoEstadisticaCard(p.grupo)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PaginationIndicator(
            count = pages.size,
            currentIndex = pagerState.currentPage,
            isGrupo = { i -> pages.getOrNull(i) is CarouselPage.GrupoPage }
        )
    }
}

// ---------------------------------------------------------------------------
// Tarjeta de EVENTO — sin cambios
// ---------------------------------------------------------------------------
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
/**
 * Componente visual reutilizable para renderizar EventoHeroCard.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun EventoHeroCard(evento: Evento) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateString = formatter.format(Date(evento.fechaPublicacion))

    Surface(
        onClick = {},
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(24.dp)),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp))
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = Color.White,
            focusedContentColor = Color.White,
            pressedContainerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.9f).padding(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val url = evento.imagenUrl
            if (!url.isNullOrEmpty()) {
                val imageModel = remember(url) {
                    try {
                        val b64 = if (url.startsWith("data:image")) url.substringAfter("base64,") else url
                        if (b64.length > 500 && !b64.startsWith("http")) {
                            val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } else url
                    } catch (e: Exception) { url }
                }
                AsyncImage(model = imageModel, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
            }
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)), startY = 200f)))
            Column(modifier = Modifier.fillMaxSize().padding(48.dp),
                verticalArrangement = Arrangement.SpaceBetween) {
                Spacer(modifier = Modifier.weight(1f))
                Column {
                    Text(evento.titulo, style = MaterialTheme.typography.displayMedium,
                        color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(evento.descripcion, style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = 0.9f), lineHeight = 40.sp, maxLines = 4)
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        if (evento.lugar.isNotEmpty()) InfoBadge("Lugar: ${evento.lugar}")
                        else Spacer(modifier = Modifier.width(1.dp))
                        InfoBadge("Publicado: $dateString")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tarjeta de GRUPO con grafica de barras
// ---------------------------------------------------------------------------
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
/**
 * Componente visual reutilizable para renderizar GrupoEstadisticaCard.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun GrupoEstadisticaCard(grupo: EstadisticaGrupo) {
    val pg            = grupo.promedioGeneral
    val promColor     = pg.calColor()
    val promLabel     = pg.calLabel()

    val rendimiento = when {
        pg == null -> "Sin datos"
        pg >= 9.0  -> "Excelente"
        pg >= 8.0  -> "Muy bueno"
        pg >= 7.0  -> "Bueno"
        pg >= 6.0  -> "Suficiente"
        else       -> "Por mejorar"
    }

    val pctCalificados = if (grupo.totalAlumnos > 0)
        grupo.alumnosCalificados.toFloat() / grupo.totalAlumnos.toFloat()
    else 0f

    Surface(
        onClick = {},
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(28.dp)),
        border = ClickableSurfaceDefaults.border(
            border = Border(border = BorderStroke(1.5.dp, Color(0xFF4A3B69).copy(alpha = 0.6f)), shape = RoundedCornerShape(28.dp)),
            focusedBorder = Border(border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(28.dp))
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF171224),
            focusedContainerColor = Color(0xFF1D172E),
            contentColor = Color.White,
            focusedContentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.92f)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF221A36),
                            Color(0xFF140F22),
                            Color(0xFF0F0B1A)
                        )
                    )
                )
        ) {
            // Barra de acento lateral izquierda
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(promColor, promColor.copy(alpha = 0.2f))
                        )
                    )
                    .align(Alignment.CenterStart)
            )

            // Contenido principal en 2 columnas
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 44.dp, end = 36.dp, top = 32.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                // ── Columna izquierda: Resumen del Grupo & KPI ──
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Badge de Categoría y Rendimiento
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(Color(0xFF2B2144), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(promColor, CircleShape)
                            )
                            Text(
                                text = "ESTADISTICAS • $rendimiento".uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = promColor,
                                letterSpacing = 1.sp
                            )
                        }

                        // Nombre del grupo
                        Text(
                            text = grupo.nombreClase,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Promedio General Hero
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A142A).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = promLabel,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 64.sp
                            )
                            Text(
                                text = "/ 10.0",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = promColor,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Text(
                            text = "Promedio General del Grupo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCCC2DC)
                        )
                    }

                    // Tarjeta de métricas (Alumnos, Evaluados, Tareas)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A142A).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatRow(label = "Alumnos inscritos", value = "${grupo.totalAlumnos}")
                        StatRow(label = "Alumnos evaluados", value = "${grupo.alumnosCalificados}")
                        StatRow(label = "Tareas registradas", value = "${grupo.promediosPorTarea.size}")

                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Avance de evaluación",
                                fontSize = 11.sp,
                                color = Color(0xFFAAA5B8)
                            )
                            Text(
                                text = "${(pctCalificados * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(Color(0xFF2D2542), CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pctCalificados.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF7C4DFF), Color(0xFFD0BCFF))
                                        ),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }

                // ── Columna derecha: Gráfica de barras por tarea ──
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    Column {
                        Text(
                            text = "Rendimiento por Tarea",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Promedio de calificaciones obtenidas por actividad",
                            fontSize = 12.sp,
                            color = Color(0xFFAAA5B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (grupo.promediosPorTarea.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color(0xFF1A142A).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin tareas registradas en esta materia",
                                color = Color(0xFFAAA5B8),
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            grupo.promediosPorTarea.take(6).forEachIndexed { idx, mat ->
                                BarRow(index = idx + 1, materia = mat)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Fila de barra horizontal por tarea
// ---------------------------------------------------------------------------
@Composable
/**
 * Metodo principal que ejecuta la operacion: BarRow.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun BarRow(index: Int, materia: PromedioMateria) {
    val score = materia.promedio ?: 0.0
    val barColor = materia.promedio.calColor()
    val frac = (score / 10.0).toFloat().coerceIn(0f, 1f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B152B).copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        // Número y Nombre de la tarea
        Row(
            modifier = Modifier.width(180.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color(0xFF2C2245), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF)
                )
            }
            Text(
                text = materia.nombreTarea,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Barra de progreso
        Box(
            modifier = Modifier
                .weight(1f)
                .height(18.dp)
                .background(Color(0xFF251E38), RoundedCornerShape(6.dp))
        ) {
            if (frac > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(frac)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(barColor.copy(alpha = 0.75f), barColor)
                            ),
                            RoundedCornerShape(6.dp)
                        )
                )
            }
        }

        // Badge de Valor Numérico
        Box(
            modifier = Modifier
                .width(48.dp)
                .background(
                    if (materia.promedio != null) barColor.copy(alpha = 0.18f) else Color(0xFF251E38),
                    RoundedCornerShape(6.dp)
                )
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = materia.promedio.calLabel(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (materia.promedio != null) barColor else Color(0xFF9E95A8)
            )
        }

        // Total calificados
        Text(
            text = if (materia.totalCalificados > 0) "${materia.totalCalificados} calif." else "Sin calif.",
            fontSize = 11.sp,
            color = Color(0xFFAAA5B8),
            modifier = Modifier.width(62.dp),
            textAlign = TextAlign.End
        )
    }
}

// ---------------------------------------------------------------------------
// Fila de estadistica resumen (etiqueta + valor)
// ---------------------------------------------------------------------------
@Composable
/**
 * Metodo principal que ejecuta la operacion: StatRow.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFFAAA5B8)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// ---------------------------------------------------------------------------
// Badge reutilizable
// ---------------------------------------------------------------------------
@Composable
/**
 * Metodo principal que ejecuta la operacion: InfoBadge.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
private fun InfoBadge(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF2B2144), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------------------------------------------------------------------------
// Indicador de paginacion
// ---------------------------------------------------------------------------
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
/**
 * Metodo principal que ejecuta la operacion: PaginationIndicator.
 * Contiene la logica de negocio y control de flujo.
 * @param param Parametros de entrada (depende de la firma).
 * @return Retorna el resultado de la operacion o Unit si es un componente.
 */
fun PaginationIndicator(count: Int, currentIndex: Int, isGrupo: (Int) -> Boolean = { false }) {
    if (count <= 12) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(count) { index ->
                val isActive = index == currentIndex
                val isG = isGrupo(index)
                val dotColor by animateColorAsState(
                    targetValue = when {
                        isActive && isG -> Color(0xFFD0BCFF)
                        isActive        -> MaterialTheme.colorScheme.primary
                        isG             -> Color.White.copy(alpha = 0.28f)
                        else            -> Color(0xFF2E2742)
                    },
                    animationSpec = tween(300),
                    label = "dotColor"
                )
                val dotSize by animateDpAsState(
                    targetValue = if (isActive) 18.dp else 10.dp,
                    animationSpec = tween(300),
                    label = "dotSize"
                )
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .background(
                            dotColor,
                            shape = if (isG) RoundedCornerShape(3.dp) else CircleShape
                        )
                )
            }
        }
    } else {
        InfoBadge(
            if (isGrupo(currentIndex)) "Grupo ${currentIndex + 1} de $count"
            else "Anuncio ${currentIndex + 1} de $count"
        )
    }
}
```

**Explicación detallada por sección:**

### Constantes de color (líneas ~44-54)
- `CalifAlta = Color.White`: Promedios ≥ 8.0 se muestran en blanco puro.
- `CalifMedia = Color(0xFFFFB74D)`: Promedios ≥ 6.0 en ámbar.
- `CalifBaja = Color(0xFFCF6679)`: Promedios < 6.0 en rosa/rojo.
- `CalifSinDatos = Color(0xFF9E9E9E)`: Sin datos en gris.

### `sealed class CarouselPage` (líneas ~57-60)
Tipos sellados que unifican las noticias y los grupos en un solo carrusel:
- `EventoPage(evento)`: Una diapositiva de noticia.
- `GrupoPage(grupo)`: Una diapositiva de estadísticas de grupo.

### `TVHomeScreen()` (líneas ~66-115)
1. Observa `uiState` y `estadisticasState` con `collectAsStateWithLifecycle`.
2. Construye la lista `pages` combinando eventos + grupos.
3. Renderiza estados de carga, error o vacío según corresponda.
4. Si hay páginas, muestra `AutoCarousel(pages)`.

### `AutoCarousel()` (líneas ~120-150)
1. Crea un `HorizontalPager` con todas las páginas.
2. Un `LaunchedEffect` avanza automáticamente cada 6,000 milisegundos.
3. Cada página se renderiza como `EventoHeroCard` o `GrupoEstadisticaCard` según su tipo.
4. Al final muestra el `PaginationIndicator`.

### `EventoHeroCard()` (líneas ~155-227)
Tarjeta de noticia institucional a pantalla completa:
1. Decodifica imagen (Base64 o URL) con `AsyncImage` de Coil.
2. Aplica un gradiente negro inferior para que el texto sea legible sobre la imagen.
3. Muestra título (`displayMedium`), descripción (`headlineSmall`), lugar y fecha.

### `GrupoEstadisticaCard()` (líneas ~232-390)
Tarjeta de estadísticas de un grupo, dividida en 2 columnas:
- **Columna izquierda (260dp):**
  - Etiqueta de categoría (`"ESTADISTICAS • Excelente"`)
  - Nombre del grupo (`displaySmall`)
  - Promedio general gigante (80sp)
  - Tabla resumen: alumnos inscritos, calificados y tareas
  - Barra de progreso de avance de calificación
- **Columna derecha:**
  - Título "Promedio por tarea"
  - Hasta 7 barras horizontales (`BarRow`)

### `BarRow()` (líneas ~395-440)
Barra horizontal proporcional de calificación:
- Calcula la fracción `promedio / 10.0` para determinar el ancho.
- Nombre de la tarea (150dp fijo), barra con gradiente, valor numérico y total calificados.

### `PaginationIndicator()` (líneas ~470-520)
- Si hay ≤ 12 páginas: muestra puntos individuales.
  - Círculos `●` para noticias.
  - Cuadrados `◻` para grupos.
  - El punto activo es más grande (18dp vs 10dp) y animado.
- Si hay > 12 páginas: muestra un badge de texto `"Grupo X de N"`.

---

## 8. Dependencia: `EventosSharedViewModel.kt` (módulo Core)

**Ruta:** `core/src/main/java/com/pmlp/edutask/ui/EventosSharedViewModel.kt`

Este ViewModel es compartido entre los módulos `:app` (coordinador) y `:tv`. Contiene toda la lógica de conexión con Firestore.

```kotlin
/**
 * ViewModel compartido entre modulos que sincroniza en tiempo real los eventos escolares
 * y recalcula los promedios y metricas de grupos para el modulo de TV.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.pmlp.edutask.model.Evento
import com.pmlp.edutask.model.EstadisticaGrupo
import com.pmlp.edutask.model.PromedioMateria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class EventosUiState {
    object Idle : EventosUiState()
    object Loading : EventosUiState()
    data class Success(val eventos: List<Evento>) : EventosUiState()
    data class Error(val message: String) : EventosUiState()
}

sealed class EstadisticasUiState {
    object Loading : EstadisticasUiState()
    data class Success(val grupos: List<EstadisticaGrupo>) : EstadisticasUiState()
    data class Error(val message: String) : EstadisticasUiState()
}

class EventosSharedViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<EventosUiState>(EventosUiState.Idle)
    val uiState: StateFlow<EventosUiState> = _uiState

    private val _estadisticasState = MutableStateFlow<EstadisticasUiState>(EstadisticasUiState.Loading)
    val estadisticasState: StateFlow<EstadisticasUiState> = _estadisticasState

    /** Listener de evidencias en tiempo real para el dashboard TV */
    private var evidenciasListener: ListenerRegistration? = null

    init {
        fetchEventos()
        fetchEstadisticasInstitucionales()
    }

    /**
     * Obtiene o recupera datos asociados a fetchEventos desde la base de datos o API.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun fetchEventos() {
        Log.d("FIRESTORE_DEBUG", "Iniciando listener a la coleccion 'eventos_escolares'...")
        _uiState.value = EventosUiState.Loading

        db.collection("eventos_escolares")
            .orderBy("fechaEvento", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIRESTORE_DEBUG", "Error al escuchar Firestore: ${error.message}", error)
                    _uiState.value = EventosUiState.Error(error.message ?: "Error al obtener eventos")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("FIRESTORE_DEBUG", "Snapshot recibido con ${snapshot.size()} documentos")
                    val list = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val titulo = doc.getString("titulo") ?: ""
                        val descripcion = doc.getString("descripcion") ?: ""

                        // Extracción segura del campo 'fechaEvento' soportando múltiples formatos
                        val fechaMillis: Long = try {
                            when (val fechaRaw = doc.get("fechaEvento")) {
                                is Timestamp -> fechaRaw.toDate().time
                                is String -> {
                                    // Si la fecha viene como ISO-8601 o String numérico
                                    try {
                                        java.time.Instant.parse(fechaRaw).toEpochMilli()
                                    } catch (_: Exception) {
                                        fechaRaw.toLongOrNull() ?: System.currentTimeMillis()
                                    }
                                }
                                is Number -> fechaRaw.toLong()
                                is Map<*, *> -> {
                                    // Maneja estructuras exportadas como {_seconds: X, _nanoseconds: Y}
                                    val seconds = (fechaRaw["_seconds"] as? Number)?.toLong() ?: 0L
                                    seconds * 1000
                                }
                                else -> {
                                    doc.getLong("fechaPublicacion") ?: System.currentTimeMillis()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FIRESTORE_DEBUG", "Error al procesar la fecha en documento $id: ${e.message}")
                            doc.getLong("fechaPublicacion") ?: System.currentTimeMillis()
                        }

                        val lugar = doc.getString("lugar") ?: ""
                        val imagenUrl = doc.getString("imagenUrl")

                        Evento(
                            idEvento = id,
                            titulo = titulo,
                            descripcion = descripcion,
                            lugar = lugar,
                            fechaPublicacion = fechaMillis,
                            imagenUrl = imagenUrl
                        )
                    }

                    if (list.isEmpty()) {
                        Log.w("FIRESTORE_DEBUG", "La lista procesada esta vacia.")
                    } else {
                        Log.d("FIRESTORE_DEBUG", "Exito: Se cargaron ${list.size} eventos correctamente.")
                    }

                    _uiState.value = EventosUiState.Success(list)
                }
            }
    }

    /**
     * Escucha en tiempo real la colección evidencias_tarea y recalcula las estadísticas
     * por grupo cada vez que el profesor guarda o actualiza una calificación.
     */
    private fun fetchEstadisticasInstitucionales() {
        evidenciasListener?.remove()

        // Listener en tiempo real sobre evidencias_tarea.
        // Cuando un profesor califica, este listener se activa automáticamente.
        evidenciasListener = db.collection("evidencias_tarea")
            .addSnapshotListener { evidenciasSnap, error ->
                if (error != null) {
                    Log.e("ESTADISTICAS_TV", "Error escuchando evidencias: ${error.message}")
                    _estadisticasState.value = EstadisticasUiState.Error(error.message ?: "Error")
                    return@addSnapshotListener
                }
                if (evidenciasSnap == null) return@addSnapshotListener

                // Cada vez que hay un cambio en evidencias, recalculamos las estadísticas
                viewModelScope.launch {
                    try {
                        calcularEstadisticas()
                    } catch (e: Exception) {
                        Log.e("ESTADISTICAS_TV", "Error calculando estadísticas: ${e.message}")
                        _estadisticasState.value = EstadisticasUiState.Error(e.message ?: "Error")
                    }
                }
            }
    }

    /**
     * Metodo principal que ejecuta la operacion: calcularEstadisticas.
     * Contiene la logica de negocio y control de flujo.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    private suspend fun calcularEstadisticas() {
        // 1. Obtener todas las clases
        val clasesSnap = db.collection("clases").get().await()
        val clases = clasesSnap.documents.map { doc ->
            Pair(doc.id, doc.getString("nombre") ?: doc.getString("nombreClase") ?: "Grupo sin nombre")
        }

        if (clases.isEmpty()) {
            _estadisticasState.value = EstadisticasUiState.Success(emptyList())
            return
        }

        val grupos = mutableListOf<EstadisticaGrupo>()

        for ((idClase, nombreClase) in clases) {
            try {
                // 2. Total de alumnos en el grupo
                val alumnosSnap = db.collection("clase_alumno")
                    .whereEqualTo("idClase", idClase)
                    .get().await()
                val totalAlumnos = alumnosSnap.size()
                val alumnoIds = alumnosSnap.documents.mapNotNull { it.getString("idUsuario") }

                // 3. Tareas del grupo
                val tareasSnap = db.collection("tareas")
                    .whereEqualTo("idClase", idClase)
                    .get().await()
                val tareas = tareasSnap.documents.map { it.id to (it.getString("titulo") ?: "Sin título") }

                if (tareas.isEmpty()) {
                    grupos.add(
                        EstadisticaGrupo(
                            idClase = idClase,
                            nombreClase = nombreClase,
                            promedioGeneral = null,
                            promediosPorTarea = emptyList(),
                            totalAlumnos = totalAlumnos,
                            alumnosCalificados = 0
                        )
                    )
                    continue
                }

                val tareaIds = tareas.map { it.first }

                // 4. Asignaciones de esas tareas
                // Map idAsignacion → (idAlumno, idTarea)
                val asigMap = mutableMapOf<String, Pair<String, String>>()
                val chunks = tareaIds.chunked(10)
                for (chunk in chunks) {
                    val asigSnap = db.collection("asignaciones_tarea")
                        .whereIn("idTarea", chunk)
                        .get().await()
                    for (doc in asigSnap.documents) {
                        val idAlumno = doc.getString("idUsuario") ?: continue
                        val idTarea = doc.getString("idTarea") ?: continue
                        asigMap[doc.id] = Pair(idAlumno, idTarea)
                    }
                }

                // 5. Evidencias con calificaciones
                // Map (idAlumno, idTarea) → calificacion
                val calificaciones = mutableMapOf<Pair<String, String>, Int>()
                val asigIds = asigMap.keys.toList()
                val asigChunks = asigIds.chunked(10)
                for (chunk in asigChunks) {
                    val evSnap = db.collection("evidencias_tarea")
                        .whereIn("idAsignacion", chunk)
                        .get().await()
                    for (ev in evSnap.documents) {
                        val califRaw = ev.get("calificacion")
                        val calif = when (califRaw) {
                            is Number -> califRaw.toInt()
                            is String -> califRaw.toIntOrNull()
                            else -> null
                        } ?: continue
                        val idAsig = ev.getString("idAsignacion") ?: continue
                        val pair = asigMap[idAsig] ?: continue
                        calificaciones[pair] = calif
                    }
                }

                // 6. Calcular promedio por tarea (promedio del grupo en esa materia)
                val promediosPorTarea = tareas.map { (idTarea, nombreTarea) ->
                    val califsEnTarea = alumnoIds.mapNotNull { idAlumno ->
                        calificaciones[Pair(idAlumno, idTarea)]
                    }
                    val promedio = if (califsEnTarea.isNotEmpty())
                        califsEnTarea.sum().toDouble() / califsEnTarea.size.toDouble()
                    else null
                    PromedioMateria(
                        idTarea = idTarea,
                        nombreTarea = nombreTarea,
                        promedio = promedio,
                        totalCalificados = califsEnTarea.size
                    )
                }

                // 7. Promedio general del grupo
                val todasLasCalifs = calificaciones.filter { (key, _) ->
                    alumnoIds.contains(key.first)
                }.values.toList()

                val promedioGeneral = if (todasLasCalifs.isNotEmpty())
                    todasLasCalifs.sum().toDouble() / todasLasCalifs.size.toDouble()
                else null

                val alumnosConCalif = alumnoIds.count { idAlumno ->
                    tareaIds.any { idTarea -> calificaciones.containsKey(Pair(idAlumno, idTarea)) }
                }

                grupos.add(
                    EstadisticaGrupo(
                        idClase = idClase,
                        nombreClase = nombreClase,
                        promedioGeneral = promedioGeneral,
                        promediosPorTarea = promediosPorTarea,
                        totalAlumnos = totalAlumnos,
                        alumnosCalificados = alumnosConCalif
                    )
                )
            } catch (e: Exception) {
                Log.e("ESTADISTICAS_TV", "Error procesando grupo $idClase: ${e.message}")
            }
        }

        _estadisticasState.value = EstadisticasUiState.Success(grupos.sortedBy { it.nombreClase })
        Log.d("ESTADISTICAS_TV", "Estadísticas actualizadas: ${grupos.size} grupos procesados")
    }

    /**
     * Manejador de evento para la accion onCleared.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    override fun onCleared() {
        super.onCleared()
        evidenciasListener?.remove()
    }

    /**
     * Guarda o actualiza los datos de saveEvento en la base de datos.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun saveEvento(evento: Evento, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val map = mutableMapOf<String, Any>(
                    "titulo" to evento.titulo,
                    "descripcion" to evento.descripcion,
                    "lugar" to evento.lugar,
                    "fechaEvento" to Timestamp(java.util.Date(evento.fechaPublicacion)),
                    "fechaPublicacion" to evento.fechaPublicacion
                )
                evento.imagenUrl?.let { map["imagenUrl"] = it }
                if (evento.idEvento.isEmpty()) {
                    db.collection("eventos_escolares").add(map).await()
                } else {
                    db.collection("eventos_escolares").document(evento.idEvento).set(map).await()
                }
                // No need to fetchEventos() here since snapshot listener updates automatically
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al guardar evento")
            }
        }
    }

    /**
     * Elimina el registro correspondiente a deleteEvento del sistema.
     * @param param Parametros de entrada (depende de la firma).
     * @return Retorna el resultado de la operacion o Unit si es un componente.
     */
    fun deleteEvento(idEvento: String) {
        viewModelScope.launch {
            try {
                db.collection("eventos_escolares").document(idEvento).delete().await()
                // No need to fetchEventos() here since snapshot listener updates automatically
            } catch (e: Exception) {
                _uiState.value = EventosUiState.Error(e.message ?: "Error al eliminar evento")
            }
        }
    }
}
```

**Explicación de las funciones principales:**

### `fetchEventos()` (líneas 52-121)
Establece un `addSnapshotListener` en tiempo real sobre `eventos_escolares`:
- Parsea el campo `fechaEvento` que puede venir como `Timestamp`, `String` (ISO-8601), `Number` (epoch millis) o `Map` (exportación JSON con `_seconds`).
- Construye objetos `Evento` y actualiza `_uiState`.

### `fetchEstadisticasInstitucionales()` (líneas 127-151)
Establece un listener sobre **toda** la colección `evidencias_tarea`. Cada vez que cualquier documento cambia (un profesor califica), lanza `calcularEstadisticas()`.

### `calcularEstadisticas()` (líneas 153-281)
Algoritmo de agregación en 7 pasos:
1. Obtiene todas las clases del sistema.
2. Para cada clase, cuenta los alumnos inscritos (`clase_alumno`).
3. Obtiene las tareas de la clase.
4. Obtiene las asignaciones con `whereIn` chunkeado a 10 (límite de Firestore).
5. Obtiene las evidencias calificadas y extrae las notas.
6. Calcula el promedio por tarea.
7. Calcula el promedio general y los alumnos con al menos una calificación.

### `saveEvento()` y `deleteEvento()` (líneas 288-321)
CRUD para eventos institucionales, usado por el módulo del coordinador.

---

## Modelos de Datos Compartidos (módulo Core)

### `EstadisticaGrupo.kt`

```kotlin
/**
 * Modelos de datos compartidos (EstadisticaGrupo y PromedioMateria) para estructurar
 * las metricas y promedios calculados por grupo y tarea para el Dashboard de TV.
 */
package com.pmlp.edutask.model

/**
 * Estadísticas de calificaciones para un grupo/clase.
 * Usada por el Dashboard institucional del módulo TV.
 */
data class EstadisticaGrupo(
    val idClase: String = "",
    val nombreClase: String = "",
    /** Promedio general de todos los alumnos del grupo en todas las tareas */
    val promedioGeneral: Double? = null,
    /** Lista de (nombreTarea ? promedio del grupo en esa tarea) */
    val promediosPorTarea: List<PromedioMateria> = emptyList(),
    /** Total de alumnos en el grupo */
    val totalAlumnos: Int = 0,
    /** Total de alumnos que tienen al menos una calificación */
    val alumnosCalificados: Int = 0
)

data class PromedioMateria(
    val idTarea: String = "",
    val nombreTarea: String = "",
    val promedio: Double? = null,
    val totalCalificados: Int = 0
)
```

### `Evento.kt`

```kotlin
/**
 * Modelo de datos compartido representativo de los eventos y avisos institucionales
 * publicados para la comunidad escolar.
 */
package com.pmlp.edutask.model

data class Evento(
    val idEvento: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val lugar: String = "",
    val fechaPublicacion: Long = System.currentTimeMillis(),
    val imagenUrl: String? = null
)
```

### `Calificacion.kt`

```kotlin
/**
 * Modelo de datos compartido que representa la calificacion y retroalimentacion
 * asignada a una entrega de tarea.
 */
package com.pmlp.edutask.model

import java.util.Date

data class Calificacion(
    val idCalificacion: String = "",
    val idEvidencia: String = "",
    val idProfesor: String = "",
    val valor: Int = 0,
    val comentario: String = "",
    val fechaCalificacion: Date = Date()
)
```

---

## Guía de Compilación y Ejecución

```bash
# Compilar el APK de depuración
./gradlew :tv:assembleDebug

# Instalar en emulador o TV con ADB
adb install tv/build/outputs/apk/debug/tv-debug.apk
```
