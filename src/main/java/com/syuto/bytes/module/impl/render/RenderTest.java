package com.syuto.bytes.module.impl.render;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.RenderTickEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import dev.blend.util.render.Alignment;
import dev.blend.util.render.DrawUtil;
import dev.blend.util.render.ResourceManager;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;

import static com.syuto.bytes.Byte.mc;

public class RenderTest extends Module {
    public RenderTest() {
        super("RenderTest", "test", Category.RENDER);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    void onRenderTick(RenderTickEvent event) {
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player en) || !isEntityInView(en) || !en.isAlive() || en == mc.player)
                continue;

            Vec3 pos = en.getPosition(event.partialTicks);
            Vec3 worldPos = new Vec3(pos.x, pos.y + en.getBbHeight() + 0.8, pos.z);
            Vec3 screenPos = RenderUtils.worldToScreen(worldPos);

            String name = en.getName().getString() + " " + String.format("%.1f", en.getHealth()) + "HP";
            double nameWidth = DrawUtil.getStringWidth(name, 12, ResourceManager.FontResources.regular);
            double nameHeight = 12;

            double x = screenPos.x;
            double y = screenPos.y;

            DrawUtil.begin();
            DrawUtil.rect(
                    x, y,
                    nameWidth + 4, nameHeight + 4,
                    new Color(0, 0, 0, 125),
                    Alignment.TOP_CENTER
            );
            DrawUtil.drawString(
                    name,
                    x, y + 2,
                    12,
                    Color.WHITE,
                    Alignment.TOP_CENTER,
                    ResourceManager.FontResources.regular
            );
            DrawUtil.end();
        }
    }




    public boolean isEntityInView(Entity entity) {
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null) return false;
        Vec3 cameraLook = cameraEntity.getViewVector(1.0F).normalize();
        Vec3 toEntity = entity.position()
                .add(0, entity.getEyeHeight(), 0)
                .subtract(cameraEntity.getEyePosition(1.0F))
                .normalize();

        double dot = cameraLook.dot(toEntity);

        double fov = mc.options.fov().get();
        double fovRadians = Math.toRadians(fov);
        double threshold = Math.cos(fovRadians);

        return dot > threshold;
    }
}
