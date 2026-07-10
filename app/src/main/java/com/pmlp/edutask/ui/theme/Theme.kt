package com.pmlp.edutask.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary              = EduPrimary,
    onPrimary            = EduOnPrimary,
    primaryContainer     = EduPrimaryContainer,
    onPrimaryContainer   = EduOnPrimaryContainer,
    secondary            = EduSecondary,
    onSecondary          = EduOnSecondary,
    secondaryContainer   = EduSecondaryContainer,
    onSecondaryContainer = EduOnSecondaryContainer,
    tertiary             = EduTertiary,
    onTertiary           = EduOnTertiary,
    tertiaryContainer    = EduTertiaryContainer,
    onTertiaryContainer  = EduOnTertiaryContainer,
    error                = EduError,
    onError              = EduOnError,
    errorContainer       = EduErrorContainer,
    onErrorContainer     = EduOnErrorContainer,
    background           = EduBackground,
    onBackground         = EduOnBackground,
    surface              = EduSurface,
    onSurface            = EduOnSurface,
    surfaceVariant       = EduSurfaceVariant,
    onSurfaceVariant     = EduOnSurfaceVariant,
    outline              = EduOutline,
    outlineVariant       = EduOutlineVariant,
    scrim                = EduScrim,
    inverseSurface       = EduInverseSurface,
    inverseOnSurface     = EduInverseOnSurface,
    inversePrimary       = EduInversePrimary,
    surfaceTint          = EduSurfaceTint,
    surfaceContainerLowest  = EduSurfaceContainerLowest,
    surfaceContainerLow     = EduSurfaceContainerLow,
    surfaceContainer        = EduSurfaceContainer,
    surfaceContainerHigh    = EduSurfaceContainerHigh,
    surfaceContainerHighest = EduSurfaceContainerHighest
)

private val DarkColorScheme = darkColorScheme(
    primary              = EduPrimaryDark,
    onPrimary            = EduOnPrimaryDark,
    primaryContainer     = EduPrimaryContainerDark,
    onPrimaryContainer   = EduOnPrimaryContainerDark,
    secondary            = EduSecondaryDark,
    onSecondary          = EduOnSecondaryDark,
    secondaryContainer   = EduSecondaryContainerDark,
    onSecondaryContainer = EduOnSecondaryContainerDark,
    tertiary             = EduTertiaryDark,
    onTertiary           = EduOnTertiaryDark,
    tertiaryContainer    = EduTertiaryContainerDark,
    onTertiaryContainer  = EduOnTertiaryContainerDark,
    error                = EduErrorDark,
    onError              = EduOnErrorDark,
    errorContainer       = EduErrorContainerDark,
    onErrorContainer     = EduOnErrorContainerDark,
    background           = EduBackgroundDark,
    onBackground         = EduOnBackgroundDark,
    surface              = EduSurfaceDark,
    onSurface            = EduOnSurfaceDark,
    surfaceVariant       = EduSurfaceVariantDark,
    onSurfaceVariant     = EduOnSurfaceVariantDark,
    outline              = EduOutlineDark,
    outlineVariant       = EduOutlineVariantDark,
    inverseSurface       = EduInverseSurfaceDark,
    inverseOnSurface     = EduInverseOnSurfaceDark,
    inversePrimary       = EduInversePrimaryDark,
    surfaceContainerLowest  = EduSurfaceContainerLowestDark,
    surfaceContainerLow     = EduSurfaceContainerLowDark,
    surfaceContainer        = EduSurfaceContainerDark,
    surfaceContainerHigh    = EduSurfaceContainerHighDark,
    surfaceContainerHighest = EduSurfaceContainerHighestDark
)

@Composable
fun EduTaskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, typography = EduTaskTypography, content = content)
}