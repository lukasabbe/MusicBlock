package com.lukasabbe.musicblock.music;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.lukasabbe.musicblock.game.GameHandler;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MusicHandler {
    public final static List<Music> musicList = new ArrayList<>();
    private static List<PausableAudioPlayer> audioPlayers = new ArrayList<>();
    private static final Gson GSON = new Gson();

    private static List<Vote> votes = new ArrayList<>();
    private static Music nextSong;

    public static void init(){
        MetaData.init();
        Path gamePath = FabricLoader.getInstance().getGameDir();
        Path musicDir = gamePath.resolve("music/");
        Path metaData = gamePath.resolve("music/meta_data.json");
        if(!Files.exists(musicDir)) createMusicDir(musicDir);
        if(!Files.exists(metaData)) createMusicMetaDataFile(metaData);
        loadMusicMetaData(metaData);
        File dir = new File(musicDir.toUri());
        File[] files = dir.listFiles();
        if (files == null) return;
        for(File file : files){
            if(file.getName().endsWith(".json")) continue;
            musicList.add(new Music(file));
        }
    }
    private static void createMusicDir(Path musicDir) {
        try{
            Files.createDirectory(musicDir);
        } catch (IOException _) {}
    }
    private static void createMusicMetaDataFile(Path musicMetaData){
        try(FileWriter writer = new FileWriter(musicMetaData.toFile())){
            GSON.toJson(MetaData.data, writer);
        } catch (IOException _) {}
    }
    public static void loadMusicMetaData(Path musicMetaData){
        try{
            JsonReader reader = new JsonReader(new FileReader(musicMetaData.toFile()));
            MetaData.data = GSON.fromJson(reader, MetaData.class);
        } catch (FileNotFoundException _) {}
    }


    public static void playRandomSong(ServerLevel level){
        var music = nextSong;
        UUID streamId = UUID.randomUUID();
        for (var player : GameHandler.players){
            VoicechatConnection connection = VoiceChatImp.serverApi.getConnectionOf(player.playerUUID);
            if(connection == null) continue;
            if(!connection.isConnected()) continue;
            de.maxhenkel.voicechat.api.ServerLevel voiceChatLevel = VoiceChatImp.serverApi.fromServerLevel(level);
            StaticAudioChannel channel = VoiceChatImp.serverApi.createStaticAudioChannel(streamId, voiceChatLevel, connection);
            if(channel == null) continue;

            channel.setCategory(VoiceChatImp.music.getId());
            PausableAudioPlayer p = new PausableAudioPlayer(channel, VoiceChatImp.serverApi.createEncoder(), music.musicSample);
            p.play();
            audioPlayers.add(p);
        }
    }

    public static void stopMusic(){
        for(var audioPlayer : audioPlayers){
            audioPlayer.stop();
        }
        audioPlayers.clear();
    }

    public static void pause(){
        for(var audioPlayer : audioPlayers){
            audioPlayer.pause();
        }
    }
    public static void play(){
        for(var audioPlayer : audioPlayers){
            audioPlayer.play();
        }
    }

    public static Music getNextSong(Level level){
        if(votes.isEmpty()){
            nextSong = musicList.get(level.getRandom().nextInt(0, musicList.size()));
            return nextSong;
        }
        String musicName = votes.stream()
                .map(Vote::musicName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        votes.clear();
        nextSong = musicList.stream().filter(p -> p.musicName.equals(musicName)).findFirst().get();
        return nextSong;
    }

    public static void vote(UUID player, String musicName){
        votes.removeIf(p -> p.player().equals(player));
        votes.add(new Vote(player, musicName));
    }
}
