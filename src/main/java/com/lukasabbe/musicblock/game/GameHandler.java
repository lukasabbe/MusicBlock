package com.lukasabbe.musicblock.game;

import com.lukasabbe.musicblock.config.Config;
import com.lukasabbe.musicblock.leaderboard.LeaderBoard;
import com.lukasabbe.musicblock.music.MusicHandler;
import com.lukasabbe.musicblock.platform.PlatformHandler;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameHandler {
    public static List<GamePlayer> players = new ArrayList<>();
    public static boolean isGameActive = false;
    private static long currentTime = 0;
    private static long startClockCountDown = 0;
    private static long startTime;
    private static long nextExecuteTime;
    private static Runnable nextEvent;
    private static ServerLevel level;
    private static Block nextBlock;

    private static long currentDelayMs = Config.CONFIG.startDelayMs;
    private static double decayFactor = Config.CONFIG.decayFactor;
    private static long minDelayMs = Config.CONFIG.minDelayMs;
    private static int lastSecondSound = -1;
    private static int lastAnnouncedSecond = -1;

    private static boolean debugMode = false;
    private static boolean countDownToStart = false;
    private static boolean spawningPlatform = true;
    private static boolean gameEnded = false;

    public static void init(){
        ServerPlayerEvents.JOIN.register(GameHandler::joinEvent);
        ServerPlayerEvents.LEAVE.register(GameHandler::leaveEvent);
        ServerTickEvents.END_SERVER_TICK.register(GameHandler::tickEvent);
    }

    public static void reloadConfigNumbers(){
         currentDelayMs = Config.CONFIG.startDelayMs;
         decayFactor = Config.CONFIG.decayFactor;
         minDelayMs = Config.CONFIG.minDelayMs;
    }

    public static void startGame(){
        countDownToStart = false;
        BlockPos platformPos = Config.CONFIG.platformPos.getBlockPos();

        BlockPos startPos = platformPos.offset(Config.CONFIG.platFormSize/2, 1, Config.CONFIG.platFormSize/2);

        for(var player : players){
            player.getServerPlayer().teleportTo(startPos.getX(), startPos.getY(), startPos.getZ());
            player.alive = true;
        }
        nextEvent = GameHandler::spawnPlatformEvent;
        nextExecuteTime = currentTime;
        isGameActive = true;
        MusicHandler.playRandomSong(level);
    }

    private static void spawnPlatformEvent(){
        MusicHandler.play();
        spawningPlatform = true;
        PlatformHandler.spawnRandomPlatform(level);
        nextBlock = PlatformHandler.getRandomColor(level);
        nextEvent = GameHandler::nextBlockEvent;
        startTime = currentTime;
        nextExecuteTime += 3000;
    }

    private static void nextBlockEvent(){
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
        MusicHandler.pause();
        spawningPlatform = false;
        PlatformHandler.removeOtherBlocks(level, nextBlock);
        for(var player : players){
            ServerPlayer serverPlayer = player.getServerPlayer();
            serverPlayer.connection.send(new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ENDER_DRAGON_GROWL),
                    SoundSource.MASTER,
                    serverPlayer.getX(),
                    serverPlayer.getY(),
                    serverPlayer.getZ(),
                    1.0F,
                    1.0F,
                    serverPlayer.getRandom().nextLong()

            ));
            serverPlayer.getInventory().clearContent();
        }
        nextExecuteTime += 5000;
        startTime = currentTime;
        nextEvent = GameHandler::spawnPlatformEvent;
    }

    public static void resetGameEvent(){
        isGameActive = false;
        nextEvent = null;
        gameEnded = false;
        currentDelayMs = Config.CONFIG.startDelayMs;
        PlatformHandler.spawnRandomPlatform(level);
        Vec3 spawnPos = Config.CONFIG.spawnPos.getVec3();
        for(var player : players){
            ServerPlayer serverPlayer = player.getServerPlayer();
            serverPlayer.teleportTo(spawnPos.x, spawnPos.y, spawnPos.z);
            serverPlayer.setGameMode(GameType.ADVENTURE);
            serverPlayer.getInventory().clearContent();
        }
        MusicHandler.stopMusic();
    }

    private static void tickEvent(MinecraftServer minecraftServer) {
        currentTime = System.currentTimeMillis();


        if(!isGameActive) {
            if(!countDownToStart && minecraftServer.getPlayerList().getPlayerCount() >= 2){
                countDownToStart = true;
                minecraftServer.getPlayerList().broadcastSystemMessage(Component.literal("Music blocks startar om 45s").withStyle(ChatFormatting.BOLD).withColor(TextColor.AQUA), false);
                startClockCountDown = currentTime + 1000 * 45;
            }
            if(countDownToStart){
                if (minecraftServer.getPlayerList().getPlayerCount() < 2){
                    minecraftServer.getPlayerList().broadcastSystemMessage(Component.literal("Nedräkning avbröts, behöver minst 2 spelare").withStyle(ChatFormatting.BOLD).withColor(TextColor.RED), false);
                    countDownToStart = false;
                }
                int secondsRemaining = (int) Math.ceil((startClockCountDown - currentTime) / 1000.0);
                if (secondsRemaining != lastAnnouncedSecond && secondsRemaining >= 0) {
                    lastAnnouncedSecond = secondsRemaining;
                    switch (secondsRemaining) {
                        case 10 -> minecraftServer.getPlayerList().broadcastSystemMessage(Component.literal("Music blocks startar om 10s").withStyle(ChatFormatting.BOLD).withColor(TextColor.AQUA), false);
                        case 5 -> minecraftServer.getPlayerList().broadcastSystemMessage(Component.literal("Music blocks startar om 5s").withStyle(ChatFormatting.BOLD).withColor(TextColor.AQUA), false);
                        case 4 -> minecraftServer.getPlayerList().broadcastSystemMessage(Component.literal("Music blocks startar om 4s").withStyle(ChatFormatting.BOLD).withColor(TextColor.AQUA), false);
                        case 3 -> minecraftServer.getPlayerList().broadcastSystemMessage(Component.literal("Music blocks startar om 3s").withStyle(ChatFormatting.BOLD).withColor(TextColor.AQUA), false);
                        case 2 -> minecraftServer.getPlayerList().broadcastSystemMessage(Component.literal("Music blocks startar om 2s").withStyle(ChatFormatting.BOLD).withColor(TextColor.AQUA), false);
                        case 1 -> minecraftServer.getPlayerList().broadcastSystemMessage(Component.literal("Music blocks startar om 1s").withStyle(ChatFormatting.BOLD).withColor(TextColor.AQUA), false);
                        case 0 -> {
                            countDownToStart = false;
                            lastAnnouncedSecond = -1;
                            startGame();
                        }
                    }
                }
            }
            return;
        }


        int secondsLeft = (int) Math.ceil((nextExecuteTime - currentTime) / 1000.0);

        if(secondsLeft != lastSecondSound && secondsLeft > 0 && secondsLeft <=3 && spawningPlatform){
            lastSecondSound = secondsLeft;
            for(var player : players){
                ServerPlayer serverPlayer = player.getServerPlayer();
                serverPlayer.connection.send(new ClientboundSoundPacket(
                        SoundEvents.NOTE_BLOCK_BELL,
                        SoundSource.MASTER,
                        serverPlayer.getX(),
                        serverPlayer.getY(),
                        serverPlayer.getZ(),
                        1.0F,
                        1.0F,
                        serverPlayer.getRandom().nextLong()

                ));
            }
        }

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
        if(isGameEnded() && !gameEnded){
            gameEnded = true;
            nextExecuteTime = currentTime + 5000;
            nextEvent = GameHandler::resetGameEvent;
            players.forEach(p -> LeaderBoard.addPlayedGame(p.playerUUID, p.getServerPlayer().getName().getString()));
            Optional<GamePlayer> winnerOpt = players.stream().filter(p -> p.alive).findFirst();
            if(winnerOpt.isPresent()){
                var winner = winnerOpt.get();
                minecraftServer.getPlayerList().broadcastSystemMessage(winner.getServerPlayer().getName().copy().withStyle(ChatFormatting.BOLD).withColor(TextColor.GREEN).append(" vann"), false);
                winner.alive = false;
                PlatformHandler.spawnRandomPlatform(level);
                BlockPos platformPos = Config.CONFIG.platformPos.getBlockPos();
                BlockPos startPos = platformPos.offset(Config.CONFIG.platFormSize/2, 1, Config.CONFIG.platFormSize/2);
                winner.getServerPlayer().teleportTo(startPos.getX(), startPos.getY(), startPos.getZ());
                LeaderBoard.addWinGame(winner.playerUUID, winner.getServerPlayer().getName().getString());
            }else{
                minecraftServer.getPlayerList().broadcastSystemMessage(Component.literal("Det blev lika").withColor(TextColor.GREEN), false);
            }
            LeaderBoard.saveLeaderBoard();
            BlockPos pos1 = Config.CONFIG.platformPos.getBlockPos().above();
            BlockPos pos2 = pos1.offset(Config.CONFIG.platFormSize, 0, Config.CONFIG.platFormSize);
            BlockPos pos3 = pos1.offset(0, 0, Config.CONFIG.platFormSize);
            BlockPos pos4 = pos1.offset(Config.CONFIG.platFormSize, 0, 0);
            for(int i = 0; i < 3; i++){
                spawnWinningFireworks(pos1, level);
                spawnWinningFireworks(pos2, level);
                spawnWinningFireworks(pos3, level);
                spawnWinningFireworks(pos4, level);
            }
            lastSecondSound = -1;
        }
        if(currentTime >= nextExecuteTime) nextEvent.run();
    }

    private static void leaveEvent(ServerPlayer serverPlayer) {
        players.remove(players.stream().filter(t -> t.playerUUID.equals(serverPlayer.getUUID())).findFirst().get());
    }


    private static boolean isGameEnded(){
        return players.stream().filter(p -> p.alive).count() <= 1 && !debugMode;
    }

    private static void joinEvent(ServerPlayer player) {
        level = player.level();
        if(!isGameActive) player.setGameMode(GameType.ADVENTURE);
        else player.setGameMode(GameType.SPECTATOR);
        Vec3 pos = Config.CONFIG.spawnPos.getVec3();
        player.teleportTo(pos.x, pos.y, pos.z);
        player.getInventory().clearContent();
        players.add(new GamePlayer(player.getUUID()));
    }

    private static void spawnWinningFireworks(BlockPos pos, Level level){
        FireworkExplosion explosion = new FireworkExplosion(
                FireworkExplosion.Shape.LARGE_BALL,
                IntList.of(0xFF0000, 0xFFA500, 0xFFFF00),
                IntList.of(0xFFFFFF, 0x00FFFF),
                true,
                true
        );
        Fireworks fireworks = new Fireworks(1, List.of(explosion));

        ItemStack item = new ItemStack(Items.FIREWORK_ROCKET);
        item.set(DataComponents.FIREWORKS, fireworks);
        FireworkRocketEntity rocket = new FireworkRocketEntity(level, pos.getX(), pos.getY(), pos.getZ(), item);
        level.addFreshEntity(rocket);
    }

}
