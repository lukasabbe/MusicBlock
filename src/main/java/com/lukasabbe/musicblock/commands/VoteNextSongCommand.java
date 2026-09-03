package com.lukasabbe.musicblock.commands;

import com.lukasabbe.musicblock.game.GameHandler;
import com.lukasabbe.musicblock.music.MusicHandler;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class VoteNextSongCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands
                .literal("vote")
                .then(Commands
                        .argument("music", StringArgumentType.string())
                        .suggests(new MusicSuggestionProvider())
                        .executes(VoteNextSongCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        if(GameHandler.isGameActive) {
            commandSourceStackCommandContext.getSource().sendFailure(Component.literal("Du kan inte rösta för låt när ett spel är aktivt"));
            return 0;
        }
        String musicName = StringArgumentType.getString(commandSourceStackCommandContext, "music");
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Du har nu röstat för " + musicName), false);
        MusicHandler.vote(commandSourceStackCommandContext.getSource().getPlayer().getUUID(), musicName);
        return 1;
    }
}
