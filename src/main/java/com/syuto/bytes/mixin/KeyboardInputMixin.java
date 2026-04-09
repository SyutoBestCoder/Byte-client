package com.syuto.bytes.mixin;

import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.impl.movement.MovementFix;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.syuto.bytes.Byte.mc;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends ClientInput {

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void tick(CallbackInfo ci) {
        MovementFix test = ModuleManager.getModule(MovementFix.class);
        if (test != null && test.isEnabled() && mc.player != null) {

            float fixRotation = RotationUtils.getCamYaw();

            float mF = mc.player.input.getMoveVector().y;
            float mS = mc.player.input.getMoveVector().x;

            float delta = (RotationUtils.getRotationYaw() - fixRotation) * Mth.DEG_TO_RAD;
            float cos = Mth.cos(delta);
            float sin = Mth.sin(delta);


            float s = Math.round(mS * cos + mF * sin);
            float f = Math.round(mF * cos - mS * sin);

            boolean forward = f > 0;
            boolean backward = f < 0;
            boolean left = s > 0;
            boolean right = s < 0;

            this.keyPresses = new Input(
                    forward,
                    backward,
                    left,
                    right,
                    this.keyPresses.jump(),
                    this.keyPresses.shift(),
                    this.keyPresses.sprint()
            );

            this.moveVector = (new Vec2(s, f)).normalized();


            //boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean sneak, boolean sprint

        }
    }

}
