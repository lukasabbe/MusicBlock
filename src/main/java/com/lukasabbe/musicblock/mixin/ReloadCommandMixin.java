package com.lukasabbe.musicblock.mixin;

import com.lukasabbe.musicblock.config.Config;
import com.lukasabbe.musicblock.platform.PlatformHandler;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.commands.ReloadCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadCommand.class)
public class ReloadCommandMixin {
    @Inject(method = "lambda$register$0", at=@At("HEAD"))
    private static void addReload(CommandContext s, CallbackInfoReturnable<Integer> cir){
        Config.loadConfig();
        PlatformHandler.platforms.clear();
        PlatformHandler.init();
    }
}
