package com.example.campsettlement.client;

import com.example.campsettlement.building.BuildingManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class PlacementScreen extends Screen {
    private final BlockPos campPos;
    private final String type;

    public PlacementScreen(BlockPos campPos, String type) {
        super(Component.literal("Выбор площадки"));
        this.campPos = campPos;
        this.type = type;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int top = height / 2 - 85;

        for (int i = 0; i < BuildingManager.slotCount(); i++) {
            final int slot = i;
            int col = i % 2;
            int row = i / 2;
            boolean free = CampClient.slotFree(campPos, type, slot);

            Button button = Button.builder(
                    Component.literal("Площадка " + (i + 1) + (free ? " — свободна" : " — занята")),
                    b -> {
                        CampClient.build(campPos, type, slot);
                        minecraft.setScreen(new CampScreen(campPos));
                    }
            ).bounds(cx - 210 + col * 215, top + row * 25, 205, 20).build();

            button.active = free;
            addRenderableWidget(button);
        }

        addRenderableWidget(Button.builder(Component.literal("Назад"), b -> minecraft.setScreen(new CampScreen(campPos)))
                .bounds(cx - 50, top + 115, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int cx = width / 2;
        int top = height / 2 - 85;

        g.drawCenteredString(font, "Строительство: " + BuildingManager.displayName(type), cx, top - 35, 0xFFFFFF);
        g.drawCenteredString(font, "Цена: " + BuildingManager.costText(type), cx, top - 20, 0xD6C89C);
        g.drawCenteredString(font, "Выбери одну из свободных площадок вокруг лагеря.", cx, top - 5, 0xAAAAAA);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
