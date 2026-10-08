package com.raoulvdberge.refinedstorage.tile.data;

import net.minecraft.network.PacketBuffer;

/**
 * Mod-local serializer interface (1.8.9 port).
 *
 * DEVIATION: mirrors the 1.11.2 vanilla net.minecraft.network.datasync.DataSerializer shape,
 * which does not exist on 1.8.9. Used only by the TileData sync framework; wire behavior of
 * the VARINT/BOOLEAN/STRING impls matches vanilla (varint / boolean / UTF-8 string).
 */
public interface DataSerializer<T> {
    void write(PacketBuffer buf, T value);

    T read(PacketBuffer buf);

    DataParameter<T> createKey(int id);
}
