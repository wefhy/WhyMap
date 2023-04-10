package dev.wefhy.whymap.whygraphics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WhyColorTest {
    @Test
    fun intTest() {
        val color = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        assertEquals(0x7f7f7f7f, color.intRGBA)
        assertEquals(0x7f7f7f, color.intRGB)
        assertEquals(0x7f, color.intA)
        assertEquals(0x7f, color.intR)
        assertEquals(0x7f, color.intG)
        assertEquals(0x7f, color.intB)

        val color2 = WhyColor(1f, 1f, 1f, 1f)
        assertEquals(0xffffffff.toInt(), color2.intRGBA)
        assertEquals(0xffffff, color2.intRGB)
        assertEquals(0xff, color2.intA)
        assertEquals(0xff, color2.intR)
        assertEquals(0xff, color2.intG)
        assertEquals(0xff, color2.intB)

        val color3 = WhyColor(0f, 0f, 0f, 0f)
        assertEquals(0x00000000, color3.intRGBA)
        assertEquals(0x000000, color3.intRGB)
        assertEquals(0x00, color3.intA)
        assertEquals(0x00, color3.intR)
        assertEquals(0x00, color3.intG)
        assertEquals(0x00, color3.intB)

        val color4 = WhyColor(0.25f, 0.5f, 1.0f, 0.0f)
        assertEquals(0x3f7fff00, color4.intRGBA)
        assertEquals(0x3f7fff, color4.intRGB)
        assertEquals(0x00, color4.intA)
        assertEquals(0x3f, color4.intR)
        assertEquals(0x7f, color4.intG)
        assertEquals(0xff, color4.intB)
    }

    @Test
    fun floatTest() {
        val color = WhyColor.fromRGBA(0x7f7f7f7f)
        assertEquals(0.49803925f, color.r)
        assertEquals(0.49803925f, color.g)
        assertEquals(0.49803925f, color.b)
        assertEquals(0.49803925f, color.a)

        val color2 = WhyColor.fromRGBA(0xffffffff.toInt())
        assertEquals(1f, color2.r)
        assertEquals(1f, color2.g)
        assertEquals(1f, color2.b)
        assertEquals(1f, color2.a)

        val color3 = WhyColor.fromRGBA(0x00000000)
        assertEquals(0f, color3.r)
        assertEquals(0f, color3.g)
        assertEquals(0f, color3.b)
        assertEquals(0f, color3.a)

        val color4 = WhyColor.fromRGBA(0x3f7fff00)
        assertEquals(0.24705884f, color4.r)
        assertEquals(0.49803925f, color4.g)
        assertEquals(1f, color4.b)
        assertEquals(0f, color4.a)
    }

    @Test
    fun alphaOverTest() {
        val colorA1 = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        val colorB1 = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        val colorC1 = colorA1 alphaOver colorB1
        assertEquals(0.5f, colorC1.r)
        assertEquals(0.5f, colorC1.g)
        assertEquals(0.5f, colorC1.b)
        assertEquals(0.75f, colorC1.a)

        val colorA2 = WhyColor(0.8f, 0.5f, 0.7f, 0.4f)
        val colorB2 = WhyColor(0.3f, 0.5f, 0.1f, 0.9f)
        val colorC2 = colorA2 alphaOver colorB2
        assertEquals(0.53000003f, colorC2.r)
        assertEquals(0.5f, colorC2.g)
        assertEquals(0.37600002f, colorC2.b)
        assertEquals(0.94f, colorC2.a)

        val colorD2 = colorB2 alphaOver colorA2
        assertEquals(0.32000002f, colorD2.r)
        assertEquals(0.5f, colorD2.g)
        assertEquals(0.12400001f, colorD2.b)
        assertEquals(0.94f, colorD2.a)

        val colorC3 = WhyColor.Red alphaOver WhyColor.Green
        assertEquals(1f, colorC3.r)
        assertEquals(0f, colorC3.g)
        assertEquals(0f, colorC3.b)
        assertEquals(1f, colorC3.a)
    }

    @Test
    fun plusTest() {
        val colorA1 = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        val colorB1 = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        val colorC1 = colorA1 + colorB1
        assertEquals(1f, colorC1.r)
        assertEquals(1f, colorC1.g)
        assertEquals(1f, colorC1.b)
        assertEquals(0.5f, colorC1.a)

        val colorA2 = WhyColor(0.8f, 0.5f, 0.7f, 0.4f)
        val colorB2 = WhyColor(0.3f, 0.5f, 0.1f, 0.9f)
        val colorC2 = colorA2 + colorB2
        assertEquals(1.1f, colorC2.r)
        assertEquals(1f, colorC2.g)
        assertEquals(0.8f, colorC2.b)
        assertEquals(0.65f, colorC2.a)
    }

    @Test
    fun timesFloatTest() {
        val colorA1 = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        val colorB1 = colorA1 * 0.5f
        assertEquals(0.25f, colorB1.r)
        assertEquals(0.25f, colorB1.g)
        assertEquals(0.25f, colorB1.b)
        assertEquals(0.5f, colorB1.a)

        val colorA2 = WhyColor(0.8f, 0.5f, 0.7f, 0.4f)
        val colorB2 = colorA2 * 0.4f
        assertEquals(0.32000002f, colorB2.r)
        assertEquals(0.2f, colorB2.g)
        assertEquals(0.28f, colorB2.b)
        assertEquals(0.4f, colorB2.a)
    }

    @Test
    fun timesColorTest() {
        val colorA1 = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        val colorB1 = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        val colorC1 = colorA1 * colorB1
        assertEquals(0.25f, colorC1.r)
        assertEquals(0.25f, colorC1.g)
        assertEquals(0.25f, colorC1.b)
        assertEquals(0.25f, colorC1.a)

        val colorA2 = WhyColor(0.8f, 0.5f, 0.7f, 0.4f)
        val colorB2 = WhyColor(0.3f, 0.5f, 0.1f, 0.9f)
        val colorC2 = colorA2 * colorB2
        assertEquals(0.24000001f, colorC2.r)
        assertEquals(0.25f, colorC2.g)
        assertEquals(0.07f, colorC2.b)
        assertEquals(0.35999998f, colorC2.a)
    }

    @Test
    fun mixTest() {
        val colorA1 = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        val colorB1 = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        val colorC1 = colorA1 mix colorB1
        assertEquals(0.5f, colorC1.r)
        assertEquals(0.5f, colorC1.g)
        assertEquals(0.5f, colorC1.b)
        assertEquals(0.5f, colorC1.a)

        val colorA2 = WhyColor(0.8f, 0.5f, 0.7f, 0.4f)
        val colorB2 = WhyColor(0.3f, 0.5f, 0.1f, 0.9f)
        val colorC2 = colorA2 mix colorB2
        assertEquals(0.55f, colorC2.r)
        assertEquals(0.5f, colorC2.g)
        assertEquals(0.4f, colorC2.b)
        assertEquals(0.65f, colorC2.a)
    }

    @Test
    fun mixWeightTest() {
        val colorA1 = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        val colorB1 = WhyColor(0.5f, 0.5f, 0.5f, 0.5f)
        val colorC1 = colorA1.mixWeight(colorB1, 0.5f)
        assertEquals(0.5f, colorC1.r)
        assertEquals(0.5f, colorC1.g)
        assertEquals(0.5f, colorC1.b)
        assertEquals(0.5f, colorC1.a)

        val colorA2 = WhyColor(0.8f, 0.5f, 0.7f, 0.4f)
        val colorB2 = WhyColor(0.3f, 0.5f, 0.1f, 0.9f)
        val colorC2 = colorA2.mixWeight(colorB2, 0.5f)
        assertEquals(0.55f, colorC2.r)
        assertEquals(0.5f, colorC2.g)
        assertEquals(0.4f, colorC2.b)
        assertEquals(0.65f, colorC2.a)

        val colorD2 = colorA2.mixWeight(colorB2, 0.4f)
        assertEquals(0.5f, colorD2.r)
        assertEquals(0.5f, colorD2.g)
        assertEquals(0.34f, colorD2.b)
        assertEquals(0.70000005f, colorD2.a)
    }
}