package com.syuto.bytes.mixin;

import com.syuto.bytes.utils.impl.player.PlayerUtil;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.syuto.bytes.Byte.mc;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(at = @At("HEAD"), method = "jumpFromGround")
    private void jump(CallbackInfo ci) {
        if((Object) this == mc.player) {
            PlayerUtil.jumpAge = mc.player.tickCount;
            PlayerUtil.lastModTime = System.currentTimeMillis();
        }
    }
    @Inject(at = @At("HEAD"), method = "handleDamageEvent")
    private void onDamage(CallbackInfo info) {
        if((Object) this == mc.player) {
            PlayerUtil.hurtAge = mc.player.tickCount;
        }
    }
}
