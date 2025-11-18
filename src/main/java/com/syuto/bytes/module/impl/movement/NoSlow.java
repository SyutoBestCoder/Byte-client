package com.syuto.bytes.module.impl.movement;

import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.eventbus.impl.SlowDownEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.ModeSetting;

import net.minecraft.client.input.KeyboardInput;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends Module {
    public ModeSetting modes = new ModeSetting("mode",this,"Vanilla", "Epsilon", "Grim");

    private int ground = 0;
    private boolean state = false;


    public NoSlow() {
        super("NoSlow", "Stops you from slowing down", Category.MOVEMENT);
        setSuffix(() -> modes.getValue());
    }

    @EventHandler
    public void onSlowDown(SlowDownEvent event) {
        if (event.getMode().equals(SlowDownEvent.Mode.Item)) {
            event.setCanceled(true);
        }
    }

    @EventHandler
    void onPreMotion(PreMotionEvent event) {
        boolean ground = mc.player.isOnGround();
        Vec3d pos = mc.player.getPos();

        this.ground = ground ? this.ground + 1 : 0;

        if (canNoSlow()) {
            switch(modes.getValue()) {
                case "Epsilon" -> {
                    if (this.ground >= 18 && canNoSlow()) {
                        state = true;
                        mc.options.jumpKey.setPressed(false);
                        mc.player.jump();
                    }

                    if (state) {
                        event.posY +=  1E-14;
                    }
                }
                case "Ground" -> {

                }
            }
        }
    }





    public boolean canNoSlow() {
        return mc.player.isUsingItem() && !(mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot).getItem() instanceof SwordItem);
    }
    //grim  mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36, 1, SlotActionType.SWAP, mc.player);
}

