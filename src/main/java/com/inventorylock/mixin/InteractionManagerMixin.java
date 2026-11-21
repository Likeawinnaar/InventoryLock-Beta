package com.inventorylock.mixin;

import com.inventorylock.locking.Protection;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
abstract class InteractionManagerMixin {
    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    private void inventorylock$guard(int syncId, int slotId, int button, SlotActionType action,
            PlayerEntity player, CallbackInfo callback) {
        ScreenHandler handler = player.currentScreenHandler;
        if (handler.syncId != syncId || !Protection.hasLocks()) {
            return;
        }
        Slot slot = slotId >= 0 && slotId < handler.slots.size() ? handler.getSlot(slotId) : null;
        if (Protection.blocks(handler, player, slot, button, action, false)) {
            callback.cancel();
        }
    }
}
