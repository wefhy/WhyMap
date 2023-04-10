package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.minecraftBlocks
import dev.wefhy.whymap.config.WhyMapConfig.customMappingsDir
import dev.wefhy.whymap.config.WhyMapConfig.latestFileVersion
import dev.wefhy.whymap.config.WhyMapConfig.mappingsExportDir
import dev.wefhy.whymap.tiles.region.FileVersionManager.WhyMapFileVersion
import dev.wefhy.whymap.utils.mkDirsIfNecessary
import dev.wefhy.whymap.utils.toHex
import java.security.MessageDigest

@Suppress("UNREACHABLE_CODE")
object BlockMappingsManager {
    private val md = MessageDigest.getInstance("MD5")
    private val fileWithCurrentVersion = mappingsExportDir.resolve("current")


    val currentVersion: WhyMapFileVersion by lazy {
        val currentHash = getMappings().calculateHash()
        val currentHashHex = currentHash.toHex()
        val mappings = findInternalMapping(currentHashHex)
            ?: findExternalMapping(currentHashHex)
            ?: return@lazy createNewCustomMappings()



        if (mappings == null) {
            println("No mappings found, generating new ones")
            exportBlockMappings()
            latestFileVersion
        } else {
            val hash = calculateHash(mappings.values.joinToString("\n"))
            val version = mappings.keys.find { it.startsWith(hash.toHex()) }
            if (version == null) {
                println("No mappings found for hash ${hash.toHex()}, generating new ones")
                exportBlockMappings()
                latestFileVersion
            } else {
                WhyMapFileVersion.Custom(version.toShort())
            }
        }
    }

    private fun createNewCustomMappings(): WhyMapFileVersion {
        fileWithCurrentVersion.writeText(currentVersion.fileName, Charsets.UTF_8)
        return WhyMapFileVersion.Companion.UserDefined
    }

    init {
        fileWithCurrentVersion.delete()
    }

    private fun getMappings() = minecraftBlocks.joinToString("\n") //TODO to lazy!


    //    @ExpensiveCall
    private fun getInternalMappings(): Map<String, String>? {
        val classloader = javaClass.classLoader
        val resource = classloader.getResource("blockmappings.txt") ?: return null
        val mappingFiles = resource.openStream().use {
            it.readAllBytes().toString(Charsets.UTF_8).lines()
        }.associate { it.split(" ").let { (version, hash) -> hash to "${version}.blockmap" } }
        return mappingFiles
//        return mappingFiles.map { (fileName, hash) ->
//            val file = classloader.getResource("blockmappings/$fileName")!!
//            val data = file.openStream().use {
//                it.readAllBytes()
//            }
//            val actualHash = md.digest(data)
//            if (!actualHash.contentEquals(hash)) {
//                println("Hash mismatch for $fileName")
//                println("Expected: $hash")
//                println("Actual: $actualHash")
//                return null
//            }
//            data.toString(Charsets.UTF_8)
//        }
    }

    private fun findInternalMapping(hash: String): List<String>? {
        return getInternalMappings()?.get(hash)?.lines()
    }

    private fun findExternalMapping(hash: String): List<String>? {
        return customMappingsDir.listFiles()?.find {
            it.nameWithoutExtension == hash
        }?.readLines(Charsets.UTF_8)
    }

    private fun String.calculateHash(): ByteArray {
        return toByteArray(Charsets.UTF_8).calculateHash()
    }

    private fun ByteArray.calculateHash(): ByteArray {
        return md.digest(this)
    }

    fun exportBlockMappings(): String {
        val data = getMappings()
        val file = mappingsExportDir.resolve(latestFileVersion.next.fileName)
        file.mkDirsIfNecessary()
        file.writeText(data, Charsets.UTF_8)
        return data
    }

    fun blockMappingsForVersion(version: WhyMapFileVersion): List<String> {
        val classloader = javaClass.classLoader
        val resource = classloader.getResource("blockmappings/${version.fileName}")!!
        val mappings = resource.openStream().use {
            it.readAllBytes().toString(Charsets.UTF_8)
        }.split("\n")
        return mappings
    }

    fun getRemapLookup(version1: WhyMapFileVersion, version2: WhyMapFileVersion): List<Short> {
        return getRemapLookup(
            blockMappingsForVersion(version1),
            blockMappingsForVersion(version2)
        )
    }

    private fun getRemapLookup(mappings1: List<String>, mappings2: List<String>): List<Short> {
        //TODO memoize mappings
        return mappings1.map { mappings2.indexOf(it).toShort() }.map { if (it >= 0) it else mappings2.indexOf("block.minecraft.air").toShort() }
    }

    fun remap(data: List<Int>, remapLookUp: List<Int>): List<Int> {
        return data.map { remapLookUp[it] }
    }

    fun remap(data: List<Int>, mappings1: List<String>, mappings2: List<String>) {

    }

    sealed class MappingsType(val version: WhyMapFileVersion, val hash: String, val data: List<String>) {

        abstract val fileName: String

        class Internal(version: WhyMapFileVersion, hash: String, data: List<String>) : MappingsType(version, hash, data) {
            override val fileName: String
                get() = "${hash}.blockmap"
        }

        class External(hash: String, data: List<String>) : MappingsType(WhyMapFileVersion.Companion.UserDefined, hash, data) {
            override val fileName: String
                get() = version.fileName
        }
    }

    enum class UnsupportedBLockMappingsBehavior {
        DISABLE_WRITE,
        DONT_CONVERT_OLD,
        EMBED_MAPPINGS,
        EXTERNAL_MAPPINGS,
        SAVE_ONLY_VANILLA,
    }

    /**
     * @param mappings The mappings to use
     * When using custom mappings, file should be postfixed with _custom
     */
    class CustomMappingsManager(
        val mappings: List<String>,
    ) {
        fun getRemapLookup(fromVersion: WhyMapFileVersion): List<Short> {
            return getRemapLookup(blockMappingsForVersion(fromVersion), mappings)
        }

        fun getExportLookup(toVersion: WhyMapFileVersion): List<Short> {
            return getRemapLookup(mappings, blockMappingsForVersion(toVersion))
        }
    }
}