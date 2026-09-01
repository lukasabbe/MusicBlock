package com.lukasabbe.musicblock.commands;

import com.lukasabbe.musicblock.platform.PlatformHandler;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class RemoveRandomColorCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("removerandomcolor").executes(RemoveRandomColorCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        PlatformHandler.removeOtherBlocks(commandSourceStackCommandContext.getSource().getLevel(), PlatformHandler.getRandomColor());
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Removed random color"), false);
        return 1;
    }
}
