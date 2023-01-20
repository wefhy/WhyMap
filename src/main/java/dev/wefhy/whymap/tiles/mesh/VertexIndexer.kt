// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

class VertexIndexer {
    private var numberOfVertices = 0
    val vertices = arrayListOf<Vertex>()

    context(Vertex)
    fun newVertex(): Int {
        vertices += this@Vertex
        return numberOfVertices++
    }
}