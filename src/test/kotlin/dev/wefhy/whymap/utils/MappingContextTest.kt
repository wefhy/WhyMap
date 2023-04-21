package dev.wefhy.whymap.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MappingContextTest {
    val context1 = MappingContext(100, 0.0, 100.0)
    val context2 = MappingContext(1000, 0.0, 100.0)
    val context3 = MappingContext(1024, -1.0, 1.0)

    @Test
    fun getMapToDouble() {
        with(context1) {
            assertEquals(0.0, 0.mapToDouble)
            assertEquals(50.0, 50.mapToDouble)
            assertEquals(100.0, 100.mapToDouble)
        }
        with(context2) {
            assertEquals(0.0, 0.mapToDouble)
            assertEquals(50.0, 500.mapToDouble)
            assertEquals(100.0, 1000.mapToDouble)
        }
        with(context3) {
            assertEquals(-1.0, 0.mapToDouble)
            assertEquals(0.0, 512.mapToDouble)
            assertEquals(1.0, 1024.mapToDouble)
        }
    }

    @Test
    fun getMapToInt() {
        with(context1) {
            assertEquals(0, 0.0.mapToInt)
            assertEquals(50, 50.0.mapToInt)
            assertEquals(100, 100.0.mapToInt)
        }
        with(context2) {
            assertEquals(0, 0.0.mapToInt)
            assertEquals(500, 50.0.mapToInt)
            assertEquals(1000, 100.0.mapToInt)
        }
        with(context3) {
            assertEquals(0, (-1.0).mapToInt)
            assertEquals(512, 0.0.mapToInt)
            assertEquals(1024, 1.0.mapToInt)
        }
    }
}