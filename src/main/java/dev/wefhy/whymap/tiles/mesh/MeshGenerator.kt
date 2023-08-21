// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

import dev.wefhy.whymap.tiles.region.MapArea
import dev.wefhy.whymap.utils.*
import net.minecraft.fluid.Fluids

object MeshGenerator {
    const val bottomFaceHeight = -64f
    private const val faceSize: Float = 1f

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
    mat = bpy.data.materials.get("whymap")
    if o.data.materials:
        o.data.materials[0] = mat
    else:
        o.data.materials.append(mat)
    """.trimIndent()

    context(MapArea)
    fun getThreeJsChunkMeshes(area: RectArea<TileZoom.BlockZoom>): List<ThreeJsMesh> {
        val intersection = (area intersect location) ?: return emptyList()
        val chunks = intersection.parent(TileZoom.ChunkZoom).list()
        return chunks.map {
            Mesh().apply{
                addFaces(getChunkMesh(it))
                addFaces(getChunkWaterMesh(it))
            }.toThreeJs().apply {
                posX = it.x * 16f - location.getStart().x
                posY = it.z * 16f - location.getStart().z
            }
        }
    }

    context(MapArea)
    fun getThreeJsMesh(): ThreeJsMesh {
        val centerChunk = location.getCenter().parent(TileZoom.ChunkZoom)

        val chunkOffsets = (0 until 3).map { z ->
            (0 until 8).map { x ->
                x to z
            }
        }.flatten()

        val allFaces = chunkOffsets.map { offset ->
            getChunkMesh(LocalTile.Chunk(centerChunk.x + offset.first, centerChunk.z + offset.second), offset)
        }

        val mesh = Mesh()
        mesh.addFaces(allFaces.flatten())
        return mesh.toThreeJs()
    }

    context(MapArea)
    fun getBlenderPythonMesh(): String {
        val centerChunk = location.getCenter().parent(TileZoom.ChunkZoom)

        val chunkOffsets = (0 until 3).map { z ->
            (0 until 8).map { x ->
                x to z
            }
        }.flatten()

//        val chunkOffsets = listOf(
//            0 to 0,
//            0 to 1,
//            1 to 1,
//            1 to 0,
//        )

        val allFaces = chunkOffsets.map { offset ->
            getChunkMesh(LocalTile.Chunk(centerChunk.x + offset.first, centerChunk.z + offset.second), offset)
        }

        val mesh = Mesh()
        mesh.addFaces(allFaces.flatten())
        return "$head\n${mesh.toPython()}\n$tail"
    }

    context(MapArea)
    private fun getChunkWaterMesh(chunk:LocalTileChunk, offset: Pair<Int, Int> = 0 to 0): List<Face> {
        val chunkHeightMap = getChunkHeightmap(chunk.chunkPos)!!
        val chunkOverlays = getChunkOverlay(chunk.chunkPos)!!
        val chunkDepthMap = getChunkDepthmap(chunk.chunkPos)!!
        val surface = chunkOverlays.mapIndexed {zz, lines ->
            lines.mapIndexed checkingBlock@{ xx, block ->
                if (!block.fluidState.fluid.matchesType(Fluids.WATER)) return@checkingBlock null
                val height = chunkHeightMap[zz][xx]
                val depth = chunkDepthMap[zz][xx]
                height + depth
            }
        }
        val waterTop = surface.mapIndexed { zz, lines ->
            lines.mapIndexed { xx, height ->
                if (height == null) return@mapIndexed null
                getTopFace(xx + 16 * offset.first, zz + 16 * offset.second, height).also {
                    it.uv = TextureAtlas.getBlockUV(chunkOverlays[zz][xx].block) //TODO just use water always or even don't use uv and use texture
                }
            }
        }
        val waterSides = mutableListOf<Face>()

        for (zz in 0 until 15) {
            for (xx in 0 until 15) {
                //TODO finish generating water sides then duplicate the code for regular terrain
            }
        }

        return waterTop.flatten().filterNotNull() + waterSides
    }

    context(MapArea)
    private fun getChunkMesh(chunk: LocalTileChunk, offset: Pair<Int, Int> = 0 to 0): List<Face> {
        val chunkHeightMap = getChunkHeightmap(chunk.chunkPos)!!
        val chunkBlocks = getChunk(chunk.chunkPos)!!
//        val chunkHeightMap = getChunkHeightmap(chunk.chunkPos)!!.zip(getChunkDepthmap(chunk.chunkPos)!!) { a, b ->
//            a.zip(b.toTypedArray()) { c, d ->
//                (c + d).toShort()
//            }
//        }
//        val chunkBlocks = getChunkOverlay(chunk.chunkPos)!!
        return chunkHeightMap.mapIndexed { zz, lines ->
            lines.mapIndexed { xx, height ->
                (getSideFaces(xx + 16 * offset.first, zz + 16 * offset.second, height)).onEach {
                    it.uv = TextureAtlas.getBlockSideUv(chunkBlocks[zz][xx].block, height)
                }  + getTopFace(xx + 16 * offset.first, zz + 16 * offset.second, height).also {
                    it.uv = TextureAtlas.getBlockUV(chunkBlocks[zz][xx].block)
                }
            }
        }.flatten().flatten()
    }


    private fun getTopFace(x: Int, z: Int, height: Short): Face {
        return Face(
            Vertex((x + 0) * faceSize, (z + 0) * faceSize, height * faceSize),
            Vertex((x + 1) * faceSize, (z + 0) * faceSize, height * faceSize),
            Vertex((x + 1) * faceSize, (z + 1) * faceSize, height * faceSize),
            Vertex((x + 0) * faceSize, (z + 1) * faceSize, height * faceSize),
        )
    }

    private fun getTopFace(x: Int, z: Int, height: Int): Face {
        return Face(
            Vertex((x + 0) * faceSize, (z + 0) * faceSize, height * faceSize),
            Vertex((x + 1) * faceSize, (z + 0) * faceSize, height * faceSize),
            Vertex((x + 1) * faceSize, (z + 1) * faceSize, height * faceSize),
            Vertex((x + 0) * faceSize, (z + 1) * faceSize, height * faceSize),
        )
    }

    private fun getBottomFace(x: Int, z: Int): Face {
        return Face(
            Vertex((x + 0) * faceSize, (z + 0) * faceSize, bottomFaceHeight),
            Vertex((x + 1) * faceSize, (z + 0) * faceSize, bottomFaceHeight),
            Vertex((x + 1) * faceSize, (z + 1) * faceSize, bottomFaceHeight),
            Vertex((x + 0) * faceSize, (z + 1) * faceSize, bottomFaceHeight),
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