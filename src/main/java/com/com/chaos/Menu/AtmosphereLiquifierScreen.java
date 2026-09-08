package com.com.chaos.Menu;

import com.com.chaos.ChaoticsCreate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AtmosphereLiquifierScreen extends AbstractContainerScreen<AtmosphereLiquifierMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ChaoticsCreate.MODID, "textures/gui/container/atmosphere_liquifier_gui.png");

    private static final int BAR_WIDTH = 91;
    private static final int BAR_HEIGHT = 15;

    private static final int OXYGEN_BAR_X = 38;
    private static final int OXYGEN_BAR_Y = 15;

    private static final int NITROGEN_BAR_X = 38;
    private static final int NITROGEN_BAR_Y = 35;

    private static final int ARGON_BAR_X = 38;
    private static final int ARGON_BAR_Y = 55;

    private static final int OXYGEN_COLOR = 0xFF6EB8FF;
    private static final int NITROGEN_COLOR = 0xFFFFFFFF;
    private static final int ARGON_COLOR = 0xFFC9A0FF;

    public AtmosphereLiquifierScreen(AtmosphereLiquifierMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderFluidTooltips(guiGraphics, mouseX, mouseY);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        drawFluidBar(guiGraphics, x + OXYGEN_BAR_X, y + OXYGEN_BAR_Y, menu.getOxygenBarWidth(BAR_WIDTH), OXYGEN_COLOR);
        drawFluidBar(guiGraphics, x + NITROGEN_BAR_X, y + NITROGEN_BAR_Y, menu.getNitrogenBarWidth(BAR_WIDTH), NITROGEN_COLOR);
        drawFluidBar(guiGraphics, x + ARGON_BAR_X, y + ARGON_BAR_Y, menu.getArgonBarWidth(BAR_WIDTH), ARGON_COLOR);
    }

    private void drawFluidBar(GuiGraphics guiGraphics, int x, int y, int filledWidth, int color) {
        if (filledWidth <= 0) {
            return;
        }

        guiGraphics.fill(x, y, x + Math.min(filledWidth, BAR_WIDTH), y + BAR_HEIGHT, color);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    private void renderFluidTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHoveringBar(mouseX, mouseY, OXYGEN_BAR_X, OXYGEN_BAR_Y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.literal("Liquid Oxygen: " + menu.getOxygenStored() + " / " + menu.getCapacity() + " mB"),
                    mouseX,
                    mouseY
            );
            return;
        }

        if (isHoveringBar(mouseX, mouseY, NITROGEN_BAR_X, NITROGEN_BAR_Y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.literal("Liquid Nitrogen: " + menu.getNitrogenStored() + " / " + menu.getCapacity() + " mB"),
                    mouseX,
                    mouseY
            );
            return;
        }

        if (isHoveringBar(mouseX, mouseY, ARGON_BAR_X, ARGON_BAR_Y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.literal("Liquid Argon: " + menu.getArgonStored() + " / " + menu.getCapacity() + " mB"),
                    mouseX,
                    mouseY
            );
        }
    }

    private boolean isHoveringBar(int mouseX, int mouseY, int barX, int barY) {
        int x = leftPos + barX;
        int y = topPos + barY;
        return mouseX >= x && mouseX < x + BAR_WIDTH && mouseY >= y && mouseY < y + BAR_HEIGHT;
    }
}
