// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

class BlenderFace(vararg val vertices: Int): PythonObject {
    override fun toPython(): String {
        return "[${vertices.joinToString()}]"
    }

    fun toThreeJs(): List<Int> {
//        return vertices.toList()
//        return listOf(vertices[0], vertices[1], vertices[2], vertices[2], vertices[3], vertices[0])
        return listOf(vertices[0], vertices[2], vertices[1], vertices[2], vertices[0], vertices[3])
    }
}