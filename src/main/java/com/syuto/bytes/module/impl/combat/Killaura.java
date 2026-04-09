package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.*;
import com.syuto.bytes.mixin.SendPacketMixinAccessor;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.module.impl.player.scaffold.Scaffold;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.player.MovementUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.render.AnimationUtils;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Killaura extends Module {

    private final ModeSetting targeting = new ModeSetting("Target Mode", this, "Single", "Switch");
    private final NumberSetting delay = new NumberSetting("Switch delay", this, 200, 0, 1000, 50);
    private final NumberSetting aps = new NumberSetting("APS", this, 10, 1, 20, 1);
    private final NumberSetting reach = new NumberSetting("Reach", this, 3, 1, 8, 0.5);
    private final NumberSetting swing = new NumberSetting("Swing range", this, 6, 1, 8, 0.5);
    private final ModeSetting autoBlock = new ModeSetting("Autoblock", this, "None", "Fake", "Vanilla");

    private long lastSwitchTime, attackDelay, lastAttackTime;
    private final List<Player> targets = new ArrayList<>();
    public static Player target;
    private float[] rotations, lastRotation;
    private boolean shouldAttack, firstBlock, blocking;
    private int targetIndex = 0, ticks;

    public Killaura() {
        super("KillAura", "Attacks people for you.", Category.COMBAT);
        this.setSuffix(targeting::getValue);
    }

    @Override
    public void onDisable() {

        if (this.blocking) {
            mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
            this.blocking = false;
            ChatUtils.print("Unblocked on post prolly this will ban at some point xD.");
        }
        this.target = null;
        this.ticks = 0;
        this.lastSwitchTime = 0;
        this.firstBlock = false;
        this.lastRotation = null;
        this.lastAttackTime = 0;
        AnimationUtils.setBlocking(false);
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        var hello = 0;

        if (autoBlock.getValue() != "None") {
           // ChatUtils.print("hi");
            AnimationUtils.setBlocking(target != null);
        }

        if (targeting.getValue().equals("Single")) {
            updateSingleTarget();
        } else {
            updateSwitchTarget();
        }

        if (target != null && rotations != null) {
            EntityHitResult result = (EntityHitResult) PlayerUtil.raycast(RotationUtils.getRotationYaw(), RotationUtils.getRotationPitch(), swing.getValue().doubleValue(), delta, false);
            ticks++;


            if (result != null && result.getEntity().equals(this.target)) {
                handleAutoBlock();
                executeAttack(result);
            }
        }

        if (blocking && target == null) {
            mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));;
            this.blocking = false;
            ChatUtils.print("Unblocked");
        }

    }

    @EventHandler
    public void onRotation(RotationEvent event) {
        lastRotation = new float[]{RotationUtils.getLastRotationYaw(), RotationUtils.getLastRotationPitch()};

        if (target != null) {
            rotations = RotationUtils.getRotations(
                    lastRotation,
                    mc.player.getEyePosition(),
                    target
            );

            float currentYaw = RotationUtils.getRotationYaw();
            float targetYaw = rotations[0];

            float yaws = targetYaw - currentYaw;
            yaws %= 360.0f;
            if (yaws > 180.0f) yaws-= 360.0f;
            if (yaws < -180.0f) yaws += 360.0f;

            rotations[0] = currentYaw + yaws;

            rotations = RotationUtils.getFixedRotation(rotations, lastRotation);

            if (canAttack(target)) {
                event.setYaw(rotations[0]);
                event.setPitch(rotations[1]);

                this.lastRotation = new float[]{RotationUtils.getLastRotationYaw(), RotationUtils.getLastRotationPitch()};
            }
        }
    }

    @EventHandler
    void onPacketSent(PacketSentEvent event) {
        if (event.getPacket() instanceof ServerboundPlayerActionPacket e) {
            if (target != null && e.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) {
                ChatUtils.print("C07 " + e.getAction() + " " + ticks);
                event.setCanceled(true);
            }
        }
    }


    private long animationStartTime;
    private int animationTime = 1000;

    @EventHandler
    public void onRenderTick(RenderTickEvent e) {
        if (target != null) {


            if (System.currentTimeMillis() - lastAttackTime >= attackDelay && canSwing(target)) {
                lastAttackTime = System.currentTimeMillis();
                updateAttackDelay();
                shouldAttack = true;
            }
        }



    }

    float easeIn(float t) {
        return 1 - (float)Math.pow(1 - t, 3);
    }

    @EventHandler
    public void onRenderWorld(RenderWorldEvent e) {
        if (target != null && canAttack(target)) {
            RenderUtils.renderBox(target, e, e.partialTicks);

            RenderUtils.drawLine(
                    e.matrixStack,
                    mc.player.getEyePosition(),
                    PlayerUtil.getClosestPoint(target),
                    Color.MAGENTA.getRGB()
            );
        }
    }

    private void updateSwitchTarget() {
        if (System.currentTimeMillis() - lastSwitchTime < delay.getValue().longValue()) return;
        lastSwitchTime = System.currentTimeMillis();

        targets.clear();
        targets.addAll(mc.level.players().stream()
                .filter(ent -> ent != mc.player && canAttack(ent))
                .sorted(Comparator.comparingDouble(Player::getHealth))
                .limit(4)
                .toList());

        if (!targets.isEmpty()) {
            targetIndex = (targetIndex + 1) % targets.size();
            target = targets.get(targetIndex);
        } else {
            target = null;
        }
    }

    private void updateSingleTarget() {
        target = mc.level.players().stream()
                .filter(this::canAttack)
                .min(Comparator.comparingDouble(Player::getHealth))
                .orElse(null);
    }

    private boolean canAttack(Player entity) {
        double distance = PlayerUtil.getBiblicallyAccurateDistanceToEntity(entity);
        return entity != mc.player && distance <= swing.getValue().doubleValue() && entity.isAlive();
    }

    private boolean canSwing(Player entity) {
        double distance = PlayerUtil.getBiblicallyAccurateDistanceToEntity(entity);
        Scaffold scaffold = ModuleManager.getModule(Scaffold.class);
        return (scaffold != null && !scaffold.isEnabled()) && entity != mc.player && distance <= reach.getValue().doubleValue() && entity.isAlive();
    }

    private void updateAttackDelay() {
        attackDelay = (long) (1000.0 / aps.getValue().longValue());
    }

    private void handleAutoBlock() {
        if (autoBlock.getValue().equals("Vanilla")) {
            //swapSlots();
            block(mc.player.getYRot(), mc.player.getXRot());
        }
    }

    private void executeAttack(EntityHitResult r) {
        if (canSwing(target) && mc.player.getAttackStrengthScale(0.5f) >= 1.0) { //shouldAttack mc.player.getAttackCooldownProgress(0.5f) >= 1.0

            handleAutoBlock();

            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
            shouldAttack = false;
        }
    }

    private void swapSlots() {
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot() % 7 + 2));
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
        blocking = false;
    }
    

    private void block(float yaw, float pitch) {
        mc.getConnection().send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, yaw, pitch));
        blocking = true;
    }

    private void unblock() {
        SendPacketMixinAccessor silent = (SendPacketMixinAccessor) mc.getConnection();
        silent.byte$getConnection().send(
                new ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket
                                .Action
                                .RELEASE_USE_ITEM,
                        BlockPos.ZERO,
                        Direction.DOWN
                )
        );
    }


}
