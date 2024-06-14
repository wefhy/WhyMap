// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.styles

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val defaultMcTextStyle = TextStyle(
    fontFamily = MinecraftFont.minecraftFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    shadow = Shadow(
        color = MinecraftFont.shadow,
        offset = Offset(4f, 4f),
        blurRadius = 0.5f
    )
)

private val mcStyleNoShadow = TextStyle(
    fontFamily = MinecraftFont.minecraftFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal
)

@Composable
fun McTheme(colors: Colors, content: @Composable () -> Unit) {
//    val shadowColor = if (isSystemInDarkTheme()) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f)
    MaterialTheme(
        colors = colors,
        typography = Typography(
            defaultFontFamily = MinecraftFont.minecraftFontFamily,
            button = TextStyle(
                fontFamily = MinecraftFont.minecraftFontFamily,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
                fontSize = 20.sp,
                shadow = Shadow(
                    color = MinecraftFont.shadow,
                    offset = Offset(4f, 4f),
                    blurRadius = 0.5f
                )
            ),
//            body1 = TextStyle(
//                fontFamily = MinecraftFont.minecraftFontFamily,
//                fontWeight = FontWeight.Normal,
//                fontStyle = FontStyle.Normal,
//                fontSize = 16.sp,
//                shadow = Shadow(
//                    color = MinecraftFont.shadow,
//                    offset = Offset(4f, 4f),
//                    blurRadius = 0.5f
//                )
//            ),
            h1 = defaultMcTextStyle,
            caption = mcStyleNoShadow,
            subtitle1 = mcStyleNoShadow,
        ),
        shapes = Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(4.dp),
            large = RoundedCornerShape(0.dp)
        ),
        content = content
    )
}