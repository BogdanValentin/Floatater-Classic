package net.bogdanvalentin.floataterclassic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bogdanvalentin.floataterclassic.grid.GridCarrier;
import net.bogdanvalentin.floataterclassic.grid.SubGridBlocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
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
    protected AABB getBoundingBoxForCulling(GridCarrier carrier) {
        double x = Math.min(carrier.xOld, carrier.getX());
        double y = Math.min(carrier.yOld, carrier.getY());
        double z = Math.min(carrier.zOld, carrier.getZ());
        SubGridBlocks blocks = carrier.grid().getBlocks();
        return new AABB(x, y, z, Math.max(carrier.xOld, carrier.getX()) + blocks.sizeX() + 1.0, Math.max(carrier.yOld, carrier.getY()) + blocks.sizeY() + 1.0, Math.max(carrier.zOld, carrier.getZ()) + blocks.sizeZ() + 1.0).inflate(CULL_BUFFER_SIZE);
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
            submitNodeCollector.submitMovingBlock(poseStack, block);
            poseStack.popPose();
        }

        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
