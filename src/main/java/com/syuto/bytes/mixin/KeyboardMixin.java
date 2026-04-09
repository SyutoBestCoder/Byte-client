package com.syuto.bytes.mixin;

import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.KeyEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Inject(
            method = "keyPress",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onKey(long window, int action, net.minecraft.client.input.KeyEvent input, CallbackInfo ci) {
        if (
                Minecraft.getInstance().getWindow().handle() == window
                && Minecraft.getInstance().player != null
                && Minecraft.getInstance().screen == null
        ) {
            final KeyEvent event = new KeyEvent(input.key(), input.scancode(), action, input.modifiers());
            Byte.INSTANCE.eventBus.post(event);
        }
    }

}
