package com.syuto.bytes.mixin;

import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.impl.render.RenderingTest;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.syuto.bytes.Byte.mc;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void tick(CallbackInfo ci) {
        RenderingTest test = ModuleManager.getModule(RenderingTest.class);
        if (test != null && test.isEnabled() && mc.player != null) {

            float fixRotation = RotationUtils.getCamYaw();

            float mF = mc.player.input.getMovementInput().y;
            float mS = mc.player.input.getMovementInput().x;

            float delta = (RotationUtils.getRotationYaw() - fixRotation) * MathHelper.RADIANS_PER_DEGREE;
            float cos = MathHelper.cos(delta);
            float sin = MathHelper.sin(delta);
            // why mojang
            this.movementSideways = Math.round(mS * cos + mF * sin);
            this.movementForward = Math.round(mF * cos - mS * sin);


            //boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean sneak, boolean sprint

        }
    }

}
