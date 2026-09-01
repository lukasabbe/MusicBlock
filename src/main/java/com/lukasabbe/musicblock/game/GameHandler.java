package com.lukasabbe.musicblock.game;

import com.lukasabbe.musicblock.config.Config;
import com.lukasabbe.musicblock.platform.PlatformHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class GameHandler {
    public static List<GamePlayer> players = new ArrayList<>();
    public static boolean isGameActive = false;
    private static long currentTime = 0;
    private static long startTime;
    private static long nextExecuteTime;
    private static Runnable nextEvent;
    private static ServerLevel level;
    private static Block nextBlock;

    private static long currentDelayMs = 10000;
    private static final double decayFactor = 0.8; //20% faster every time
    private static final long minDelayMs = 1000;

    public static void init(){
        ServerPlayerEvents.JOIN.register(GameHandler::joinEvent);
        ServerPlayerEvents.LEAVE.register(GameHandler::leaveEvent);
        ServerTickEvents.END_SERVER_TICK.register(GameHandler::tickEvent);
    }

    public static void startGame(){
        BlockPos platformPos = Config.CONFIG.platformPos.getBlockPos();

        BlockPos startPos = platformPos.offset(PlatformHandler.PLATFORM_SIZE/2, 1, PlatformHandler.PLATFORM_SIZE/2);

        for(var player : players){
            player.getServerPlayer().teleportTo(startPos.getX(), startPos.getY(), startPos.getZ());
            player.alive = true;
        }
        nextEvent = GameHandler::spawnPlatformEvent;
        nextExecuteTime = currentTime;
        isGameActive = true;
    }

    private static void spawnPlatformEvent(){
        PlatformHandler.spawnRandomPlatform(level);
        nextBlock = PlatformHandler.getRandomColor();
        for(var player : players){
            if(!player.alive) continue;
            player.getServerPlayer().getInventory().setItem(4, nextBlock.asItem().getDefaultInstance());
        }
        nextExecuteTime += currentDelayMs;
        startTime = currentTime;
        currentDelayMs = (long) (currentDelayMs * decayFactor);
        if(currentDelayMs < minDelayMs){
            currentDelayMs = minDelayMs;
        }
        nextEvent = GameHandler::removePlatformEvent;
    }

    private static void removePlatformEvent(){
        PlatformHandler.removeOtherBlocks(level, nextBlock);
        nextExecuteTime += 5000;
        startTime = currentTime;
        nextEvent = GameHandler::spawnPlatformEvent;
    }

    private static void resetGameEvent(){

    }

    private static void tickEvent(MinecraftServer minecraftServer) {
        currentTime = System.currentTimeMillis();
        if(!isGameActive) return;


        float timePassed = (float) (currentTime - startTime);
        float totalDuration = (float) (nextExecuteTime - startTime);
        float progress = timePassed/totalDuration;
        float clampedProg = Mth.clamp(progress, 0, 1);
        for(var player : players){
            ServerPlayer serverPlayer = player.getServerPlayer();

            if(player.alive && serverPlayer.getY() <= Config.CONFIG.platformPos.y - 10){
                player.alive = false;
                serverPlayer.setGameMode(GameType.SPECTATOR);
                minecraftServer.getPlayerList().broadcastSystemMessage(serverPlayer.getName().copy().append(Component.literal(" dog").withColor(TextColor.RED)), false);
            }

            float difference = Mth.abs(player.getServerPlayer().experienceProgress - progress);
            if(difference >= 0.01f || progress == 1f || progress == 0f){
                serverPlayer.experienceProgress = clampedProg;
                serverPlayer.connection.send(new ClientboundSetExperiencePacket(serverPlayer.experienceProgress, serverPlayer.totalExperience, serverPlayer.experienceLevel));
            }
        }
        if(isGameEnded()){
            nextExecuteTime = currentTime + 5000;
            nextEvent = GameHandler::resetGameEvent;
            GamePlayer winner = players.stream().filter(p -> p.alive).findFirst().get();
            winner.alive = false;
            minecraftServer.getPlayerList().broadcastSystemMessage(winner.getServerPlayer().getName().copy().withStyle(ChatFormatting.BOLD).withColor(TextColor.GREEN).append(" VAN"), false);
        }
        if(currentTime >= nextExecuteTime) nextEvent.run();
    }

    private static void leaveEvent(ServerPlayer serverPlayer) {
        players.remove(players.stream().filter(t -> t.playerUUID.equals(serverPlayer.getUUID())).findFirst().get());
    }


    private static boolean isGameEnded(){
        return players.stream().filter(p -> p.alive).count() == 1;
    }

    private static void joinEvent(ServerPlayer player) {
        level = player.level();
        if(!isGameActive) player.setGameMode(GameType.ADVENTURE);
        else player.setGameMode(GameType.SPECTATOR);
        Vec3 pos = Config.CONFIG.spawnPos.getVec3();
        player.teleportTo(pos.x, pos.y, pos.z);

        players.add(new GamePlayer(player.getUUID()));
    }

}
