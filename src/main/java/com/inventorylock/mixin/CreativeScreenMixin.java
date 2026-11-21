package com.inventorylock.mixin;

import com.inventorylock.locking.Protection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
abstract class CreativeScreenMixin {
    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    private void inventorylock$guardCreative(Slot slot, int slotId, int button, SlotActionType action,
            CallbackInfo callback) {
        var player = MinecraftClient.getInstance().player;
        var screen = (CreativeInventoryScreen) (Object) this;
        if (player != null && Protection.blocks(screen.getScreenHandler(), player, slot, button, action, true)) {
            callback.cancel();
        }
    }

    @Inject(method = "onHotbarKeyPress", at = @At("HEAD"), cancellable = true)
    private static void inventorylock$guardPreset(MinecraftClient client, int preset, boolean restore,
            boolean save, CallbackInfo callback) {
        if (restore && Protection.hasLocks()) {
            for (int index = 0; index < PlayerInventory.getHotbarSize(); index++) {
                if (Protection.locked(index)) {
                    callback.cancel();
                    return;
                }
            }
        }
    }
}
