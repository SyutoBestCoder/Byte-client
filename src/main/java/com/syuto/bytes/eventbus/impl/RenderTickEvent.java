package com.syuto.bytes.eventbus.impl;

import com.syuto.bytes.eventbus.Event;
import net.minecraft.client.gui.GuiGraphics;

public class RenderTickEvent implements Event {

    public float partialTicks;
    public GuiGraphics context;

    public RenderTickEvent(float partialTicks, GuiGraphics context) {
        this.partialTicks = partialTicks;
        this.context = context;
    }

}
