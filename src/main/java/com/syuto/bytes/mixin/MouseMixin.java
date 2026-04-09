package com.syuto.bytes.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.MouseEvent;
import com.syuto.bytes.eventbus.impl.MouseInputEvent;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static com.syuto.bytes.Byte.mc;

@Mixin(MouseHandler.class)
public class MouseMixin {


    @Inject(
            method = "onButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void onMouseClickPre(long l, MouseButtonInfo mouseButtonInfo, int i, CallbackInfo ci) {

    }


    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;

    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
    private void onUpdateMouse(CallbackInfo ci) {
        MouseEvent event = new MouseEvent(this.accumulatedDX, this.accumulatedDY);
        if (mc.player != null) {
            Byte.INSTANCE.eventBus.post(event);
        }
        this.accumulatedDX = event.getCursorDeltaX();
        this.accumulatedDY = event.getCursorDeltaY();
    }

    /*@Inject(
            method = "onMove",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MouseHandler;accumulatedDY:D",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void onMoveInject(long window, double x, double y, CallbackInfo ci) {
        // runs right after accumulatedDX is updated
    }*/

}

