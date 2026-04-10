package com.syuto.bytes.commands.impl;

import com.syuto.bytes.commands.Command;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.keyboard.KeyboardUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;


public class BindCommand extends Command {
    public BindCommand() {
        super("Bind", "Binds a module by name.", ".bind <module> <key> | <clear> | <list>", "b");
    }

    @Override
    public void onCommand(String[] args, String message) {
        if (args.length == 2) {
            Module module = ModuleManager.getModule(args[0]);
            String keyName = args[1].toUpperCase();
            if (module != null) {
                int keyCode = KeyboardUtil.stringToGlfwKey(keyName); //Keyboard.getKeyIndex(keyName)
                module.setKey(keyCode);
                ChatUtils.print("Bound " + module.getName() + " to " + keyName + ".");
            } else {
                ChatUtils.print("Module \"" + args[0] + "\" was not found.");
            }
            return;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("clear")) {
                ModuleManager.modules.forEach(m -> m.setKey(GLFW.GLFW_KEY_UNKNOWN));
                ChatUtils.print("Cleared all binds.");
            } else if (args[0].equalsIgnoreCase("list")) {
                final List<Module> boundModules = ModuleManager.modules.stream().filter(m -> m.getKey() != GLFW.GLFW_KEY_UNKNOWN).toList();
                if (!boundModules.isEmpty()) {
                    ChatUtils.print("Current binds: ");
                    for (Module module : boundModules) {
                        ChatUtils.print(module.getName() + ": " + module.getKey());
                    }
                } else {
                    ChatUtils.print("No modules are currently bound.");
                }
            }
            return;
        }

        sendSyntaxHelpMessage();
    }

}
