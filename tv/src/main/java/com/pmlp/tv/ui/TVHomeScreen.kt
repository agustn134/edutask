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
 * El nucleo principal (Dashboard) del modulo de Android TV.
 * Escucha asincronamente dos flujos de datos en tiempo real provenientes de Firestore (a traves del ViewModel):
 * 1. Eventos institucionales (Noticias).
 * 2. Estadisticas academicas globales por grupo.
 * Combina ambos flujos en una sola lista inmutable (CarouselPage) y orquesta la visualizacion
 * mostrando un mensaje de carga, de error, o inyectando la lista combinada al `AutoCarousel`.
 *
 * @param viewModel ViewModel compartido instanciado desde el modulo :core que provee el estado reactivo.
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
 * Componente utilitario simple que dibuja un texto completamente centrado en la pantalla del televisor.
 * Se utiliza principalmente como 'Placeholder' (marcador de posicion) durante estados de carga (Loading)
 * o cuando la base de datos devuelve resultados vacios o arroja errores de conexion.
 *
 * @param msg Texto informativo a mostrar en pantalla.
 * @param color Color en el que se dibujara el texto, dictado por el MaterialTheme.
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
 * Paginador automatizado de desplazamiento horizontal infinito.
 * Representa el carrusel visual que ira alternando por si solo (sin intervencion humana)
 * cada una de las diapositivas de avisos y grupos. Emplea una corrutina (LaunchedEffect)
 * con un 'delay' constante de 6 segundos, forzando un 'animateScrollToPage' ciclico (modulo)
 * para lograr el efecto de cartelera rotativa.
 *
 * @param pages Lista sellada de objetos CarouselPage que contiene tanto noticias como dashboard de grupos.
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
 * Componente UI que diseña y renderiza una "Hero Card" gigante para mostrar anuncios y noticias de la escuela.
 * Utiliza Coil (AsyncImage) para descargar la fotografia en segundo plano y la dibuja ocupando todo el fondo
 * con un degradado superpuesto oscuro. Esto garantiza que la tipografia blanca
 * de los anuncios sea siempre legible independientemente del contraste de la fotografia original.
 *
 * @param evento Objeto que contiene titulo, descripcion, lugar, fecha y URL de la imagen del aviso institucional.
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
 * Dashboard analitico de nivel ejecutivo disenado para mostrar las metricas de un grupo.
 * Estructura la informacion en dos columnas grandes (ideal para aspecto 16:9 de TVs):
 * Columna Izquierda: KPIs de alto impacto, incluyendo el promedio general impreso en texto gigante,
 * insignias de rendimiento (Ej. "EXCELENTE") y una barra de progreso que indica el avance de las evaluaciones.
 * Columna Derecha: Un desglose grafico apilado mostrando el rendimiento individual obtenido por los alumnos en cada tarea.
 *
 * @param grupo Objeto EstadisticaGrupo calculado desde el modulo core que contiene los calculos y promedios reales del grupo.
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
 * Componente grafico modular que dibuja una unica barra de progreso horizontal.
 * Toma el promedio especifico de una materia/tarea (ej. 8.5) y lo traduce matematicamente
 * en un ancho proporcional usando el modifier `weight`. Pinta el interior de la barra con un gradiente
 * para darle profundidad y estilo moderno al dashboard estadistico.
 *
 * @param index Numero de orden o ranking de la materia en la lista.
 * @param materia Estructura de datos que contiene el titulo de la asignacion y la calificacion numerica.
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
 * Fila minimalista para presentar pares de clave-valor.
 * Utilizada extensamente en el panel izquierdo de estadisticas para detallar cantidades
 * exactas como total de alumnos, evaluados y tareas registradas en el periodo en curso.
 *
 * @param label El nombre descriptivo de la metrica.
 * @param value El valor numerico o textual asociado.
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
 * Etiqueta visual de esquinas redondeadas tipo "Pill" o "Chip".
 * Dibuja un fondo semitransparente (`alpha = 0.1f`) con texto de contraste alto,
 * perfecto para inyectar pequenos metadatos (como la fecha u hora de publicacion)
 * encima de las imagenes de fondo de los eventos sin perder estetica ni estorbar.
 *
 * @param text Cadena de texto corta que aparecera dentro del badge.
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
 * Sistema visual de rastreo de paginacion (los puntos en la parte inferior de la pantalla).
 * Iterara sobre la cantidad total de diapositivas y dibujara una forma diferente dependiendo
 * del tipo de contenido: Dibuja 'Circulos' si la diapositiva es un aviso, y 'Cuadrados o barras'
 * si la diapositiva es un resumen de grupo. Incluye animaciones fluidas de tamano y color que
 * reaccionan cuando cambia la pagina activa.
 *
 * @param count Cantidad total de elementos rotativos en el carrusel.
 * @param currentIndex Indice base cero (0) de la diapositiva que el espectador esta viendo actualmente.
 * @param isGrupo Funcion lambda evaluadora (predicado) que devuelve True si el slide corresponde a estadisticas.
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
