// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

class BlenderFace(vararg val vertices: Int): PythonObject {
    override fun toPython(): String {
        return "[${vertices.joinToString()}]"
    }
}