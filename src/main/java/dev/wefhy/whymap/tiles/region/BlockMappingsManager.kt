package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.minecraftBlocks
import dev.wefhy.whymap.config.WhyMapConfig.customMappingsDir
import dev.wefhy.whymap.utils.mkDirsIfNecessary
import dev.wefhy.whymap.utils.toHex
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.experimental.inv
import kotlin.random.Random

@Suppress("UNREACHABLE_CODE")
object BlockMappingsManager {
    private val md = MessageDigest.getInstance("MD5")
    private val fileWithCurrentVersion = customMappingsDir.resolve("current")

    private val internalMappings: List<BlockMapping.InternalMapping> by lazy {
        val classloader = javaClass.classLoader
        val resource = classloader.getResource("blockmappings.txt") ?: return@lazy emptyList()
        resource.openStream().use {
            it.readAllBytes()
        }.toString(Charsets.UTF_8).lines().map {
            it.split("=").let { (version, hash) ->
                BlockMapping.InternalMapping(version.toShort(), hash)
            }
        }.also { BlockMapping.WhyMapBeta = it.find { it.version == 0.toShort() }!! }
    }

    private val externalMappings: List<BlockMapping.ExternalMapping> by lazy {
        customMappingsDir.listFiles()?.map { file ->
            BlockMapping.ExternalMapping(file)
        } ?: emptyList()
    }

    //TODO this needs to be tested whether internal mappings have priority in the map!
    private val allMappings: MutableMap<String, BlockMapping> by lazy {
        (externalMappings + internalMappings).associateBy { it.hash }.toMutableMap()
    }

    val currentMapping: BlockMapping by lazy {
        val currentHash = mappingsJoined.calculateHash().toHex()
        (allMappings[currentHash] ?: createNewCustomMappings(currentHash, mappings)).also {
            it.isCurrent = true
            if (it !is BlockMapping.InternalMapping) {
                fileWithCurrentVersion.writeText(it.hash, Charsets.UTF_8)
            }
        }
    }

    private fun createNewCustomMappings(hash: String, mappings: List<String>): BlockMapping {
        val file = customMappingsDir.resolve("$hash.blockmap")
        file.mkDirsIfNecessary().writeText(mappingsJoined, Charsets.UTF_8)
        return BlockMapping.LoadedMapping(hash, mappings).also {
            allMappings[hash] = it
        }
    }

    init {
        fileWithCurrentVersion.delete()
    }

    private val mappings by lazy {
        minecraftBlocks.toList()
    }

    val mappingsJoined by lazy {
        mappings.joinToString("\n")
    }

    //    @ExpensiveCall
//    private fun getInternalMappings(): Map<String, Short>? {
//        val classloader = javaClass.classLoader
//        val resource = classloader.getResource("blockmappings.txt") ?: return null
//        val mappingFiles = resource.openStream().use {
//            it.readAllBytes().toString(Charsets.UTF_8).lines()
//        }.associate { it.split(" ").let { (version, hash) -> hash to version.toShort() } }
//        return mappingFiles
////        return mappingFiles.map { (fileName, hash) ->
////            val file = classloader.getResource("blockmappings/$fileName")!!
////            val data = file.openStream().use {
////                it.readAllBytes()
////            }
////            val actualHash = md.digest(data)
////            if (!actualHash.contentEquals(hash)) {
////                println("Hash mismatch for $fileName")
////                println("Expected: $hash")
////                println("Actual: $actualHash")
////                return null
////            }
////            data.toString(Charsets.UTF_8)
////        }
//    }

//    private fun findInternalMapping(hash: String): Internal? {
//        return getInternalMappings()?.get(hash)?.let { version ->
//            Internal(
//                version,
//                blockMappingsForVersion(version),
//                hash,
//            )
//        }
//    }

//    private fun findExternalMapping(hash: String): External? {
//        return customMappingsDir.listFiles()?.find {
//            it.nameWithoutExtension == hash
//        }?.readLines(Charsets.UTF_8)?.let { mappings ->
//            External(mappings, hash)
//        }
//    }

    fun List<String>.calculateHash(): ByteArray {
        return joinToString("\n").calculateHash()
    }

    private fun String.calculateHash(): ByteArray {
        return toByteArray(Charsets.UTF_8).calculateHash()
    }

    private fun ByteArray.calculateHash(): ByteArray {
        return md.digest(this)
    }

//    fun blockMappingsForVersion(version: Short): List<String> {
//        val classloader = javaClass.classLoader
//        val resource = classloader.getResource("blockmappings/${version.fileName}")!!
//        val mappings = resource.openStream().use {
//            it.readAllBytes().toString(Charsets.UTF_8)
//        }.split("\n")
//        return mappings
//    }

    fun getCurrentRemapLookup(mapping: BlockMapping): List<Short> {
        return getRemapLookup(
            mapping,
            currentMapping
        )
    }

    fun getRemapLookup(mapping1: BlockMapping, mapping2: BlockMapping): List<Short> {
        return getRemapLookup(
            mapping1.mapping,
            mapping2.mapping
        )
    }

    private fun getRemapLookup(mappings1: List<String>, mappings2: List<String>): List<Short> {
        //TODO memoize mappings
        return mappings1.map { mappings2.indexOf(it).toShort() }.map { if (it >= 0) it else mappings2.indexOf("block.minecraft.air").toShort() }
    }

    fun remap(data: List<Int>, remapLookUp: List<Int>): List<Int> {
        return data.map { remapLookUp[it] }
    }


    private fun findInternal(version: Short): BlockMapping? = internalMappings.find { it.version == version }

    fun recognizeVersion(metadata: FileVersionManager.WhyMapMetadata): BlockMapping? {
        val buffer = ByteBuffer.wrap(metadata.data)
        val version = buffer.short
        val c1 = Random(version.toInt()).nextInt()
        val c2 = version.inv()
        if (c1 == buffer.int && c2 == buffer.short) {
            return findInternal(version) ?: allMappings[metadata.data.toHex()]
        }
        return allMappings[metadata.data.toHex()]
    }

    enum class UnsupportedBLockMappingsBehavior {
        DISABLE_WRITE,
        DONT_CONVERT_OLD,
        EMBED_MAPPINGS,
        EXTERNAL_MAPPINGS,
        SAVE_ONLY_VANILLA,
    }

//    /**
//     * @param mappings The mappings to use
//     * When using custom mappings, file should be postfixed with _custom
//     */
//    class CustomMappingsManager(
//        val mappings: List<String>,
//    ) {
//        fun getRemapLookup(fromVersion: WhyMapFileVersion): List<Short> {
//            return getRemapLookup(blockMappingsForVersion(fromVersion), mappings)
//        }
//
//        fun getExportLookup(toVersion: WhyMapFileVersion): List<Short> {
//            return getRemapLookup(mappings, blockMappingsForVersion(toVersion))
//        }
//    }
}