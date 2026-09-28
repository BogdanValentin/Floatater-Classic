package net.bogdanvalentin.floatater.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bogdanvalentin.floatater.grid.GridCarrier;
import net.bogdanvalentin.floatater.grid.SubGridBlocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class GridCarrierRenderer extends EntityRenderer<GridCarrier, GridCarrierRenderState> {
    private static final double CULL_BUFFER_SIZE = 3.0;

    public GridCarrierRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public GridCarrierRenderState createRenderState() {
        return new GridCarrierRenderState();
    }

    @Override
    protected AABB getBoundingBoxForCulling(GridCarrier carrier, float partialTicks) {
        double x = Mth.lerp(partialTicks, carrier.xOld, carrier.getX());
        double y = Mth.lerp(partialTicks, carrier.yOld, carrier.getY());
        double z = Mth.lerp(partialTicks, carrier.zOld, carrier.getZ());
        SubGridBlocks blocks = carrier.grid().getBlocks();
        return new AABB(x, y, z, x + blocks.sizeX() + 1.0, y + blocks.sizeY() + 1.0, z + blocks.sizeZ() + 1.0).inflate(CULL_BUFFER_SIZE);
    }

    @Override
    public void extractRenderState(GridCarrier carrier, GridCarrierRenderState state, float partialTicks) {
        super.extractRenderState(carrier, state, partialTicks);
        if (carrier.level() instanceof ClientLevel level) {
            GridMesh mesh = GridMesh.of(carrier.grid(), level);
            mesh.moveTo(BlockPos.containing(state.x + 0.5, state.y + 0.5, state.z + 0.5));
            state.blocks = mesh.renderStates();
        }
    }

    @Override
    public void submit(GridCarrierRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        for (GridBlockRenderState block : state.blocks) {
            poseStack.pushPose();
            poseStack.translate(block.blockPos.getX(), block.blockPos.getY(), block.blockPos.getZ());
            submitNodeCollector.submitMovingBlock(poseStack, block, 0);
            poseStack.popPose();
        }

        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
