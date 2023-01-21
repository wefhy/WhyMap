// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

import dev.wefhy.whymap.tiles.region.MapArea
import dev.wefhy.whymap.utils.TextureAtlas
import dev.wefhy.whymap.utils.TileZoom
import dev.wefhy.whymap.utils.chunkPos

object MeshGenerator {
    const val faceSize: Float = 0.1f

    context(MapArea)
    fun generateMesh(): String {
        val chunk = location.getCenter().parent(TileZoom.ChunkZoom)
        val chunkHeightMap = getChunkHeightmap(chunk.chunkPos)!!
        val chunkBlocks = getChunk(chunk.chunkPos)!!
        val faces = chunkHeightMap.mapIndexed { zz, lines ->
            lines.mapIndexed { xx, height ->
                (getSideFaces(xx, zz, height) + getTopFace(xx, zz, height)).onEach {
                    it.uv = TextureAtlas.getBlockUV(chunkBlocks[zz][xx].block)
                }
            }
        }

        val mesh = Mesh()
        mesh.addFaces(faces.flatten().flatten())
        return mesh.toPython()
    }

//    context(VertexIndexer)
    fun getTopFace(x: Int, z: Int, height: Short): Face {
        return Face(
            Vertex((x + 0) * faceSize, (z + 0) * faceSize, height * faceSize),
            Vertex((x + 1) * faceSize, (z + 0) * faceSize, height * faceSize),
            Vertex((x + 1) * faceSize, (z + 1) * faceSize, height * faceSize),
            Vertex((x + 0) * faceSize, (z + 1) * faceSize, height * faceSize),
        )
    }

    fun getBottomFace(x: Int, z: Int): Face {
        return Face(
            Vertex((x + 0) * faceSize, (z + 0) * faceSize, 0f),
            Vertex((x + 1) * faceSize, (z + 0) * faceSize, 0f),
            Vertex((x + 1) * faceSize, (z + 1) * faceSize, 0f),
            Vertex((x + 0) * faceSize, (z + 1) * faceSize, 0f),
        )
    }

    fun getSideFaces(x: Int, z: Int, height: Short): List<Face> {
        val top = getTopFace(x, z, height)
        val bottom = getBottomFace(x, z)
        val merged = top.vertices zip bottom.vertices

        return merged.indices.map { i ->
            val j = (i + 1).mod(merged.size)
            Face(
                merged[i].first,
                merged[i].second,
                merged[j].second,
                merged[j].first
            )
        }
    }
}