package net.bogdanvalentin.floataterclassic.grid;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.function.Consumer;
import net.bogdanvalentin.floataterclassic.FloataterContent;
import net.bogdanvalentin.floataterclassic.block.FloataterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public record SubGridCapture(SubGridBlocks blocks, LongSet mask, BlockPos minPos, int engines) {
    private static final Direction[] DIRECTIONS = Direction.values();

    public static @Nullable SubGridCapture scan(ServerLevel level, BlockPos start, Direction movement) {
        Long2ObjectMap<BlockState> captured = new Long2ObjectOpenHashMap<>();
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        captured.put(start.asLong(), level.getBlockState(start));
        queue.enqueue(start.asLong());
        int minX = start.getX();
        int minY = start.getY();
        int minZ = start.getZ();
        int maxX = start.getX();
        int maxY = start.getY();
        int maxZ = start.getZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        int sizeLimit = level.getGameRules().get(FloataterContent.SIZE_LIMIT);

        while (!queue.isEmpty()) {
            long packed = queue.dequeueLastLong();
            pos.set(packed);
            BlockState state = captured.get(packed);
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
            if (maxX - minX + 1 > sizeLimit || maxY - minY + 1 > sizeLimit || maxZ - minZ + 1 > sizeLimit) {
                return null;
            }

            VoxelShape shape = state.getShape(level, pos);

            for (Direction direction : DIRECTIONS) {
                neighbor.setWithOffset(pos, direction);
                long neighborPacked = neighbor.asLong();
                if (!captured.containsKey(neighborPacked)) {
                    BlockState neighborState = level.getBlockState(neighbor);
                    VoxelShape neighborShape = neighborState.getShape(level, neighbor);
                    boolean pushedInFront = direction == movement && !neighborState.canBeReplaced();
                    if (pushedInFront || isConnected(direction, shape, neighborShape, state, neighborState)) {
                        queue.enqueue(neighborPacked);
                        captured.put(neighborPacked, neighborState);
                    }
                }
            }
        }

        SubGridBlocks blocks = new SubGridBlocks(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
        int engines = 0;

        for (Long2ObjectMap.Entry<BlockState> entry : Long2ObjectMaps.fastIterable(captured)) {
            pos.set(entry.getLongKey());
            BlockState state = entry.getValue().trySetValue(BlockStateProperties.WATERLOGGED, false);
            if (state.is(FloataterContent.FLOATATER) && state.getValue(FloataterBlock.FACING) == movement && state.getValue(FloataterBlock.TRIGGERED)) {
                engines++;
            }

            if (state.getBlock() instanceof FlyingTickable) {
                blocks.markTickable(new BlockPos(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ));
            }

            if (!state.hasBlockEntity()) {
                blocks.setBlockState(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ, state);
            }
        }

        return new SubGridCapture(blocks, new LongOpenHashSet(captured.keySet()), new BlockPos(minX, minY, minZ), engines);
    }

    private static boolean isConnected(Direction direction, VoxelShape shape, VoxelShape neighborShape, BlockState state, BlockState neighborState) {
        if (isStickyInDirection(state, direction) || isStickyInDirection(neighborState, direction.getOpposite())) {
            return true;
        }

        return areShapesConnected(direction, shape, neighborShape)
                && !isNonStickyInDirection(state, direction)
                && !isNonStickyInDirection(neighborState, direction.getOpposite());
    }

    private static boolean areShapesConnected(Direction direction, VoxelShape shape, VoxelShape neighborShape) {
        if (shape == Shapes.empty() || neighborShape == Shapes.empty()) {
            return false;
        }

        VoxelShape face = shape.getFaceShape(direction);
        VoxelShape neighborFace = neighborShape.getFaceShape(direction.getOpposite());
        return face == Shapes.block() && neighborFace == Shapes.block() || Shapes.joinIsNotEmpty(face, neighborFace, BooleanOp.AND);
    }

    private static boolean isNonStickyInDirection(BlockState state, Direction direction) {
        return state.is(FloataterContent.FLOATATER) && state.getValue(FloataterBlock.FACING) != direction;
    }

    private static boolean isStickyInDirection(BlockState state, Direction direction) {
        if (state.is(Blocks.SLIME_BLOCK) || state.is(Blocks.HONEY_BLOCK)) {
            return true;
        }

        return state.is(Blocks.STICKY_PISTON) && state.getValue(PistonBaseBlock.FACING) == direction
                || state.is(FloataterContent.FLOATATER) && state.getValue(FloataterBlock.FACING) == direction;
    }

    public void remove(Level level) {
        this.forEachPos(pos -> {
            BlockState state = level.getBlockState(pos);
            if (state.hasBlockEntity()) {
                level.destroyBlock(pos, true);
            } else {
                level.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        });
        this.forEachPos(pos -> level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock()));
    }

    private void forEachPos(Consumer<BlockPos> action) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        LongIterator iterator = this.mask.iterator();

        while (iterator.hasNext()) {
            pos.set(iterator.nextLong());
            action.accept(pos);
        }
    }
}
