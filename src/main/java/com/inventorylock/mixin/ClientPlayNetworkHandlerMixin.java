package com.inventorylock.mixin;

import com.inventorylock.InventoryLockClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void inventorylock$selectLocks(GameJoinS2CPacket packet, CallbackInfo callback) {
        var handler = (ClientPlayNetworkHandler) (Object) this;
        InventoryLockClient.selectJoinedContext(handler.getServerInfo());
    }
}
