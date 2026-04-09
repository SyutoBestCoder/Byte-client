package com.syuto.bytes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.AttackEntityEvent;
import com.syuto.bytes.eventbus.impl.SetSlotEvent;
import com.syuto.bytes.eventbus.impl.TickEvent;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.impl.combat.AimAssist;
import com.syuto.bytes.module.impl.player.FastPlace;
import com.syuto.bytes.utils.impl.client.ClientUtil;
import dev.blend.ThemeHandler;
import dev.blend.util.render.DrawUtil;
import net.minecraft.client.main.GameConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.syuto.bytes.Byte.mc;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    private int rightClickDelay;


    @Shadow
    @Nullable
    public HitResult hitResult;

    @Inject(at = @At("HEAD"), method = "tick")
    private void onTick(CallbackInfo ci) {
        TickEvent tick = new TickEvent();
        Byte.INSTANCE.eventBus.post(tick);
    }

    @Inject(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/client/Minecraft;)Lnet/minecraft/client/gui/Gui;",
                    shift = At.Shift.AFTER
            )
    )
    private void initializeNanoVG(GameConfig args, CallbackInfo ci) {
        DrawUtil.initialize();
        //RichPresenceUtil.init();
    }


    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isItemEnabled(Lnet/minecraft/world/flag/FeatureFlagSet;)Z"))
    private void onDoItemUseHand(CallbackInfo ci, @Local ItemStack itemStack) {
        final FastPlace fastPlace = ModuleManager.getModule(FastPlace.class);
        if (fastPlace != null && fastPlace.isEnabled()) {
            rightClickDelay = fastPlace.getItemUseCooldown(itemStack);
        }
    }


    @Inject(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MouseHandler;handleAccumulatedMovement()V"
            )
    )
    private void updateThemeHandler(boolean tick, CallbackInfo ci) {
        ThemeHandler.INSTANCE.update();
    }

    @Inject(
            method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"
            ),
            cancellable = true
    )
    private void onSetSelectedSlot(CallbackInfo ci) {
        int currentSlot = mc.player.getInventory().getSelectedSlot();

        SetSlotEvent event = new SetSlotEvent(currentSlot);
        Byte.INSTANCE.eventBus.post(event);

        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        if (event.getSlot() != currentSlot) {
            mc.player.getInventory().setSelectedSlot(event.getSlot());
            ci.cancel();
        }
    }

    @Inject(
            method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;startAttack()Z"
            ),
            cancellable = true
    )
    public void onA(CallbackInfo ci) {
        if (!AimAssist.allowBreaking) {
            ci.cancel();
        }
    }

    @Inject(
            method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"
            ),
            cancellable = true
    )
    public void onAs(CallbackInfo ci) {
        if (!AimAssist.allowBreaking) {
            ci.cancel();
        }
    }


    @Inject(
            method = "startAttack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"),
            cancellable = true
    )
    public void onAttt(CallbackInfoReturnable<Boolean> cir) {
        if (!AimAssist.allowBreaking) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

}
