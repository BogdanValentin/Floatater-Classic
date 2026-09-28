package net.bogdanvalentin.floatater.mixin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.bogdanvalentin.floatater.grid.GridLevel;
import net.bogdanvalentin.floatater.grid.SubGrid;
import net.minecraft.world.level.Level;
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
}
