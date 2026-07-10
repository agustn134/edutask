/* EduTask — Wear OS
 * Módulo de calificación rápida para el profesor.
 * Pantallas: lista de evidencias pendientes → calificación con un toque.
 */

package com.pmlp.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pmlp.wear.presentation.theme.EdutaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}

@Composable
fun EduTaskWearApp() {
    val vm: CalificarViewModel = viewModel()
    val uiState by vm.uiState.collectAsState()

    var destino: WearDestino by remember { mutableStateOf(WearDestino.Pendientes) }

    when (val dest = destino) {

        // ── Lista de evidencias pendientes ────────────────────────────────
        is WearDestino.Pendientes -> {
            val items = when (val s = uiState) {
                is CalificarUiState.ListaLista -> s.items
                else                           -> emptyList()
            }
            val cargando = uiState is CalificarUiState.Cargando

            PendientesScreen(
                items         = items,
                onSeleccionar = { evidencia -> destino = WearDestino.Calificar(evidencia) },
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
    }
}