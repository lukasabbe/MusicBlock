package com.lukasabbe.musicblock.leaderboard;

import java.util.UUID;

public class LeaderBoardUser {
    public UUID uuid;
    public int gamesPlayed;
    public int gamesWon;
    public String lastUserName;

    public LeaderBoardUser(UUID uuid, int gamesPlayed, int gamesWon, String lastUserName) {
        this.uuid = uuid;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.lastUserName = lastUserName;
    }
}
