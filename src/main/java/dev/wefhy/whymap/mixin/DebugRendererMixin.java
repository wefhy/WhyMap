// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.mixin;

import dev.wefhy.whymap.overlay.WaypointRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {
    @Inject(method = "render", at = @At("RETURN"))
    private void renderDebugRenderers(MatrixStack matrixStack, VertexConsumerProvider.Immediate vtx,
                                      double cameraX, double cameraY, double cameraZ, CallbackInfo ci)
    {
        WaypointRenderer.INSTANCE.renderDebugRenderers(matrixStack, vtx, cameraX, cameraY, cameraZ);
    }
}
