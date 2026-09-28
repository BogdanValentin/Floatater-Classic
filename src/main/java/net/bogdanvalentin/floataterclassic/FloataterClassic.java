package net.bogdanvalentin.floataterclassic;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FloataterClassic {
    public static final String MOD_ID = "floatater_classic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private FloataterClassic() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
