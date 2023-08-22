// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

class Uv(vararg val coordinates: UvCoordinate): PythonObject {
    override fun toPython(): String {
        return "[${coordinates.joinToString { it.toPython() }}]"
    }

    fun toThreeJs(): List<Float> {
        return coordinates.flatMap { it.toThreeJs() }
    }
}

class UvCoordinate(val x: Double, val y: Double): PythonObject {
    override fun toPython(): String {
        return "mathutils.Vector(($x, $y))"
    }

    fun toThreeJs(): List<Float> {
        return listOf(x.toFloat(), y.toFloat())
    }
}