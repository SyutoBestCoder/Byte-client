package com.syuto.bytes.commands;

import com.syuto.bytes.utils.impl.client.ChatUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public abstract class Command {
    protected final String name, description, syntax;
    protected final List<String> alliases;

    public Command(String name, String description, String syntax, String... alliases) {
        this.name = name;
        this.description = description;
        this.syntax = syntax;
        this.alliases = Arrays.asList(alliases);
    }

    protected void sendSyntaxHelpMessage() {
        ChatUtils.print("§bCommand usage: §7" + syntax);
    }

    public void onCommand(String[] args, String message) throws Exception {}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSyntax() {
        return syntax;
    }

    public List<String> getAlliases() {
        return alliases;
    }

    protected String buildStringFromArgs(String[] args, int start, boolean format) {
        StringBuilder builder = new StringBuilder();
        for (String string : Arrays.copyOfRange(args, start, args.length)) {
            builder.append(" ").append(string);
        }
        builder = new StringBuilder(builder.substring(1));

        if (format) {
            char[] chars = builder.toString().toCharArray();
            for (int i = 0; i < chars.length - 1; i++) {
                if (chars[i] == '&' && StringUtils.containsIgnoreCase("0123456789AaBbCcDdEeFfKkLlMmNnOoRr",
                        String.valueOf(chars[i + 1]))) {
                    chars[i] = '§';
                    chars[i + 1] = Character.toLowerCase(chars[i + 1]);
                }
            }
            return new String(chars);
        }

        return builder.toString();
    }

    protected String buildStringFromArgs(String[] args, boolean format) {
        return buildStringFromArgs(args, 0, format);
    }

    protected String buildStringFromArgs(String[] args, int start) {
        return buildStringFromArgs(args, start, false);
    }

    protected String buildStringFromArgs(String[] args) {
        return buildStringFromArgs(args, 0);
    }
}