package com.syuto.bytes.mixin;


import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Connection.class)
public interface ConnectionAccessor {

    @Accessor("packetListener")
    PacketListener byte$getPacketListener();

    @Invoker("genericsFtw")
    <T extends PacketListener> void byte$genericsFtw(Packet<?> packet, T listener);
}