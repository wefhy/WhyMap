// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.mixin;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//@Mixin({PlayerManager.class})
//public class PlayerManagerMixin {
//
//    @Inject(
//            at = {@At("HEAD")},
//            method = {"sendLevelInfo"}
//    )
//    public void onSendWorldInfo(ServerPlayerEntity player, ServerWorld world, CallbackInfo info) {
//        System.out.println("WORLD INFO" + world.getDimension().coordinateScale());
//    }
//}