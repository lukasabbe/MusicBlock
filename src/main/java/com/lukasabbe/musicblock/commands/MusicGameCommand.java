package com.lukasabbe.musicblock.commands;

import com.lukasabbe.musicblock.game.GameHandler;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public class MusicGameCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("music").requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)).then(Commands.literal("start").executes(MusicGameCommand::startGame));
    }

    private static int startGame(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        GameHandler.startGame();
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Started Game"), false);
        return 1;
    }
}
