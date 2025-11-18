package com.syuto.bytes.utils.impl.client;

import net.minecraft.text.Text;

import static com.syuto.bytes.Byte.mc;

public class ChatUtils {

    public static void print(Object message) {
        if (mc.player != null && mc.world != null) {
            String m = "§7[" + "§3Byte§7" + "] §f"  + message;
            mc.player.sendMessage(Text.of(m),false);
            //mc.inGameHud.getChatHud().addMessage(Text.of(m));
        }
    }
}