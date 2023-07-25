package dev.wefhy.whymap.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RectAreaTest {

    val rectArea1 = RectArea(
        LocalTile.Block(0,0),
        LocalTile.Block(0, 0)
    )
    val rectArea2 = RectArea(
        LocalTile.Block(5,10),
        LocalTile.Block(-5, 20)
    )
    val rectArea3 = RectArea(
        LocalTile.Chunk(-1, 1),
        LocalTile.Chunk(-4, 0)
    )

    @Test
    fun getStartEnd() {
        assertEquals(0, rectArea1.end.x)
        assertEquals(0, rectArea1.end.z)
        assertEquals(0, rectArea1.start.x)
        assertEquals(0, rectArea1.start.z)

        assertEquals(5, rectArea2.end.x)
        assertEquals(20, rectArea2.end.z)
        assertEquals(-5, rectArea2.start.x)
        assertEquals(10, rectArea2.start.z)

        assertEquals(-1, rectArea3.end.x)
        assertEquals(1, rectArea3.end.z)
        assertEquals(-4, rectArea3.start.x)
        assertEquals(0, rectArea3.start.z)
    }


    @Test
    fun getSize() {
        assertEquals(1, rectArea1.sizeX)
        assertEquals(1, rectArea1.sizeZ)
        assertEquals(1, rectArea1.size)

        assertEquals(11, rectArea2.sizeX)
        assertEquals(11, rectArea2.sizeZ)
        assertEquals(121, rectArea2.size)

        assertEquals(4, rectArea3.sizeX)
        assertEquals(2, rectArea3.sizeZ)
        assertEquals(8, rectArea3.size)
    }


    @Test
    fun parent() {
        assertEquals(
            RectArea(
                LocalTile.Chunk(0,0),
                LocalTile.Chunk(0, 0)
            ),
            rectArea1.parent(TileZoom.ChunkZoom)
        )
        assertEquals(
            RectArea(
                LocalTile.Region(0,0),
                LocalTile.Region(0, 0)
            ),
            rectArea1.parent(TileZoom.RegionZoom)
        )
        assertEquals(
            RectArea(
                LocalTile.Chunk(-1,0),
                LocalTile.Chunk(0, 1)
            ),
            rectArea2.parent(TileZoom.ChunkZoom)
        )
        assertEquals(
            RectArea(
                LocalTile.Region(-1,0),
                LocalTile.Region(0, 0)
            ),
            rectArea2.parent(TileZoom.RegionZoom)
        )
        assertEquals(
            RectArea(
                LocalTile.Region(-1,0),
                LocalTile.Region(-1, 0)
            ),
            rectArea3.parent(TileZoom.RegionZoom)
        )
    }

    @Test
    fun blockArea() {
        assertEquals(
            RectArea(
                LocalTile.Block(0,0),
                LocalTile.Block(0, 0)
            ),
            rectArea1.blockArea()
        )
        assertEquals(
            RectArea(
                LocalTile.Block(-5,10),
                LocalTile.Block(5, 20)
            ),
            rectArea2.blockArea()
        )
        assertEquals(
            RectArea(
                LocalTile.Block(-64,0),
                LocalTile.Block(-1, 31)
            ),
            rectArea3.blockArea()
        )
    }

    @Test
    fun list() {
        assertEquals(
            listOf(
                LocalTile.Block(0,0)
            ),
            rectArea1.list()
        )
        assertEquals(
            (-5..5).flatMap { x ->
                (10..20).map { z ->
                    LocalTile.Block(x, z)
                }
            },
            rectArea2.list()
        )
        assertEquals(
            (-4..-1).flatMap { x ->
                (0..1).map { z ->
                    LocalTile.Chunk(x, z)
                }
            },
            rectArea3.list()
        )
    }

    @Test
    fun array() {
        val t1 = listOf(
            LocalTile.Block(0,0)
        )
        assertTrue(t1.containsAll(rectArea1.array().toList()))
        assertTrue(rectArea1.array().toList().containsAll(t1))

        val t2 = (-5..5).flatMap { x ->
            (10..20).map { z ->
                LocalTile.Block(x, z)
            }
        }
        assertTrue(t2.containsAll(rectArea2.array().toList()))
        assertTrue(rectArea2.array().toList().containsAll(t2))

        val t3 = (-4..-1).flatMap { x ->
            (0..1).map { z ->
                LocalTile.Chunk(x, z)
            }
        }
        assertTrue(t3.containsAll(rectArea3.array().toList()))
        assertTrue(rectArea3.array().toList().containsAll(t3))
    }

    @Test
    fun containsTile() {
        assertTrue(rectArea1.contains(LocalTile.Block(0,0)))
        assertTrue(!rectArea1.contains(LocalTile.Block(1,0)))
        assertTrue(!rectArea1.contains(LocalTile.Block(0,1)))
        assertTrue(!rectArea1.contains(LocalTile.Block(1,1)))

        assertTrue(rectArea2.contains(LocalTile.Block(5,10)))
        assertTrue(rectArea2.contains(LocalTile.Block(0,15)))
        assertTrue(rectArea2.contains(LocalTile.Block(-5,20)))
        assertTrue(!rectArea2.contains(LocalTile.Block(6,10)))
        assertTrue(!rectArea2.contains(LocalTile.Block(5,21)))
        assertTrue(!rectArea2.contains(LocalTile.Block(-6,20)))
        assertTrue(!rectArea2.contains(LocalTile.Block(6,21)))

        assertTrue(rectArea3.contains(LocalTile.Chunk(-1,1)))
        assertTrue(rectArea3.contains(LocalTile.Chunk(-2,1)))
        assertTrue(rectArea3.contains(LocalTile.Chunk(-3,1)))
        assertTrue(rectArea3.contains(LocalTile.Chunk(-4,1)))
        assertTrue(rectArea3.contains(LocalTile.Chunk(-1,0)))
        assertTrue(rectArea3.contains(LocalTile.Chunk(-2,0)))
        assertTrue(rectArea3.contains(LocalTile.Chunk(-3,0)))
        assertTrue(rectArea3.contains(LocalTile.Chunk(-4,0)))
        assertTrue(!rectArea3.contains(LocalTile.Chunk(0,0)))
        assertTrue(!rectArea3.contains(LocalTile.Chunk(-1,2)))
        assertTrue(!rectArea3.contains(LocalTile.Chunk(-5,0)))
    }

    @Test
    fun containsArea() {
        assertTrue(rectArea1 in rectArea1)
        assertTrue(rectArea2 in rectArea2)
        assertTrue(rectArea3 in rectArea3)

        assertTrue(rectArea1 !in rectArea2)
        assertTrue(rectArea2 !in rectArea1)

        val t1 = RectArea(
            LocalTile.Chunk(-1,0),
            LocalTile.Chunk(-1, 0)
        )
        assertTrue(rectArea3 !in t1)
        assertTrue(t1 in rectArea3)
    }

    @Test
    fun intersects() {
        assertTrue(rectArea1 intersects rectArea1)
        assertTrue(rectArea2 intersects rectArea2)
        assertTrue(rectArea3 intersects rectArea3)

        assertFalse(rectArea1 intersects rectArea2)
        assertFalse(rectArea2 intersects rectArea1)

        val t1 = RectArea(
            LocalTile.Chunk(-1,0),
            LocalTile.Chunk(-1, 0)
        )
        assertTrue(rectArea3 intersects t1)
        assertTrue(t1 intersects rectArea3)
    }

    @Test
    fun intersect() {
        assertEquals(rectArea1, rectArea1 intersect rectArea1)
        assertEquals(rectArea2, rectArea2 intersect rectArea2)
        assertEquals(rectArea3, rectArea3 intersect rectArea3)

        assertEquals(
            null,
            rectArea1 intersect rectArea2
        )
        assertEquals(
            null,
            rectArea2 intersect rectArea1
        )

        val t1 = RectArea(
            LocalTile.Chunk(-1,0),
            LocalTile.Chunk(-1, 0)
        )
        assertEquals(t1, rectArea3 intersect t1)
        assertEquals(t1, t1 intersect rectArea3)

        val t2 = RectArea(
            LocalTile.Block(-5,-10),
            LocalTile.Block(5, 20)
        )
        assertEquals(
            RectArea(
                LocalTile.Block(-5,10),
                LocalTile.Block(5, 20)
            ),
            rectArea2 intersect t2
        )
        assertEquals(
            RectArea(
                LocalTile.Block(-5,10),
                LocalTile.Block(5, 20)
            ),
            t2 intersect rectArea2
        )
        assertEquals(
            rectArea1,
            rectArea1 intersect t2
        )
        assertEquals(
            rectArea1,
            t2 intersect rectArea1
        )
    }

    @Test
    fun union() {
        assertEquals(rectArea1, rectArea1 union rectArea1)
        assertEquals(rectArea2, rectArea2 union rectArea2)
        assertEquals(rectArea3, rectArea3 union rectArea3)

        assertEquals(
            RectArea(
                LocalTile.Block(-5,0),
                LocalTile.Block(5, 20)
            ),
            rectArea1 union rectArea2
        )
        assertEquals(
            RectArea(
                LocalTile.Block(-5,0),
                LocalTile.Block(5, 20)
            ),
            rectArea2 union rectArea1
        )
        val t1 = RectArea(
            LocalTile.Chunk(-1,0),
            LocalTile.Chunk(-1, 0)
        )
        assertEquals(
            RectArea(
                LocalTile.Chunk(-4,0),
                LocalTile.Chunk(-1, 1)
            ),
            rectArea3 union t1
        )
        assertEquals(
            RectArea(
                LocalTile.Chunk(-4,0),
                LocalTile.Chunk(-1, 1)
            ),
            t1 union rectArea3
        )
        val t2 = RectArea(
            LocalTile.Block(-5,-10),
            LocalTile.Block(5, 20)
        )
        assertEquals(
            RectArea(
                LocalTile.Block(-5,-10),
                LocalTile.Block(5, 20)
            ),
            rectArea2 union t2
        )
        assertEquals(
            RectArea(
                LocalTile.Block(-5,-10),
                LocalTile.Block(5, 20)
            ),
            t2 union rectArea2
        )

    }

    @Test
    fun testEquals() {
        assertEquals(rectArea1, rectArea1)
        assertEquals(rectArea2, rectArea2)
        assertEquals(rectArea3, rectArea3)

        assertNotEquals(rectArea1, rectArea2)
        assertNotEquals(rectArea2, rectArea1)

        val t1 = RectArea(
            LocalTile.Chunk(-1,0),
            LocalTile.Chunk(-1, 0)
        )
        assertNotEquals(rectArea3, t1)
        assertNotEquals(t1, rectArea3)
    }
}