// Copyright (c) 2023 wefhy

package dev.wefhy.whymap

import java.awt.image.BufferedImage
import java.awt.image.RescaleOp

fun mergeTextures(textures: List<List<BufferedImage>>): BufferedImage {
    val height = textures.size * 16
    val width = textures.first().size * 16

    val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g2d = bufferedImage.createGraphics()

    val op = RescaleOp(floatArrayOf(1f, 1f, 1f, 1f), FloatArray(4), null)

    textures.forEachIndexed { y, line ->
        line.forEachIndexed { x, image ->
            try {
                g2d.drawImage(image, op, x * 16, y * 16)
            } catch (e: IllegalArgumentException) {

            }
        }
    }
    return bufferedImage
}

fun testTexture(texture: BufferedImage) : BufferedImage {
    val height = 16
    val width = 16
    val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g2d = bufferedImage.createGraphics()
    val op = RescaleOp(floatArrayOf(1f, 1f, 1f, 1f), FloatArray(4), null)
    g2d.drawImage(texture, op, 0, 0)
    return bufferedImage
}

