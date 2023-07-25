// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

class Mesh : PythonObject {

    val vertexStorage = arrayListOf<Vertex>()
    val faceStorage = arrayListOf<BlenderFace>()
    val uvStorage = arrayListOf<Uv>()

    fun addFaces(faces: Collection<Face>) {
        for (face in faces) {
            val vertexIndices = face.vertices.map {
                vertexStorage += it
                vertexStorage.lastIndex
            }.toIntArray()
            faceStorage += BlenderFace(*vertexIndices)
            uvStorage += face.uv ?: Uv(*face.vertices.map { UvCoordinate(-1.0, -1.0) }.toTypedArray())
        }
    }

    override fun toPython(): String {
        return """
            vertices = [${vertexStorage.joinToString { it.toPython() }}]
            faces = [${faceStorage.joinToString { it.toPython() }}]
            uvs = [${uvStorage.joinToString { it.toPython() }}]
        """.trimIndent()
    }

    fun toThreeJs(): ThreeJsMesh {
        return ThreeJsMesh(
            vertices = vertexStorage.flatMap { it.toThreeJs() },
            faces = faceStorage.flatMap { it.toThreeJs() },
            uvs = uvStorage.flatMap { it.toThreeJs() }
        )
    }
}