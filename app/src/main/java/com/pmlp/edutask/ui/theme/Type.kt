package com.pmlp.edutask.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pmlp.edutask.R

val PlayfairDisplay = FontFamily(Font(R.font.playfair_display, FontWeight.Bold))
val RobotoFamily    = FontFamily(Font(R.font.roboto, FontWeight.Normal))

val EduTaskTypography = Typography(
    displayMedium = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,   fontSize = 36.sp, lineHeight = 44.sp),
    displaySmall  = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,   fontSize = 28.sp, lineHeight = 36.sp),
    headlineLarge = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Bold,      fontSize = 28.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge   = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall   = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall  = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp)
)