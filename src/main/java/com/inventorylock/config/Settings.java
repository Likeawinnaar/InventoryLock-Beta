package com.inventorylock.config;

import java.util.Locale;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class Settings {
    public boolean enabled = true;
    public boolean showLockIcons = true;
    public boolean preventDropping = true;
    public boolean perServerLocks = true;
    public boolean perWorldLocks;
    public Modifier lockModifier = Modifier.LEFT_ALT;
    public Style overlayStyle = Style.ICON_ONLY;
    public int overlayOpacity = 20;
    public Color indicatorColor = Color.WHITE;

    public Settings copy() {
        Settings copy = new Settings();
        copy.enabled = enabled;
        copy.showLockIcons = showLockIcons;
        copy.preventDropping = preventDropping;
        copy.perServerLocks = perServerLocks;
        copy.perWorldLocks = perWorldLocks;
        copy.lockModifier = lockModifier;
        copy.overlayStyle = overlayStyle;
        copy.overlayOpacity = overlayOpacity;
        copy.indicatorColor = indicatorColor;
        return copy;
    }

    boolean sameValues(Settings other) {
        return enabled == other.enabled && showLockIcons == other.showLockIcons
                && preventDropping == other.preventDropping && perServerLocks == other.perServerLocks
                && perWorldLocks == other.perWorldLocks && lockModifier == other.lockModifier
                && overlayStyle == other.overlayStyle && overlayOpacity == other.overlayOpacity
                && indicatorColor == other.indicatorColor;
    }

    public enum Modifier {
        LEFT_ALT(GLFW.GLFW_KEY_LEFT_ALT), RIGHT_ALT(GLFW.GLFW_KEY_RIGHT_ALT),
        LEFT_SHIFT(GLFW.GLFW_KEY_LEFT_SHIFT), RIGHT_SHIFT(GLFW.GLFW_KEY_RIGHT_SHIFT),
        LEFT_CONTROL(GLFW.GLFW_KEY_LEFT_CONTROL), RIGHT_CONTROL(GLFW.GLFW_KEY_RIGHT_CONTROL),
        LEFT_SUPER(GLFW.GLFW_KEY_LEFT_SUPER), RIGHT_SUPER(GLFW.GLFW_KEY_RIGHT_SUPER);

        public final int key;
        Modifier(int key) { this.key = key; }
    }

    public enum Style {
        ICON_ONLY, SUBTLE_OVERLAY, ICON_AND_OVERLAY
    }

    public enum Color {
        WHITE(0xF4F4F4),
        LIGHT_GRAY(0xB0B0B0),
        GRAY(0x686B77),
        BLACK(0x2A2C35),
        BROWN(0x98572C),
        RED(0xF3363D),
        ORANGE(0xFA8E21),
        YELLOW(0xFED93F),
        LIME(0x7CDB3D),
        GREEN(0x329949),
        CYAN(0x26C4CF),
        LIGHT_BLUE(0x5BBDF7),
        BLUE(0x366ADA),
        PURPLE(0x8A48CF),
        MAGENTA(0xD14ABB),
        PINK(0xF587B8);

        public final int rgb;
        public final Identifier texture;

        Color(int rgb) {
            this.rgb = rgb;
            String name = name().toLowerCase(Locale.ROOT);
            this.texture = Identifier.of("inventorylock", "textures/gui/locks/inventorylock_" + name + ".png");
        }

        static Color fromConfig(String name) {
            // Map the color from older configurations to the closest supplied sprite.
            return "GOLD".equals(name) ? YELLOW : valueOf(name);
        }
    }
}
