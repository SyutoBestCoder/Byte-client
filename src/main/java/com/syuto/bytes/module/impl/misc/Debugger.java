package com.syuto.bytes.module.impl.misc;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.player.MovementUtil;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class Debugger extends Module {
    public Debugger() {
        super("Debugger", "Prints incoming packets", Category.EXPLOIT);
    }

    private int ticks;

    @EventHandler
    void onPreUpdate(PreUpdateEvent e) {
        //mc.interactionManager.attackEntity(mc.player,mc.player);
       // mc.player.swingHand(Hand.MAIN_HAND);
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
    }

}
