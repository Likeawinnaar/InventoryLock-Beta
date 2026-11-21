package com.inventorylock.mixin;

import com.inventorylock.gui.LockIndicator;
import com.inventorylock.locking.Protection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
abstract class InGameHudMixin {
    @Inject(method = "renderHotbarItem", at = @At("RETURN"))
    private void inventorylock$drawHotbarIndicator(DrawContext context, int x, int y, RenderTickCounter tickCounter,
            PlayerEntity player, ItemStack stack, int seed, CallbackInfo callback) {
        if (MinecraftClient.getInstance().player != player) {
            return;
        }
        // Vanilla uses seeds 1–9 for the hotbar and the next seed for a visible offhand item.
        int index = seed - 1;
        if (index == PlayerInventory.getHotbarSize()) {
            index = PlayerInventory.OFF_HAND_SLOT;
        }
        if (Protection.locked(index)) {
            LockIndicator.render(context, x, y, 0);
        }
    }
}
