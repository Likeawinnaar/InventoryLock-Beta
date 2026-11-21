package com.inventorylock.locking;

import com.inventorylock.mixin.CreativeSlotAccessor;
import java.util.OptionalInt;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;

public final class SlotResolver {
    private SlotResolver() {
    }

    public static OptionalInt resolve(Slot slot, PlayerInventory inventory) {
        if (slot instanceof CreativeSlotAccessor wrapper) {
            slot = wrapper.inventorylock$getWrappedSlot();
        }
        if (slot == null || inventory == null || slot.inventory != inventory) {
            return OptionalInt.empty();
        }
        int index = slot.getIndex();
        return supported(index) ? OptionalInt.of(index) : OptionalInt.empty();
    }

    public static boolean supported(int index) {
        return (index >= 0 && index < PlayerInventory.MAIN_SIZE)
                || index == PlayerInventory.OFF_HAND_SLOT;
    }
}
