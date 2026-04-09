package com.syuto.bytes.module.impl.movement;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.module.impl.combat.Triggerbot;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.player.MovementUtil;

import static com.syuto.bytes.Byte.mc;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Sprints for you", Category.MOVEMENT);
    }


    @EventHandler
    void onPreUpdate(PreUpdateEvent event) {

        if (mc.player.horizontalCollision && mc.player.isSprinting()) {
            mc.options.keySprint.setDown(false);
            return;
        }

        if (MovementUtil.isMoving()) {
            mc.options.keySprint.setDown(true);
        }

    }
}
