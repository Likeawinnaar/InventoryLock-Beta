package com.inventorylock;

import com.inventorylock.config.ConfigStore;
import com.inventorylock.config.LockContext;
import com.inventorylock.config.Settings;
import com.inventorylock.locking.SlotResolver;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

public final class InventoryLockChecks {
    private InventoryLockChecks() {
    }

    public static void main(String[] args) throws Exception {
        checkSlots();
        Path temp = Files.createTempDirectory("inventorylock-checks-");
        try {
            checkConfig(temp.resolve("inventorylock.json"));
            checkColors(temp.resolve("color.json"));
            checkScopes(temp.resolve("scopes.json"));
        } finally {
            try (var entries = Files.walk(temp)) {
                for (Path entry : entries.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(entry);
                }
            }
        }
        System.out.println("InventoryLock checks passed.");
    }

    private static void checkSlots() {
        expect(SlotResolver.supported(0) && SlotResolver.supported(35), "main inventory range");
        expect(SlotResolver.supported(40), "offhand");
        expect(!SlotResolver.supported(-1) && !SlotResolver.supported(36)
                && !SlotResolver.supported(39) && !SlotResolver.supported(41), "unsupported indices");
    }

    private static void checkConfig(Path path) throws Exception {
        UUID first = new UUID(0, 1);
        UUID second = new UUID(0, 2);
        ConfigStore config = new ConfigStore(path);
        config.selectProfile(first);
        expect(config.saveSettings(config.settings().copy()), "unchanged save");
        expect(Files.notExists(path), "no file for unchanged defaults");
        expect(config.toggle(0) && config.toggle(40), "lock toggle");
        expect(!config.toggle(39), "invalid index rejected");
        config.selectProfile(second);
        expect(!config.hasLocks() && config.toggle(8), "second profile isolation");

        ConfigStore reloaded = new ConfigStore(path);
        reloaded.selectProfile(first);
        expect(reloaded.isLocked(0) && reloaded.isLocked(40) && !reloaded.isLocked(8), "restart persistence");
        Settings draft = reloaded.settings().copy();
        draft.enabled = false;
        draft.overlayOpacity = 60;
        expect(reloaded.saveSettings(draft) && !reloaded.hasLocks(), "disable preserves saved locks");
        draft.enabled = true;
        expect(reloaded.saveSettings(draft) && reloaded.isLocked(0), "re-enable restores locks");
        draft.overlayOpacity = 35;
        expect(reloaded.clearLocks() && !reloaded.hasLocks(), "clear first profile immediately");
        ConfigStore afterClear = new ConfigStore(path);
        afterClear.selectProfile(first);
        expect(!afterClear.hasLocks() && afterClear.settings().overlayOpacity == 60, "clear persists without draft settings");
        reloaded.selectProfile(second);
        expect(reloaded.isLocked(8), "clear applies to active profile");
        reloaded.selectProfile(null);
        expect(reloaded.toggle(3) && reloaded.isLocked(3), "temporary lock");
        var fresh = new ConfigStore(path);
        fresh.selectProfile(null);
        expect(!fresh.hasLocks() && fresh.settings().overlayOpacity == 60, "temporary lock is not persisted");

        String malformed = "{configVersion:1}";
        Files.writeString(path, malformed);
        var blocked = new ConfigStore(path);
        blocked.selectProfile(first);
        expect(blocked.readOnly() && !blocked.toggle(0), "malformed settings become read-only");
        expect(Files.readString(path).equals(malformed), "malformed settings preserved");

        Files.writeString(path, "{\"configVersion\":1,\"overlayOpacity\":4,\"custom\":123}");
        var inherited = new ConfigStore(path);
        expect(inherited.settings().overlayOpacity == 20, "invalid opacity falls back");
        inherited.selectProfile(first);
        expect(inherited.toggle(1) && Files.readString(path).contains("\"custom\": 123"), "unknown key preserved");

        Path directory = Files.createDirectory(path.resolveSibling("blocked"));
        ConfigStore unwritable = new ConfigStore(directory.resolve("settings.json"));
        Files.delete(directory);
        Files.writeString(directory, "block directory creation");
        unwritable.selectProfile(first);
        expect(!unwritable.toggle(1) && !unwritable.isLocked(1), "write failure rolls back the toggle");
    }

