package net.bogdanvalentin.floatater.mixin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.bogdanvalentin.floatater.grid.GridLevel;
import net.bogdanvalentin.floatater.grid.SubGrid;
import net.bogdanvalentin.floatater.grid.SubGridCollisions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Level.class)
public abstract class LevelMixin implements GridLevel {
    @Unique
    private final Map<UUID, SubGrid> floatater$grids = new LinkedHashMap<>();

    @Override
    public Map<UUID, SubGrid> floatater$grids() {
        return this.floatater$grids;
    }

    public boolean noCollision(@Nullable Entity entity, AABB aabb, boolean alwaysCollideWithFluids) {
        Level level = (Level) (Object) this;
        return level.noBlockCollision(entity, aabb, alwaysCollideWithFluids)
                && level.noEntityCollision(entity, aabb)
                && level.noBorderCollision(entity, aabb)
                && (alwaysCollideWithFluids || SubGridCollisions.noGridCollision(entity, aabb, level));
    }
}
