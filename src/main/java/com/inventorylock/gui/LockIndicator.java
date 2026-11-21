package com.inventorylock.gui;

import com.inventorylock.InventoryLockClient;
import com.inventorylock.config.Settings;
import net.minecraft.client.gui.DrawContext;

public final class LockIndicator {
    private LockIndicator() {
    }

    public static void render(DrawContext context, int x, int y, int itemLayerOffset) {
        Settings settings = InventoryLockClient.configuration().settings();
        if (settings.overlayStyle != Settings.Style.ICON_ONLY && settings.overlayOpacity > 0) {
            int alpha = Math.round(settings.overlayOpacity * 255 / 100f);
            int color = (alpha << 24) | settings.indicatorColor.rgb;
            // Tint the slot and item while leaving the stack count above the overlay.
            context.fill(x, y, x + 16, y + 16, itemLayerOffset + 190, color);
        }
        if (settings.showLockIcons && settings.overlayStyle != Settings.Style.SUBTLE_OVERLAY) {
            var matrices = context.getMatrices();
            matrices.push();
            matrices.translate(0, 0, itemLayerOffset + 210);
            context.drawTexture(settings.indicatorColor.texture, x + 7, y, 9, 9,
                    1, 1, 30, 30, 32, 32);
            matrices.pop();
        }
    }
}
