package net.bogdanvalentin.floataterclassic.network;

import net.bogdanvalentin.floataterclassic.FloataterClassic;
import net.bogdanvalentin.floataterclassic.grid.GridCarrier;
import net.bogdanvalentin.floataterclassic.grid.SubGridBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public record SubGridPayload(int entityId, SubGridBlocks blocks, Holder<Biome> biome) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SubGridPayload> TYPE = new CustomPacketPayload.Type<>(FloataterClassic.id("sub_grid"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SubGridPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SubGridPayload::entityId,
            SubGridBlocks.STREAM_CODEC, SubGridPayload::blocks,
            ByteBufCodecs.holderRegistry(Registries.BIOME), SubGridPayload::biome,
            SubGridPayload::new);

    @Override
    public CustomPacketPayload.Type<SubGridPayload> type() {
        return TYPE;
    }

    public void apply(Level level) {
        if (level.getEntity(this.entityId) instanceof GridCarrier carrier) {
            carrier.applyPayload(this);
        }
    }
}
