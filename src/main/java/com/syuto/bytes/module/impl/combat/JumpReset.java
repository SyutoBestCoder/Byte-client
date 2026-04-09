package com.syuto.bytes.module.impl.combat;

import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.*;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.BooleanSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import kotlinx.coroutines.CoroutineId;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

import static com.syuto.bytes.Byte.mc;

public class JumpReset extends Module {
    public JumpReset() {
        super("JumpReset", "auto jump reset", Category.COMBAT);
    }


    private final InputConstants.Key jumpKey = InputConstants.getKey("key.keyboard.space");
    private boolean jump = false;

    @Override
    public void onEnable() {
        jump = false;
    }

    @EventHandler
    public void onDamage(PlayerDamageEvent event) {
        if (event.getId() == mc.player.getId()) {
            if (mc.player.onGround() && !jump) {
                jump = true;
            }
        }
    }

    @EventHandler
    public void onTick(PreUpdateEvent event) {
        //ChatUtils.print(KeyMapping.get(mc.options.keyAttack.getName()).isDown());
        if (jump) {
            KeyMapping.set(jumpKey, true);
        }

        if (!mc.player.onGround() && jump) {
            KeyMapping.set(jumpKey, false);
            jump = false;
        }
    }
}