    private static void checkColors(Path path) throws Exception {
        expect(Settings.Color.values().length == 16, "sixteen indicator colors");
        Files.writeString(path, "{\"indicatorColor\":\"GOLD\"}");
        expect(new ConfigStore(path).settings().indicatorColor == Settings.Color.YELLOW, "legacy Gold migration");
        for (Settings.Color color : Settings.Color.values()) {
            String resource = "/assets/" + color.texture.getNamespace() + "/" + color.texture.getPath();
            expect(InventoryLockChecks.class.getResource(resource) != null, "packaged icon for " + color);
            ConfigStore config = new ConfigStore(path);
            Settings draft = config.settings().copy();
            draft.indicatorColor = color;
            expect(config.saveSettings(draft), "save " + color);
            expect(new ConfigStore(path).settings().indicatorColor == color, "reload " + color);
        }
    }

    private static void checkScopes(Path path) throws Exception {
        UUID player = new UUID(0, 1);
        var worldA = LockContext.world("World A");
        var worldB = LockContext.world("World B");
        var serverA = LockContext.server("game-a.example");
        var serverB = LockContext.server("game-b.example:25566");
        expect(serverA.equals(LockContext.server("GAME-A.example:25565")), "normalize server case and default port");
        expect(!serverA.equals(LockContext.server("game-a.example:25566")), "different server ports");
        expect(LockContext.server("") == null, "missing server address");
        expect(!LockContext.realm(1).equals(LockContext.realm(2)), "stable separate Realm keys");

        expect(new ConfigStore(path).settings().perServerLocks, "per-server locks default on for new installs");
        Files.writeString(path, "{\"configVersion\":1,\"profiles\":{\"" + player + "\":[0]}}");
        ConfigStore config = new ConfigStore(path);
        config.selectProfile(player);
        config.selectContext(worldA);
        expect(config.isLocked(0), "existing locks remain active with world scoping off");

        expect(config.settings().perServerLocks, "config without a server preference inherits default on");
        config.selectContext(serverA);
        expect(!config.hasLocks(), "server uses a separate set by default");
        config.selectContext(worldA);

        Settings draft = config.settings().copy();
        draft.perServerLocks = false;
        draft.perWorldLocks = true;
        expect(config.saveSettings(draft) && !config.hasLocks(), "new world scope starts empty");
        expect(config.toggle(1), "set first world lock");
        config.selectContext(worldB);
        expect(!config.hasLocks() && config.toggle(2), "second world stays separate");

        config.selectContext(serverA);
        expect(config.isLocked(0), "world setting leaves the server scope off");
        draft.perServerLocks = true;
        expect(config.saveSettings(draft) && !config.hasLocks() && config.toggle(3), "set first server lock");
        config.selectContext(serverB);
        expect(!config.hasLocks() && config.toggle(4), "second server stays separate");
        config.selectContext(serverA);
        expect(config.isLocked(3) && !config.isLocked(4), "return to first server");
        expect(config.clearLocks() && !config.hasLocks(), "clear affects active server only");

        ConfigStore reloaded = new ConfigStore(path);
        reloaded.selectProfile(player);
        reloaded.selectContext(worldA);
        expect(reloaded.isLocked(1), "reload first world");
        reloaded.selectContext(worldB);
        expect(reloaded.isLocked(2), "reload second world");
        reloaded.selectContext(serverB);
        expect(reloaded.isLocked(4), "reload second server after clearing the first");
        draft = reloaded.settings().copy();
        draft.perServerLocks = false;
        expect(reloaded.saveSettings(draft) && reloaded.isLocked(0), "disabling returns to shared locks");
        ConfigStore disabledOnRestart = new ConfigStore(path);
        disabledOnRestart.selectProfile(player);
        disabledOnRestart.selectContext(serverB);
        expect(!disabledOnRestart.settings().perServerLocks && disabledOnRestart.isLocked(0),
                "saved OFF overrides the ON default after restart");
        draft.perServerLocks = true;
        expect(reloaded.saveSettings(draft) && reloaded.isLocked(4), "re-enabling restores separate locks");
        reloaded.selectContext(null);
        expect(reloaded.isLocked(0), "disconnect restores shared locks");
        reloaded.selectContext(worldA);
        reloaded.selectProfile(new UUID(0, 2));
        expect(!reloaded.hasLocks(), "world locks isolated by profile");

        reloaded.selectProfile(null);
        expect(reloaded.toggle(5), "world-scoped temporary lock");
        reloaded.selectContext(worldB);
        expect(!reloaded.hasLocks(), "temporary locks follow scopes");
        reloaded.selectContext(worldA);
        expect(reloaded.isLocked(5), "temporary lock restored on return");
        ConfigStore restarted = new ConfigStore(path);
        restarted.selectProfile(null);
        restarted.selectContext(worldA);
        expect(!restarted.hasLocks(), "temporary locks are never persisted");
    }

    private static void expect(boolean condition, String scenario) {
        if (!condition) {
            throw new AssertionError(scenario);
        }
    }
}
