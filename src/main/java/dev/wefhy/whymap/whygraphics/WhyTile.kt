// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.whygraphics

import net.minecraft.client.texture.NativeImage
import java.awt.image.BufferedImage
import java.awt.image.WritableRaster

open class WhyTile(val data: Array<WhyColor> = Array(arraySize) { WhyColor.Transparent }) : WhyImage(chunkSize, chunkSize) {

    @OptIn(ExpensiveCall::class)
    override fun get(y: Int, x: Int): WhyColor {
        return data[(y shl lineShl) + x]
    }

    fun getLine(y: Int): Array<WhyColor> {
        return data.sliceArray((y shl lineShl) until (y shl lineShl) + y)
    }


    fun alphaInvariantAverage(): WhyColor {
        var r = 0f
        var g = 0f
        var b = 0f
        var a = 0f

        for (color in data) {
            r += color.r
            g += color.g
            b += color.b
            a += color.a
        }

        return WhyColor(
            r * arraySizeDiv,
            g * arraySizeDiv,
            b * arraySizeDiv,
            a * arraySizeDiv
        )
    }


    fun average(): WhyColor {
        var r = 0f
        var g = 0f
        var b = 0f
        var a = 0f

        for (color in data) {
            val _a = color.a
            r += color.r * _a
            g += color.g * _a
            b += color.b * _a
            a += _a
        }
        return if (a == 0f)
            WhyColor(r, g, b, 0f)
        else WhyColor(
            r / a,
            g / a,
            b / a,
            a * arraySizeDiv
        )
    }

    infix fun alphaOver(o: WhyTile): WhyTile {
        return WhyTile(
            Array(arraySize) { i ->
                data[i] alphaOver o.data[i]
            }
        )
    }

    fun writeInto(raster: WritableRaster, xOffset: Int, yOffset: Int) {
        var i = 0
        for (y in yOffset until yOffset + chunkSize) {
            for (x in xOffset until xOffset + chunkSize) {
                val color = data[i++]
                raster.setSample(x, y, 0, color.intR)
                raster.setSample(x, y, 1, color.intG)
                raster.setSample(x, y, 2, color.intB)
//                raster.setSample(x, y, 3, color.intA)
            }
        }
    }

    fun writeInto(nativeImage: NativeImage, xOffset: Int, yOffset: Int) {
        var i = 0
        for (y in yOffset until yOffset + chunkSize) {
            for (x in xOffset until xOffset + chunkSize) {
                val color = data[i++]
                nativeImage.setColor(x, y, color.intRGBA)
            }
        }
    }

//    fun writeIntoUnsafe(nativeImage: NativeImage, xOffset: Int, yOffset: Int) {
//        var i = 0
//        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
//        val p = nativeImage.pointer
//        val w = nativeImage.width
//        for (y in yOffset until yOffset + chunkSize) {
//            var l = p + y * w * 4L
//            for (x in xOffset until xOffset + chunkSize) {
//                val color = data[i++]
//                MemoryUtil.memPutInt(l, color.intARGB)
//                l += 4L
//            }
//        }
//    }

    companion object {
        const val lineShl = 4
        const val chunkSize = 1 shl lineShl
        const val arrayShl = lineShl shl 1
        const val arraySize = chunkSize * chunkSize
        const val lineMask = chunkSize - 1
        const val arrayMask = arraySize - 1
        const val arraySizeDiv = 1f / arraySize

        fun BufferedImage.asWhyTile(): WhyTile {
            assert(width == chunkSize)
            assert(height == chunkSize)
            var x = 0
            var y = 0
            return WhyTile(Array(arraySize) { i ->

                WhyColor.fromInts(
                    raster.getSample(x, y, 0),
                    raster.getSample(x, y, 1),
                    raster.getSample(x, y, 2),
//                    raster.getSample(x, y, 3)
                ).also {
                    x++
                    if (x == chunkSize) {
                        x = 0
                        y++
                    }
                }
            })
        }
    }
}

