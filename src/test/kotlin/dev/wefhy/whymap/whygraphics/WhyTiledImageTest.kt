package dev.wefhy.whymap.whygraphics

import dev.wefhy.whymap.utils.ExpensiveCall
import dev.wefhy.whymap.whygraphics.WhyTile.Companion.chunkSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WhyTiledImageTest {

    @OptIn(ExpensiveCall::class)
    @Test
    fun get() {
        val tile = WhyTile(Array(WhyTile.arraySize) {
            if (it % (chunkSize + 1) == 0) WhyColor.Red else WhyColor.Blue
        })
        val image = WhyTiledImage(2, 2)
        image.data[0][1] = tile
        for (x in 0 until chunkSize * 2) {
            for (y in 0 until chunkSize * 2) {
                val result = image[y, x]
                if (y in 0 until chunkSize && x in chunkSize until chunkSize * 2) {
                    if (x - chunkSize == y) {
                        assertEquals(WhyColor.Red.r, result?.r)
                        assertEquals(WhyColor.Red.g, result?.g)
                        assertEquals(WhyColor.Red.b, result?.b)
                        assertEquals(WhyColor.Red.a, result?.a)
                    } else {
                        assertEquals(WhyColor.Blue.r, result?.r)
                        assertEquals(WhyColor.Blue.g, result?.g)
                        assertEquals(WhyColor.Blue.b, result?.b)
                        assertEquals(WhyColor.Blue.a, result?.a)
                    }
                } else {
                    assertEquals(null, result)
                }
            }
        }
    }

    @Test
    fun toBufferedImage() {
    }

    @Test
    fun toNativeImage() {
    }

    @Test
    fun toPng() {
    }

    @Test
    fun toJpeg() {
    }
}