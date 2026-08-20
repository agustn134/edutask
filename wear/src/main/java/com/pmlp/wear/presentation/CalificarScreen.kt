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
 * Pantalla de interaccion critica (Core Feature) del reloj.
 * Le muestra al profesor los datos del estudiante, botones de acceso para abrir fotos,
 * y lo mas importante: los botones de evaluacion numerica (10, 9, 8 o 0) para evaluar rapidamente la tarea
 * con pocos toques. Almacena en memoria (confirmando = x) la calificacion seleccionada y lanza
 * un dialogo de confirmacion para evitar toques accidentales o evaluaciones erroneas.
 *
 * @param evidencia El objeto EvidenciaPendiente con la informacion a evaluar.
 * @param esCargando Bandera que activa un indicador circular de progreso y deshabilita botones si se esta subiendo la nota a Firebase.
 * @param onCalificar Callback disparado cuando el profesor confirma finalmente la calificacion numerica.
 * @param onVolver Callback ejecutado para retroceder a la lista de pendientes.
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
 * Componente UI modular y reutilizable para pintar los botones de calificacion (10, 9, 8, 0).
 * Facilita establecer un diseno uniforme (alturas fijas, tipografia gruesa) y permite
 * la inyeccion dinamica de esquemas de colores (tertiary, primary, secondary, error) para una
 * comunicacion visual mas clara segun la nota (verde para 10, rojo para 0).
 *
 * @param label El texto numerico del boton (ej. "10").
 * @param colors Objeto de Compose que define el color de fondo y de texto del boton.
 * @param onClick Accion ejecutada cuando el profesor presiona el boton en el smartwatch.
 * @param modifier Modificador opcional para inyectar estilos extra de layout (padding, pesos, etc.).
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
 * Vista de paso intermedio de seguridad.
 * Cuando el profesor toca un boton numerico, se superpone esta vista preguntandole "Guardar nota?".
 * Previene el fat-finger (toques accidentales muy comunes en relojes pequenos) y obliga
 * a elegir 'Si' o 'No' (onConfirmar u onCancelar) antes de lanzar el update a la base de datos de Firebase.
 *
 * @param nota La calificacion numerica elegida que se somete a verificacion visual.
 * @param onConfirmar Callback que lanza la actualizacion asincrona hacia Firestore.
 * @param onCancelar Callback que limpia el estado y regresa la UI a la matriz de botones de calificacion.
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
 * Herramienta de visualizacion para Android Studio (Tooling).
 * Renderiza la pantalla 'CalificarScreen' inyectando un modelo de evidencia falso.
 * Permite a los desarrolladores ajustar el padding, tipografia y colores de los botones
 * sin tener que lanzar el emulador cada vez que hacen un cambio estético.
 */
private fun PreviewCalificar() {
    CalificarScreen(
        evidencia   = EvidenciaPendiente("1", "María López", "Ensayo Revolución"),
        esCargando  = false,
        onCalificar = {},
        onVolver    = {}
    )
}
