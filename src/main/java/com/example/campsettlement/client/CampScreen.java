package com.example.campsettlement.client;

import com.example.campsettlement.building.BuildingManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class CampScreen extends Screen {
    private final BlockPos campPos;

    public CampScreen(BlockPos campPos) {
        super(Component.literal("Управление поселением"));
        this.campPos = campPos;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int top = height / 2 - 80;

        addRenderableWidget(Button.builder(
                Component.literal("Построить палатку"),
                b -> minecraft.setScreen(new PlacementScreen(campPos, BuildingManager.TENT))
        ).bounds(cx - 105, top + 65, 210, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Построить огород"),
                b -> minecraft.setScreen(new PlacementScreen(campPos, BuildingManager.FARM))
        ).bounds(cx - 105, top + 90, 210, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Построить лесной навес"),
                b -> minecraft.setScreen(new PlacementScreen(campPos, BuildingManager.LUMBER))
        ).bounds(cx - 105, top + 115, 210, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Закрыть"), b -> onClose())
                .bounds(cx - 50, top + 150, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int cx = width / 2;
        int top = height / 2 - 80;

        CampClient.CampStats s = CampClient.stats(campPos);
        g.drawCenteredString(font, title, cx, top - 25, 0xFFFFFF);
        g.drawCenteredString(font,
                "Жители: " + s.residents() + "/" + s.capacity() + "   Ожидают решения: " + s.pending(),
                cx, top, 0xE8E8E8);
        g.drawCenteredString(font,
                "Палатки: " + s.tents() + "   Огороды: " + s.farms() + "   Навесы: " + s.lumber(),
                cx, top + 15, 0xBFE8FF);

        g.drawCenteredString(font, "Ресурсы берутся из твоего инвентаря.", cx, top + 35, 0xD6C89C);
        g.drawCenteredString(font, "ПКМ по жителю — управление конкретным человеком.", cx, top + 48, 0xAAAAAA);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
