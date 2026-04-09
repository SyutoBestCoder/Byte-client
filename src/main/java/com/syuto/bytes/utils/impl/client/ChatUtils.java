package com.syuto.bytes.utils.impl.client;

import static com.syuto.bytes.Byte.mc;

import net.minecraft.network.chat.Component;

public class ChatUtils {

    public static void print(Object message) {
        if (mc.player != null && mc.level != null) {
            String m = "§f"  + message;
            mc.player.displayClientMessage(Component.nullToEmpty(m),false);
            //mc.inGameHud.getChatHud().addMessage(Text.of(m));
        }
    }
}