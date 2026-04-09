package com.syuto.bytes.eventbus;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.KeyEvent;
import com.syuto.bytes.eventbus.impl.MouseInputEvent;
import com.syuto.bytes.eventbus.impl.RenderTickEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.impl.combat.AimAssist;
import com.syuto.bytes.module.impl.combat.AttributeSwap;
import com.syuto.bytes.utils.impl.keyboard.MouseUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.MouseHandler;
import org.lwjgl.glfw.GLFW;

import java.awt.event.MouseEvent;

import static com.syuto.bytes.Byte.mc;

public class Handlers {

    @EventHandler
    public void onKey(KeyEvent event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            ModuleManager.modules.stream()
                    .filter(m -> m.getKey() == event.getKey())
                    .forEach(Module::toggle);
        }
    }

    @EventHandler
    public void onMouseInput(MouseInputEvent event) {
        if (event.getMouseButton() == MouseUtil.LEFT_CLICK.getValue()) {
            AimAssist.allowBreaking = !(AimAssist.target != null && AimAssist.rotations != null);

        }
    }
}