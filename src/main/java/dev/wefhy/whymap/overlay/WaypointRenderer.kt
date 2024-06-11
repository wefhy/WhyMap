// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.overlay

import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.debug.DebugRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d

object WaypointRenderer {
    fun renderDebugRenderers(matrixStack: MatrixStack?, vertexConsumers: VertexConsumerProvider?, camX: Double, camY: Double, camZ: Double) {
        val playerPos = Vec3d(camX, camY, camZ)
        for (waypoint in activeWorld?.waypoints?.waypoints ?: emptyList()) {
            var loc = waypoint.location.toVec3d()
            val distance = loc.distanceTo(playerPos)
            if (distance > 100) {
                loc = playerPos.add(loc.subtract(playerPos).normalize().multiply(100.0))
            }
            val size = 0.002f * distance.coerceAtMost(100.0).toFloat()
            //todo parse color
            DebugRenderer.drawString(matrixStack, vertexConsumers, "${waypoint.name}(${distance.toInt()}m)", loc.x, loc.y, loc.z, -1, size, true, 0.0f, true)
        }
    }
}