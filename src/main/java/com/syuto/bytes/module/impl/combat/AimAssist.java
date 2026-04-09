package com.syuto.bytes.module.impl.combat;

import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.*;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.BooleanSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.client.TimerUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.phys.EntityHitResult;

import static com.syuto.bytes.Byte.mc;

public class AimAssist extends Module {

    public AimAssist() {
        super("SilentAimAssist", "Automatically aims at nearby players.", Category.COMBAT);
    }

    private final NumberSetting a = new NumberSetting("Aim disance", this, 5, 3, 10, 0.5);
    private final NumberSetting reach = new NumberSetting("Reach", this, 3, 3, 8, 0.5);
    private final NumberSetting cps = new NumberSetting("CPS", this, 10, 0, 20, 1);
    private final NumberSetting angle = new NumberSetting("Angle", this, 180, 0, 360, 1);
    private final NumberSetting smooth = new NumberSetting("Smoothing", this, 1, 0.2, 1.0, 0.05);
    private final BooleanSetting rotation = new BooleanSetting("Rotations", this,true);
    private BooleanSetting highlight = new BooleanSetting("Target Esp", this, true);

    private long lastAttackTime = 0;
    private final List<Player> targets = new ArrayList<>();
    public static Player target;
    public static float[] rotations, lastRotation;
    public static boolean allowBreaking = true;
    private double clickAccumulator = 0.0;

    public static boolean swapping = true;

    @Override
    public void onDisable() {
        this.lastRotation = null;
        this.target = null;
        this.allowBreaking = true;
        this.swapping = false;
        this.lastAttackTime = 0;
    }

    @EventHandler
    public void onRotation(RotationEvent event) {
        lastRotation = new float[]{RotationUtils.getLastRotationYaw(), RotationUtils.getLastRotationPitch()};
        if (rotation.getValue()) {
            if (target != null) {
                rotations = RotationUtils.getRotations(
                        lastRotation,
                        mc.player.getEyePosition(),
                        target
                );

                rotations = RotationUtils.getFixedRotation(rotations, lastRotation);


                float currentYaw = lastRotation[0];
                float currentPitch = lastRotation[1];

                float targetYaw = rotations[0];
                float targetPitch = rotations[1];

                float smoothValue = smooth.getValue().floatValue();

                float smoothedYaw = Mth.rotLerp(smoothValue, currentYaw, targetYaw);
                float smoothedPitch = Mth.rotLerp(smoothValue, currentPitch, targetPitch);

                rotations = new float[]{smoothedYaw, smoothedPitch};


                if (
                        mc.options.keyAttack.isDown() &&
                                PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) <= a.getValue().doubleValue() && (PlayerUtil.isHoldingMace() || PlayerUtil.isHoldingWeapon()) && isInAngle(target)
                ) {


                    event.setYaw(rotations[0]);
                    event.setPitch(rotations[1]);


                    this.lastRotation = new float[]{RotationUtils.getLastRotationYaw(), RotationUtils.getLastRotationPitch()};
                }
            }
        }
    }

    private boolean usingItemLastFrame = false;
    private boolean justReleasedItem = false;

    @EventHandler
    public void onRenderTick(RenderTickEvent event) {
        allowBreaking = !(mc.options.keyAttack.isDown() && target != null && rotations != null);

        usingItemLastFrame = mc.player.isUsingItem();

        swapping = AttributeSwap.swapping;
    }




    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {


        targets.clear();
        targets.addAll(mc.level.players().stream()
                .filter(ent -> ent != mc.player && PlayerUtil.getBiblicallyAccurateDistanceToCentreOfEntity(ent) <= a.getValue().doubleValue())
                .sorted(Comparator.comparingDouble(PlayerUtil::getBiblicallyAccurateDistanceToCentreOfEntity))
                .limit(10)
                .toList());


        target = targets.isEmpty() ? null : targets.get(0);

        allowBreaking = true;

        if (target == null || rotations == null) return;

        EntityHitResult result = (EntityHitResult) PlayerUtil.raycast(
                RotationUtils.getRotationYaw(),
                RotationUtils.getRotationPitch(),
                reach.getValue().doubleValue(),
                delta,
                false
        );



        if (mc.gameMode.isDestroying()) {
            mc.gameMode.stopDestroyBlock();
            return;
        }

        ItemStack t = mc.player.getMainHandItem();
        Item item = mc.player.getMainHandItem().getItem();
        if (!mc.options.keyAttack.isDown() || !(PlayerUtil.isHoldingWeapon() || PlayerUtil.isHoldingMace()) || !isInAngle(target)) return;

        if (mc.player.isUsingItem() || usingItemLastFrame) {
            mc.gameMode.releaseUsingItem(mc.player);
            //ChatUtils.print("Stop wait 1 tick");
            return;
        }

        if (swapping) {
            //ChatUtils.print("Swap and attack so stop");
            return;
        }

        if (justReleasedItem) {
            justReleasedItem = false;
            return;
        }

        if (result != null && result.getEntity().equals(target)) {

            mc.hitResult = result;
            double cpsValue = cps.getValue().doubleValue();
            long delay = (long) (1000.0 / cpsValue);

            long currentTime = System.currentTimeMillis();
            //if (currentTime - lastAttackTime >= delay) {

            if (cps.getValue().intValue() != 0) {
                if (currentTime - lastAttackTime >= delay) {
                    if (AttributeSwap.swapping) return;

                    mc.gameMode.attack(mc.player, target);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    lastAttackTime = currentTime;
                    //ChatUtils.print("Attacked");
                }
            }
        }

    }

    @EventHandler
    public void onRenderWorld(RenderWorldEvent e) {
        if (target != null && highlight.getValue()) {
            RenderUtils.renderBox(target, e, e.partialTicks);
        }
    }

    private boolean isInAngle(Player target) {
        float playerYaw = mc.player.getYRot();
        float targetYaw = RotationUtils.getRotations(
                new float[]{playerYaw, mc.player.getXRot()},
                mc.player.getEyePosition(),
                target
        )[0];

        float diff = Mth.wrapDegrees(targetYaw - playerYaw);
        return Math.abs(diff) <= angle.getValue().floatValue() / 2F;
    }


}
