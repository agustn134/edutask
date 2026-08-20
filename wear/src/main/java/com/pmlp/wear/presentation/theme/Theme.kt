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
 * Aplica el tema visual principal de EduTask para el modulo de Wear OS.
 * Esta funcion se encarga de inyectar los colores, tipografias y formas
 * proporcionados por MaterialTheme de Compose for Wear OS, asegurando
 * que toda la interfaz sea coherente, de alto contraste y facil de leer
 * en pantallas pequenas (circulares o cuadradas).
 *
 * @param content El contenido composable (pantallas, botones, etc.) que estara envuelto por este tema.
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