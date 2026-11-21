package com.inventorylock.gui;

import com.inventorylock.InventoryLockClient;
import com.inventorylock.config.ConfigStore;
import com.inventorylock.config.Settings;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public final class InventoryLockScreen extends Screen {
    private static final int ROW = 20;
    private static final int WIDGET_HEIGHT = 18;

    private final Screen parent;
    private final ConfigStore config;
    private final Settings draft;
    private SliderWidget opacity;
    private Text feedback;
    private int feedbackColor = 0xFF8181;
    private int top;

    public InventoryLockScreen(Screen parent) {
        super(label("title"));
        this.parent = parent;
        this.config = InventoryLockClient.configuration();
        this.draft = config.settings().copy();
        if (config.readOnly()) {
            feedback = label("error.read");
        }
    }

    @Override
    protected void init() {
        int widgetWidth = Math.min(250, width - 20);
        int left = (width - widgetWidth) / 2;
        top = Math.max(3, (height - 236) / 2);
        int y = top + 18;
        int half = (widgetWidth - 4) / 2;

        addDrawableChild(onOff(draft.enabled)
                .build(left, y, widgetWidth, WIDGET_HEIGHT, label("enabled"),
                        (button, value) -> draft.enabled = value));
        addDrawableChild(onOff(draft.showLockIcons)
                .build(left, y += ROW, widgetWidth, WIDGET_HEIGHT, label("icons"),
                        (button, value) -> draft.showLockIcons = value));
        addDrawableChild(onOff(draft.preventDropping)
                .build(left, y += ROW, widgetWidth, WIDGET_HEIGHT, label("drops"),
                        (button, value) -> draft.preventDropping = value));
        addDrawableChild(onOff(draft.perServerLocks)
                .tooltip(value -> Tooltip.of(label("scope.server.tip")))
                .build(left, y += ROW, half, WIDGET_HEIGHT, label("scope.server"),
                        (button, value) -> draft.perServerLocks = value));
        addDrawableChild(onOff(draft.perWorldLocks)
                .tooltip(value -> Tooltip.of(label("scope.world.tip")))
                .build(left + half + 4, y, half, WIDGET_HEIGHT, label("scope.world"),
                        (button, value) -> draft.perWorldLocks = value));
        addDrawableChild(CyclingButtonWidget.<Settings.Modifier>builder(value -> enumLabel("modifier", value))
                .values(Settings.Modifier.values()).initially(draft.lockModifier)
                .build(left, y += ROW, widgetWidth, WIDGET_HEIGHT, label("modifier"),
                        (button, value) -> draft.lockModifier = value));
        addDrawableChild(CyclingButtonWidget.<Settings.Style>builder(value -> enumLabel("style", value))
                .values(Settings.Style.values()).initially(draft.overlayStyle)
                .build(left, y += ROW, widgetWidth, WIDGET_HEIGHT, label("style"), (button, value) -> {
                    draft.overlayStyle = value;
                    opacity.active = value != Settings.Style.ICON_ONLY;
                }));
        opacity = addDrawableChild(new SliderWidget(left, y += ROW, widgetWidth, WIDGET_HEIGHT,
                Text.empty(), draft.overlayOpacity / 60.0) {
            {
                updateMessage();
            }

            @Override
            protected void updateMessage() {
                setMessage(Text.translatable("inventorylock.opacity", draft.overlayOpacity));
            }

            @Override
            protected void applyValue() {
                draft.overlayOpacity = (int) Math.round(value * 60);
            }
        });
        opacity.active = draft.overlayStyle != Settings.Style.ICON_ONLY;
        addDrawableChild(CyclingButtonWidget.<Settings.Color>builder(value -> enumLabel("color", value))
                .values(Settings.Color.values()).initially(draft.indicatorColor)
                .build(left, y += ROW, widgetWidth, WIDGET_HEIGHT, label("color"),
                        (button, value) -> draft.indicatorColor = value));
        addDrawableChild(ButtonWidget.builder(label("unlock"), button -> confirmClear())
                .dimensions(left, y + ROW, widgetWidth, WIDGET_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(label("done"), button -> save())
                .dimensions(left, top + 213, half, WIDGET_HEIGHT).build());
        addDrawableChild(ButtonWidget.builder(label("cancel"), button -> close())
                .dimensions(left + half + 4, top + 213, half, WIDGET_HEIGHT).build());
    }

    private void confirmClear() {
        if (client == null) {
            return;
        }
        client.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                if (config.clearLocks()) {
                    feedback = label("unlock.success");
                    feedbackColor = 0xFFFFFF;
                } else {
                    showSaveFailure();
                }
            }
            client.setScreen(this);
        }, label("unlock"), label("unlock.confirm"), label("unlock.action"), label("cancel")));
    }

    private void save() {
        if (config.saveSettings(draft)) {
            close();
        } else {
            showSaveFailure();
        }
    }

    private void showSaveFailure() {
        feedback = label(config.readOnly() ? "error.read" : "error.write");
        feedbackColor = 0xFF8181;
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, top + 3, 0xFFFFFF);
        if (feedback != null) {
            context.drawCenteredTextWithShadow(textRenderer, feedback, width / 2, top + 201, feedbackColor);
        }
    }

    private static CyclingButtonWidget.Builder<Boolean> onOff(boolean value) {
        return CyclingButtonWidget.onOffBuilder(label("on"), label("off")).initially(value);
    }

    private static Text label(String key) {
        return Text.translatable("inventorylock." + key);
    }

    private static Text enumLabel(String key, Enum<?> value) {
        return label(key + "." + value.name().toLowerCase(Locale.ROOT));
    }
}
