package net.bogdanvalentin.floatater;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import java.util.function.BiConsumer;
import net.bogdanvalentin.floatater.block.FloatatoItem;
import net.bogdanvalentin.floatater.block.FloataterBlock;
import net.bogdanvalentin.floatater.grid.GridCarrier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.material.MapColor;

public final class FloataterContent {
    public static final Identifier FLOATATER_ID = Floatater.id("floatater");
    public static final Identifier FLOATATO_ID = Floatater.id("floatato");
    public static final Identifier GRID_CARRIER_ID = Floatater.id("grid_carrier");
    public static final Identifier SIZE_LIMIT_ID = Floatater.id("floatater_size_limit");

    public static Block FLOATATER;
    public static Block FLOATATO;
    public static Item FLOATATER_ITEM;
    public static Item FLOATATO_ITEM;
    public static EntityType<GridCarrier> GRID_CARRIER;
    public static GameRule<Integer> SIZE_LIMIT;

    private FloataterContent() {
    }

    public static void registerBlocks(BiConsumer<Identifier, Block> registrar) {
        FLOATATO = new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.QUARTZ)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .sound(SoundType.WOOL)
                .strength(0.3F)
                .setId(ResourceKey.create(Registries.BLOCK, FLOATATO_ID)));
        FLOATATER = new FloataterBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.QUARTZ)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .sound(SoundType.STONE)
                .strength(0.5F)
                .requiresCorrectToolForDrops()
                .setId(ResourceKey.create(Registries.BLOCK, FLOATATER_ID)));
        registrar.accept(FLOATATO_ID, FLOATATO);
        registrar.accept(FLOATATER_ID, FLOATATER);
    }

    public static void registerItems(BiConsumer<Identifier, Item> registrar) {
        FLOATATO_ITEM = new FloatatoItem(FLOATATO, itemProperties(FLOATATO_ID));
        FLOATATER_ITEM = new BlockItem(FLOATATER, itemProperties(FLOATATER_ID));
        registrar.accept(FLOATATO_ID, FLOATATO_ITEM);
        registrar.accept(FLOATATER_ID, FLOATATER_ITEM);
    }

    public static void registerEntityTypes(BiConsumer<Identifier, EntityType<?>> registrar) {
        GRID_CARRIER = EntityType.Builder.<GridCarrier>of(GridCarrier::new, MobCategory.MISC)
                .noSummon()
                .noLootTable()
                .sized(0.0F, 0.0F)
                .clientTrackingRange(10)
                .updateInterval(2)
                .build(ResourceKey.create(Registries.ENTITY_TYPE, GRID_CARRIER_ID));
        registrar.accept(GRID_CARRIER_ID, GRID_CARRIER);
    }

    public static void registerGameRules(BiConsumer<Identifier, GameRule<?>> registrar) {
        SIZE_LIMIT = new GameRule<>(
                GameRuleCategory.MISC,
                GameRuleType.INT,
                IntegerArgumentType.integer(1),
                GameRuleTypeVisitor::visitInteger,
                Codec.intRange(1, Integer.MAX_VALUE),
                value -> value,
                32,
                FeatureFlagSet.of());
        registrar.accept(SIZE_LIMIT_ID, SIZE_LIMIT);
    }

    private static Item.Properties itemProperties(Identifier id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix();
    }
}
