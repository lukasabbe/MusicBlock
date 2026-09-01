package com.lukasabbe.musicblock.game;

import com.lukasabbe.musicblock.Musicblock;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class GamePlayer {
    public UUID playerUUID;
    public boolean alive;

    public GamePlayer(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public ServerPlayer getServerPlayer(){
        return Musicblock.server.getPlayerList().getPlayer(playerUUID);
    }
}
