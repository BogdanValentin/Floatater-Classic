package net.bogdanvalentin.floataterclassic.mixin;

import net.bogdanvalentin.floataterclassic.grid.GridCarrier;
import net.bogdanvalentin.floataterclassic.grid.GridLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class ServerLevelEntityCallbacksMixin {
    @Inject(method = "onTickingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void floatater_classic$tickGridsSeparately(Entity entity, CallbackInfo ci) {
        if (entity instanceof GridCarrier) {
            ci.cancel();
        }
    }

    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void floatater_classic$trackGrid(Entity entity, CallbackInfo ci) {
        if (entity instanceof GridCarrier carrier) {
            GridLevel.track(carrier.level(), carrier);
        }
    }

    @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void floatater_classic$untrackGrid(Entity entity, CallbackInfo ci) {
        if (entity instanceof GridCarrier carrier) {
            GridLevel.untrack(carrier.level(), carrier);
        }
    }
}
