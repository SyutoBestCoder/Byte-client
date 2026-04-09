package com.syuto.bytes.eventbus.impl;

import com.syuto.bytes.eventbus.Event;
import lombok.Setter;
import net.minecraft.network.protocol.Packet;

public class PacketSentEvent implements Event {
    private final Packet<?> packet;
    @Setter
    private boolean canceled;

    public PacketSentEvent(Packet<?> packet) {
        this.packet = packet;
        this.canceled = false;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public boolean isCanceled() {
        return this.canceled;
    }
}
