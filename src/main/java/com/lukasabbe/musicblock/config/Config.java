package com.lukasabbe.musicblock.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    public static Config CONFIG = new Config();
    private static final Gson GSON = new Gson();
    public ConfigPos platformPos = new ConfigPos(0, 0, 0);
    public ConfigPos spawnPos = new ConfigPos(0,0,0);
    public int platFormSize = 26;
    public long startDelayMs = 10000;
    public double decayFactor = 0.8d;
    public long minDelayMs = 1000;


    public static void loadConfig(){
        Path configPath = FabricLoader.getInstance().getConfigDir();
        Path configFilePath = configPath.resolve("music_block_config.json");
        if(!Files.exists(configFilePath)) createConfigFile(configFilePath);
        try{
            JsonReader reader = new JsonReader(new FileReader(configFilePath.toFile()));
            Config.CONFIG = GSON.fromJson(reader, Config.class);
        } catch (FileNotFoundException _) {}
    }

    public static void createConfigFile(Path configFilePath){
        try(FileWriter writer = new FileWriter(configFilePath.toFile())){
            GSON.toJson(Config.CONFIG, writer);
        } catch (IOException _) {}
    }
}
