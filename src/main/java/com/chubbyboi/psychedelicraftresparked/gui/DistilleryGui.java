package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.client.rendering.FluidGuiRenderer;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDistillery;
import com.chubbyboi.psychedelicraftresparked.util.TimeHelper;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.PsychedelicraftJEIPlugin;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.PsychedelicraftRecipeCategoryUid;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class DistilleryGui extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID + ":textures/gui/gui_distillery.png");

    private final TileEntityDistillery tileentity;
    private GuiButton transferDirectionButton;

    public DistilleryGui(InventoryPlayer player, TileEntityDistillery tileentity) {
        super(new DistilleryContainer(player, tileentity));
        this.tileentity = tileentity;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        this.transferDirectionButton = new GuiButton(DistilleryContainer.TOGGLE_DIRECTION_BUTTON_ID, this.guiLeft + 7, this.guiTop + 60, 50, 20, "");
        this.buttonList.add(this.transferDirectionButton);
        updateTransferButtonTitle();
    }

    private void updateTransferButtonTitle() {
        this.transferDirectionButton.displayString = I18n.format(this.tileentity.drainingMode ? "container.distillery.drain" : "container.distillery.fill");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == DistilleryContainer.TOGGLE_DIRECTION_BUTTON_ID) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, button.id);
            this.tileentity.drainingMode = !this.tileentity.drainingMode;
            updateTransferButtonTitle();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0 && Loader.isModLoaded("jei")) {
            PsychedelicraftJEIPlugin.tryOpenRecipes(mouseX, mouseY, this.guiLeft + 24, this.guiTop + 15, 20, 13, PsychedelicraftRecipeCategoryUid.DISTILLERY);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        int tankX = this.guiLeft + 60;
        int tankY = this.guiTop + 14;
        if (mouseX >= tankX && mouseX < tankX + 108 && mouseY >= tankY && mouseY < tankY + 57) {
            FluidStack fluid = this.tileentity.getTank().getFluid();
            String name = fluid != null ? fluid.getLocalizedName() : I18n.format("container.distillery.empty");
            int amount = fluid != null ? fluid.amount : 0;
            this.drawHoveringText(Collections.singletonList(
                name + " (" + amount + "mB / " + TileEntityDistillery.CAPACITY + "mB)"
            ), mouseX, mouseY);
        }

        if (this.tileentity.isDistilling()) {
            int barX = this.guiLeft + 24;
            int barY = this.guiTop + 15;
            if (mouseX >= barX && mouseX < barX + 20 && mouseY >= barY && mouseY < barY + 13) {
                int progress = this.tileentity.getTimeDistilled();
                int total = this.tileentity.getNeededDistillationTime();
                int percent = total > 0 ? Math.min(100, (int) (progress * 100.0 / total)) : 0;
                int ticksLeft = Math.max(0, total - progress);
                this.drawHoveringText(Arrays.asList(
                    I18n.format("container.distillery.distilling", percent),
                    I18n.format("container.distillery.time_left", TimeHelper.formatTicksAsTime(ticksLeft))
                ), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.distillery"), 8, 6, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        int timeLeftDistilling = this.tileentity.getRemainingDistillationTimeScaled(13);
        if (timeLeftDistilling < 13) {
            this.drawTexturedModalRect(this.guiLeft + 24, this.guiTop + 15 + timeLeftDistilling, 176, timeLeftDistilling, 20, 13 - timeLeftDistilling);
        }

        int tankX = this.guiLeft + 60;
        int tankY = this.guiTop + 14;
        int tankWidth = 108;
        int tankHeight = 57;
        FluidStack fluid = this.tileentity.getTank().getFluid();
        int fillHeight = TileEntityDistillery.CAPACITY > 0
            ? (tankHeight * this.tileentity.getTank().getFluidAmount()) / TileEntityDistillery.CAPACITY
            : 0;
        if (fluid != null && fillHeight > 0) {
            FluidGuiRenderer.drawTiledFluidRect(fluid, tankX, tankY + tankHeight - fillHeight, tankWidth, fillHeight);
        }
    }
}