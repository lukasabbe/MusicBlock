package com.lukasabbe.musicblock.mixin;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.ClickAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class DropItemMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePlayerAction", at=@At("HEAD"), cancellable = true)
    private void preventItemDrop(ServerboundPlayerActionPacket packet, CallbackInfo ci){
        if(packet.getAction() == ServerboundPlayerActionPacket.Action.DROP_ITEM || packet.getAction() == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS){
            if(!this.player.gameMode.isCreative()) {
                ci.cancel();
                this.player.inventoryMenu.sendAllDataToRemote();
            }
        }
    }

    @Inject(method = "handleContainerClick", at=@At("HEAD"), cancellable = true)
    private void preventDragItemDrop(ServerboundContainerClickPacket packet, CallbackInfo ci){
        if(!this.player.gameMode.isCreative()) {
            ci.cancel();
            this.player.inventoryMenu.sendAllDataToRemote();
        }
    }
}
