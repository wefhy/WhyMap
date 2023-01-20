// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

class Mesh: PythonObject {

    val vertexStorage = arrayListOf<Vertex>()
    val faceStorage = arrayListOf<BlenderFace>()

    fun addFaces(faces: Collection<Face>) {
        for (face in faces) {
            val vertexIndices = face.vertices.map {
                vertexStorage += it
                vertexStorage.lastIndex
            }.toIntArray()
            faceStorage += BlenderFace(*vertexIndices)
        }
    }

    override fun toPython(): String {
        return """
            vertices = [${vertexStorage.joinToString{it.toPython()}}]
            faces = [${faceStorage.joinToString{it.toPython()}}]
        """.trimIndent()
    }
}