package com.syuto.bytes.module.impl.render;

import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.ModeSetting;
import dev.blend.ui.dropdown.DropdownClickGUI;
import org.lwjgl.glfw.GLFW;

public class ClickGUIModule extends Module {

    public final ModeSetting openAnimation = new ModeSetting("Animation", this, "None", "Scale");


    public ClickGUIModule() {
        super("ClickGUI", "Displays a GUI for the user to configure the client in.", Category.RENDER);
        setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    protected void onEnable() {
        mc.setScreen(DropdownClickGUI.INSTANCE);
    }

    @Override
    protected void onDisable() {
        if (mc.screen instanceof DropdownClickGUI) {
            mc.setScreen(null);
        }
    }

}
