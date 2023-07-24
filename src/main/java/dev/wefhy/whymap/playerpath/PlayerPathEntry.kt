// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.playerpath

import java.nio.ByteBuffer

data class PlayerPathEntry(val time: Long, val x: Double, val y: Double, val z: Double) {
//    fun serialize(): ByteArray {
//        val byteBuffer = ByteBuffer.allocate(8 + 8 + 8 + 8)
//        byteBuffer.putLong(time)
//        byteBuffer.putDouble(x)
//        byteBuffer.putDouble(y)
//        byteBuffer.putDouble(z)
//        return byteBuffer.array()
//    }

    fun serializeToBuffer(byteBuffer: ByteBuffer) {
        byteBuffer.putLong(time)
        byteBuffer.putDouble(x)
        byteBuffer.putDouble(y)
        byteBuffer.putDouble(z)
    }

    companion object {
        const val size = 8 + 8 + 8 + 8

        fun loadFromBuffer(byteBuffer: ByteBuffer): PlayerPathEntry {
            return PlayerPathEntry(
                byteBuffer.getLong(),
                byteBuffer.getDouble(),
                byteBuffer.getDouble(),
                byteBuffer.getDouble(),
            )
        }
    }
}