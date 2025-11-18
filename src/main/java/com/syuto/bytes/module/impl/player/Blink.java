package com.syuto.bytes.module.impl.player;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PacketSentEvent;
import com.syuto.bytes.mixin.SendPacketMixinAccessor;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;

import java.util.ArrayList;
import java.util.Collections;

public class Blink extends Module {

    private ArrayList<Packet<?>> packets = new ArrayList<>();

    public Blink() {
        super("Blink", "blink", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        this.clear();
    }


    @EventHandler
    public void onPacketSent(PacketSentEvent event) {
        if (event.getPacket() instanceof DisconnectS2CPacket) {
            this.clear();
        }

        if (this.isEnabled()) {
            this.addPacket(event.getPacket());
            event.setCanceled(true);
        }
    }


    private void addPacket(Packet<?> packet) {
        synchronized (packets) {
            packets.add(packet);
        }
    }

    private void clear() {
        if (packets.isEmpty()) return;
        SendPacketMixinAccessor silent = (SendPacketMixinAccessor) mc.getNetworkHandler();

        synchronized (packets) {
            for (Packet<?> packet : packets) {
                silent.getConnection().send(packet);
            }
            packets.clear();
        }
    }
}
