package net.bogdanvalentin.floatater.grid;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public interface GridRider {
    int SERVER_RELOAD_TIMEOUT = 40;
    int CLIENT_RELOAD_TIMEOUT = 60;

    @Nullable UUID floatater$pendingGrid();

    void floatater$waitForGrid(UUID gridId, int timeout);
}
