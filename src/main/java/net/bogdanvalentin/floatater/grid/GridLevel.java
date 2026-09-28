package net.bogdanvalentin.floatater.grid;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.level.Level;

public interface GridLevel {
    Map<UUID, SubGrid> floatater$grids();

    static Map<UUID, SubGrid> grids(Level level) {
        return ((GridLevel) level).floatater$grids();
    }

    static List<SubGrid> snapshot(Level level) {
        Map<UUID, SubGrid> grids = grids(level);
        return grids.isEmpty() ? List.of() : List.copyOf(grids.values());
    }

    static void track(Level level, GridCarrier carrier) {
        grids(level).put(carrier.getUUID(), carrier.grid());
    }

    static void untrack(Level level, GridCarrier carrier) {
        grids(level).remove(carrier.getUUID(), carrier.grid());
    }
}
