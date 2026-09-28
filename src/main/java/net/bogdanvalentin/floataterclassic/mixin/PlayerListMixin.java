package net.bogdanvalentin.floataterclassic.mixin;

import java.util.UUID;
import net.bogdanvalentin.floataterclassic.grid.GridRider;
import net.bogdanvalentin.floataterclassic.network.FloataterNetwork;
import net.bogdanvalentin.floataterclassic.network.GridWaitPayload;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void floatater_classic$sendPendingGrid(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        UUID gridId = ((GridRider) player).floatater_classic$pendingGrid();
        if (gridId != null) {
            FloataterNetwork.sendToPlayer(player, new GridWaitPayload(gridId));
        }
    }
}
