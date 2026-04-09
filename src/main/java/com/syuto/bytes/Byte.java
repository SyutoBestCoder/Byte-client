package com.syuto.bytes;

import com.syuto.bytes.commands.CommandManager;
import com.syuto.bytes.eventbus.EventBus;
import com.syuto.bytes.eventbus.Handlers;
import com.syuto.bytes.module.ModuleManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Byte implements ModInitializer {

	public static final String MOD_ID = "byte";
	public static final String NAME = "Byte", VERSION = "1.0.0";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public static Byte INSTANCE;
	public EventBus eventBus;
	public static Minecraft mc = Minecraft.getInstance();
	public CommandManager commandManager;
	public Handlers handlers;

	public Byte() {
		eventBus = new EventBus();
		commandManager = new CommandManager();
		handlers = new Handlers();

		registerEvents();
	}

	@Override
	public void onInitialize() {
        final long time = System.currentTimeMillis();
		ModuleManager.registerModules();

        LOGGER.info("Initialized {} in {}ms.", NAME, System.currentTimeMillis() - time);
    }


	public void registerEvents() {
		eventBus.register(handlers);
		eventBus.register(commandManager);

		INSTANCE = this;
	}

}