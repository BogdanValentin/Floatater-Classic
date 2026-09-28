package net.bogdanvalentin.floatater;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Floatater {
    public static final String MOD_ID = "floatater";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private Floatater() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
