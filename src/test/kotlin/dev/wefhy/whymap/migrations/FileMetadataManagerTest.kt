package dev.wefhy.whymap.migrations

import dev.wefhy.whymap.utils.toHex
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.security.MessageDigest

class FileMetadataManagerTest {

    val md5 = MessageDigest.getInstance("MD5")

    @Test
    fun encodeDecodeMetadata() {
        val sample1 = "Hello worlld!"
        val hash1 = md5.digest(sample1.toByteArray(Charsets.UTF_8))
        val hashHex1 = hash1.toHex()
        val sample2 = "Bye world!"
        val hash2 = md5.digest(sample2.toByteArray(Charsets.UTF_8))
        val hashHex2 = hash2.toHex()

        val blockMapping = BlockMapping.InternalMapping(1, hashHex1)
        val biomeMapping = BiomeMapping.InternalMapping(1, hashHex2)

        val metadata = FileMetadataManager.encodeMetadata(blockMapping, biomeMapping)

        val decoded = FileMetadataManager.decodeMetadata(metadata)

        Assertions.assertEquals(decoded?.fileVersion, 1)
        Assertions.assertEquals(decoded?.blockMapHash, hashHex1)
        Assertions.assertEquals(decoded?.biomeMapHash, hashHex2)

    }
}