// Copyright (c) 2023 wefhy

package dev.wefhy.whymap

import dev.wefhy.whymap.events.FeatureUpdateQueue
import dev.wefhy.whymap.gui.WhyConfirmScreen
import dev.wefhy.whymap.gui.WhyInputScreen
import dev.wefhy.whymap.waypoints.CoordXYZ
import dev.wefhy.whymap.waypoints.LocalWaypoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class WhyMapClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            if (keyBinding.wasPressed()) {
                GlobalScope.launch {
                    with(mc) {
                        val playerPos = player?.pos ?: return@with
                        val coords = CoordXYZ(playerPos.x.toInt(), playerPos.y.toInt(), playerPos.z.toInt())
                        WhyInputScreen("Adding new waypoint", "Do you want to add a new waypoint at $coords?") { answer, input ->
                            if (!answer) return@WhyInputScreen
                            val waypoint = LocalWaypoint(input, coords)
                            WhyMapMod.activeWorld?.waypoints?.add(waypoint) ?: println("Failed to add waypoint!")
                            FeatureUpdateQueue.addUpdate(waypoint.asOnlineWaypoint())
                        }.show()
                    }
                }
            }
        }
    }

    companion object {
        val keyBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.whymap.newwaypoint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.whymap"
            )
        )
    }
}