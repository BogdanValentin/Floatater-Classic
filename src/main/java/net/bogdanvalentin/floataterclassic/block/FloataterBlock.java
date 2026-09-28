package net.bogdanvalentin.floataterclassic.block;

import net.bogdanvalentin.floataterclassic.FloataterContent;
import net.bogdanvalentin.floataterclassic.grid.FlyingTickable;
import net.bogdanvalentin.floataterclassic.grid.GridCarrier;
import net.bogdanvalentin.floataterclassic.grid.SubGridBlocks;
import net.bogdanvalentin.floataterclassic.grid.SubGridCapture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FloataterBlock extends Block implements FlyingTickable {
    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    public FloataterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(TRIGGERED)) {
            if (powered) {
                level.scheduleTick(pos, this, 1);
            }

            level.setBlock(pos, state.setValue(TRIGGERED, powered), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction direction = state.getValue(FACING);
        SubGridCapture capture = SubGridCapture.scan(level, pos, direction);
        if (capture != null) {
            GridCarrier carrier = new GridCarrier(FloataterContent.GRID_CARRIER, level);
            BlockPos minPos = capture.minPos();
            carrier.snapTo(minPos.getX(), minPos.getY(), minPos.getZ());
            carrier.grid().setBlocks(capture.blocks());
            carrier.grid().setBiome(level.getBiome(pos));
            carrier.setMovement(direction, capture.engines() * 0.1F);
            capture.remove(level);
            level.addFreshEntity(carrier);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    @Override
    public void flyingTick(Level level, SubGridBlocks blocks, BlockState state, BlockPos localPos, Vec3 worldPos, Direction movement) {
        if (level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            if (movement == facing && state.getValue(TRIGGERED) && level.getRandom().nextBoolean()) {
                Direction exhaust = facing.getOpposite();
                if (blocks.getBlockState(localPos.relative(exhaust)).isAir()) {
                    Vec3 particle = worldPos.add(0.5, 0.5, 0.5).add(exhaust.getStepX() * 0.5, exhaust.getStepY() * 0.5, exhaust.getStepZ() * 0.5);
                    level.addParticle(ParticleTypes.CLOUD, particle.x, particle.y, particle.z, 0.0, 0.0, 0.0);
                }
            }
        }
    }
}
