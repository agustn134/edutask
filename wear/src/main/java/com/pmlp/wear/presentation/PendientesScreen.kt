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
private fun PreviewPendientes() {
    val mocks = listOf(
        EvidenciaPendiente("1", "María López",  "Ensayo Revolución"),
        EvidenciaPendiente("2", "Juan Ramírez", "Diagramas UML"),
        EvidenciaPendiente("3", "Ana Torres",   "Actividad 5")
    )
    PendientesScreen(nombreProfesor = "Martha Elena", items = mocks, onSeleccionar = {}, onRefrescar = {})
}
