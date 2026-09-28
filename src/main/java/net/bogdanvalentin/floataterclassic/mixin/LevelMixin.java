package net.bogdanvalentin.floataterclassic.mixin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.bogdanvalentin.floataterclassic.grid.GridLevel;
import net.bogdanvalentin.floataterclassic.grid.SubGrid;
import net.bogdanvalentin.floataterclassic.grid.SubGridCollisions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Level.class)
public abstract class LevelMixin implements GridLevel {
    @Unique
    private final Map<UUID, SubGrid> floatater_classic$grids = new LinkedHashMap<>();

    @Override
    public Map<UUID, SubGrid> floatater_classic$grids() {
        return this.floatater_classic$grids;
    }

    public boolean noCollision(@Nullable Entity entity, AABB aabb, boolean alwaysCollideWithFluids) {
        Level level = (Level) (Object) this;
        return level.noBlockCollision(entity, aabb, alwaysCollideWithFluids)
                && level.noEntityCollision(entity, aabb)
                && level.noBorderCollision(entity, aabb)
                && (alwaysCollideWithFluids || SubGridCollisions.noGridCollision(entity, aabb, level));
    }
}
