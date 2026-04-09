package com.syuto.bytes.module.impl.render;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import java.awt.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class Esp extends Module {
    public Esp() {
        super("Esp", "Shows players through walls", Category.RENDER);
    }



    @EventHandler
    public void onRenderWorld(RenderWorldEvent event) {
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player en && entity.isAlive() && entity != mc.player) {
                float delta = mc.getDeltaTracker().getGameTimeDeltaTicks();
                RenderUtils.renderHealth(
                        en,
                        event,
                        en.getHealth(),
                        en.getMaxHealth() + en.getAbsorptionAmount(),
                        (en.getHealth() / en.getMaxHealth() + en.getAbsorptionAmount()),
                        event.partialTicks
                );
            }
        }

    }




    public boolean isEntityInView(Entity entity) {
        Vec3 playerLook = mc.player.getViewVector(1.0F);
        Vec3 entityPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        Vec3 pos = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        Vec3 toEntity = entityPos.subtract(pos).normalize();
        double angle = Math.acos(playerLook.dot(toEntity));
        return Math.toDegrees(angle) < mc.options.fov().get() / 1.5f;
    }


}


