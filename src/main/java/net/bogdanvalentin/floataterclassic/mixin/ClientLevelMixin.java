package net.bogdanvalentin.floataterclassic.mixin;

import net.bogdanvalentin.floataterclassic.grid.GridCarrier;
import net.bogdanvalentin.floataterclassic.grid.GridLevel;
import net.bogdanvalentin.floataterclassic.grid.SubGrid;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "tickEntities", at = @At("HEAD"))
    private void floatater_classic$tickGridsFirst(CallbackInfo ci) {
        ClientLevel level = (ClientLevel) (Object) this;
        for (SubGrid grid : GridLevel.snapshot(level)) {
            GridCarrier carrier = grid.carrier();
            if (!carrier.isRemoved() && !level.tickRateManager().isEntityFrozen(carrier)) {
                level.guardEntityTick(level::tickNonPassenger, carrier);
            }
        }
    }
}
