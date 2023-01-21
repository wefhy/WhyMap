// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

import dev.wefhy.whymap.tiles.region.MapArea
import dev.wefhy.whymap.utils.*

object MeshGenerator {
    private const val faceSize: Float = 0.1f

    private val head = """
    import bpy, math, mathutils   
    """.trimIndent()

    private val tail = """
    m = bpy.data.meshes.new('chunkmesh')
    o = bpy.data.objects.new('chunkmesh', m)
    bpy.context.collection.objects.link(o)
    o.location = bpy.context.scene.cursor.location
    m.from_pydata(vertices, [], faces)
    m.update(calc_edges=True)
    print('mesh loaded')
    
    
    uv_data = o.data.uv_layers.new(name='NewUV').data
    polys = o.data.polygons
    #uv_data = o.data.uv_layers.active.data
    
    for poly in polys:
        i = poly.index
        uv_data[poly.loop_indices[0]].uv = uvs[i][0]
        uv_data[poly.loop_indices[1]].uv = uvs[i][1]
        uv_data[poly.loop_indices[2]].uv = uvs[i][2]
        uv_data[poly.loop_indices[3]].uv = uvs[i][3]
    """.trimIndent()

    context(MapArea)
    fun getBlenderPythonMesh(): String {
        val centerChunk = location.getCenter().parent(TileZoom.ChunkZoom)
        val chunks = listOf(
            centerChunk,
            LocalTile.Chunk(centerChunk.x, centerChunk.z + 1),
            LocalTile.Chunk(centerChunk.x + 1, centerChunk.z + 1),
            LocalTile.Chunk(centerChunk.x + 1, centerChunk.z),
        )
        val allFaces = chunks.map { chunk ->
            getChunkMesh(chunk)
        }

        val mesh = Mesh()
        mesh.addFaces(allFaces.flatten().flatten().flatten())
        return "$head\n${mesh.toPython()}\n$tail"
    }

    context(MapArea)
    private fun getChunkMesh(chunk: LocalTileChunk): List<List<List<Face>>> {
        val chunkHeightMap = getChunkHeightmap(chunk.chunkPos)!!
        val chunkBlocks = getChunk(chunk.chunkPos)!!
        return chunkHeightMap.mapIndexed { zz, lines ->
            lines.mapIndexed { xx, height ->
                (getSideFaces(xx, zz, height) + getTopFace(xx, zz, height)).onEach {
                    it.uv = TextureAtlas.getBlockUV(chunkBlocks[zz][xx].block)
                }
            }
        }
    }


    private fun getTopFace(x: Int, z: Int, height: Short): Face {
        return Face(
            Vertex((x + 0) * faceSize, (z + 0) * faceSize, height * faceSize),
            Vertex((x + 1) * faceSize, (z + 0) * faceSize, height * faceSize),
            Vertex((x + 1) * faceSize, (z + 1) * faceSize, height * faceSize),
            Vertex((x + 0) * faceSize, (z + 1) * faceSize, height * faceSize),
        )
    }

    private fun getBottomFace(x: Int, z: Int): Face {
        return Face(
            Vertex((x + 0) * faceSize, (z + 0) * faceSize, 0f),
            Vertex((x + 1) * faceSize, (z + 0) * faceSize, 0f),
            Vertex((x + 1) * faceSize, (z + 1) * faceSize, 0f),
            Vertex((x + 0) * faceSize, (z + 1) * faceSize, 0f),
        )
    }

    private fun getSideFaces(x: Int, z: Int, height: Short): List<Face> {
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