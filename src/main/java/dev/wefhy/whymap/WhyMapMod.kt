// Copyright (c) 2022-2023 wefhy

package dev.wefhy.whymap

import dev.wefhy.whymap.config.WhyMapConfig.DEV_VERSION
import dev.wefhy.whymap.config.WhyMapConfig.mapLink
import dev.wefhy.whymap.utils.LocalTile
import dev.wefhy.whymap.events.RegionUpdateQueue
import dev.wefhy.whymap.events.WorldEventQueue
import dev.wefhy.whymap.utils.plus
import dev.wefhy.whymap.utils.serialize
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import net.minecraft.world.dimension.DimensionType
import org.slf4j.LoggerFactory
import java.awt.Dimension


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
                    /**
                    TODO Actually these coroutines NEED to be cancelled?
                    TODO Or I need to properly support browsing background dimensions? But no way to keep chunk loaded until it's saved

[19:52:53] [DefaultDispatcher-worker-57/ERROR] (FabricLoader) Uncaught exception in thread "DefaultDispatcher-worker-57"
 java.lang.NullPointerException: getBlockState(mutablePosition) must not be null
	at dev.wefhy.whymap.tiles.region.MapArea.getOceanFloorHeightMapHotFix(MapArea.kt:256) ~[main/:?]
	at dev.wefhy.whymap.tiles.region.MapArea.updateChunk(MapArea.kt:295) ~[main/:?]
	at dev.wefhy.whymap.WhyMapMod$onInitialize$1$1.invokeSuspend(WhyMapMod.kt:39) ~[main/:?]
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33) ~[kotlin-stdlib-1.8.0.jar:?]
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:570) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:750) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:677) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:664) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:?]
	Suppressed: kotlinx.coroutines.DiagnosticCoroutineContextException
                     */
                }
            }

        }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register{_, _, newWorld -> dimensionChangeListener(newWorld.dimension)}
        ClientPlayConnectionEvents.DISCONNECT.register(worldLeaveListener)
        ClientPlayConnectionEvents.JOIN.register(worldJoinListener)
        GlobalScope.launch {
            WhyServer.host()
        }
    }
    companion object {

        private var oldDimensionName: String = ""

        @JvmStatic
        fun dimensionChangeListener(newDimension: DimensionType) {
            val newDimensionName = newDimension.serialize()
            if (oldDimensionName == newDimensionName) return Unit.also { println("NOT CHANGED WORLD") }
            println("CHANGED WORLD! old: $oldDimensionName, new: $newDimensionName")
            oldDimensionName = newDimensionName
            activeWorld?.close()
            RegionUpdateQueue.reset()
            LOGGER.info("Saved all data")
            activeWorld = CurrentWorld(MinecraftClient.getInstance())
            WorldEventQueue.addUpdate(WorldEventQueue.WorldEvent.DimensionChange)
        }

        val worldLeaveListener = { handler: ClientPlayNetworkHandler, client: MinecraftClient ->
            LOGGER.info("SAVING ALL DATA!!!")
            activeWorld!!.close()
            RegionUpdateQueue.reset()
            LOGGER.info("Saved all data")
            activeWorld = null
            WorldEventQueue.addUpdate(WorldEventQueue.WorldEvent.LeaveWorld)
        }

        val worldJoinListener = { handler: ClientPlayNetworkHandler, sender: PacketSender, client: MinecraftClient ->
            println("JOINED WORLD! ${client.world?.dimension?.coordinateScale}")
            activeWorld = CurrentWorld(client)

            val message = Text.literal("WhyMap: see your map at ") + Text.literal(mapLink).apply {
                style = style.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, mapLink)).withUnderline(true)
            }
            client.player!!.sendMessage(message, false)
            WorldEventQueue.addUpdate(WorldEventQueue.WorldEvent.EnterWorld)
        }

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