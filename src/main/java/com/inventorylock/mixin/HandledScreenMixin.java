package com.inventorylock.mixin;

import com.inventorylock.InventoryLockClient;
import com.inventorylock.config.ConfigStore;
import com.inventorylock.gui.LockIndicator;
import com.inventorylock.locking.Protection;
import com.inventorylock.locking.SlotResolver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
abstract class HandledScreenMixin {
    @Shadow private Slot getSlotAt(double x, double y) { throw new AssertionError(); }
    @Shadow private boolean doubleClicking;
    @Shadow private Slot lastClickedSlot;
    @Shadow private long lastButtonClickTime;
    @Unique private boolean inventorylock$consumeRelease;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void inventorylock$toggle(double x, double y, int button, CallbackInfoReturnable<Boolean> callback) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }
        inventorylock$consumeRelease = false;
        MinecraftClient client = MinecraftClient.getInstance();
        ConfigStore config = InventoryLockClient.configuration();
        if (client.player == null || config == null || !config.settings().enabled
                || !InputUtil.isKeyPressed(client.getWindow().getHandle(), config.settings().lockModifier.key)) {
            return;
        }
        int index = SlotResolver.resolve(getSlotAt(x, y), client.player.getInventory()).orElse(-1);
        if (index < 0) {
            return;
        }
        if (!config.toggle(index)) {
            client.player.sendMessage(Text.translatable(config.readOnly()
                    ? "inventorylock.error.read" : "inventorylock.error.write"), true);
        }
        inventorylock$consumeRelease = true;
        doubleClicking = false;
        lastClickedSlot = null;
        lastButtonClickTime = 0;
        callback.setReturnValue(true);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void inventorylock$release(double x, double y, int button, CallbackInfoReturnable<Boolean> callback) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && inventorylock$consumeRelease) {
            inventorylock$consumeRelease = false;
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void inventorylock$swallowToggleDrag(double x, double y, int button, double dx, double dy,
            CallbackInfoReturnable<Boolean> callback) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && inventorylock$consumeRelease) {
            callback.setReturnValue(true);
        }
    }

    // Exclude protected slots from the UI's drag set before it emits any quick-craft clicks.
    @WrapOperation(method = "mouseDragged", at = @At(value = "INVOKE",
            target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private boolean inventorylock$dragCandidate(Set<Slot> slots, Object candidate, Operation<Boolean> add) {
        var player = MinecraftClient.getInstance().player;
        return (player == null || !Protection.locked((Slot) candidate, player.getInventory()))
                && add.call(slots, candidate);
    }

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void inventorylock$drawIndicator(DrawContext context, Slot slot, CallbackInfo callback) {
        var player = MinecraftClient.getInstance().player;
        if (player == null || !Protection.locked(slot, player.getInventory())) {
            return;
        }
        // HandledScreen draws slot items on top of a 100-unit depth offset.
        LockIndicator.render(context, slot.x, slot.y, 100);
    }
}
