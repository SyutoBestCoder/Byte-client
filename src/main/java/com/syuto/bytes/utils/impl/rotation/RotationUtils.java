package com.syuto.bytes.utils.impl.rotation;

import com.syuto.bytes.Byte;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.player.MovementUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static com.syuto.bytes.Byte.mc;

public class RotationUtils {


    public static boolean yawChanged;
    public static boolean pitchChanged;

    @Setter
    @Getter
    public static float rotationYaw, rotationPitch, lastRotationYaw, lastRotationPitch, camYaw, camPitch;


    public static float[] getFixedRotation(float[] rotations, float[] lastRotations) {
        double gcd = gcd();

        double deltaYaw = rotations[0] - lastRotations[0];
        double deltaPitch = rotations[1] -  lastRotations[1];

        double y = Math.round(deltaYaw / gcd) * gcd;
        double p = Math.round(deltaPitch / gcd) * gcd;

        double fixedYaw = lastRotations[0] + y;
        double fixedPitch = lastRotations[1] + p;

        return new float[]{(float) fixedYaw, (float) MathHelper.clamp(fixedPitch, -90f, 90f)};
    }


    public static double gcd() {
        double d = mc.options.getMouseSensitivity().getValue() * 0.6000000238418579 + 0.20000000298023224;
        return d * d * d * 1.2;
    }


    public static float[] getRotations(float[] last, Vec3d eye, Entity entity) {
        Vec3d to = PlayerUtil.getClosestPoint(entity);
        Vec3d diff = to.subtract(eye);

        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float pitch = (float) Math.toDegrees(-Math.atan2(diff.y , dist));
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;

        yaw = unwrap(last[0], yaw);

        return new float[]{yaw, pitch};
    }

    private static float unwrap(float oldYaw, float currentYaw) {
        float diff = currentYaw - (oldYaw % 360);

        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;

        return oldYaw + diff;
    }


    public static float[] getRotationsToBlock(Vec3d eye, BlockPos blockPos, Direction face) {
        Vec3d target = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
        target = target.add(new Vec3d(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5));
        double diffX = target.x - eye.x;
        double diffY = target.y - eye.y;
        double diffZ = target.z - eye.z;

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);
        pitch = (float) ((pitch * 180F) / Math.PI + 90) * -1;
        yaw = (float) ((yaw * 180) / Math.PI) - 90;

        return new float[]{yaw, clampPitch(pitch)};
    }


    public static float[] getBlockRotations(BlockPos blockPos, Direction facing) {
        Vec3d direction = blockPos.toCenterPos().add(Vec3d.of(facing.getVector()).multiply(0.5).subtract(mc.player.getEyePos()));

        float yaw = (float) Math.toDegrees(
                Math.atan2(
                        -direction.x,
                        direction.z
                )
        );

        float pitch = (float) Math.toDegrees(
                Math.atan2(
                        -direction.y,
                        Math.hypot(
                                direction.x,
                                direction.z
                        )
                )
        );


        yaw = unwrap(RotationUtils.getLastRotationYaw(),yaw);

        return new float[]{yaw, clampPitch(pitch)};
    }

    public static float[] getBlockRotations(BlockPos blockPos, Vec3d hitVec, Direction facing) {
        Vec3d direction = hitVec.subtract(mc.player.getEyePos());

        float yaw = (float) Math.toDegrees(
                Math.atan2(
                        -direction.x,
                        direction.z
                )
        );

        float pitch = (float) Math.toDegrees(
                Math.atan2(
                        -direction.y,
                        Math.hypot(direction.x, direction.z)
                )
        );

        return new float[]{yaw, clampPitch(pitch)};
    }


    private static float clampPitch(float pitch) {
        return MathHelper.clamp(pitch, -90.0F, 90.0F);
    }

    public static void turnHead(float yaw) {
        float f = MathHelper.wrapDegrees(yaw - mc.player.bodyYaw);
        //mc.player.bodyYaw += f * 0.3F;
        float h = 85.0f;
        if (Math.abs(f) > h) {
            //mc.player.bodyYaw += f - (float) MathHelper.sign(f) * h;
        }
        mc.player.bodyYaw = yaw;
        mc.player.headYaw = yaw;
    }
}
