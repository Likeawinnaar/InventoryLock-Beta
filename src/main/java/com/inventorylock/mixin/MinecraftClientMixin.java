package com.inventorylock.mixin;

import com.inventorylock.InventoryLockClient;
import com.inventorylock.locking.Protection;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {
    @Shadow public ClientPlayerEntity player;

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("TAIL"))
    private void inventorylock$leaveContext(Screen screen, boolean transferring, CallbackInfo callback) {
        var config = InventoryLockClient.configuration();
        if (config != null) {
            config.selectContext(null);
        }
    }

    // This input path sends the hand-swap request without predicting an inventory change.
    @WrapOperation(method = "handleInputEvents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void inventorylock$guardHands(ClientPlayNetworkHandler handler, Packet<?> packet, Operation<Void> send) {
        if (packet instanceof PlayerActionC2SPacket action
                && action.getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND
                && player != null
                && (Protection.locked(player.getInventory().selectedSlot)
                || Protection.locked(PlayerInventory.OFF_HAND_SLOT))) {
            return;
        }
        send.call(handler, packet);
    }

    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void inventorylock$guardPickBlock(CallbackInfo callback) {
        // Pick-block can move an existing inventory stack or overwrite a chosen hotbar slot.
        if (Protection.hasLocks()) {
            callback.cancel();
        }
    }
}
