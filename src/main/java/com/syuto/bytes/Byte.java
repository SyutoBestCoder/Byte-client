package com.syuto.bytes;

import com.syuto.bytes.commands.CommandManager;
import com.syuto.bytes.eventbus.EventBus;
import com.syuto.bytes.eventbus.Handlers;
import com.syuto.bytes.module.ModuleManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Byte implements ModInitializer {

	public static final String MOD_ID = "byte";
	public static final String NAME = "Byte", VERSION = "1.0.0";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public static Byte INSTANCE;
	public EventBus eventBus;
	public static MinecraftClient mc = MinecraftClient.getInstance();
	public CommandManager commandManager;
	public Handlers handlers;
    public final static String CHATGPT_API = "";

	public static final Identifier FEM_SOUND_ID = Identifier.of("byte", "music.fem");
	public static final SoundEvent FEM_SOUND_EVENT = SoundEvent.of(FEM_SOUND_ID);


	public Byte() {
		eventBus = new EventBus();
		commandManager = new CommandManager();
		handlers = new Handlers();

		Registry.register(Registries.SOUND_EVENT, FEM_SOUND_ID, FEM_SOUND_EVENT);
		registerEvents();
	}

	@Override
	public void onInitialize() {
        final long time = System.currentTimeMillis();
		ModuleManager.registerModules();
        //start the http server after modules have been initialized

        LOGGER.info("Initialized {} in {}ms.", NAME, System.currentTimeMillis() - time);
    }


	public void registerEvents() {
		eventBus.register(handlers);
		eventBus.register(commandManager);

		INSTANCE = this;
	}

}