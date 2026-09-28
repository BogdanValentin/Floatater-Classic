package net.bogdanvalentin.floataterclassic.network;

import java.util.function.BiConsumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class FloataterNetwork {
    private static BiConsumer<ServerPlayer, CustomPacketPayload> sender = (player, payload) -> {
    };

    private FloataterNetwork() {
    }

    public static void setSender(BiConsumer<ServerPlayer, CustomPacketPayload> loaderSender) {
        sender = loaderSender;
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        sender.accept(player, payload);
    }
}
