package com.brilliance.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class BrillianceClient implements ClientModInitializer {
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.brilliance.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen instanceof BrillianceScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new BrillianceScreen());
                }
            }
        });
    }

    static class BrillianceScreen extends Screen {
        private static final int BG = 0xF20B0B0D;
        private static final int PANEL = 0xFF141417;
        private static final int CARD = 0xFF1C1C20;
        private static final int BORDER = 0xFF3B3B42;
        private static final int WHITE = 0xFFF3F3F5;
        private static final int MUTED = 0xFF9A9AA3;
        private static final int SILVER = 0xFFD1D1D8;
        private static final String[] CATEGORIES = {
                "General", "Render", "Utility", "World"
        };
        private static final String[][] MODULES = {
                {"HUD", "Coordinates", "Notifications", "Waypoints"},
                {"Entity Outline", "Particles", "Fullbright", "Visual Options"},
                {"Inventory Helper", "Auto Reconnect", "Chat Tools", "Quick Actions"},
                {"World Info", "Time Display", "Biome Info", "Map Notes"}
        };
        private final boolean[][] enabled = new boolean[4][4];
        private int selectedCategory = 0;
        private long openedAt;

        protected BrillianceScreen() {
            super(Text.literal("Brilliance Client"));
            this.openedAt = System.currentTimeMillis();
        }

        @Override
        public boolean shouldPause() {
            return false;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            int anim = (int) Math.min(255, (System.currentTimeMillis() - openedAt) * 4);
            int overlay = (anim << 24) | 0x000000;
            context.fill(0, 0, width, height, overlay | 0x000000);
            int panelW = Math.min(500, width - 28);
            int panelH = Math.min(310, height - 28);
            int x = (width - panelW) / 2;
            int y = (height - panelH) / 2;

            context.fill(x + 3, y + 4, x + panelW + 3, y + panelH + 4, 0x55000000);
            context.fill(x, y, x + panelW, y + panelH, BG);
            border(context, x, y, panelW, panelH, BORDER);

            // Header
            context.fill(x, y, x + panelW, y + 53, PANEL);
            context.drawText(textRenderer, "♛", x + 14, y + 11, SILVER, false);
            context.drawText(textRenderer, "BRILLIANCE", x + 34, y + 9, WHITE, false);
            context.drawText(textRenderer, "Q U I E T   E X C E L L E N C E", x + 34, y + 25, MUTED, false);
            context.drawText(textRenderer, "v0.2.0", x + panelW - 43, y + 13, MUTED, false);
            context.fill(x, y + 52, x + panelW, y + 53, BORDER);

            // Sidebar
            int sideW = 118;
            context.fill(x, y + 53, x + sideW, y + panelH, 0xCC101013);
            for (int i = 0; i < CATEGORIES.length; i++) {
                int cy = y + 68 + i * 34;
                boolean active = i == selectedCategory;
                if (active) {
                    context.fill(x + 8, cy - 5, x + sideW - 8, cy + 22, 0xFF303036);
                    context.fill(x + 8, cy - 5, x + 10, cy + 22, SILVER);
                }
                context.drawText(textRenderer, CATEGORIES[i], x + 19, cy + 3,
                        active ? WHITE : MUTED, false);
            }
            context.drawText(textRenderer, "FABRIC 1.21.11", x + 12, y + panelH - 20, MUTED, false);

            // Main area
            int contentX = x + sideW + 14;
            int contentW = panelW - sideW - 26;
            context.drawText(textRenderer, CATEGORIES[selectedCategory], contentX, y + 67, WHITE, false);
            context.drawText(textRenderer, "Click a card to toggle its UI state", contentX, y + 82, MUTED, false);

            int gap = 8;
            int cardW = (contentW - gap) / 2;
            for (int i = 0; i < 4; i++) {
                int col = i % 2;
                int row = i / 2;
                int cx = contentX + col * (cardW + gap);
                int cy = y + 105 + row * 68;
                boolean on = enabled[selectedCategory][i];
                context.fill(cx, cy, cx + cardW, cy + 58, CARD);
                border(context, cx, cy, cardW, 58, on ? 0xFF777780 : BORDER);
                context.drawText(textRenderer, MODULES[selectedCategory][i], cx + 9, cy + 9, WHITE, false);
                context.drawText(textRenderer, "Prototype module", cx + 9, cy + 25, MUTED, false);
                int tx = cx + cardW - 35;
                int ty = cy + 39;
                context.fill(tx, ty, tx + 25, ty + 12, on ? SILVER : 0xFF45454B);
                context.fill(on ? tx + 14 : tx + 2, ty + 2, on ? tx + 23 : tx + 11, ty + 10,
                        on ? 0xFF161619 : 0xFFE8E8EB);
            }
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
            int panelW = Math.min(500, width - 28);
            int panelH = Math.min(310, height - 28);
            int x = (width - panelW) / 2;
            int y = (height - panelH) / 2;
            int sideW = 118;

            for (int i = 0; i < CATEGORIES.length; i++) {
                int cy = y + 68 + i * 34;
                if (mouseX >= x + 8 && mouseX <= x + sideW - 8
                        && mouseY >= cy - 5 && mouseY <= cy + 22) {
                    selectedCategory = i;
                    return true;
                }
            }

            int contentX = x + sideW + 14;
            int contentW = panelW - sideW - 26;
            int gap = 8;
            int cardW = (contentW - gap) / 2;
            for (int i = 0; i < 4; i++) {
                int col = i % 2;
                int row = i / 2;
                int cx = contentX + col * (cardW + gap);
                int cy = y + 105 + row * 68;
                if (mouseX >= cx && mouseX <= cx + cardW
                        && mouseY >= cy && mouseY <= cy + 58) {
                    enabled[selectedCategory][i] = !enabled[selectedCategory][i];
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        private void border(DrawContext context, int x, int y, int w, int h, int color) {
            context.fill(x, y, x + w, y + 1, color);
            context.fill(x, y + h - 1, x + w, y + h, color);
            context.fill(x, y, x + 1, y + h, color);
            context.fill(x + w - 1, y, x + w, y + h, color);
        }
    }
                                     }
