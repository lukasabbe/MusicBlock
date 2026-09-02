package com.lukasabbe.musicblock.leaderboard;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LeaderBoard {

    public List<LeaderBoardUser> users = new ArrayList<>();
    public static LeaderBoard LEADER_BOARD = new LeaderBoard();
    private static final Gson GSON = new Gson();

    public static void loadLeaderBoard(){
        Path configPath = FabricLoader.getInstance().getConfigDir();
        Path leaderBoardFilePath = configPath.resolve("music_leaderboard.json");
        if(!Files.exists(leaderBoardFilePath)) createLeaderBoardFile(leaderBoardFilePath);
        try{
            JsonReader reader = new JsonReader(new FileReader(leaderBoardFilePath.toFile()));
            LeaderBoard.LEADER_BOARD = GSON.fromJson(reader, LeaderBoard.class);
        } catch (FileNotFoundException _) {}
    }

    public static void saveLeaderBoard(){
        Path configPath = FabricLoader.getInstance().getConfigDir();
        Path leaderBoardFilePath = configPath.resolve("music_leaderboard.json");
        if(!Files.exists(leaderBoardFilePath)) createLeaderBoardFile(leaderBoardFilePath);
        try(FileWriter writer = new FileWriter(leaderBoardFilePath.toFile())){
            GSON.toJson(LeaderBoard.LEADER_BOARD, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createLeaderBoardFile(Path leaderBoardFilePath){
        try(FileWriter writer = new FileWriter(leaderBoardFilePath.toFile())){
            GSON.toJson(LeaderBoard.LEADER_BOARD, writer);
        } catch (IOException _) {}
    }

    public static void addPlayedGame(UUID player, String userName){
        Optional<LeaderBoardUser> userOpt = LeaderBoard.LEADER_BOARD.users.stream().filter(p -> p.uuid == player).findFirst();
        userOpt.ifPresentOrElse(
                leaderBoardUser -> leaderBoardUser.gamesPlayed += 1,
                () -> LeaderBoard.LEADER_BOARD.users.add(new LeaderBoardUser(player, 1, 0, userName))
        );
    }
    public static void addWinGame(UUID player, String userName){
        Optional<LeaderBoardUser> userOpt = LeaderBoard.LEADER_BOARD.users.stream().filter(p -> p.uuid == player).findFirst();
        userOpt.ifPresentOrElse(
                leaderBoardUser -> leaderBoardUser.gamesWon += 1,
                () -> LeaderBoard.LEADER_BOARD.users.add(new LeaderBoardUser(player, 0, 1, userName))
        );
    }
}
