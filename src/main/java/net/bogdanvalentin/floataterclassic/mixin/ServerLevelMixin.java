package net.bogdanvalentin.floataterclassic.mixin;

import java.util.function.BooleanSupplier;
import net.bogdanvalentin.floataterclassic.grid.GridCarrier;
import net.bogdanvalentin.floataterclassic.grid.GridLevel;
import net.bogdanvalentin.floataterclassic.grid.SubGrid;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntityTickList;forEach(Ljava/util/function/Consumer;)V")
    )
    private void floatater_classic$tickGridsFirst(BooleanSupplier haveTime, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        for (SubGrid grid : GridLevel.snapshot(level)) {
            GridCarrier carrier = grid.carrier();
            if (!carrier.isRemoved() && !level.tickRateManager().isEntityFrozen(carrier)) {
                level.guardEntityTick(level::tickNonPassenger, carrier);
            }
        }
    }
}
