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
