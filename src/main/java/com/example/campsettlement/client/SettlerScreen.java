package com.example.campsettlement.client;

import com.example.campsettlement.action.CampActions;
import com.example.campsettlement.entity.SettlerEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class SettlerScreen extends Screen {
    private final int entityId;

    public SettlerScreen(int entityId) {
        super(Component.literal("Поселенец"));
        this.entityId = entityId;
    }

    private SettlerEntity settler() {
        if (minecraft == null || minecraft.level == null) return null;
        Entity e = minecraft.level.getEntity(entityId);
        return e instanceof SettlerEntity s ? s : null;
    }

    @Override
    protected void init() {
        SettlerEntity s = settler();
        if (s == null) return;

        int cx = width / 2;
        int top = height / 2 - 75;

        if (!s.isAccepted()) {
            addRenderableWidget(Button.builder(Component.literal("Принять в поселение"), b -> {
                CampClient.acceptSettler(entityId);
                onClose();
            }).bounds(cx - 105, top + 45, 210, 20).build());

            addRenderableWidget(Button.builder(Component.literal("Отказать"), b -> {
                CampClient.rejectSettler(entityId);
                onClose();
            }).bounds(cx - 105, top + 70, 210, 20).build());
        } else {
            addJobButton(cx - 210, top + 45, "Фермер", "farmer");
            addJobButton(cx + 5, top + 45, "Строитель", "builder");
            addJobButton(cx - 210, top + 70, "Страж", "guard");
            addJobButton(cx + 5, top + 70, "Лесоруб", "woodcutter");
            addJobButton(cx - 105, top + 95, "Без работы", "idle");
        }

        addRenderableWidget(Button.builder(Component.literal("Закрыть"), b -> onClose())
                .bounds(cx - 50, top + 130, 100, 20).build());
    }

    private void addJobButton(int x, int y, String text, String job) {
        addRenderableWidget(Button.builder(Component.literal(text), b -> {
            CampClient.assignJob(entityId, job);
            onClose();
        }).bounds(x, y, 205, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        SettlerEntity s = settler();
        int cx = width / 2;
        int top = height / 2 - 75;

        if (s == null) {
            g.drawCenteredString(font, "Житель больше недоступен.", cx, top, 0xFF7777);
        } else if (!s.isAccepted()) {
            g.drawCenteredString(font, "Путник просит разрешения остаться.", cx, top - 20, 0xFFFFFF);
            g.drawCenteredString(font, "Свободное место зависит от количества палаток.", cx, top, 0xAAAAAA);
        } else {
            g.drawCenteredString(font, s.getName(), cx, top - 20, 0xFFFFFF);
            g.drawCenteredString(font, "Текущая работа: " + CampActions.jobName(s.getJob()), cx, top, 0xBFE8FF);
            g.drawCenteredString(font, "Фермеру нужен огород, лесорубу — лесной навес.", cx, top + 16, 0xAAAAAA);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
