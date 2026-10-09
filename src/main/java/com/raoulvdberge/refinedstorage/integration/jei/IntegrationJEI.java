package com.raoulvdberge.refinedstorage.integration.jei;

import net.minecraftforge.fml.common.Loader;

public final class IntegrationJEI {
    public static boolean isLoaded() {
        // DEVIATION: JEI 1.8.9 uses modid "JEI" (uppercase); 1.11.2 uses "jei".
        return Loader.isModLoaded("JEI");
    }
}
