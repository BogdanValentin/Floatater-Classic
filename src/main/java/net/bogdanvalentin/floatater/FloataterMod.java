package net.bogdanvalentin.floatater;

import net.bogdanvalentin.floatater.network.FloataterNetwork;
import net.bogdanvalentin.floatater.network.SubGridPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class FloataterMod implements ModInitializer {
    @Override
    public void onInitialize() {
        FloataterContent.registerBlocks((id, block) -> Registry.register(BuiltInRegistries.BLOCK, id, block));
        FloataterContent.registerItems((id, item) -> {
            Registry.register(BuiltInRegistries.ITEM, id, item);
            if (item instanceof BlockItem blockItem) {
                blockItem.registerBlocks(Item.BY_BLOCK, item);
            }
        });
        FloataterContent.registerEntityTypes((id, type) -> Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type));
        FloataterContent.registerGameRules((id, rule) -> Registry.register(BuiltInRegistries.GAME_RULE, id, rule));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(output -> {
            output.accept(FloataterContent.FLOATATO_ITEM);
            output.accept(FloataterContent.FLOATATER_ITEM);
        });

        PayloadTypeRegistry.clientboundPlay().register(SubGridPayload.TYPE, SubGridPayload.CODEC);
        FloataterNetwork.setSender(ServerPlayNetworking::send);
    }
}
