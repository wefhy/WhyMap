package dev.wefhy.whymap.whygraphics

import net.minecraft.client.texture.NativeImage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.image.DataBuffer
import java.awt.image.Raster

class WhyTileTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun get() {
        val array = Array(WhyTile.arraySize) {
            if (it % (WhyTile.chunkSize + 1) == 0) WhyColor.Red else WhyColor.Blue
        }
        val tile = WhyTile(array)
        for (x in 0 until WhyTile.chunkSize) {
            for (y in 0 until WhyTile.chunkSize) {
                if (x == y) {
                    assertEquals(WhyColor.Red, tile[x, y])
                } else {
                    assertEquals(WhyColor.Blue, tile[x, y])
                }
            }
        }
    }

    @Test
    fun getLine() {
    }

    @Test
    fun alphaInvariantAverage() {
        val array = Array(WhyTile.arraySize) {
            if (it % (WhyTile.chunkSize + 1) == 0) WhyColor(0.8f, 0.5f, 0.7f, 0.4f) else WhyColor(0.3f, 0.5f, 0.1f, 0.9f)
        }
        val tile = WhyTile(array)
        val alphaInvariantAverage = tile.alphaInvariantAverage()
        assertEquals(0.33125034f, alphaInvariantAverage.r)
        assertEquals(0.5f, alphaInvariantAverage.g)
        assertEquals(0.13750015f, alphaInvariantAverage.b)
        assertEquals(0.868748f, alphaInvariantAverage.a)
    }

    @Test
    fun average() {
        val array = Array(WhyTile.arraySize) {
            if (it % (WhyTile.chunkSize + 1) == 0) WhyColor(0.8f, 0.5f, 0.7f, 0.4f) else WhyColor(0.3f, 0.5f, 0.1f, 0.9f)
        }
        val tile = WhyTile(array)
        val average = tile.average()
        assertEquals(0.31438926f, average.r)
        assertEquals(0.5f, average.g)
        assertEquals(0.1172666f, average.b)
        assertEquals(0.868748f, average.a)
    }

    @Test
    fun alphaOver() {
        val arrayA = Array(WhyTile.arraySize) {
            if (it % (WhyTile.chunkSize + 1) == 0) WhyColor(0.8f, 0.5f, 0.7f, 0.4f) else WhyColor(0.3f, 0.5f, 0.1f, 0.9f)
        }
        val arrayB = Array(WhyTile.arraySize) {
            if (it % (WhyTile.chunkSize + 1) == 0) WhyColor.Red else WhyColor.Blue
        }
        val arrayExpectedC = Array(WhyTile.arraySize) {
            if (it % (WhyTile.chunkSize + 1) == 0) WhyColor(0.9200001f, 0.2f, 0.28f, 1.0f) else WhyColor(0.27f, 0.45f, 0.19000003f, 1.0f)
        }
        val arrayExpectedD = Array(WhyTile.arraySize) {
            if (it % (WhyTile.chunkSize + 1) == 0) WhyColor.Red else WhyColor.Blue
        }
        val tileA = WhyTile(arrayA)
        val tileB = WhyTile(arrayB)
        val tileExpectedC = WhyTile(arrayExpectedC)
        val tileExpectedD = WhyTile(arrayExpectedD)
        val tileResultC = tileA alphaOver tileB
        val tileResultD = tileB alphaOver tileA

        for (x in 0 until WhyTile.chunkSize) {
            for (y in 0 until WhyTile.chunkSize) {
                val expectedColor = tileExpectedC[x, y]
                val resultColor = tileResultC[x, y]
                assertEquals(expectedColor.r, resultColor.r)
                assertEquals(expectedColor.g, resultColor.g)
                assertEquals(expectedColor.b, resultColor.b)
                assertEquals(expectedColor.a, resultColor.a)
            }
        }
        for (x in 0 until WhyTile.chunkSize) {
            for (y in 0 until WhyTile.chunkSize) {
                val expectedColor = tileExpectedD[x, y]
                val resultColor = tileResultD[x, y]
                assertEquals(expectedColor.r, resultColor.r)
                assertEquals(expectedColor.g, resultColor.g)
                assertEquals(expectedColor.b, resultColor.b)
                assertEquals(expectedColor.a, resultColor.a)
            }
        }
    }

    @Test
    fun writeIntoNative() {
        return
        val arrayA = Array(WhyTile.arraySize) {
            if (it % (WhyTile.chunkSize + 1) == 0) WhyColor(0.8f, 0.5f, 0.7f, 0.4f) else WhyColor(0.3f, 0.5f, 0.1f, 0.9f)
        }
        val tileA = WhyTile(arrayA)
        val nativeImage = NativeImage(WhyTile.chunkSize * 2, WhyTile.chunkSize * 2, false)
        tileA.writeInto(nativeImage, WhyTile.chunkSize / 2, WhyTile.chunkSize / 2)
        for (x in 0 until WhyTile.chunkSize * 2) {
            for (y in 0 until WhyTile.chunkSize * 2) {
//                println("$x, $y")
                val expectedColor = if (
                    x in WhyTile.chunkSize / 2 until WhyTile.chunkSize * 3 / 2 &&
                    y in WhyTile.chunkSize / 2 until WhyTile.chunkSize * 3 / 2
                )
                    tileA[x - WhyTile.chunkSize / 2, y - WhyTile.chunkSize / 2]
                else
                    WhyColor(0.0f, 0.0f, 0.0f, 0.0f)

                val resultColor = WhyColor.fromRGBA(nativeImage.getPixelColor(x, y))
                assertEquals((expectedColor.r * 255).toInt(), (resultColor.r * 255).toInt())
                assertEquals((expectedColor.g * 255).toInt(), (resultColor.g * 255).toInt())
                assertEquals((expectedColor.b * 255).toInt(), (resultColor.b * 255).toInt())
                assertEquals((expectedColor.a * 255).toInt(), (resultColor.a * 255).toInt())
            }
        }
    }

    @Test
    fun writeIntoRaster() {
        val arrayA = Array(WhyTile.arraySize) {
            if (it % (WhyTile.chunkSize + 1) == 0) WhyColor(0.8f, 0.5f, 0.7f, 0.4f) else WhyColor(0.3f, 0.5f, 0.1f, 0.9f)
        }
        val tileA = WhyTile(arrayA)
        val raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, WhyTile.chunkSize * 2, WhyTile.chunkSize * 2, 4, null)
        tileA.writeInto(raster, WhyTile.chunkSize / 2, WhyTile.chunkSize / 2)
        for (x in 0 until WhyTile.chunkSize * 2) {
            for (y in 0 until WhyTile.chunkSize * 2) {
//                println("$x, $y")
                val expectedColor = if (
                    x in WhyTile.chunkSize / 2 until WhyTile.chunkSize * 3 / 2 &&
                    y in WhyTile.chunkSize / 2 until WhyTile.chunkSize * 3 / 2
                )
                    tileA[x - WhyTile.chunkSize / 2, y - WhyTile.chunkSize / 2]
                else
                    WhyColor(0.0f, 0.0f, 0.0f, 0.0f)

                val resultColor = WhyColor.fromInts(
                    raster.getSample(x, y, 0),
                    raster.getSample(x, y, 1),
                    raster.getSample(x, y, 2),
                    raster.getSample(x, y, 3),
                )
                assertEquals((expectedColor.r * 255).toInt(), (resultColor.r * 255).toInt())
                assertEquals((expectedColor.g * 255).toInt(), (resultColor.g * 255).toInt())
                assertEquals((expectedColor.b * 255).toInt(), (resultColor.b * 255).toInt())
//                assertEquals((expectedColor.a * 255).toInt(), (resultColor.a * 255).toInt())
            }
        }
    }

    @Test
    fun getData() {
    }
}