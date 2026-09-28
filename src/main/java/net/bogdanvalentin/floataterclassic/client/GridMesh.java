package net.bogdanvalentin.floataterclassic.client;

import java.util.ArrayList;
import java.util.List;
import net.bogdanvalentin.floataterclassic.grid.SubGrid;
import net.bogdanvalentin.floataterclassic.grid.SubGridBlocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public final class GridMesh {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final SubGridBlocks blocks;
    private final int version;
    private final List<GridBlockRenderState> renderStates = new ArrayList<>();
    private final BlockPos.MutableBlockPos worldPos = new BlockPos.MutableBlockPos();
    private ClientLevel level;
    private BlockPos origin = BlockPos.ZERO;

    private GridMesh(SubGrid grid, ClientLevel level) {
        this.blocks = grid.getBlocks();
        this.version = grid.version();
        this.level = level;
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();

        for (int x = 0; x < this.blocks.sizeX(); x++) {
            for (int y = 0; y < this.blocks.sizeY(); y++) {
                for (int z = 0; z < this.blocks.sizeZ(); z++) {
                    BlockState state = this.blocks.getBlockState(x, y, z);
                    if (state.getRenderShape() == RenderShape.MODEL && !this.isHidden(x, y, z, neighbor)) {
                        GridBlockRenderState renderState = new GridBlockRenderState(this, new BlockPos(x, y, z), state);
                        renderState.biome = grid.getBiome();
                        renderState.cardinalLighting = level.cardinalLighting();
                        renderState.lightEngine = level.getLightEngine();
                        this.renderStates.add(renderState);
                    }
                }
            }
        }
    }

    public static GridMesh of(SubGrid grid, ClientLevel level) {
        if (grid.renderCache() instanceof GridMesh mesh && mesh.version == grid.version()) {
            mesh.level = level;
            return mesh;
        }

        GridMesh mesh = new GridMesh(grid, level);
        grid.setRenderCache(mesh);
        return mesh;
    }

    private boolean isHidden(int x, int y, int z, BlockPos.MutableBlockPos neighbor) {
        for (Direction direction : DIRECTIONS) {
            neighbor.set(x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ());
            if (!this.blocks.getBlockState(neighbor).isSolidRender()) {
                return false;
            }
        }

        return true;
    }

    public void moveTo(BlockPos origin) {
        this.origin = origin;
    }

    public SubGridBlocks blocks() {
        return this.blocks;
    }

    public List<GridBlockRenderState> renderStates() {
        return this.renderStates;
    }

    int worldBrightness(LightLayer layer, BlockPos localPos) {
        this.worldPos.setWithOffset(this.origin, localPos);
        return this.level.getBrightness(layer, this.worldPos);
    }
}
