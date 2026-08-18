package com.pmlp.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import coil.compose.AsyncImage
import androidx.compose.foundation.BorderStroke
import androidx.tv.material3.Border
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.tv.material3.*
import com.pmlp.edutask.model.Evento
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.edutask.ui.EventosUiState
import com.pmlp.tv.R
import com.pmlp.tv.ui.theme.EdutaskTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TVHomeScreen(viewModel: EventosSharedViewModel) {
    EdutaskTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        // 2. Tema Visual
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Text(
                    text = "EduTask TV - Tablón de Anuncios",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                when (val state = uiState) {
                    is EventosUiState.Loading, EventosUiState.Idle -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Cargando anuncios...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 24.sp)
                        }
                    }
                    is EventosUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, fontSize = 24.sp)
                        }
                    }
                    is EventosUiState.Success -> {
                        if (state.eventos.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay anuncios disponibles", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 24.sp)
                            }
                        } else {
                            // 3. Carrusel a Pantalla Completa (Hero Banner)
                            AutoCarousel(eventos = state.eventos)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AutoCarousel(eventos: List<Evento>) {
    val pagerState = rememberPagerState(pageCount = { eventos.size })

    LaunchedEffect(eventos) {
        if (eventos.isNotEmpty()) {
            while (true) {
                delay(5000)
                val nextPage = (pagerState.currentPage + 1) % eventos.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EventoHeroCard(eventos[page])
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Indicador Dinámico de Paginación
        PaginationIndicator(
            count = eventos.size,
            currentIndex = pagerState.currentPage
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EventoHeroCard(evento: Evento) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateString = formatter.format(Date(evento.fechaPublicacion))

    Surface(
        onClick = { },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(24.dp)),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp)
            )
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = Color.White,
            focusedContentColor = Color.White,
            pressedContainerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.9f)
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val url = evento.imagenUrl
            if (!url.isNullOrEmpty()) {
                val imageModel = remember(url) {
                    try {
                        val base64Str = if (url.startsWith("data:image")) url.substringAfter("base64,") else url
                        if (base64Str.length > 500 && !base64Str.startsWith("http")) {
                            val bytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } else {
                            url
                        }
                    } catch (e: Exception) {
                        url
                    }
                }

                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Column {
                    Text(
                        text = evento.titulo,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = evento.descripcion,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 40.sp,
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (evento.lugar.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "📍 Lugar: ${evento.lugar}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Publicado: $dateString",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PaginationIndicator(count: Int, currentIndex: Int) {
    if (count <= 6) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(count) { index ->
                val color = if (index == currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val size = if (index == currentIndex) 16.dp else 12.dp
                Box(
                    modifier = Modifier
                        .size(size)
                        .background(color, shape = CircleShape)
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Anuncio ${currentIndex + 1} de $count",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}