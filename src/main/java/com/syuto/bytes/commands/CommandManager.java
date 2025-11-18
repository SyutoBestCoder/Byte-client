package com.syuto.bytes.commands;

import com.syuto.bytes.commands.impl.Bind;
import com.syuto.bytes.commands.impl.Pathfind;
import com.syuto.bytes.commands.impl.Toggle;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.ChatEvent;
import com.syuto.bytes.utils.impl.client.ChatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {
    public List<Command> commands = new ArrayList<>();
    public String prefix = ".";

    public CommandManager() {
        registerAllCommands();
    }

    private void registerAllCommands() {
        commands.add(new Bind());
        commands.add(new Toggle());
        commands.add(new Pathfind());
    }


    @EventHandler
    public void onChat(ChatEvent event) throws Exception {
        String message = event.getMessage();
        if (!message.startsWith(prefix)) return;
        event.setCanceled(true);

        message = message.substring(prefix.length());

        if (message.split(" ").length == 0)
            return;

        String commandName = message.split(" ")[0];
        Command command = commands.stream()
                .filter(command1 -> command1.alliases.contains(commandName) || command1.name.equalsIgnoreCase(commandName))
                .findFirst()
                .orElse(null);

        if (command != null) {
            command.onCommand(Arrays.copyOfRange(message.split(" "), 1, message.split(" ").length), message);
            return;
        }

        ChatUtils.print("§4Unknown command.");
    }

    public List<Command> getCommands() {
        return commands;
    }
}
