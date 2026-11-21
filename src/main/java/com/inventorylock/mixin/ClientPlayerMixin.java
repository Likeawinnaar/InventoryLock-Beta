package com.inventorylock.mixin;

import com.inventorylock.locking.Protection;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerMixin {
    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void inventorylock$guardDrop(boolean entireStack, CallbackInfoReturnable<Boolean> callback) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (Protection.preventsDropping() && Protection.locked(player.getInventory().selectedSlot)) {
            callback.setReturnValue(false);
        }
    }
}
