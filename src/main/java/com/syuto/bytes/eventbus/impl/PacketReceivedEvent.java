package com.syuto.bytes.eventbus.impl;

import com.syuto.bytes.eventbus.Event;
import lombok.Setter;
import net.minecraft.network.protocol.Packet;

public class PacketReceivedEvent implements Event {
    @Setter
    private Packet<?> packet;
    @Setter
    private boolean canceled;

    public PacketReceivedEvent(Packet<?> packet) {
        this.packet = packet;
        this.canceled = false;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public boolean isCanceled() {
        return canceled;
    }
}

