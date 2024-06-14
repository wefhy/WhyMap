// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

object MinecraftFont {
    val minecraftFontFamily = FontFamily(
        Font(
            resource = "fonts/minecraft-font/MinecraftRegular.otf",
            style = FontStyle.Normal,
            weight = FontWeight.Normal
        ),
        Font(
            resource = "fonts/minecraft-font/MinecraftItalic.otf",
            style = FontStyle.Italic,
            weight = FontWeight.Normal
        ),
        Font(
            resource = "fonts/minecraft-font/MinecraftBold.otf",
            style = FontStyle.Normal,
            weight = FontWeight.Bold
        ),
        Font(
            resource = "fonts/minecraft-font/MinecraftBoldItalic.otf",
            style = FontStyle.Italic,
            weight = FontWeight.Bold
        )
    )
    val background = Color(0xFF6E6E6E)
//    val shadow = Color(0xFF404040)
    val shadow = Color.Black.copy(alpha = 0.5f)
}