package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.AttackEntityEvent;
import com.syuto.bytes.eventbus.impl.PacketSentEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import net.minecraft.item.Item;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import static com.syuto.bytes.Byte.mc;

public class Criticals extends Module {
    public ModeSetting modes = new ModeSetting("mode",this,"Mace", "Packet");

    public Criticals() {
        super("Criticals", "auto crits", Category.COMBAT);
        setSuffix(() -> modes.getValue());
    }


    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (event.getMode() == AttackEntityEvent.Mode.Pre) {
            performCritical();
        }

    }

    public void performCritical() {
        switch(modes.getValue()) {
            case "Mace" -> {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();

                if (mc.player.getMainHandStack() != null) {
                    Item item = mc.player.getMainHandStack().getItem();

                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, mc.player.horizontalCollision));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.501 + 15, z, false, mc.player.horizontalCollision));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, mc.player.horizontalCollision));
                    ChatUtils.print("Critical Hit!");

                }


            }

            case "Packet" -> {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();

                if (mc.player.getMainHandStack() != null) {
                    Item item = mc.player.getMainHandStack().getItem();

                    if (item instanceof MaceItem) {
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, mc.player.horizontalCollision));
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.501 + 15, z, false, mc.player.horizontalCollision));
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, mc.player.horizontalCollision));
                        //ChatUtils.print("Critical Hit!");
                    } else {
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false, mc.player.horizontalCollision));
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, mc.player.horizontalCollision));
                       // ChatUtils.print("Critical Hit!");
                    }

                }

                //mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, mc.player.horizontalCollision));
            }
        }
    }
}
