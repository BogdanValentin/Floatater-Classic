package net.bogdanvalentin.floatater.client;

import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class GridBlockRenderState extends MovingBlockRenderState {
    private final GridMesh mesh;

    public GridBlockRenderState(GridMesh mesh, BlockPos localPos, BlockState state) {
        this.mesh = mesh;
        this.blockPos = localPos;
        this.randomSeedPos = localPos;
        this.blockState = state;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.mesh.blocks().getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }

    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        return this.mesh.worldBrightness(layer, pos);
    }

    @Override
    public int getHeight() {
        return this.mesh.blocks().sizeY();
    }

    @Override
    public int getMinY() {
        return 0;
    }
}
