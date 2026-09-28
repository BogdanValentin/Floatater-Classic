package net.bogdanvalentin.floatater;

import net.bogdanvalentin.floatater.network.FloataterNetwork;
import net.bogdanvalentin.floatater.network.SubGridPayload;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Floatater.MOD_ID)
public class FloataterMod {
    public FloataterMod(IEventBus modBus) {
        modBus.addListener(this::register);
        modBus.addListener(this::buildCreativeTabs);
        modBus.addListener(this::registerPayloads);
        FloataterNetwork.setSender(PacketDistributor::sendToPlayer);
    }

    private void register(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> FloataterContent.registerBlocks(helper::register));
        event.register(Registries.ITEM, helper -> FloataterContent.registerItems(helper::register));
        event.register(Registries.ENTITY_TYPE, helper -> FloataterContent.registerEntityTypes(helper::register));
        event.register(Registries.GAME_RULE, helper -> FloataterContent.registerGameRules(helper::register));
    }

    private void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(FloataterContent.FLOATATO_ITEM);
            event.accept(FloataterContent.FLOATATER_ITEM);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SubGridPayload.TYPE, SubGridPayload.CODEC, (payload, context) -> payload.apply(context.player().level()));
    }
}
