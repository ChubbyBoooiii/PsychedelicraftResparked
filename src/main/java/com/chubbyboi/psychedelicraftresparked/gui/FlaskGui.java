package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.client.rendering.FluidGuiRenderer;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityFlask;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;
import java.util.Collections;

public class FlaskGui extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID + ":textures/gui/gui_flask.png");

    private final TileEntityFlask tileentity;
    private GuiButton transferDirectionButton;

    public FlaskGui(InventoryPlayer player, TileEntityFlask tileentity) {
        super(new FlaskContainer(player, tileentity));
        this.tileentity = tileentity;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        this.transferDirectionButton = new GuiButton(FlaskContainer.TOGGLE_DIRECTION_BUTTON_ID, this.guiLeft + 7, this.guiTop + 60, 50, 20, "");
        this.buttonList.add(this.transferDirectionButton);
        updateTransferButtonTitle();
    }

    private void updateTransferButtonTitle() {
        this.transferDirectionButton.displayString = I18n.format(this.tileentity.drainingMode ? "container.flask.drain" : "container.flask.fill");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == FlaskContainer.TOGGLE_DIRECTION_BUTTON_ID) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, button.id);
            this.tileentity.drainingMode = !this.tileentity.drainingMode;
            updateTransferButtonTitle();
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
            String name = fluid != null ? fluid.getLocalizedName() : I18n.format("container.flask.empty");
            int amount = fluid != null ? fluid.amount : 0;
            this.drawHoveringText(Collections.singletonList(
                name + " (" + amount + "mB / " + TileEntityFlask.CAPACITY + "mB)"
            ), mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.flask"), 8, 6, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        int tankX = this.guiLeft + 60;
        int tankY = this.guiTop + 14;
        int tankWidth = 108;
        int tankHeight = 57;
        FluidStack fluid = this.tileentity.getTank().getFluid();
        int fillHeight = TileEntityFlask.CAPACITY > 0
            ? (tankHeight * this.tileentity.getTank().getFluidAmount()) / TileEntityFlask.CAPACITY
            : 0;
        if (fluid != null && fillHeight > 0) {
            FluidGuiRenderer.drawTiledFluidRect(fluid, tankX, tankY + tankHeight - fillHeight, tankWidth, fillHeight);
        }
    }
}