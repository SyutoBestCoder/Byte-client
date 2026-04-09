package com.syuto.bytes.module.impl.movement;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PacketSentEvent;
import com.syuto.bytes.eventbus.impl.PostMotionEvent;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.mixin.SendPacketMixinAccessor;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.module.impl.combat.Killaura;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.player.MovementUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.Objects;
import java.util.Random;
import java.util.function.BooleanSupplier;

import static com.syuto.bytes.Byte.mc;

@Deprecated
public class Speed extends Module {

    public ModeSetting modes = new ModeSetting(
            "Mode",
            this,
            "Vulcan",
            "Verus",
            "Grim",
            "Custom"
    );



    public NumberSetting speed = new NumberSetting(
            "sped",
            this,
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
        ticks++;
    }


    @EventHandler
    void onPreMotion(PreMotionEvent event) {
        boolean ground = mc.player.onGround();
        Vec3 motion = mc.player.getDeltaMovement();

        this.ground = !ground ? this.ground + 1 : 0;

        //mc.options.keyJump.setDown(false);


        switch (modes.getValue()) {
            case "Vulcan" -> {


                double my = MovementUtil.predictedMotion(motion.y, 5);
                //ChatUtils.print("Vulcan " + my);
                //  setMotY(0.0000001);


                jump();
                setSpeed(0.6);

                //mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

            }

            case "Verus" -> {

                if (MovementUtil.isMoving() && mc.player.onGround())

                    setMotY(0.1);

                switch(this.ground) {
                    case 1 -> {
                    }

                }
                setSpeed(0.8);
                if (Killaura.target != null && mc.options.keyJump.isDown()) {
                    //setStafe(Killaura.target);
                }
            }

            case "Custom" -> {
                jump();
                setSpeed(speed.getValue().doubleValue());
            }

            case "Grim" -> {

                BlockPos pos = mc.player.blockPosition();
                Block block = mc.level.getBlockState(pos).getBlock();

                if (block != null && block == Blocks.COBWEB) {
                    if (MovementUtil.isMoving()) {
                        MovementUtil.setSpeed(0.643d);
                    }
                }
            }
        }
    }


    @EventHandler
    public void onPostMotion(PostMotionEvent event) {
        Vec3 motion = mc.player.getDeltaMovement();
        switch (modes.getValue()) {
            case "Grim" -> {

            }
        }

    }



    @EventHandler
    public void onPacketSent(PacketSentEvent event) {
        if (event.getPacket() instanceof ServerboundMovePlayerPacket p) {

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

        Vec3 movementInput = new Vec3(dx, mc.player.getDeltaMovement().y, dz);

        mc.player.setDeltaMovement(movementInput);
    }


    private void jump() {
        if (mc.player.onGround() && MovementUtil.isMoving()) {
            mc.player.jumpFromGround();
        }
    }

    private void setY(double y) {
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(motion.x, motion.y - y, motion.z);
    }

    private void setMotY(double y) {
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(motion.x, y, motion.z);
    }


    private void setSpeed(double speed) {
        if (MovementUtil.isMoving()) {
            MovementUtil.setSpeed(speed);
        }
    }


}
