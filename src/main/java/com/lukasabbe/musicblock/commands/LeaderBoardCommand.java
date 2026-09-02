package com.lukasabbe.musicblock.commands;

import com.lukasabbe.musicblock.leaderboard.LeaderBoard;
import com.lukasabbe.musicblock.leaderboard.LeaderBoardUser;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.stream.IntStream;

public class LeaderBoardCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands
                .literal("leaderboard")
                .executes(LeaderBoardCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        List<LeaderBoardUser> topTenUsers = LeaderBoard
                .LEADER_BOARD.users
                .stream()
                .sorted((u1, u2) -> Integer.compare(u2.gamesWon, u1.gamesWon))
                .limit(10)
                .toList();
        MutableComponent leaderBoardMessage = Component.literal("Leader Board\n")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN);
        IntStream.range(0, topTenUsers.size()).forEach(i -> {
            LeaderBoardUser user = topTenUsers.get(i);
            String playerName = topTenUsers.get(i).lastUserName;
            String line = (i + 1) + ". " + playerName + " - " + user.gamesWon + " vinster\n";
            leaderBoardMessage.append(Component.literal(line).withStyle(ChatFormatting.WHITE));
        });

        commandSourceStackCommandContext.getSource().sendSuccess(() -> leaderBoardMessage, false);
        return 1;
    }
}
