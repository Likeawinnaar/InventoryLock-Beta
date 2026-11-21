package com.inventorylock;

import com.inventorylock.config.ConfigStore;
import com.inventorylock.config.LockContext;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.util.WorldSavePath;
import org.slf4j.LoggerFactory;

public final class InventoryLockClient implements ClientModInitializer {
    // Realms creates the connection on a worker thread and hands the same ServerInfo to the client.
    private static final Map<ServerInfo, Long> REALM_IDS = Collections.synchronizedMap(new WeakHashMap<>());
    private static ConfigStore configuration;

    public static ConfigStore configuration() {
        return configuration;
    }

    public static void recordRealm(ServerInfo info, long id) {
        REALM_IDS.put(info, id);
    }

    public static void selectJoinedContext(ServerInfo info) {
        var integratedServer = MinecraftClient.getInstance().getServer();
        if (integratedServer != null) {
            var directory = integratedServer.getSavePath(WorldSavePath.ROOT).normalize().getFileName();
            configuration.selectContext(directory == null ? null : LockContext.world(directory.toString()));
        } else if (info != null && info.isRealm()) {
            Long id = REALM_IDS.get(info);
            configuration.selectContext(id == null ? null : LockContext.realm(id));
        } else {
            configuration.selectContext(info == null ? null : LockContext.server(info.address));
        }
    }

    @Override
    public void onInitializeClient() {
        configuration = new ConfigStore(FabricLoader.getInstance().getConfigDir().resolve("inventorylock.json"));
        configuration.selectProfile(MinecraftClient.getInstance().getSession().getUuidOrNull());
        LoggerFactory.getLogger("inventorylock").info("InventoryLock client initialized.");
    }
}
