package com.diabloxmj.xpbank.network;

import com.diabloxmj.xpbank.Xpbank;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ConfigSyncPayload(
        int smallMax, int mediumMax, int largeMax,
        int smallSuperMax, int mediumSuperMax, int largeSuperMax
) implements CustomPayload {

    public static final Id<ConfigSyncPayload> ID = new Id<>(Xpbank.id("config_sync"));

    public static final PacketCodec<RegistryByteBuf, ConfigSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodec.of((value, buf) -> buf.writeInt(value), buf -> buf.readInt()), ConfigSyncPayload::smallMax,
            PacketCodec.of((value, buf) -> buf.writeInt(value), buf -> buf.readInt()), ConfigSyncPayload::mediumMax,
            PacketCodec.of((value, buf) -> buf.writeInt(value), buf -> buf.readInt()), ConfigSyncPayload::largeMax,
            PacketCodec.of((value, buf) -> buf.writeInt(value), buf -> buf.readInt()), ConfigSyncPayload::smallSuperMax,
            PacketCodec.of((value, buf) -> buf.writeInt(value), buf -> buf.readInt()), ConfigSyncPayload::mediumSuperMax,
            PacketCodec.of((value, buf) -> buf.writeInt(value), buf -> buf.readInt()), ConfigSyncPayload::largeSuperMax,
            ConfigSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}