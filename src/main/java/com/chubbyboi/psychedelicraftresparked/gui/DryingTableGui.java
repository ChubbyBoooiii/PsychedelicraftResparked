package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDryingTable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import java.util.Arrays;

public class DryingTableGui extends GuiContainer {

    private static final ResourceLocation TEXTURES = new ResourceLocation(Tags.MOD_ID + ":textures/gui/gui_drying_table.png");
    private final InventoryPlayer player;
    private final TileEntityDryingTable tileentity;

    public DryingTableGui(InventoryPlayer player, TileEntityDryingTable tileentity) {
        super(new DryingTableContainer(player, tileentity));
        this.player = player;
        this.tileentity = tileentity;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        // Daylight Tooltip
        int sunX = this.guiLeft + 148;
        int sunY = this.guiTop + 26;
        if (mouseX >= sunX && mouseX < sunX + 23 && mouseY <= sunY && mouseY > sunY - 23) {
            // Check if it's raining and can see sky - this stops the drying process
            if (this.mc.world.isRainingAt(this.tileentity.getPos().up())) {
                this.drawHoveringText(I18n.format("container.drying_table.rain_warning"), mouseX, mouseY);
            } else {
                this.drawHoveringText(Arrays.asList(
                    I18n.format("container.drying_table.drying_efficiency"),
                    I18n.format("container.drying_table.light_percent", this.tileentity.getField(3) / 10.0),
                    I18n.format("container.drying_table.temp_percent", this.tileentity.getField(4) / 10),
                    I18n.format("container.drying_table.total_speed", this.tileentity.getField(2) / 10.0)
                ), mouseX, mouseY);
            }
        }

        // Progress % Tooltip
        if (this.tileentity.getField(1) != 0) {
            int progressX = this.guiLeft + 88;
            int progressY = this.guiTop + 35 + 16;
            float dryingProgress = this.tileentity.getField(0) / 1000F;
            int totalDryingTime = this.tileentity.getField(1);
            // Percent to 1dp
            double dryingPercent = Math.round(dryingProgress / totalDryingTime * 1000) / 10.0;

            if (mouseX >= progressX && mouseX < progressX + 24 && mouseY <= progressY && mouseY > progressY - 16) {
                this.drawHoveringText(I18n.format("container.drying_table.progress", dryingPercent), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String tileName = this.tileentity.getDisplayName().getUnformattedText();
        this.fontRenderer.drawString(tileName, 28, 6, 4210752);
        this.fontRenderer.drawString(this.player.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        // Initial Drying Texture
        int totalDryingTime = this.tileentity.getField(1);
        if (totalDryingTime > 0.0F) {
            this.drawTexturedModalRect(this.guiLeft + 88, this.guiTop + 35, 176, 59, 25, 16);
        }

        // Progress Texture
        int dryingProgressScaled = this.getDryProgressScaled(24);
        this.drawTexturedModalRect(this.guiLeft + 88, this.guiTop + 35, 176, 42, dryingProgressScaled + 1, 16);

        // Heat Ratio % Texture
        int heatPixels = this.getHeatRatioScaled(20);
        this.drawTexturedModalRect(this.guiLeft + 148, this.guiTop + 6 + (20 - heatPixels), 176, 21 + (20 - heatPixels), 21, heatPixels);
    }

    private int getDryProgressScaled (int pixels) {
        float dryingProgress = this.tileentity.getField(0) / 1000F;
        int totalDryingTime = this.tileentity.getField(1);
        return totalDryingTime != 0 && dryingProgress != 0 ? (int)(dryingProgress * pixels / totalDryingTime) : 0;
    }

    private int getHeatRatioScaled (int pixels) {
        if (this.mc.world.isRainingAt(this.tileentity.getPos().up())) {
            return 0;
        } else {
            float heatRatio = this.tileentity.getField(2) / 1000F;
            return heatRatio != 0 ? (int)(heatRatio * pixels) : 0;
        }
    }
}
