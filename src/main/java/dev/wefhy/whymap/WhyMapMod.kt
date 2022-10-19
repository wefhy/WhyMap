// Copyright (c) 2022 wefhy

package dev.wefhy.whymap

import dev.wefhy.whymap.config.WhyMapConfig.DEV_VERSION
import dev.wefhy.whymap.config.WhyMapConfig.mapLink
import dev.wefhy.whymap.tiles.region.BlockMappingsManager
import dev.wefhy.whymap.utils.LocalTile
import dev.wefhy.whymap.utils.plus
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.ClickEvent
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import org.slf4j.LoggerFactory


class WhyMapMod : ModInitializer {
    override fun onInitialize() {
        ClientChunkEvents.CHUNK_LOAD.register { cw, wc ->
            if (cw == null || wc == null || wc.isEmpty) return@register
            if (DEV_VERSION) LOGGER.info("Loaded(${wc.pos.x}:${wc.pos.z})")
//            val filename = ChunkSaver.chunkToBmp(wc)
//            MinecraftClient.getInstance()!!.player!!.sendChatMessage("Saved: $filename")
            GlobalScope.launch(Dispatchers.Default) {
                activeWorld!!.mapRegionManager.getRegionForWriteAndLoad(
                    LocalTile.Region(
                        wc.pos.regionX,
                        wc.pos.regionZ
                    )
                ) {
                    updateChunk(wc)
                }
            }

        }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, oldWorld, newWorld ->
            println("CHANGED WORLD! old: ${oldWorld.dimension.coordinateScale}, new: ${newWorld.dimension.coordinateScale}")
            activeWorld!!.close()
            activeWorld = CurrentWorld(MinecraftClient.getInstance())
        }

        ClientPlayConnectionEvents.DISCONNECT.register { handler, client ->
            LOGGER.info("SAVING ALL DATA!!!")
            activeWorld!!.close()
            LOGGER.info("Saved all data")
            activeWorld = null
        }

        ClientPlayConnectionEvents.JOIN.register { handler, sender, client ->
            println("JOINED WORLD! ${client.world?.dimension?.coordinateScale}")
            activeWorld = CurrentWorld(client)

            val message = Text.literal("WhyMap: see your map at ") + Text.literal(mapLink).apply {
                style = style.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, mapLink)).withUnderline(true)
            }
            client.player!!.sendMessage(message, false)
        }

        GlobalScope.launch {
            WhyServer.host()
        }
    }


    companion object {
        var activeWorld: CurrentWorld? = null
        const val MOD_ID = "whymap"

        //        val imageIOFormat = "png"
        const val imageIOFormat = "jpg"

        //        val contentType = ContentType.Image.PNG
        val contentType = ContentType.Image.JPEG


        // This logger is used to write text to the console and the log file.
        // It is considered best practice to use your mod id as the logger's name.
        // That way, it's clear which mod wrote info, warnings, and errors.
        @JvmField
        val LOGGER = LoggerFactory.getLogger(MOD_ID)
    }
}