package com.raoulvdberge.refinedstorage;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public final class RSKeyBindings {
    // DEVIATION: 1.8.9 has no KeyConflictContext; GUI-only scoping is lost (binding is global).
    public static final KeyBinding FOCUS_SEARCH_BAR = new KeyBinding("key.refinedstorage.focusSearchBar", Keyboard.KEY_TAB, "Refined Storage");

    public static void init() {
        ClientRegistry.registerKeyBinding(FOCUS_SEARCH_BAR);
    }
}