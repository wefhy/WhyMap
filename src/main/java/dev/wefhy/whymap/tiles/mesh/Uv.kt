// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

class Uv(vararg val coordinates: UvCoordinate): PythonObject {
    override fun toPython(): String {
        return "[${coordinates.joinToString { it.toPython() }}]"
    }
}

class UvCoordinate(val x: Double, val y: Double): PythonObject {
    override fun toPython(): String {
        return "mathutils.Vector(($x, $y))"
    }
}