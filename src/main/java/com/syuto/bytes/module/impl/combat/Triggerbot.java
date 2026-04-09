package com.syuto.bytes.module.impl.combat;

import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.AttackEntityEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import com.syuto.bytes.mixin.KeyMappingAccessor;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.BooleanSetting;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.keyboard.KeyUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import static com.syuto.bytes.Byte.mc;


public class Triggerbot extends Module {

    private ModeSetting attackMode = new ModeSetting("Attack Mode", this, "Default", "Fast", "Super Fast");
    private BooleanSetting highlight = new BooleanSetting("Target Esp", this, false);
    private BooleanSetting crit = new BooleanSetting("Crit", this, false);
    private BooleanSetting sreset = new BooleanSetting("W-Tap", this, false);
    private BooleanSetting held = new BooleanSetting("Only while held", this, false);

    public Triggerbot() {
        super("Triggerbot", "Attacks for you while over a tagret", Category.COMBAT);
    }

    private EntityHitResult entityHit;
    private final InputConstants.Key AttackKey = InputConstants.getKey("key.mouse.left");
    public boolean resetting = false;
    public int t = 0;
    private AimAssist aim;

    @EventHandler
    void onPreUpdate(PreUpdateEvent event) {
        aim = ModuleManager.getModule(AimAssist.class);

        if (!mc.options.keyAttack.isDown() && held.getValue()) return;

        HitResult hit = mc.hitResult;
        if (hit.getType() == HitResult.Type.ENTITY) {
            entityHit = (EntityHitResult) hit;

            if (canAttack() && !(aim != null && aim.isEnabled())) {
                float cooldown = mc.player.getAttackStrengthScale(0.5f);

                String mode = attackMode.getValue();
                float delay = mode.equals("Fast") ? 0.9f :
                        (mode.equals("Super Fast") ? 0.8f : 1.0f);

                boolean attack = !crit.getValue() ||
                        (mc.player.onGround() && !canCrit()) ||
                        (canCrit());

                if (attack && cooldown >= delay) {
                    KeyUtil.pressKey(mc.options.keyAttack);
                }
            }
        } else {
            entityHit = null;
        }

        if (entityHit == null) {
            if (!mc.options.keyUp.isDown() && resetting) {
                mc.options.keyUp.setDown(true);
            }

            resetting = false;
            t = 0;
        }

        if (sreset.getValue()) {

            if (resetting) {
                t++;

                int delay = 4 + mc.player.getRandom().nextInt(3); // 2–4 ticks

                if (t >= delay) {
                    mc.options.keyUp.setDown(true);

                    if (!mc.player.isSprinting()) {
                        mc.options.keySprint.setDown(true);
                    }

                    resetting = false;
                    t = 0;
                }
            }
        }
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (!sreset.getValue()) return;

        float mF = mc.player.input.getMoveVector().y;

        if (mc.options.keyUp.isDown() && mF > 0) {
            resetting = true;
            t = 0;
            mc.options.keyUp.setDown(false);
        }
    }


    private boolean canCrit() {
        return mc.player.fallDistance > 0
                && !mc.player.onGround()
                && !mc.player.isInWater()
                && !mc.player.isPassenger()
                && !mc.player.onClimbable()
                && !mc.player.hasEffect(MobEffects.BLINDNESS);
    }

    @EventHandler
    public void onRenderWorld(RenderWorldEvent e) {
        if (highlight.getValue()) {
            if (canAttack()) {
                RenderUtils.renderBox(entityHit.getEntity(), e, e.partialTicks);
            }
        }
    }

    private boolean canAttack() {
        return entityHit != null && entityHit.getEntity().isAlive() && PlayerUtil.isHoldingWeapon() && entityHit.getEntity() instanceof LivingEntity;
    }
}
