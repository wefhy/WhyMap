// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

//context(VertexIndexer)
class Vertex(val x: Float, val y: Float, val z: Float): PythonObject {
//    val index: Int = newVertex()

    override fun toPython(): String {
        return "[$x, $y, $z]"
    }

    fun toThreeJs(): List<Float> {
        return listOf(x, z, y)
    }
}