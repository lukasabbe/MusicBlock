package com.lukasabbe.musicblock;

import com.lukasabbe.musicblock.commands.CommandHandler;
import com.lukasabbe.musicblock.config.Config;
import com.lukasabbe.musicblock.game.GameHandler;
import com.lukasabbe.musicblock.music.MusicHandler;
import com.lukasabbe.musicblock.platform.PlatformHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class Musicblock implements ModInitializer {

    public static MinecraftServer server;

    @Override
    public void onInitialize() {
        CommandHandler.init();
        ServerLifecycleEvents.SERVER_STARTED.register(server ->{
            Musicblock.server = server;
            Config.loadConfig();
            PlatformHandler.init();
            GameHandler.init();
            MusicHandler.init();
        });
    }
}
