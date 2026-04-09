package com.syuto.bytes.module.impl.misc;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PacketSentEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.eventbus.impl.WorldJoinEvent;
import com.syuto.bytes.mixin.SendPacketMixinAccessor;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.module.impl.movement.Speed;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import java.util.ArrayList;

@Deprecated
public class Disabler extends Module {
    public ModeSetting modes = new ModeSetting("mode",this,"Vulcan", "CubeCraft");
    public Disabler() {
        super("Disabler", "Disables the ac", Category.EXPLOIT);
        setSuffix(() -> modes.getValue());
    }

    private boolean accept;
    private int ticks, id;
    private ArrayList<Packet<?>> packetList = new ArrayList<>();

    @Override
    public void onEnable() {
        reset();
        ChatUtils.print("To use this wait for io.netty.handler.timeout.ReadTimeoutException and it's disabled");
    }

    @EventHandler
    public void onWorldJoin(WorldJoinEvent event) {
        if (modes.getValue().equals("Vulcan")) {
            if (event.getEntityId() == mc.player.getId()) {
                accept = true;
                ticks = 0;
                ChatUtils.print("Disabling..");
            }
        }

    }

    @EventHandler
    public void onPacketSent(PacketSentEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof ServerboundInteractPacket || packet instanceof ServerboundUseItemPacket || packet instanceof ServerboundPlayerActionPacket) {
            ChatUtils.print(event.getPacket());
        }
        /*if (modes.getValue().equals("Vulcan")) {

            if (event.getPacket() instanceof CommonPongC2SPacket) {
                if (accept) {
                    ChatUtils.print("Cancel " + ticks);
                    event.setCanceled(true);
                }
            }
        }
        if (modes.getValue().equals("CubeCraft")) {
            if (event.getPacket() instanceof CommonPongC2SPacket) {
                synchronized (packetList) {
                    packetList.add(event.getPacket());
                }
                event.setCanceled(true);
            }
        }

        Speed speedModule = ModuleManager.getModule(Speed.class);
        if (speedModule != null && speedModule.isEnabled()) {
            if (event.getPacket() instanceof ClientCommandC2SPacket commandC2SPacket) {
                ClientCommandC2SPacket.Mode mode = commandC2SPacket.getMode();
                if (mode == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY || mode == ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY) {
                    ChatUtils.print("Canceled");
                    event.setCanceled(true);
                }
            }
        }*/
    }


    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if (modes.getValue().equals("Vulcan")) {
            if (accept) ticks++;

            if (ticks >= 1000) {
                ChatUtils.print("if you didn't get disconnected relog.");
                reset();
            }
        }
        if (modes.getValue().equals("CubeCraft")) {
            if (!packetList.isEmpty()) ticks++;

            if (ticks >= 50) {
                clear();
                ChatUtils.print("s");
                ticks = 0;
            }
        }
    }

    private void reset() {
        ticks = 0;
        accept = false;
    }


    private void clear() {
        SendPacketMixinAccessor silent = (SendPacketMixinAccessor) mc.getConnection();

        synchronized (packetList) {
            if (!packetList.isEmpty()) {
                for (Packet<?> packet : packetList) {
                    silent.byte$getConnection().send(packet);
                }
                packetList.clear();
                ChatUtils.print("Clear");
            }
        }
    }

}

