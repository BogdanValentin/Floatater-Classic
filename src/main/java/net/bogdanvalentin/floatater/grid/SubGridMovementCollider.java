package net.bogdanvalentin.floatater.grid;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SubGridMovementCollider {
    private final LongList edgeBlocks;
    private final BlockPos size;

    private SubGridMovementCollider(LongList edgeBlocks, BlockPos size) {
        this.edgeBlocks = edgeBlocks;
        this.size = size;
    }

    public static SubGridMovementCollider generate(SubGridBlocks blocks, Direction movement) {
        LongList edgeBlocks = new LongArrayList();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Direction backwards = movement.getOpposite();
        int depth = movement.getAxis().choose(blocks.sizeX(), blocks.sizeY(), blocks.sizeZ());

        for (BlockPos front : getFrontSide(blocks, movement)) {
            pos.set(front);
            boolean previousCollidable = false;

            for (int i = 0; i < depth; i++) {
                if (isCollidable(blocks.getBlockState(pos))) {
                    if (!previousCollidable) {
                        edgeBlocks.add(pos.asLong());
                    }

                    previousCollidable = true;
                } else {
                    previousCollidable = false;
                }

                pos.move(backwards);
            }
        }

        return new SubGridMovementCollider(edgeBlocks, new BlockPos(blocks.sizeX(), blocks.sizeY(), blocks.sizeZ()));
    }

    private static Iterable<BlockPos> getFrontSide(SubGridBlocks blocks, Direction movement) {
        BlockPos start = new BlockPos(
                Math.max(movement.getStepX(), 0) * (blocks.sizeX() - 1),
                Math.max(movement.getStepY(), 0) * (blocks.sizeY() - 1),
                Math.max(movement.getStepZ(), 0) * (blocks.sizeZ() - 1));
        BlockPos end = start.offset(
                movement.getAxis() == Direction.Axis.X ? 0 : blocks.sizeX() - 1,
                movement.getAxis() == Direction.Axis.Y ? 0 : blocks.sizeY() - 1,
                movement.getAxis() == Direction.Axis.Z ? 0 : blocks.sizeZ() - 1);
        return BlockPos.betweenClosed(start, end);
    }

    public boolean checkCollision(Level level, BlockPos origin) {
        int bottom = origin.getY();
        int top = bottom + this.size.getY() - 1;
        if (bottom < level.getMinY() || top > level.getMaxY()) {
            return true;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        LongIterator iterator = this.edgeBlocks.iterator();

        while (iterator.hasNext()) {
            pos.set(iterator.nextLong());
            pos.move(origin);
            if (isCollidable(level.getBlockState(pos))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isCollidable(BlockState state) {
        return !state.canBeReplaced();
    }
}
