package net.bogdanvalentin.floataterclassic;

import net.bogdanvalentin.floataterclassic.client.GridCarrierRenderer;
import net.bogdanvalentin.floataterclassic.network.GridWaitPayload;
import net.bogdanvalentin.floataterclassic.network.SubGridPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class FloataterClassicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(FloataterContent.GRID_CARRIER, GridCarrierRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(SubGridPayload.TYPE, (payload, context) -> payload.apply(context.player().level()));
        ClientPlayNetworking.registerGlobalReceiver(GridWaitPayload.TYPE, (payload, context) -> payload.apply(context.player()));
    }
}
