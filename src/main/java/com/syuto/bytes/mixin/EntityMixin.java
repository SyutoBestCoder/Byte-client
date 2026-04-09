package com.syuto.bytes.mixin;


import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.impl.movement.MovementFix;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static com.syuto.bytes.Byte.mc;


@Mixin(Entity.class)
public abstract class EntityMixin {


    @Shadow public abstract float getYRot();

    @Shadow public abstract void setDeltaMovement(Vec3 velocity);

    @Shadow public abstract Vec3 getDeltaMovement();

    @ModifyArgs(
            method = "moveRelative",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private void mf(Args args) {
        MovementFix test = ModuleManager.getModule(MovementFix.class);
        if (test != null && test.isEnabled()) {
            float customYaw = RotationUtils.getRotationYaw();
            // args.set(2, customYaw);
        }
    }
}
