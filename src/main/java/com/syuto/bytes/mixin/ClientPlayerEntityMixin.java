package com.syuto.bytes.mixin;

import com.mojang.authlib.GameProfile;
import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.*;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.impl.movement.MovementFix;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.syuto.bytes.Byte.mc;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer {

    public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow protected abstract void sendIsSprintingIfNeeded();

    @Shadow protected abstract boolean isControlledCamera();

    @Shadow private double xLast;

    @Shadow private double yLast;

    @Shadow private double zLast;

    @Shadow private float yRotLast;

    @Shadow private float xRotLast;

    @Shadow public abstract boolean isShiftKeyDown();

    @Shadow private int positionReminder;

    @Shadow @Final public ClientPacketListener connection;

    @Shadow private boolean lastOnGround;

    @Shadow private boolean lastHorizontalCollision;

    @Shadow private boolean autoJumpEnabled;

    @Shadow @Final protected Minecraft minecraft;

    @Shadow public abstract boolean isUsingItem();

    @Shadow public abstract boolean isUnderWater();


    @Inject(
            at = @At(value = "HEAD"),
            method = "tick"
    )
    public void start(CallbackInfo ci) {
        RotationUtils.setCamYaw(mc.player.getYRot());
        RotationUtils.setCamPitch(mc.player.getXRot());

        RotationUtils.setLastRotationYaw(RotationUtils.getRotationYaw());
        RotationUtils.setLastRotationPitch(RotationUtils.getRotationPitch());

        RotationEvent rotationEvent = new RotationEvent(
                this.getYRot(),
                this.getXRot()
        );

        Byte.INSTANCE.eventBus.post(rotationEvent);

        RotationUtils.yawChanged = rotationEvent.getYaw() != this.getYRot();
        RotationUtils.pitchChanged = rotationEvent.getPitch() != this.getXRot();

        RotationUtils.setRotationYaw(rotationEvent.getYaw());
        RotationUtils.setRotationPitch(rotationEvent.getPitch());



        MovementFix test = ModuleManager.getModule(MovementFix.class);
        if (test != null && test.isEnabled()) {
            mc.player.setYRot(RotationUtils.getRotationYaw());
            mc.player.setXRot(RotationUtils.getRotationPitch());
        }
    }

    @Inject(
            at = @At(value = "TAIL"),
            method = "tick"
    )

    public void end(CallbackInfo ci) {
        MovementFix test = ModuleManager.getModule(MovementFix.class);
        if (test != null && test.isEnabled()) {
            mc.player.setYRot(RotationUtils.getCamYaw());
            mc.player.setXRot(RotationUtils.getCamPitch());
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V"), method = "tick")
    public void onPreUpdate(CallbackInfo ci) {

        Byte.INSTANCE.eventBus.post(new PreUpdateEvent());
    }

    /**
     * @author
     * @reason
     */

    @Overwrite
    private void sendPosition() {
        PreMotionEvent event = new PreMotionEvent(
                this.getX(),
                this.getBoundingBox().minY,
                this.getZ(),
                RotationUtils.getRotationYaw(),
                RotationUtils.getRotationPitch(),
                RotationUtils.getLastRotationYaw(),
                RotationUtils.getLastRotationPitch(),
                this.onGround(),
                this.isShiftKeyDown(),
                this.isSprinting(),
                this.horizontalCollision
        );

        this.sendIsSprintingIfNeeded();
        if (this.isControlledCamera()) {

            Byte.INSTANCE.eventBus.post(event);

            double d = event.posX - this.xLast;
            double e = event.posY - this.yLast;
            double f = event.posZ - this.zLast;
            double g = (double)(event.yaw - event.lastYaw);
            double h = (double)(event.pitch - event.lastPitch);
            ++this.positionReminder;
            boolean bl = Mth.lengthSquared(d, e, f) > Mth.square(2.0E-4) || this.positionReminder >= 20;
            boolean bl2 = g != 0.0 || h != 0.0;
            if (bl && bl2) {
                this.connection.send(new ServerboundMovePlayerPacket.PosRot(event.posX, event.posY, event.posZ, event.yaw, event.pitch, event.onGround, event.horizontalCollision));
            } else if (bl) {
                this.connection.send(new ServerboundMovePlayerPacket.Pos(event.posX, event.posY, event.posZ, event.onGround, event.horizontalCollision));
            } else if (bl2) {
                this.connection.send(new ServerboundMovePlayerPacket.Rot(event.yaw, event.pitch, event.onGround, event.horizontalCollision));
            } else if (this.lastOnGround != event.onGround ||  this.lastHorizontalCollision != event.horizontalCollision) {
                this.connection.send(new ServerboundMovePlayerPacket.StatusOnly(event.onGround, event.horizontalCollision));
            }

            if (bl) {
                this.xLast = event.posX;
                this.yLast = event.posY;
                this.zLast = event.posZ;
                this.positionReminder = 0;
            }

            if (bl2) {
                this.yRotLast = event.yaw;
                this.xRotLast = event.pitch;
            }

            this.lastOnGround = event.onGround;
            this.lastHorizontalCollision = event.horizontalCollision;
            this.autoJumpEnabled = (Boolean)this.minecraft.options.autoJump().get();
        }
        PostMotionEvent eventt = new PostMotionEvent();
        Byte.INSTANCE.eventBus.post(eventt);
    }


    /**
     * @author
     * @reason
     */



    @Inject(
            at = @At("HEAD"),
            method = "move"

    )
    private void moveHead(CallbackInfo ci) {
        Byte.INSTANCE.eventBus.post(new PlayerMoveEvent());
    }

}
