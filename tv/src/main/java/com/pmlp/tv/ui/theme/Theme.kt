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