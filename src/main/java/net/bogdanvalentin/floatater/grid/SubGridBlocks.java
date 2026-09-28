package net.bogdanvalentin.floatater.grid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class SubGridBlocks {
    private static final BlockState EMPTY_BLOCK_STATE = Blocks.AIR.defaultBlockState();
    private static final StreamCodec<ByteBuf, List<BlockState>> PALETTE_STREAM_CODEC =
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY).apply(ByteBufCodecs.list());
    private static final StreamCodec<ByteBuf, List<BlockPos>> TICKABLES_STREAM_CODEC =
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<RegistryFriendlyByteBuf, SubGridBlocks> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SubGridBlocks decode(RegistryFriendlyByteBuf buffer) {
            int sizeX = buffer.readVarInt();
            int sizeY = buffer.readVarInt();
            int sizeZ = buffer.readVarInt();
            List<BlockState> palette = PALETTE_STREAM_CODEC.decode(buffer);
            BlockState[] blockStates = new BlockState[sizeX * sizeY * sizeZ];
            SimpleBitStorage storage = new SimpleBitStorage(bitsFor(palette.size()), blockStates.length, buffer.readLongArray());

            for (int i = 0; i < blockStates.length; i++) {
                blockStates[i] = palette.get(storage.get(i));
            }

            List<BlockPos> tickables = new ArrayList<>(TICKABLES_STREAM_CODEC.decode(buffer));
            return new SubGridBlocks(blockStates, tickables, sizeX, sizeY, sizeZ);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, SubGridBlocks blocks) {
            buffer.writeVarInt(blocks.sizeX);
            buffer.writeVarInt(blocks.sizeY);
            buffer.writeVarInt(blocks.sizeZ);
            Reference2IntMap<BlockState> indices = new Reference2IntOpenHashMap<>();
            indices.defaultReturnValue(-1);
            List<BlockState> palette = new ArrayList<>();

            for (BlockState state : blocks.blockStates) {
                if (indices.putIfAbsent(state, palette.size()) == -1) {
                    palette.add(state);
                }
            }

            PALETTE_STREAM_CODEC.encode(buffer, palette);
            SimpleBitStorage storage = new SimpleBitStorage(bitsFor(palette.size()), blocks.blockStates.length);

            for (int i = 0; i < blocks.blockStates.length; i++) {
                storage.set(i, indices.getInt(blocks.blockStates[i]));
            }

            buffer.writeLongArray(storage.getRaw());
            TICKABLES_STREAM_CODEC.encode(buffer, blocks.tickables);
        }
    };

    public static final Codec<SubGridBlocks> CODEC = Stored.CODEC.xmap(SubGridBlocks::fromStored, SubGridBlocks::toStored);

    final BlockState[] blockStates;
    final List<BlockPos> tickables;
    final int sizeX;
    final int sizeY;
    final int sizeZ;

    SubGridBlocks(BlockState[] blockStates, List<BlockPos> tickables, int sizeX, int sizeY, int sizeZ) {
        this.blockStates = blockStates;
        this.tickables = tickables;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public SubGridBlocks(int sizeX, int sizeY, int sizeZ) {
        this.blockStates = new BlockState[sizeX * sizeY * sizeZ];
        Arrays.fill(this.blockStates, EMPTY_BLOCK_STATE);
        this.tickables = new ArrayList<>();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    private static int bitsFor(int paletteSize) {
        return Math.max(1, Mth.ceillog2(paletteSize));
    }

    public void setBlockState(int x, int y, int z, BlockState state) {
        int index = this.index(x, y, z);
        if (index == -1) {
            throw new IllegalStateException("Block was out of bounds");
        }

        this.blockStates[index] = state;
    }

    public void markTickable(BlockPos pos) {
        this.tickables.add(pos);
    }

    public void tick(Level level, Vec3 origin, Direction movement) {
        this.tickables.forEach(pos -> {
            BlockState state = this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
            if (state.getBlock() instanceof FlyingTickable tickable) {
                tickable.flyingTick(level, this, state, pos, origin.add(pos.getX(), pos.getY(), pos.getZ()), movement);
            }
        });
    }

    public BlockState getBlockState(int x, int y, int z) {
        int index = this.index(x, y, z);
        return index == -1 ? EMPTY_BLOCK_STATE : this.blockStates[index];
    }

    public BlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    private int index(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < this.sizeX && y < this.sizeY && z < this.sizeZ ? (x + z * this.sizeX) * this.sizeY + y : -1;
    }

    public int sizeX() {
        return this.sizeX;
    }

    public int sizeY() {
        return this.sizeY;
    }

    public int sizeZ() {
        return this.sizeZ;
    }

    public SubGridBlocks copy() {
        return new SubGridBlocks(Arrays.copyOf(this.blockStates, this.blockStates.length), new ArrayList<>(this.tickables), this.sizeX, this.sizeY, this.sizeZ);
    }

    public void place(BlockPos origin, Level level) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int z = 0; z < this.sizeZ; z++) {
            for (int x = 0; x < this.sizeX; x++) {
                for (int y = 0; y < this.sizeY; y++) {
                    pos.setWithOffset(origin, x, y, z);
                    BlockState state = this.getBlockState(x, y, z);
                    if (!state.isAir()) {
                        if (level.getFluidState(pos).is(Fluids.WATER)) {
                            state = state.trySetValue(BlockStateProperties.WATERLOGGED, true);
                        }

                        level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }
        }

        for (int z = 0; z < this.sizeZ; z++) {
            for (int x = 0; x < this.sizeX; x++) {
                for (int y = 0; y < this.sizeY; y++) {
                    pos.setWithOffset(origin, x, y, z);
                    level.updateNeighborsAt(pos, this.getBlockState(x, y, z).getBlock());
                }
            }
        }
    }

    private static SubGridBlocks fromStored(Stored stored) {
        BlockState[] blockStates = new BlockState[stored.sizeX() * stored.sizeY() * stored.sizeZ()];
        int[] indices = stored.blocks().toArray();
        if (indices.length != blockStates.length) {
            return new SubGridBlocks(stored.sizeX(), stored.sizeY(), stored.sizeZ());
        }

        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            blockStates[i] = index >= 0 && index < stored.palette().size() ? stored.palette().get(index) : EMPTY_BLOCK_STATE;
        }

        List<BlockPos> tickables = new ArrayList<>();
        stored.tickables().mapToObj(BlockPos::of).forEach(tickables::add);
        return new SubGridBlocks(blockStates, tickables, stored.sizeX(), stored.sizeY(), stored.sizeZ());
    }

    private Stored toStored() {
        Reference2IntMap<BlockState> indices = new Reference2IntOpenHashMap<>();
        indices.defaultReturnValue(-1);
        List<BlockState> palette = new ArrayList<>();
        int[] blocks = new int[this.blockStates.length];

        for (int i = 0; i < this.blockStates.length; i++) {
            int index = indices.putIfAbsent(this.blockStates[i], palette.size());
            if (index == -1) {
                blocks[i] = palette.size();
                palette.add(this.blockStates[i]);
            } else {
                blocks[i] = index;
            }
        }

        return new Stored(this.sizeX, this.sizeY, this.sizeZ, palette, IntStream.of(blocks),
                this.tickables.stream().mapToLong(BlockPos::asLong));
    }

    private record Stored(int sizeX, int sizeY, int sizeZ, List<BlockState> palette, IntStream blocks, LongStream tickables) {
        static final Codec<Stored> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("size_x").forGetter(Stored::sizeX),
                Codec.INT.fieldOf("size_y").forGetter(Stored::sizeY),
                Codec.INT.fieldOf("size_z").forGetter(Stored::sizeZ),
                BlockState.CODEC.listOf().fieldOf("palette").forGetter(Stored::palette),
                Codec.INT_STREAM.fieldOf("blocks").forGetter(Stored::blocks),
                Codec.LONG_STREAM.optionalFieldOf("tickables", LongStream.empty()).forGetter(Stored::tickables)
        ).apply(instance, Stored::new));
    }
}
