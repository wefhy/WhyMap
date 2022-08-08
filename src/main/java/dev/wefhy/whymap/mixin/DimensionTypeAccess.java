// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.mixin;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(DimensionType.class)
public interface DimensionTypeAccess {

    @Accessor("OVERWORLD")
    static DimensionType getOverworld() {
        throw new AssertionError();
    }

    @Accessor("THE_NETHER")
    static DimensionType getNether() {
        throw new AssertionError();
    }

    @Accessor("THE_END")
    static DimensionType getEnd() {
        throw new AssertionError();
    }

}
