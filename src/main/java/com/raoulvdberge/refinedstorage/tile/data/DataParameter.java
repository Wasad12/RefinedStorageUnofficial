package com.raoulvdberge.refinedstorage.tile.data;

/**
 * Mod-local key interface (1.8.9 port). Mirrors the vanilla shape; RS always passes null keys.
 */
public interface DataParameter<T> {
    int getId();
}
