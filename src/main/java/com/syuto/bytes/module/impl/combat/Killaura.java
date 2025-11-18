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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.util.*;
import java.util.List;


public class Killaura extends Module {

    private final ModeSetting targeting = new ModeSetting("Target Mode", this, "Single", "Switch");
    private final NumberSetting delay = new NumberSetting("Switch delay", this, 200, 0, 1000, 50);
    private final NumberSetting aps = new NumberSetting("APS", this, 10, 1, 20, 1);
    private final NumberSetting reach = new NumberSetting("Reach", this, 6, 1, 8, 0.5);
    private final NumberSetting swing = new NumberSetting("Swing range", this, 6, 1, 8, 0.5);
    private final ModeSetting autoBlock = new ModeSetting("Autoblock", this, "None", "Fake", "Vanilla");

    private long lastSwitchTime, attackDelay, lastAttackTime;
    private final List<PlayerEntity> targets = new ArrayList<>();
    public static PlayerEntity target;
    private float[] rotations, lastRotation;
    private boolean shouldAttack, firstBlock, blocking;
    private int targetIndex = 0, ticks;

    public Killaura() {
        super("Killaura", "Attacks people for you.", Category.COMBAT);
        this.setSuffix(targeting::getValue);
    }

    @Override
    public void onDisable() {

        if (this.blocking) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
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


            handleAutoBlock();
            executeAttack(null);

            /*if (result != null && result.getEntity().equals(this.target)) {
                handleAutoBlock();
                executeAttack(result);
            }*/
        }

        if (blocking && target == null) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));;
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
                    mc.player.getEyePos(),
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

            if (canSwing(target)) {
                event.setYaw(rotations[0]);
                event.setPitch(rotations[1]);

                this.lastRotation = new float[]{RotationUtils.getLastRotationYaw(), RotationUtils.getLastRotationPitch()};
            }
        }
    }

    @EventHandler
    void onPacketSent(PacketSentEvent event) {
        if (event.getPacket() instanceof PlayerActionC2SPacket e) {
            if (target != null && e.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) {
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

            DrawContext context = e.context;

            MatrixStack matrixStack = context.getMatrices();

            float height = mc.getWindow().getScaledHeight();
            float width = mc.getWindow().getScaledWidth();

            float left = width / 2 + 50;
            float right = left + 160;
            float top = height / 2 + 50;
            float bottom = top + 40;

            float currentHealth = target.getMaxHealth() + target.getAbsorptionAmount();
            float maxHealth = target.getHealth() / currentHealth;
            float health = target.getHealth() + target.getAbsorptionAmount();

            Color healthColor = maxHealth < 0.3D ? Color.RED : (maxHealth < 0.5D ? Color.orange : (maxHealth < 0.7D ? Color.yellow : Color.green));

            String name = target.getName().getString();

            float hbX = width / 2 + 55;
            float hbeX = hbX + (150 * maxHealth);

            float hbY = top + 30;
            float hbeY = hbY + 5;
            float g = hbX + 150;

            float gg = hbX + 140;

            RenderUtils.drawRect(
                    matrixStack,
                    left,
                    right,
                    top,
                    bottom,
                    new Color(0,0,0, 75).getRGB()
            );

            RenderUtils.drawRect(
                    matrixStack,
                    hbX,
                    g,
                    hbY,
                    hbeY,
                    healthColor.darker().getRGB()
            );

            RenderUtils.drawRect(
                    matrixStack,
                    hbX,
                    hbeX,
                    hbY,
                    hbeY,
                    healthColor.getRGB()
            );

            RenderUtils.drawRectOutline(
                    matrixStack,
                    left,
                    right,
                    top,
                    bottom,
                    healthColor.getRGB()
            );

            float y = ((top + bottom) - mc.textRenderer.fontHeight) / 2;

            RenderUtils.drawText(
                    context,
                    name,
                    hbX,
                    y,
                    Color.WHITE.getRGB()
            );

            String text = String.format("%.1f", health) + " HP";
            RenderUtils.drawText(
                    context,
                    text,
                    gg - mc.textRenderer.getWidth(text),
                    y,
                    Color.WHITE.getRGB()
            );

            float hh = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            String xx = hh > health ? "W" : "L";
            Color xxg = hh > health ? Color.GREEN : Color.RED;
            RenderUtils.drawText(
                    context,
                    xx,
                    g - mc.textRenderer.getWidth(xx),
                    y,
                    xxg.getRGB()
            );

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
                    mc.player.getEyePos(),
                    PlayerUtil.getClosestPoint(target),
                    Color.MAGENTA.getRGB()
            );
        }
    }

    private void updateSwitchTarget() {
        if (System.currentTimeMillis() - lastSwitchTime < delay.getValue().longValue()) return;
        lastSwitchTime = System.currentTimeMillis();

        targets.clear();
        targets.addAll(mc.world.getPlayers().stream()
                .filter(ent -> ent != mc.player && canAttack(ent))
                .sorted(Comparator.comparingDouble(PlayerEntity::getHealth))
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
        target = mc.world.getPlayers().stream()
                .filter(this::canAttack)
                .min(Comparator.comparingDouble(PlayerEntity::getHealth))
                .orElse(null);
    }

    private boolean canAttack(PlayerEntity entity) {
        double distance = PlayerUtil.getBiblicallyAccurateDistanceToEntity(entity);
        return entity != mc.player && distance <= swing.getValue().doubleValue() && entity.isAlive();
    }

    private boolean canSwing(PlayerEntity entity) {
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
            block(mc.player.getYaw(), mc.player.getPitch());
        }
    }

    private void executeAttack(EntityHitResult r) {
        if (canSwing(target) && mc.player.getAttackCooldownProgress(0.5f) >= 1.0) { //shouldAttack mc.player.getAttackCooldownProgress(0.5f) >= 1.0

            handleAutoBlock();

            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            shouldAttack = false;
        }
    }

    private void swapSlots() {
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 7 + 2));
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        blocking = false;
    }
    

    private void block(float yaw, float pitch) {
        mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));
        blocking = true;
    }

    private void unblock() {
        SendPacketMixinAccessor silent = (SendPacketMixinAccessor) mc.getNetworkHandler();
        silent.getConnection().send(
                new PlayerActionC2SPacket(
                        PlayerActionC2SPacket
                                .Action
                                .RELEASE_USE_ITEM,
                        BlockPos.ORIGIN,
                        Direction.DOWN
                )
        );
    }


}
