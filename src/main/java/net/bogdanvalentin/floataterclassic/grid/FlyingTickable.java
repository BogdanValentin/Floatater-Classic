package net.bogdanvalentin.floataterclassic.grid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface FlyingTickable {
    void flyingTick(Level level, SubGridBlocks blocks, BlockState state, BlockPos localPos, Vec3 worldPos, Direction movement);
}
