package com.syuto.bytes.utils.impl.rotation;

import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Unique;

import static com.syuto.bytes.Byte.mc;

public class MixinUtils {

    public static float getLerpedPitch(float tickDelta, LivingEntity entity) {
        if(RotationUtils.pitchChanged) {
            return tickDelta == 1.0F ? RotationUtils.getRotationPitch() : Mth.lerp(tickDelta, RotationUtils.getLastRotationPitch(), RotationUtils.getRotationPitch());
        } else {
            return entity.getXRot(tickDelta);
        }
    }

    public static void turnHead(float yaw, LivingEntityRenderState state) {
        float f = Mth.wrapDegrees(yaw - state.bodyRot);
        state.bodyRot += f * 0.3f;

        float h = 80.0f;
        if (Math.abs(f) > h) {
            state.bodyRot += f - Math.copySign(h, f);
        }

        float headRotation = Mth.wrapDegrees(yaw - state.bodyRot);
        state.yRot = headRotation;

    }



}
