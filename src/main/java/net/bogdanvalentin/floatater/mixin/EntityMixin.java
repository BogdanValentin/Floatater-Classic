package net.bogdanvalentin.floatater.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.bogdanvalentin.floatater.grid.SubGrid;
import net.bogdanvalentin.floatater.grid.SubGridCollisions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public boolean verticalCollision;

    @Shadow
    public boolean verticalCollisionBelow;

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract AABB getBoundingBox();

    @Unique
    private @Nullable SubGrid floatater$attachedGrid;

    @Unique
    private int floatater$attachedGridTimeout;

    @Unique
    private boolean floatater$ignoreGridCollision;

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void floatater$tickAttachedGrid(CallbackInfo ci) {
        if (this.floatater$attachedGridTimeout > 0) {
            if (--this.floatater$attachedGridTimeout == 0) {
                this.floatater$attachedGrid = null;
            } else if (this.floatater$attachedGrid != null && this.floatater$attachedGrid.carrier().isRemoved()) {
                this.floatater$attachedGrid = null;
            }
        }
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
