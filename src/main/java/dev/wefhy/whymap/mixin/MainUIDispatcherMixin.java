// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.mixin;

//public class GlobalSnapshotManagerMixin {
//}


import dev.wefhy.whymap.utils.WhyDispatchers;
import kotlinx.coroutines.CoroutineDispatcher;
import org.jetbrains.skiko.MainUIDispatcher_awtKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MainUIDispatcher_awtKt.class, remap = false)
public class MainUIDispatcherMixin {
    @Inject(method = "getMainUIDispatcher", at = @At("HEAD"), cancellable = true)
//@Inject(method = "getMainUIDispatcher()Lkotlinx/coroutines/CoroutineDispatcher;", at = @At("HEAD"), cancellable = true)
//@Inject(method = "Lkotlinx/coroutines/CoroutineDispatcher;", at = @At("HEAD"), cancellable = true)
    private static void getMainUIDispatcher(CallbackInfoReturnable<CoroutineDispatcher> cir) {
        cir.setReturnValue(WhyDispatchers.INSTANCE.getMainDispatcher());
    }
}