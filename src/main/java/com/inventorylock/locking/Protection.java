package com.inventorylock.locking;

import com.inventorylock.InventoryLockClient;
import com.inventorylock.config.ConfigStore;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public final class Protection {
    private Protection() {
    }

    public static boolean hasLocks() {
        ConfigStore config = InventoryLockClient.configuration();
        return config != null && config.hasLocks();
    }

    public static boolean locked(int index) {
        ConfigStore config = InventoryLockClient.configuration();
        return config != null && config.isLocked(index);
    }

    public static boolean locked(Slot slot, PlayerInventory inventory) {
        return locked(SlotResolver.resolve(slot, inventory).orElse(-1));
    }

    public static boolean preventsDropping() {
        ConfigStore config = InventoryLockClient.configuration();
        return config != null && config.settings().preventDropping;
    }

    public static boolean blocks(ScreenHandler handler, PlayerEntity player, Slot slot, int button,
            SlotActionType action, boolean creative) {
        if (!hasLocks()) {
            return false;
        }
        PlayerInventory inventory = player.getInventory();
        boolean targetLocked = locked(slot, inventory);
        return switch (action) {
            case PICKUP -> targetLocked;
            case THROW -> targetLocked && preventsDropping();
            case SWAP -> targetLocked || locked(button) || wouldReinsertDisplacedStack(slot, inventory, button);
            case QUICK_MOVE -> targetLocked || (creative ? slot != null : couldMoveIntoLock(handler, slot, inventory));
            case QUICK_CRAFT -> ScreenHandler.unpackQuickCraftStage(button) == 1 && targetLocked;
            case PICKUP_ALL -> targetLocked || collectsFromLock(handler, player);
            case CLONE -> false;
        };
    }

    private static boolean wouldReinsertDisplacedStack(Slot slot, PlayerInventory inventory, int button) {
        if (slot == null || !slot.hasStack() || !SlotResolver.supported(button)) {
            return false;
        }
        ItemStack incoming = inventory.getStack(button);
        // Vanilla can reinsert the displaced stack through PlayerInventory when the target caps the count.
        return !incoming.isEmpty() && incoming.getCount() > slot.getMaxItemCount(incoming);
    }

    private static boolean couldMoveIntoLock(ScreenHandler handler, Slot source, PlayerInventory inventory) {
        if (source == null || !source.hasStack()) {
            return false;
        }
        if (handler.getClass() == GenericContainerScreenHandler.class && source.inventory == inventory) {
            return false;
        }
        ItemStack moving = source.getStack();
        for (Slot target : handler.slots) {
            if (target == source || !locked(target, inventory)) {
                continue;
            }
            ItemStack existing = target.getStack();
            if (existing.isEmpty() ? target.canInsert(moving)
                    : ItemStack.areItemsAndComponentsEqual(moving, existing)
                    && existing.getCount() < target.getMaxItemCount(existing)) {
                return true;
            }
        }
        return false;
    }

    private static boolean collectsFromLock(ScreenHandler handler, PlayerEntity player) {
        ItemStack cursor = handler.getCursorStack();
        if (cursor.isEmpty() || cursor.getCount() >= cursor.getMaxCount()) {
            return false;
        }
        for (Slot slot : handler.slots) {
            if (locked(slot, player.getInventory()) && slot.canTakeItems(player)
                    && ItemStack.areItemsAndComponentsEqual(cursor, slot.getStack())) {
                return true;
            }
        }
        return false;
    }
}
