package com.syuto.bytes.mixin;


import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.PlayerDamageEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.syuto.bytes.Byte.mc;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {


    @Inject(
            method = "handleDamageEvent",
            at = @At(
                value = "HEAD"
            )
    )
    public void damageEvent(ClientboundDamageEventPacket clientboundDamageEventPacket, CallbackInfo ci) {
        PlayerDamageEvent event = new PlayerDamageEvent(clientboundDamageEventPacket.entityId());
        Byte.INSTANCE.eventBus.post(event);
    }
}
