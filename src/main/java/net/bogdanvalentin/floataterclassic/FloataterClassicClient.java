package net.bogdanvalentin.floataterclassic;

import net.bogdanvalentin.floataterclassic.client.GridCarrierRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = FloataterClassic.MOD_ID, dist = Dist.CLIENT)
public class FloataterClassicClient {
    public FloataterClassicClient(IEventBus modBus) {
        modBus.addListener(this::registerRenderers);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FloataterContent.GRID_CARRIER, GridCarrierRenderer::new);
    }
}
