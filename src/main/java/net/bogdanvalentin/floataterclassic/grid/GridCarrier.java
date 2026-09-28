package net.bogdanvalentin.floataterclassic.grid;

import java.util.Optional;
import net.bogdanvalentin.floataterclassic.network.FloataterNetwork;
import net.bogdanvalentin.floataterclassic.network.SubGridPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class GridCarrier extends Entity {
    public static final int LERP_STEPS = 2;
    private static final EntityDataAccessor<Direction> MOVEMENT_DIRECTION = SynchedEntityData.defineId(GridCarrier.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Float> MOVEMENT_SPEED = SynchedEntityData.defineId(GridCarrier.class, EntityDataSerializers.FLOAT);
    private final SubGrid grid;
    private final InterpolationHandler interpolation = new InterpolationHandler(this, LERP_STEPS);
    private @Nullable SubGridMovementCollider movementCollider;
    private int placeInTicks;

    public GridCarrier(EntityType<?> type, Level level) {
        super(type, level);
        this.grid = new SubGrid(level, this);
    }

    public void setMovement(Direction direction, float speed) {
        this.getEntityData().set(MOVEMENT_DIRECTION, direction);
        this.getEntityData().set(MOVEMENT_SPEED, speed);
        this.movementCollider = SubGridMovementCollider.generate(this.grid.getBlocks(), direction);
    }

    public void clearMovement() {
        this.getEntityData().set(MOVEMENT_SPEED, 0.0F);
        this.movementCollider = null;
    }

    public SubGrid grid() {
        return this.grid;
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        if (this.grid != null) {
            this.grid.updatePosition(x, y, z);
        }
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            this.interpolation.interpolate();
        }

        super.tick();
        this.grid.getBlocks().tick(this.level(), this.position(), this.getMovementDirection());
        if (!this.level().isClientSide()) {
            this.tickServer();
        }
    }

    private void tickServer() {
        float speed = this.getMovementSpeed();
        if (this.placeInTicks == 0 && speed == 0.0F) {
            this.placeInTicks = 2;
        }

        if (this.placeInTicks > 0) {
            this.placeInTicks--;
            if (this.placeInTicks == 1) {
                this.grid.getBlocks().place(this.blockPosition(), this.level());
            } else if (this.placeInTicks == 0) {
                this.discard();
            }
        } else if (this.movementCollider != null) {
            this.tickMovement(this.movementCollider, this.getMovementDirection(), speed);
        }
    }

    private void tickMovement(SubGridMovementCollider collider, Direction direction, float speed) {
        Vec3 from = this.position();
        Vec3 to = from.add(direction.getStepX() * speed, direction.getStepY() * speed, direction.getStepZ() * speed);
        BlockPos start = this.getCollidingPos(from, direction);
        BlockPos end = this.getCollidingPos(to, direction);
        BlockPos.MutableBlockPos pos = start.mutable();

        while (!pos.equals(end)) {
            pos.move(direction);
            if (collider.checkCollision(this.level(), pos)) {
                this.setPos(Vec3.atLowerCornerOf(pos.relative(direction, -1)));
                this.clearMovement();
                this.placeInTicks = 5;
                return;
            }
        }

        this.setPos(to);
    }

    private BlockPos getCollidingPos(Vec3 position, Direction direction) {
        BlockPos pos = BlockPos.containing(position);
        return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? pos.relative(direction) : pos;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(MOVEMENT_DIRECTION, Direction.NORTH);
        entityData.define(MOVEMENT_SPEED, 0.0F);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        input.read("blocks", SubGridBlocks.CODEC).ifPresent(this.grid::setBlocks);
        input.read("biome", ResourceKey.codec(Registries.BIOME))
                .flatMap(key -> this.level().registryAccess().lookupOrThrow(Registries.BIOME).get(key))
                .ifPresent(this.grid::setBiome);
        Optional<Direction> direction = input.read("movement_direction", Direction.CODEC);
        if (direction.isPresent()) {
            this.setMovement(direction.get(), input.getFloatOr("movement_speed", 0.0F));
        } else {
            this.clearMovement();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("blocks", SubGridBlocks.CODEC, this.grid.getBlocks());
        this.grid.getBiome().unwrapKey().ifPresent(key -> output.store("biome", ResourceKey.codec(Registries.BIOME), key));
        output.store("movement_direction", Direction.CODEC, this.getMovementDirection());
        output.putFloat("movement_speed", this.getMovementSpeed());
    }

    private float getMovementSpeed() {
        return this.getEntityData().get(MOVEMENT_SPEED);
    }

    private Direction getMovementDirection() {
        return this.getEntityData().get(MOVEMENT_DIRECTION);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        FloataterNetwork.sendToPlayer(player, new SubGridPayload(this.getId(), this.grid.getBlocks().copy(), this.grid.getBiome()));
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    public void applyPayload(SubGridPayload payload) {
        this.grid.setBlocks(payload.blocks());
        this.grid.setBiome(payload.biome());
    }
}
