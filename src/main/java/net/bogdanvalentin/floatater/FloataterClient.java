package net.bogdanvalentin.floatater;

import net.bogdanvalentin.floatater.client.GridCarrierRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = Floatater.MOD_ID, dist = Dist.CLIENT)
public class FloataterClient {
    public FloataterClient(IEventBus modBus) {
        modBus.addListener(this::registerRenderers);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FloataterContent.GRID_CARRIER, GridCarrierRenderer::new);
    }
}
