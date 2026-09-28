package net.bogdanvalentin.floataterclassic.grid;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public interface GridRider {
    int SERVER_RELOAD_TIMEOUT = 40;
    int CLIENT_RELOAD_TIMEOUT = 60;

    @Nullable UUID floatater_classic$pendingGrid();

    void floatater_classic$waitForGrid(UUID gridId, int timeout);
}
