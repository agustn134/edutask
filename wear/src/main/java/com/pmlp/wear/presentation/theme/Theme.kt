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