package com.syuto.bytes.mixin;

import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.RenderEntityEvent;
import com.syuto.bytes.utils.impl.rotation.MixinUtils;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.syuto.bytes.Byte.mc;


@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<
        T extends LivingEntity,
        S extends LivingEntityRenderState> {


    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;isEntityUpsideDown(Lnet/minecraft/world/entity/LivingEntity;)Z",
                    shift = At.Shift.AFTER
            )
    )
    public void updateRenderState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
        if (livingEntity == mc.player) {
            RenderEntityEvent event = new RenderEntityEvent();

            Byte.INSTANCE.eventBus.post(event);


            if (RotationUtils.yawChanged) {
                float g = Mth.rotLerp(
                        f,
                        RotationUtils.getLastRotationYaw(),
                        RotationUtils.getRotationYaw()
                );

                MixinUtils.turnHead(g, livingEntityRenderState);

            }

            livingEntityRenderState.xRot = MixinUtils.getLerpedPitch(f, livingEntity);
        }
    }

}
