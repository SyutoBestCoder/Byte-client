package com.syuto.bytes.module.impl.movement;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.RenderEntityEvent;
import com.syuto.bytes.eventbus.impl.RenderTickEvent;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;

public class MovementFix extends Module {
    public MovementFix() {
        super("MovementFix", "r", Category.MOVEMENT);
    }


    @EventHandler
    public void onRenderEntity(RenderEntityEvent event) {
        if (RotationUtils.yawChanged) {
            mc.player.yBob = RotationUtils.getCamYaw();
        }
        if (RotationUtils.pitchChanged) {
            mc.player.xBob = RotationUtils.getCamPitch();
        }
    }

}
