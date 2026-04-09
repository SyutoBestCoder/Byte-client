package com.syuto.bytes.mixin;


import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.MouseInputEvent;
import com.syuto.bytes.utils.impl.keyboard.MouseUtil;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {

    @Inject(
            at = @At("HEAD"),
            method = "click",
            cancellable = true
    )
    private static void inputEvent(InputConstants.Key key, CallbackInfo ci) {
        if (key == MouseUtil.LEFT_CLICK || key == MouseUtil.RIGHT_CLICK) {
            int button = key == MouseUtil.LEFT_CLICK ? 0 : 1;
            MouseInputEvent event = new MouseInputEvent(button, false);
            Byte.INSTANCE.eventBus.post(event);

            if (event.isCanceled()) {
                ci.cancel();
            }
        }
    }
}
