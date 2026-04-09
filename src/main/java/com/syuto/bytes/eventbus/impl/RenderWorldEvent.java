package com.syuto.bytes.eventbus.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.syuto.bytes.eventbus.Event;

public class RenderWorldEvent implements Event {

    public float partialTicks;
    public PoseStack matrixStack;

    public RenderWorldEvent(float partialTicks, PoseStack matrixStack) {
        this.partialTicks = partialTicks;
        this.matrixStack = matrixStack;
    }

}
