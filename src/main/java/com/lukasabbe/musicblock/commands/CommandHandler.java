package com.lukasabbe.musicblock.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandHandler {
    public static void init(){
        CommandRegistrationCallback.EVENT.register(CommandHandler::register);
    }

    private static void register(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        commandSourceStackCommandDispatcher.register(SpawnPlatformCommand.getCommand());
        commandSourceStackCommandDispatcher.register(RemoveRandomColorCommand.getCommand());
        commandSourceStackCommandDispatcher.register(MusicGameCommand.getCommand());
    }
}
