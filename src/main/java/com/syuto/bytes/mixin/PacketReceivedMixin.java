package com.syuto.bytes.mixin;


import com.syuto.bytes.Byte;
import com.syuto.bytes.eventbus.impl.PacketReceivedEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Connection.class)
public class PacketReceivedMixin {

    @Shadow
    private static <T extends PacketListener> void genericsFtw(Packet<T> packet, PacketListener packetListener) {}


    @Shadow
    private PacketListener packetListener;

    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;isOpen()Z"),
            cancellable = true
    )
    private void onPacketRead(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        PacketReceivedEvent event = new PacketReceivedEvent(packet);
        Byte.INSTANCE.eventBus.post(event);

        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        Packet<?> newPacket = event.getPacket();
        PacketListener listener = this.packetListener;

        if (listener != null) {
            if (newPacket != packet) {
                ci.cancel();
                genericsFtw(newPacket, listener);
            }
        }
    }
}
