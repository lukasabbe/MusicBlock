package com.lukasabbe.musicblock.commands;

import com.lukasabbe.musicblock.platform.PlatformHandler;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SpawnPlatformCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands
                .literal("spawnplatform")
                .executes(SpawnPlatformCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        PlatformHandler.spawnRandomPlatform(commandSourceStackCommandContext.getSource().getLevel());
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Generated random platform"), false);
        return 1;
    }
}
