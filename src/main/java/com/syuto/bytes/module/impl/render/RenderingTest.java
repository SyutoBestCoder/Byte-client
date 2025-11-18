package com.syuto.bytes.module.impl.render;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.RenderTickEvent;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.*;

import static com.syuto.bytes.Byte.mc;

public class RenderingTest extends Module {
    public RenderingTest() {
        super("RenderingTest", "r", Category.RENDER);
    }


    @EventHandler
    public void onRenderTick(RenderWorldEvent event) {
        if (RotationUtils.yawChanged) {
            mc.player.renderYaw = RotationUtils.getCamYaw();
        }
        //mc.player.renderPitch = RotationUtils.getCamPitch();
    }

}
