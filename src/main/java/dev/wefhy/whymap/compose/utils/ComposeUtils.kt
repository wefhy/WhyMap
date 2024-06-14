// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.toOffset
import dev.wefhy.whymap.utils.ImageFormat
import dev.wefhy.whymap.utils.ImageWriter.encode
import dev.wefhy.whymap.utils.ImageWriter.encodeJPEG
import dev.wefhy.whymap.utils.LocalTileBlock
import org.jetbrains.skia.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream

object ComposeUtils {
    fun LocalTileBlock.toOffset(): Offset = androidx.compose.ui.unit.IntOffset(x, z).toOffset()
    fun Offset.toLocalTileBlock(): LocalTileBlock = LocalTileBlock(x.toInt(), y.toInt())
    val goodColors = listOf<Color>(
        Color(0xFF0000FF),
        Color(0xFF00FF00),
        Color(0xFFFF0000),
        Color(0xFFFFFF00),
        Color(0xFFFF00FF),
        Color(0xFF00FFFF),
        Color(0xFF000000),
        Color(0xFFFFFFFF),
        Color(0xFF808080),
        Color(0xFF663300)
    )
    fun Color.goodBackground(): Color = if (luminance() > 0.5f) Color.Black else Color.White

    fun BufferedImage.toImageBitmap(intermediate: ImageFormat): ImageBitmap? {
        val stream = ByteArrayOutputStream()
        stream.encode(this, intermediate)
        return try {
            Image.makeFromEncoded(stream.toByteArray()).toComposeImageBitmap()
        } catch (e: Throwable) {
            null
        }
    }
}