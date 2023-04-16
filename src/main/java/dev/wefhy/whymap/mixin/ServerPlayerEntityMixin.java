// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.mixin;

import com.mojang.authlib.GameProfile;
import dev.wefhy.whymap.events.WorldEventQueue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    @Inject(method="onDeath", at=@At("TAIL"))
    void onPlayerDeath(CallbackInfo ci) {
//        ((PlayerEntity)(Object)this).getPos();
        System.out.println("PLAYER SERVER DEAAAATH");
        WorldEventQueue.INSTANCE.addUpdate$whymap(WorldEventQueue.WorldEvent.PlayerDeath);
    }
}
