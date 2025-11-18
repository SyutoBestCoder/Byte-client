package com.syuto.bytes.mixin;

import com.syuto.bytes.Byte;
import com.syuto.bytes.utils.impl.render.Snow;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.syuto.bytes.Byte.mc;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Unique
    Identifier imageIdentifier = Identifier.of("byte", "background/back.png");

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V"
            )
    )
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int windowWidth = mc.getWindow().getScaledWidth();
        int windowHeight = mc.getWindow().getScaledHeight();

        drawImage(context, 0, 0, windowWidth, windowHeight);
        //Byte.LOGGE R.info("w {}, h {}", windowWidth, windowHeight);
    }

    @Unique
    private void drawImage(DrawContext context, int x, int y, int width, int height) {
        context.drawTexture(
                RenderLayer::getGuiTextured,
                imageIdentifier,
                x, y,
                0, 0,
                width, height,
                width, height
        );
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
                    shift = At.Shift.AFTER
            )
    )
    public void renderr(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Snow.renderSnowflakes(context);
    }
}
