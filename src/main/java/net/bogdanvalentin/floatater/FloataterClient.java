package net.bogdanvalentin.floatater;

import net.bogdanvalentin.floatater.client.GridCarrierRenderer;
import net.bogdanvalentin.floatater.network.SubGridPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class FloataterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(FloataterContent.GRID_CARRIER, GridCarrierRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(SubGridPayload.TYPE, (payload, context) -> payload.apply(context.player().level()));
    }
}
