package com.inventorylock.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.inventorylock.locking.SlotResolver;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigStore {
    private static final Logger LOGGER = LoggerFactory.getLogger("inventorylock");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int VERSION = 1;

    private final Path path;
    private JsonObject document = new JsonObject();
    private Settings settings = new Settings();
    private final BitSet locks = new BitSet();
    private final Map<String, BitSet> sessionLocks = new HashMap<>();
    private UUID profile;
    private LockContext context;
    private String scope;
    private boolean profileLoaded;
    private boolean readOnly;
    private boolean reportedWriteFailure;

    public ConfigStore(Path path) {
        this.path = path;
        if (Files.notExists(path)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(path); JsonReader json = new JsonReader(reader)) {
            JsonObject loaded = GSON.getAdapter(JsonElement.class).read(json).getAsJsonObject();
            if (json.peek() != JsonToken.END_DOCUMENT) {
                throw new IOException("Unexpected data after the configuration");
            }
            if (loaded.has("configVersion") && integer(loaded.get("configVersion")) != VERSION) {
                throw new IOException("Unsupported config version");
            }
            if (loaded.has("profiles") && !loaded.get("profiles").isJsonObject()) {
                throw new IOException("Invalid profile storage");
            }
            if (loaded.has("scopedProfiles") && !loaded.get("scopedProfiles").isJsonObject()) {
                throw new IOException("Invalid scoped profile storage");
            }
            document = loaded;
            settings.enabled = read("enabled", true, ConfigStore::bool);
            settings.showLockIcons = read("showLockIcons", true, ConfigStore::bool);
            settings.preventDropping = read("preventDropping", true, ConfigStore::bool);
            settings.perServerLocks = read("perServerLocks", settings.perServerLocks, ConfigStore::bool);
            settings.perWorldLocks = read("perWorldLocks", false, ConfigStore::bool);
            settings.lockModifier = read("lockModifier", settings.lockModifier,
                    value -> Settings.Modifier.valueOf(value.getAsString()));
            settings.overlayStyle = read("overlayStyle", settings.overlayStyle,
                    value -> Settings.Style.valueOf(value.getAsString()));
            settings.overlayOpacity = read("overlayOpacity", 20, value -> {
                double opacity = value.getAsDouble();
                if (!Double.isFinite(opacity) || opacity < 0 || opacity > 0.6) {
                    throw new IllegalArgumentException("Invalid opacity");
                }
                return (int) Math.round(opacity * 100);
            });
            settings.indicatorColor = read("indicatorColor", settings.indicatorColor,
                    value -> Settings.Color.fromConfig(value.getAsString()));
        } catch (IOException | RuntimeException e) {
            readOnly = true;
            LOGGER.warn("Could not read {}. The original configuration was preserved.", path, e);
        }
    }

    private static int integer(JsonElement value) {
        return value.getAsBigDecimal().intValueExact();
    }

    private static boolean bool(JsonElement value) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Expected a boolean");
        }
        return value.getAsBoolean();
    }

    private <T> T read(String key, T fallback, Function<JsonElement, T> parser) {
        JsonElement value = document.get(key);
        if (value == null) {
            return fallback;
        }
        try {
            return parser.apply(value);
        } catch (RuntimeException e) {
            LOGGER.warn("Ignoring invalid InventoryLock setting {}", key);
            return fallback;
        }
    }

    public Settings settings() {
        return settings;
    }

    public boolean readOnly() {
        return readOnly;
    }

    public void selectProfile(UUID id) {
        if (profileLoaded && Objects.equals(profile, id)) {
            return;
        }
        profile = id;
        profileLoaded = true;
        sessionLocks.clear();
        loadLocks();
    }

    public void selectContext(LockContext context) {
        this.context = context;
        refreshScope();
    }

    private void refreshScope() {
        String selected = null;
        if (context != null) {
            boolean scoped = switch (context.type()) {
                case WORLD -> settings.perWorldLocks;
                case SERVER -> settings.perServerLocks;
            };
            if (scoped) {
                selected = context.key();
            }
        }
        if (!Objects.equals(scope, selected)) {
            scope = selected;
            loadLocks();
        }
    }

    private void loadLocks() {
        locks.clear();
        if (profile == null) {
            BitSet saved = sessionLocks.get(scope);
            if (saved != null) {
                locks.or(saved);
            }
            return;
        }
        String id = profile.toString();
        JsonElement saved;
        if (scope == null) {
            saved = object(document.get("profiles")).get(id);
        } else {
            JsonObject scopedProfiles = object(document.get("scopedProfiles"));
            saved = object(scopedProfiles.get(id)).get(scope);
        }
        if (saved == null || !saved.isJsonArray()) {
            return;
        }
        for (JsonElement value : saved.getAsJsonArray()) {
            try {
                int index = integer(value);
                if (SlotResolver.supported(index)) {
                    locks.set(index);
                }
            } catch (RuntimeException ignored) {
                LOGGER.debug("Ignoring an invalid InventoryLock slot index");
            }
        }
    }

    public boolean isLocked(int index) {
        return settings.enabled && SlotResolver.supported(index) && locks.get(index);
    }

    public boolean hasLocks() {
        return settings.enabled && !locks.isEmpty();
    }

    public boolean toggle(int index) {
        if (!SlotResolver.supported(index)) {
            return false;
        }
        BitSet updated = (BitSet) locks.clone();
        updated.flip(index);
        return commit(settings, updated);
    }

    public boolean clearLocks() {
        return !readOnly && (locks.isEmpty() || commit(settings, new BitSet()));
    }

    public boolean saveSettings(Settings updated) {
        if (settings.sameValues(updated)) {
            return !readOnly;
        }
        return commit(updated.copy(), (BitSet) locks.clone());
    }

    private static JsonObject object(JsonElement value) {
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
    }

    private boolean commit(Settings updated, BitSet nextLocks) {
        if (readOnly) {
            return false;
        }
        if (profile == null && updated == settings) {
            if (nextLocks.isEmpty()) {
                sessionLocks.remove(scope);
            } else {
                sessionLocks.put(scope, nextLocks);
            }
        } else {
            JsonObject next = document.deepCopy();
            writeSettings(next, updated);
            if (profile != null && !locks.equals(nextLocks)) {
                writeLocks(next, nextLocks);
            }
            if (!next.equals(document) && !write(next)) {
                return false;
            }
            document = next;
        }
        settings = updated;
        locks.clear();
        locks.or(nextLocks);
        refreshScope();
        return true;
    }

    private static void writeSettings(JsonObject next, Settings updated) {
        next.addProperty("configVersion", VERSION);
        next.addProperty("enabled", updated.enabled);
        next.addProperty("showLockIcons", updated.showLockIcons);
        next.addProperty("preventDropping", updated.preventDropping);
        next.addProperty("perServerLocks", updated.perServerLocks);
        next.addProperty("perWorldLocks", updated.perWorldLocks);
        next.addProperty("lockModifier", updated.lockModifier.name());
        next.addProperty("overlayStyle", updated.overlayStyle.name());
        next.addProperty("overlayOpacity", updated.overlayOpacity / 100.0);
        next.addProperty("indicatorColor", updated.indicatorColor.name());
    }

    private void writeLocks(JsonObject next, BitSet nextLocks) {
        String id = profile.toString();
        if (scope == null) {
            JsonObject profiles = object(next.get("profiles"));
            putLocks(profiles, id, nextLocks);
            putObject(next, "profiles", profiles);
        } else {
            JsonObject scopedProfiles = object(next.get("scopedProfiles"));
            JsonObject locations = object(scopedProfiles.get(id));
            putLocks(locations, scope, nextLocks);
            putObject(scopedProfiles, id, locations);
            putObject(next, "scopedProfiles", scopedProfiles);
        }
    }

    private static void putLocks(JsonObject target, String key, BitSet nextLocks) {
        if (nextLocks.isEmpty()) {
            target.remove(key);
            return;
        }
        JsonArray slots = new JsonArray();
        nextLocks.stream().forEach(slots::add);
        target.add(key, slots);
    }

    private static void putObject(JsonObject target, String key, JsonObject value) {
        if (value.isEmpty()) {
            target.remove(key);
        } else {
            target.add(key, value);
        }
    }

    private boolean write(JsonObject next) {
        Path temporary = null;
        try {
            Files.createDirectories(path.getParent());
            temporary = Files.createTempFile(path.getParent(), "inventorylock-", ".json.tmp");
            Files.writeString(temporary, GSON.toJson(next) + "\n");
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            reportedWriteFailure = false;
            return true;
        } catch (IOException | SecurityException e) {
            if (!reportedWriteFailure) {
                LOGGER.warn("Could not save {}", path, e);
                reportedWriteFailure = true;
            }
            return false;
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    LOGGER.debug("Could not remove temporary settings file", e);
                }
            }
        }
    }
}
