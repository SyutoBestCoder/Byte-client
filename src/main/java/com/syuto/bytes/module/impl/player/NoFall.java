package com.syuto.bytes.module.impl.player;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;


public class NoFall extends Module {

    public ModeSetting mode = new ModeSetting(
            "mode",this,
            "Universal",
            "Spoof", "Old Grim"
    );

    private boolean shouldNoFall = false;
    private boolean jumpNextTick = false;

    public NoFall() {
        super("NoFall", "Stops fall damage", Category.PLAYER);
        setSuffix(() -> mode.getValue());
    }

    @EventHandler
    public void onPreMotion(PreMotionEvent event) {
        boolean ground = mc.player.onGround();

        switch (mode.getValue()) {

            case "Universal" -> {
                if(PlayerUtil.getFallDistance() < 4) {
                    event.onGround = true;
                }
            }

            case "Spoof" -> {
                event.onGround = true;
            }

            case "Old Grim" -> {
                //ChatUtils.print("nofall " + PlayerUtil.getFallDistance() );
                if (PlayerUtil.getFallDistance() == 10) {
                    shouldNoFall = true;
                }

                var player = mc.player;

                if (shouldNoFall) {
                    if (!jumpNextTick) {
                        mc.getConnection().send(new ServerboundPlayerCommandPacket(
                                player,
                                ServerboundPlayerCommandPacket.Action.START_FALL_FLYING
                        ));

                        if (mc.player.onGround()) {
                            jumpNextTick = true;
                            mc.options.keyJump.setDown(true);
                        }
                    } else {
                        mc.options.keyJump.setDown(false);
                        jumpNextTick = false;
                        shouldNoFall = false;
                    }
                }
            }
        }
    }
}
