package net.bogdanvalentin.floataterclassic.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.bogdanvalentin.floataterclassic.grid.SubGridCollisions;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @ModifyReturnValue(method = "noBlocksAround", at = @At("RETURN"))
    private boolean floatater_classic$gridCountsAsGround(boolean noBlocksAround, Entity entity) {
        return noBlocksAround && SubGridCollisions.findGridBelow(entity) == null;
    }
}
