// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

import dev.wefhy.whymap.tiles.region.MapArea
import dev.wefhy.whymap.utils.TileZoom
import dev.wefhy.whymap.utils.chunkPos

object MeshGenerator {
    const val faceSize: Float = 0.1f

    context(MapArea)
    fun generateMesh(): String {
        val chunk = location.getCenter().parent(TileZoom.ChunkZoom)
        val chunkHeightMap = getChunkHeightmap(chunk.chunkPos)!!
        val faces = chunkHeightMap.mapIndexed { zz, lines ->
            lines.mapIndexed{xx, height ->
                getTopFace(xx, zz, height)
            }
        }

        val mesh = Mesh()
        mesh.addFaces(faces.flatten())
        return mesh.toPython()






//        val vertices = Array(16) {zz ->
//            Array(16) {xx->
//                getTopFace(xx, zz, )
//            }
//        }
    }

//    context(VertexIndexer)
    fun getTopFace(x: Int, z: Int, height: Short): Face {
        return Face(
            Vertex(x * (faceSize + 0), z * (faceSize + 0), height * faceSize),
            Vertex(x * (faceSize + 1), z * (faceSize + 0), height * faceSize),
            Vertex(x * (faceSize + 1), z * (faceSize + 1), height * faceSize),
            Vertex(x * (faceSize + 0), z * (faceSize + 1), height * faceSize),
        )
    }
}