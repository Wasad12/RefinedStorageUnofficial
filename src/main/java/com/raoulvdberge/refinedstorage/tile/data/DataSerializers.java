package com.raoulvdberge.refinedstorage.tile.data;

import net.minecraft.network.PacketBuffer;

/**
 * Mod-local primitive serializers (1.8.9 port). Same wire encoding as vanilla 1.11.2 datasync.
 */
public final class DataSerializers {
    public static final DataSerializer<Integer> VARINT = new DataSerializer<Integer>() {
        @Override
        public void write(PacketBuffer buf, Integer value) {
            buf.writeVarIntToBuffer(value);
        }

        @Override
        public Integer read(PacketBuffer buf) {
            return buf.readVarIntFromBuffer();
        }

        @Override
        public DataParameter<Integer> createKey(int id) {
            return null;
        }
    };

    public static final DataSerializer<Boolean> BOOLEAN = new DataSerializer<Boolean>() {
        @Override
        public void write(PacketBuffer buf, Boolean value) {
            buf.writeBoolean(value);
        }

        @Override
        public Boolean read(PacketBuffer buf) {
            return buf.readBoolean();
        }

        @Override
        public DataParameter<Boolean> createKey(int id) {
            return null;
        }
    };

    public static final DataSerializer<String> STRING = new DataSerializer<String>() {
        @Override
        public void write(PacketBuffer buf, String value) {
            buf.writeString(value);
        }

        @Override
        public String read(PacketBuffer buf) {
            return buf.readStringFromBuffer(32767);
        }

        @Override
        public DataParameter<String> createKey(int id) {
            return null;
        }
    };
}
