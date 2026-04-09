package com.syuto.bytes.mixin;

import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.AttackEntityEvent;
import com.syuto.bytes.module.impl.combat.AimAssist;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static com.syuto.bytes.Byte.mc;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {


    @Inject(
            method = "attack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void attackEntity(Player player, Entity target, CallbackInfo ci) {

        AttackEntityEvent event = new AttackEntityEvent(AttackEntityEvent.Mode.Pre, target);
        Byte.INSTANCE.eventBus.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }

    }

    @Inject(
            method = "attack",
            at = @At("TAIL")
    )
    private void attackEntityPost(Player player, Entity target, CallbackInfo ci) {
        AttackEntityEvent event = new AttackEntityEvent(AttackEntityEvent.Mode.Post, target);
        Byte.INSTANCE.eventBus.post(event);
    }


    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!AimAssist.allowBreaking) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void modifyInteractBlockPacket(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!AimAssist.allowBreaking) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void modifyInteractItemPacket(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!AimAssist.allowBreaking) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void modifyInteractPacket(Player player, Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!AimAssist.allowBreaking) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void modifyInteractAtPacket(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!AimAssist.allowBreaking) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }
}

