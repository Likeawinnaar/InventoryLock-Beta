package com.inventorylock.mixin;

import com.inventorylock.InventoryLockClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.dto.RealmsServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RealmsServer.class)
abstract class RealmsServerMixin {
    @Shadow public long id;

    @Inject(method = "createServerInfo", at = @At("RETURN"))
    private void inventorylock$recordRealm(String address, CallbackInfoReturnable<ServerInfo> callback) {
        if (id > 0) {
            InventoryLockClient.recordRealm(callback.getReturnValue(), id);
        }
    }
}
