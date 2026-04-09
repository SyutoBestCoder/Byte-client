package com.syuto.bytes.mixin;

import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.WorldJoinEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.syuto.bytes.Byte.mc;

@Mixin(ClientPacketListener.class)
public class ClientPlayerNetworkHandlerMixin {

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void onWorldJoin(ClientboundLoginPacket packet, CallbackInfo info) {

        if (mc.level != null) {
            WorldJoinEvent event = new WorldJoinEvent(packet.playerId());
            Byte.INSTANCE.eventBus.post(event);
        }
    }
}
