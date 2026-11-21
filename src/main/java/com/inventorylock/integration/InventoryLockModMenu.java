package com.inventorylock.integration;

import com.inventorylock.gui.InventoryLockScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class InventoryLockModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return InventoryLockScreen::new;
    }
}
