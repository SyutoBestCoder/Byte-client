package com.syuto.bytes.commands.impl;

import com.syuto.bytes.commands.Command;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import net.minecraft.ChatFormatting;

public class Toggle extends Command {
    public Toggle() {
        super("toggle", "Toggles a module.", ".toggle <module>", "t");
    }

    @Override
    public void onCommand(String[] args, String message) {
        if (args.length > 0) {
            String modName = args[0];
            Module m = ModuleManager.getModule(modName);
            if (m != null) {
                m.toggle();
                ChatUtils.print((m.isEnabled() ? ChatFormatting.GREEN + "Enabled " : ChatFormatting.RED + "Disabled ") + m.getName());
            } else {
                ChatUtils.print("Module name " +  ChatFormatting.RED  + modName + ChatFormatting.RED + " is invalid");
            }
        } else {
            ChatUtils.print("§bCorrect usage:§7 " + getSyntax());
        }
    }
}
