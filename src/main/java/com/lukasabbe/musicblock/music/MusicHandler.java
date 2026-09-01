package com.lukasabbe.musicblock.music;

import com.lukasabbe.musicblock.game.GameHandler;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MusicHandler {
    public final static List<Music> musicList = new ArrayList<>();
    private static final Random rnd = new Random();
    private static List<PausableAudioPlayer> audioPlayers = new ArrayList<>();
    public static void init(){
        Path gamePath = FabricLoader.getInstance().getGameDir();
        Path musicDir = gamePath.resolve("music/");
        if(!Files.exists(musicDir)) createMusicDir(musicDir);

        File dir = new File(musicDir.toUri());
        File[] files = dir.listFiles();
        if (files == null) return;
        for(File file : files){
            musicList.add(new Music(file));
        }
    }
    private static void createMusicDir(Path musicDir) {
        try{
            Files.createDirectory(musicDir);
        } catch (IOException _) {}
    }

    public static void playRandomSong(ServerLevel level){
        var music = musicList.get(rnd.nextInt(0, musicList.size()));
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
}
