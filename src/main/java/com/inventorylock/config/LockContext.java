package com.inventorylock.config;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import net.minecraft.client.network.ServerAddress;

public record LockContext(Type type, String key) {
    public enum Type {
        WORLD, SERVER
    }

    public static LockContext world(String directory) {
        return new LockContext(Type.WORLD, "world:" + directory);
    }

    public static LockContext server(String address) {
        if (address == null || !ServerAddress.isValid(address.strip())) {
            return null;
        }
        ServerAddress endpoint = ServerAddress.parse(address.strip());
        String canonical = endpoint.getAddress().toLowerCase(Locale.ROOT) + ":" + endpoint.getPort();
        String digest = Hashing.sha256().hashString(canonical, StandardCharsets.UTF_8).toString();
        return new LockContext(Type.SERVER, "server:" + digest);
    }

    public static LockContext realm(long id) {
        return new LockContext(Type.SERVER, "realm:" + id);
    }
}
