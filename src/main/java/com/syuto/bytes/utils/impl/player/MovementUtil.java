package com.syuto.bytes.utils.impl.player;

import com.syuto.bytes.utils.impl.client.ChatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.phys.Vec3;

import static com.syuto.bytes.Byte.mc;

public class MovementUtil {
    public static void setSpeed(double speed) {
        double dir = direction();
        mc.player.setDeltaMovement(-Math.sin(dir) * speed, mc.player.getDeltaMovement().y, Math.cos(dir) * speed);
    }

    public static void setMotionY(double d) {
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(new Vec3(motion.x, d, motion.z));
    }

    public static float direction() {
        float rotationYaw = mc.player.getYRot();
        if (mc.player.zza < 0) {
            rotationYaw += 180;
        }

        float forward = 1;

        if (mc.player.zza  < 0) {
            forward = -0.5F;
        } else if (mc.player.zza  > 0) {
            forward = 0.5F;
        }

        if (mc.player.xxa > 0) {
            rotationYaw -= 90 * forward;
        }
        if (mc.player.xxa < 0) {
            rotationYaw += 90 * forward;
        }
        return (float) Math.toRadians(rotationYaw);
    }

    public static float[] move(float yaw) {
        float radians = (float) Math.toRadians(yaw);
        float forward = (float) -Math.cos(radians);
        float sideways = (float) Math.sin(radians);

        return new float[]{forward, sideways};
    }


    public static float directionAtan() {
        return (float) Math.toDegrees(Math.atan2(-mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().z));
    }

    public static double predictedMotion(final double motion, final int ticks) {
        if (ticks == 0) return motion;
        double predicted = motion;
        final double gravity = 0.08;

        for (int i = 0; i < ticks; i++) {
            predicted = (predicted - gravity) * 0.98f;
        }

        return predicted;
    }

    public static boolean isMoving() {
        return mc.player.zza > 0 || mc.player.zza < 0 || mc.player.xxa > 0 || mc.player.xxa < 0;
    }
}
