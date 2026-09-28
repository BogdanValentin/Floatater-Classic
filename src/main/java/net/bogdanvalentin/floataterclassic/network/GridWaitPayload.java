package net.bogdanvalentin.floataterclassic.network;

import java.util.UUID;
import net.bogdanvalentin.floataterclassic.FloataterClassic;
import net.bogdanvalentin.floataterclassic.grid.GridRider;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record GridWaitPayload(UUID gridId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GridWaitPayload> TYPE = new CustomPacketPayload.Type<>(FloataterClassic.id("grid_wait"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GridWaitPayload> CODEC =
            UUIDUtil.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().map(GridWaitPayload::new, GridWaitPayload::gridId);

    @Override
    public CustomPacketPayload.Type<GridWaitPayload> type() {
        return TYPE;
    }

    public void apply(Player player) {
        ((GridRider) player).floatater_classic$waitForGrid(this.gridId, GridRider.CLIENT_RELOAD_TIMEOUT);
    }
}
