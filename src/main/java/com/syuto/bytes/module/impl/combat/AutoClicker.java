package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.NumberSetting;
import java.util.Random;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoClicker extends Module {

    public AutoClicker() {
        super("AutoClicker", "Clicks for you", Category.COMBAT);
    }

    public NumberSetting aps = new NumberSetting("CPs", this, 10, 1, 1000, 10);

    private double clickAccumulator = 0.0;

    private boolean usingItemLastFrame = false;
    private boolean justReleasedItem = false;

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        boolean actuallyUsingItem = mc.player.isUsingItem();

        justReleasedItem = usingItemLastFrame && !actuallyUsingItem;
        usingItemLastFrame = actuallyUsingItem;

        if (actuallyUsingItem || justReleasedItem) {
            return;
        }

        if (!mc.options.keyAttack.isDown()) {
            clickAccumulator = 0.0;
            return;
        }


        clickAccumulator += aps.getValue().doubleValue() / 20.0;

        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {

            EntityHitResult entitys = (EntityHitResult) mc.hitResult;

            Entity entity = entitys.getEntity();

            if (!(entity instanceof Player player)) return;

            ItemStack active = player.getUseItem();

            if (player.isUsingItem() && active.getItem() instanceof ShieldItem) {
                while (clickAccumulator >= 1.0) {
                    performClick();
                    clickAccumulator -= 1.0;
                }
            }
        }
    }

    private void performClick() {
        if (mc.hitResult != null
                && mc.hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) mc.hitResult;
            mc.gameMode.attack(mc.player, entityHit.getEntity());
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }
}