package com.syuto.bytes.module.impl.movement;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PostMotionEvent;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.module.impl.combat.Killaura;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.player.MovementUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Random;
import java.util.function.BooleanSupplier;

import static com.syuto.bytes.Byte.mc;

public class Speed extends Module {

    public ModeSetting modes = new ModeSetting(
            "Mode",
            this,
            "Watchdog",
            "Verus",
            "Grim",
            "Custom"
    );

    private BooleanSupplier view = () -> modes.getValue().equals("Custom");


    public NumberSetting speed = new NumberSetting(
            "sped",
            this,
            view,
            1,
            0,
            8,
            0.01
    );

    public Speed() {
        super("Speed", "Zoom", Category.MOVEMENT);
        setSuffix(() -> modes.getValue());
    }


    private int ground = 0, ticks = 0;


    @Override
    public void onEnable() {
        super.onEnable();

    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        view = () -> modes.getValue().equals("Custom");
    }


    @EventHandler
    void onPreMotion(PreMotionEvent event) {
        boolean ground = event.onGround;
        Vec3d motion = mc.player.getVelocity();

        this.ground = !ground ? this.ground + 1 : 0;

        //mc.options.jumpKey.setPressed(false);


        switch (modes.getValue()) {
            case "Watchdog" -> {
                jump();
                setSpeed(0.5f);

                if (Killaura.target != null && mc.options.jumpKey.isPressed()) {
                    setStafe(Killaura.target);
                }
            }
            case "Verus" -> {

                jump();

                switch(this.ground) {
                    case 1 -> {
                        setMotY(0.2);
                    }

                    case 2 -> {
                        if (!mc.player.horizontalCollision) {;
                            mc.player.setVelocity(motion.x * 1.45, motion.y, motion.z * 1.45);
                        }
                    }
                }
                setSpeed(0.6);
                if (Killaura.target != null && mc.options.jumpKey.isPressed()) {
                    //setStafe(Killaura.target);
                }
            }

            case "Custom" -> {
                if (!mc.player.isOnGround()) {
                    ticks++;
                    if (ticks == 1) {
                        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                        ChatUtils.print("1");
                    } else if (ticks == 2) {
                        Vec3d pos = mc.player.getPos();
                        double forward = 0.03;
                        float yaw = MovementUtil.direction();
                        double dx = -Math.sin(yaw) * forward;
                        double dz = Math.cos(yaw) * forward;
                        mc.player.setPosition(pos.x + dx, pos.y , pos.z + dz) ;
                        ChatUtils.print("2");
                    } else if (ticks >= 11) {
                        mc.player.setJumping(true);
                        mc.player.jump();
                        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                        ChatUtils.print("jump");
                        ticks = 0;
                    }
                }
            }

            case "Grim" -> {
                if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) return;
                ticks++;

                if (mc.player.isOnGround() && MovementUtil.isMoving()) {
                    mc.options.jumpKey.setPressed(true);
                    mc.player.jump();
                } else {
                    mc.options.jumpKey.setPressed(false);
                }



                if (!mc.player.isOnGround() && this.ground < 1) {
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }

               //. ChatUtils.print("ticks: " + this.ground);

            }

        }
    }


    @EventHandler
    public void onPostMotion(PostMotionEvent event) {
        switch (modes.getValue()) {
            case "Grim" -> {
                if (mc.player.isOnGround()) {
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
                if (!mc.player.isOnGround() && ground > 1) {
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
                mc.player.setSneaking(false);
            }
        }

    }

    void setStafe(Entity target) {
        double centerX = target.getX();
        double centerZ = target.getZ();
        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();

        double angle = Math.atan2(playerZ - centerZ, playerX - centerX) + Math.toRadians(45);

        double targetX = centerX + 1.5 * Math.cos(angle);
        double targetZ = centerZ + 1.5 * Math.sin(angle);

        double dx = targetX - playerX;
        double dz = targetZ - playerZ;

        double length = Math.sqrt(dx * dx + dz * dz);

        dx /= length;
        dz /= length;

        Vec3d movementInput = new Vec3d(dx, mc.player.getVelocity().y, dz);

        mc.player.setVelocity(movementInput);
    }


    private void jump() {
        if (mc.player.isOnGround() && MovementUtil.isMoving()) {
            mc.player.jump();
        }
    }

    private void setY(double y) {
        Vec3d motion = mc.player.getVelocity();
        mc.player.setVelocity(motion.x, motion.y - y, motion.z);
    }

    private void setMotY(double y) {
        Vec3d motion = mc.player.getVelocity();
        mc.player.setVelocity(motion.x, y, motion.z);
    }


    private void setSpeed(double speed) {
        if (MovementUtil.isMoving()) {
            MovementUtil.setSpeed(speed);
        }
    }


}
