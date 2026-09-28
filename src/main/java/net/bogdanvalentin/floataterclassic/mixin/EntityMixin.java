package net.bogdanvalentin.floataterclassic.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.UUID;
import net.bogdanvalentin.floataterclassic.grid.GridLevel;
import net.bogdanvalentin.floataterclassic.grid.GridRider;
import net.bogdanvalentin.floataterclassic.grid.SubGrid;
import net.bogdanvalentin.floataterclassic.grid.SubGridCollisions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements GridRider {
    @Unique
    private static final String FLOATATER$ATTACHED_GRID_KEY = "floatater_classic:attached_grid";

    @Unique
    private static final String FLOATATER$ATTACHED_GRID_OFFSET_KEY = "floatater_classic:attached_grid_offset";

    @Shadow
    public boolean verticalCollision;

    @Shadow
    public boolean verticalCollisionBelow;

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract void setDeltaMovement(double x, double y, double z);

    @Shadow
    public abstract void snapTo(double x, double y, double z);

    @Unique
    private @Nullable SubGrid floatater_classic$attachedGrid;

    @Unique
    private @Nullable Vec3 floatater_classic$pendingOffset;

    @Unique
    private int floatater_classic$attachedGridTimeout;

    @Unique
    private boolean floatater_classic$ignoreGridCollision;

    @Unique
    private @Nullable UUID floatater_classic$pendingGrid;

    @Unique
    private int floatater_classic$pendingGridTimeout;

    @Override
    public @Nullable UUID floatater_classic$pendingGrid() {
        return this.floatater_classic$pendingGrid;
    }

    @Override
    public void floatater_classic$waitForGrid(UUID gridId, int timeout) {
        this.floatater_classic$pendingGrid = gridId;
        this.floatater_classic$pendingGridTimeout = timeout;
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void floatater_classic$tickAttachedGrid(CallbackInfo ci) {
        if (this.floatater_classic$attachedGridTimeout > 0) {
            if (--this.floatater_classic$attachedGridTimeout == 0) {
                this.floatater_classic$attachedGrid = null;
            } else if (this.floatater_classic$attachedGrid != null && this.floatater_classic$attachedGrid.carrier().isRemoved()) {
                this.floatater_classic$attachedGrid = null;
            }
        }

        if (this.floatater_classic$pendingGrid != null) {
            SubGrid grid = GridLevel.grids(this.level()).get(this.floatater_classic$pendingGrid);
            if (grid != null) {
                this.floatater_classic$reseatOn(grid);
                this.floatater_classic$attachedGrid = grid;
                this.floatater_classic$attachedGridTimeout = SubGridCollisions.ATTACHED_GRID_TIMEOUT;
                this.floatater_classic$pendingGrid = null;
                this.floatater_classic$pendingOffset = null;
            } else if (--this.floatater_classic$pendingGridTimeout <= 0 || this.floatater_classic$attachedGrid != null) {
                this.floatater_classic$pendingGrid = null;
                this.floatater_classic$pendingOffset = null;
            } else {
                Vec3 motion = this.getDeltaMovement();
                if (motion.y < 0.0) {
                    this.setDeltaMovement(motion.x, 0.0, motion.z);
                }
            }
        }
    }

    @Unique
    private void floatater_classic$reseatOn(SubGrid grid) {
        Vec3 offset = this.floatater_classic$pendingOffset;
        if (offset == null || this.level().isClientSide()) {
            return;
        }

        Vec3 target = grid.carrier().position().add(offset);
        if ((Object) this instanceof ServerPlayer player) {
            player.connection.teleport(target.x, target.y, target.z, player.getYRot(), player.getXRot());
        } else {
            this.snapTo(target.x, target.y, target.z);
        }
    }

    @Inject(method = "getGravity", at = @At("HEAD"), cancellable = true)
    private void floatater_classic$hoverWhileWaitingForGrid(CallbackInfoReturnable<Double> cir) {
        if (this.floatater_classic$pendingGrid != null) {
            cir.setReturnValue(0.0);
        }
    }

    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void floatater_classic$saveAttachedGrid(ValueOutput output, CallbackInfo ci) {
        SubGrid grid = this.floatater_classic$attachedGrid != null ? this.floatater_classic$attachedGrid : SubGridCollisions.findGridBelow((Entity) (Object) this);
        UUID gridId = grid != null ? grid.id() : this.floatater_classic$pendingGrid;
        Vec3 offset = grid != null ? ((Entity) (Object) this).position().subtract(grid.carrier().position()) : this.floatater_classic$pendingOffset;
        if (gridId != null) {
            output.store(FLOATATER$ATTACHED_GRID_KEY, UUIDUtil.CODEC, gridId);
            if (offset != null) {
                output.store(FLOATATER$ATTACHED_GRID_OFFSET_KEY, Vec3.CODEC, offset);
            }
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void floatater_classic$loadAttachedGrid(ValueInput input, CallbackInfo ci) {
        input.read(FLOATATER$ATTACHED_GRID_KEY, UUIDUtil.CODEC).ifPresent(gridId -> {
            this.floatater_classic$waitForGrid(gridId, GridRider.SERVER_RELOAD_TIMEOUT);
            this.floatater_classic$pendingOffset = input.read(FLOATATER$ATTACHED_GRID_OFFSET_KEY, Vec3.CODEC).orElse(null);
        });
    }

    @WrapOperation(
            method = {"move", "applyMovementEmissionAndPlaySound", "spawnSprintParticle"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState floatater_classic$effectStateFromGrid(Level level, BlockPos pos, Operation<BlockState> original) {
        BlockState state = original.call(level, pos);
        if (this.floatater_classic$attachedGrid != null && state.isAir()) {
            BlockState gridState = SubGridCollisions.stateUnderFeet((Entity) (Object) this, this.floatater_classic$attachedGrid);
            if (!gridState.isAir()) {
                return gridState;
            }
        }

        return state;
    }

    @WrapMethod(method = "move")
    private void floatater_classic$moveWithAttachedGrid(MoverType moverType, Vec3 delta, Operation<Void> original) {
        boolean previouslyIgnoring = this.floatater_classic$ignoreGridCollision;
        this.floatater_classic$ignoreGridCollision = moverType == MoverType.PLAYER;
        try {
            if (!this.floatater_classic$ignoreGridCollision && this.floatater_classic$attachedGrid != null && SubGridCollisions.movesWithGrid((Entity) (Object) this)) {
                delta = delta.add(this.floatater_classic$attachedGrid.getLastMovement());
            }

            original.call(moverType, delta);
        } finally {
            this.floatater_classic$ignoreGridCollision = previouslyIgnoring;
        }
    }

    @ModifyReturnValue(method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"))
    private Vec3 floatater_classic$collideWithGrids(Vec3 movement) {
        if (this.floatater_classic$ignoreGridCollision) {
            return movement;
        }

        SubGridCollisions.Result result = SubGridCollisions.collide((Entity) (Object) this, movement, this.getBoundingBox(), this.level());
        if (result.grid() != null) {
            this.floatater_classic$attachedGrid = result.grid();
            this.floatater_classic$attachedGridTimeout = SubGridCollisions.ATTACHED_GRID_TIMEOUT;
        }

        return result.movement();
    }

    @WrapOperation(
            method = "move",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setOnGroundWithMovement(ZZLnet/minecraft/world/phys/Vec3;)V")
    )
    private void floatater_classic$groundedOnGrid(Entity entity, boolean onGround, boolean horizontalCollision, Vec3 movement, Operation<Void> original,
                                          @Local(argsOnly = true) Vec3 delta) {
        if (this.floatater_classic$attachedGrid != null) {
            onGround = this.verticalCollision && delta.y < this.floatater_classic$attachedGrid.getLastMovement().y;
            this.verticalCollisionBelow = onGround;
        }

        original.call(entity, onGround, horizontalCollision, movement);
    }
}
