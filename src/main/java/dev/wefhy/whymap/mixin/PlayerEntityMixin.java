// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.mixin;

import dev.wefhy.whymap.events.WorldEventQueue;
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

    @Inject(method="onDeath", at=@At("TAIL"))
    void onPlayerDeath(CallbackInfo ci) {
//        ((PlayerEntity)(Object)this).getPos();
        System.out.println("PLAYER DEAAAATH");
        WorldEventQueue.INSTANCE.addUpdate$whymap(WorldEventQueue.WorldEvent.PlayerDeath);
    }

    @Inject(method="setLastDeathPos", at=@At("TAIL"))
    void playerDeathLocation(CallbackInfo ci) {
        System.out.println("PLAYER DEAAAATH position");
    }
}
