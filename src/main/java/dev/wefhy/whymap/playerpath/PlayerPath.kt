// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.playerpath

import dev.wefhy.whymap.config.WhyMapConfig.playerPathExtension
import dev.wefhy.whymap.config.WhyMapConfig.playerPathPath
import java.io.File
import java.nio.channels.FileChannel

class PlayerPath private constructor(
    val playerName: String,
//    val time: Long,
    val entries: MutableList<PlayerPathEntry>,
) {
    val time
        get() = entries.firstOrNull()?.time ?: 0L

    constructor(playerName: String) : this(
        playerName,
//        System.currentTimeMillis(),
        mutableListOf<PlayerPathEntry>()
    )

    fun addEntry(entry: PlayerPathEntry) {
        entries.add(entry)
    }

    fun saveToFile() {
        val file = playerPathPath.resolve("$playerName-$time.$playerPathExtension")
        file.parentFile.mkdirs()
        file.outputStream().use {
            val byteBuffer = it.channel.map(
                FileChannel.MapMode.READ_WRITE,
                0,
                (entries.size * PlayerPathEntry.size).toLong()
            )
            byteBuffer.putInt(pathVersion)
            byteBuffer.putLong(time)
            byteBuffer.putLong(entries.last().time)
            val encodedName = playerName.encodeToByteArray()
            byteBuffer.putInt(encodedName.size)
            byteBuffer.put(encodedName)
            for (entry in entries) {
                entry.serializeToBuffer(byteBuffer)
            }
        }
    }

    companion object {
        const val pathVersion = 1
        fun loadFromFile(file: File): PlayerPath {
            file.inputStream().use {
                val byteBuffer = it.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    0,
                    file.length()
                )
                val version = byteBuffer.int
                if (version != pathVersion) {
                    throw Exception("Unsupported path version: $version")
                }
                val time = byteBuffer.long
                val lastTime = byteBuffer.long
                val nameSize = byteBuffer.int
                val name = ByteArray(nameSize)
                byteBuffer.get(name)
                val entries = mutableListOf<PlayerPathEntry>()
                while (byteBuffer.hasRemaining()) {
                    entries.add(PlayerPathEntry.loadFromBuffer(byteBuffer))
                }
                return PlayerPath(
                    name.decodeToString(),
                    entries
                )
            }
        }
    }
}