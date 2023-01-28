// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.mixin;

import dev.wefhy.whymap.WhyMapMod;
import dev.wefhy.whymap.events.WorldEventQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

//    @Inject(
//            at = {@At("HEAD")},
//            method = {"tick"}
//    )
//    public void onTickStart(CallbackInfo info) {
//        double a = MinecraftClient.getInstance().world.getDimension().coordinateScale();
////        System.out.println("COORDINATE SCALE: " + a);
//    }

    @Inject(method="onDeath", at=@At("TAIL"))
    void onPlayerDeath(CallbackInfo ci) {
//        ((PlayerEntity)(Object)this).getPos();
        System.out.println("PLAYER DEAAAATH");
        WorldEventQueue.INSTANCE.addUpdate$whymap(WorldEventQueue.WorldEvent.PlayerDeath);
    }

    @Inject(method="setLastDeathPos", at=@At("TAIL"))
    void playerDeathLocation(CallbackInfo ci) {
        ClientWorld clientWorld = MinecraftClient.getInstance().world;
        if (clientWorld != null) {
            WhyMapMod.dimensionChangeListener(clientWorld.getDimension());
        }
        System.out.println("PLAYER DEAAAATH position");
    }
}
