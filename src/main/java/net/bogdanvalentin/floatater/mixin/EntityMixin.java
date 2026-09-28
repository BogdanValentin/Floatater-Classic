package net.bogdanvalentin.floatater.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.UUID;
import net.bogdanvalentin.floatater.grid.GridLevel;
import net.bogdanvalentin.floatater.grid.GridRider;
import net.bogdanvalentin.floatater.grid.SubGrid;
import net.bogdanvalentin.floatater.grid.SubGridCollisions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
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
    private static final String FLOATATER$ATTACHED_GRID_KEY = "floatater:attached_grid";

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

    @Unique
    private @Nullable SubGrid floatater$attachedGrid;

    @Unique
    private int floatater$attachedGridTimeout;

    @Unique
    private boolean floatater$ignoreGridCollision;

    @Unique
    private @Nullable UUID floatater$pendingGrid;

    @Unique
    private int floatater$pendingGridTimeout;

    @Override
    public @Nullable UUID floatater$pendingGrid() {
        return this.floatater$pendingGrid;
    }

    @Override
    public void floatater$waitForGrid(UUID gridId, int timeout) {
        this.floatater$pendingGrid = gridId;
        this.floatater$pendingGridTimeout = timeout;
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void floatater$tickAttachedGrid(CallbackInfo ci) {
        if (this.floatater$attachedGridTimeout > 0) {
            if (--this.floatater$attachedGridTimeout == 0) {
                this.floatater$attachedGrid = null;
            } else if (this.floatater$attachedGrid != null && this.floatater$attachedGrid.carrier().isRemoved()) {
                this.floatater$attachedGrid = null;
            }
        }

        if (this.floatater$pendingGrid != null) {
            SubGrid grid = GridLevel.grids(this.level()).get(this.floatater$pendingGrid);
            if (grid != null) {
                this.floatater$attachedGrid = grid;
                this.floatater$attachedGridTimeout = SubGridCollisions.ATTACHED_GRID_TIMEOUT;
                this.floatater$pendingGrid = null;
            } else if (--this.floatater$pendingGridTimeout <= 0 || this.floatater$attachedGrid != null) {
                this.floatater$pendingGrid = null;
            } else {
                Vec3 motion = this.getDeltaMovement();
                if (motion.y < 0.0) {
                    this.setDeltaMovement(motion.x, 0.0, motion.z);
                }
            }
        }
    }

    @Inject(method = "getGravity", at = @At("HEAD"), cancellable = true)
    private void floatater$hoverWhileWaitingForGrid(CallbackInfoReturnable<Double> cir) {
        if (this.floatater$pendingGrid != null) {
            cir.setReturnValue(0.0);
        }
    }

    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void floatater$saveAttachedGrid(ValueOutput output, CallbackInfo ci) {
        SubGrid grid = this.floatater$attachedGrid != null ? this.floatater$attachedGrid : SubGridCollisions.findGridBelow((Entity) (Object) this);
        UUID gridId = grid != null ? grid.id() : this.floatater$pendingGrid;
        if (gridId != null) {
            output.store(FLOATATER$ATTACHED_GRID_KEY, UUIDUtil.CODEC, gridId);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void floatater$loadAttachedGrid(ValueInput input, CallbackInfo ci) {
        input.read(FLOATATER$ATTACHED_GRID_KEY, UUIDUtil.CODEC)
                .ifPresent(gridId -> this.floatater$waitForGrid(gridId, GridRider.SERVER_RELOAD_TIMEOUT));
    }

    @WrapOperation(
            method = {"move", "applyMovementEmissionAndPlaySound", "spawnSprintParticle"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState floatater$effectStateFromGrid(Level level, BlockPos pos, Operation<BlockState> original) {
        BlockState state = original.call(level, pos);
        if (this.floatater$attachedGrid != null && state.isAir()) {
            BlockState gridState = SubGridCollisions.stateUnderFeet((Entity) (Object) this, this.floatater$attachedGrid);
            if (!gridState.isAir()) {
                return gridState;
            }
        }

        return state;
    }

    @WrapMethod(method = "move")
    private void floatater$moveWithAttachedGrid(MoverType moverType, Vec3 delta, Operation<Void> original) {
        boolean previouslyIgnoring = this.floatater$ignoreGridCollision;
        this.floatater$ignoreGridCollision = moverType == MoverType.PLAYER;
        try {
            if (!this.floatater$ignoreGridCollision && this.floatater$attachedGrid != null && SubGridCollisions.movesWithGrid((Entity) (Object) this)) {
                delta = delta.add(this.floatater$attachedGrid.getLastMovement());
            }

            original.call(moverType, delta);
        } finally {
            this.floatater$ignoreGridCollision = previouslyIgnoring;
        }
    }

    @ModifyReturnValue(method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"))
    private Vec3 floatater$collideWithGrids(Vec3 movement) {
        if (this.floatater$ignoreGridCollision) {
            return movement;
        }

        SubGridCollisions.Result result = SubGridCollisions.collide((Entity) (Object) this, movement, this.getBoundingBox(), this.level());
        if (result.grid() != null) {
            this.floatater$attachedGrid = result.grid();
            this.floatater$attachedGridTimeout = SubGridCollisions.ATTACHED_GRID_TIMEOUT;
        }

        return result.movement();
    }

    @WrapOperation(
            method = "move",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setOnGroundWithMovement(ZZLnet/minecraft/world/phys/Vec3;)V")
    )
    private void floatater$groundedOnGrid(Entity entity, boolean onGround, boolean horizontalCollision, Vec3 movement, Operation<Void> original,
                                          @Local(argsOnly = true) Vec3 delta) {
        if (this.floatater$attachedGrid != null) {
            onGround = this.verticalCollision && delta.y < this.floatater$attachedGrid.getLastMovement().y;
            this.verticalCollisionBelow = onGround;
        }

        original.call(entity, onGround, horizontalCollision, movement);
    }
}
