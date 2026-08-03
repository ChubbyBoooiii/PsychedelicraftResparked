package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.client.rendering.FluidGuiRenderer;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidSlurry;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import com.chubbyboi.psychedelicraftresparked.util.TimeHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class VatGui extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID + ":textures/gui/gui_vat.png");

    private final TileEntityVat tileentity;
    private GuiButton transferDirectionButton;

    public VatGui(InventoryPlayer player, TileEntityVat tileentity) {
        super(new VatContainer(player, tileentity));
        this.tileentity = tileentity;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        this.transferDirectionButton = new GuiButton(VatContainer.TOGGLE_DIRECTION_BUTTON_ID, this.guiLeft + 7, this.guiTop + 60, 50, 20, "");
        this.buttonList.add(this.transferDirectionButton);
        updateTransferButtonTitle();
    }

    private void updateTransferButtonTitle() {
        this.transferDirectionButton.displayString = I18n.format(this.tileentity.drainingMode ? "container.vat.drain" : "container.vat.fill");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == VatContainer.TOGGLE_DIRECTION_BUTTON_ID) {
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
        if (mouseX >= tankX && mouseX < tankX + 108 && mouseY >= tankY && mouseY < tankY + 57
                && !isMouseOverVisibleIngredientSlot(mouseX, mouseY)) {
            FluidStack fluid = this.tileentity.getTank().getFluid();
            String name = fluid != null ? fluid.getLocalizedName() : I18n.format("container.vat.empty");
            int amount = fluid != null ? fluid.amount : 0;
            this.drawHoveringText(Collections.singletonList(
                name + " (" + amount + "mB / " + TileEntityVat.CAPACITY + "mB)"
            ), mouseX, mouseY);
        }

        int arrowX = this.guiLeft + 23;
        int arrowY = this.guiTop + 14;
        if (mouseX >= arrowX && mouseX < arrowX + 24 && mouseY >= arrowY && mouseY < arrowY + 17) {
            if (this.tileentity.fermenting && this.tileentity.totalFermentationTime > 0) {
                int progress = this.tileentity.fermentationProgress;
                int total = this.tileentity.totalFermentationTime;
                int percent = Math.min(100, (int) (progress * 100.0 / total));
                int ticksLeft = Math.max(0, total - progress);
                String labelKey = isSpoiling() ? "container.vat.spoiling"
                    : isHardening() ? "container.vat.hardening"
                    : "container.vat.fermenting";
                this.drawHoveringText(Arrays.asList(
                    I18n.format(labelKey, percent),
                    I18n.format("container.vat.time_left", TimeHelper.formatTicksAsTime(ticksLeft))
                ), mouseX, mouseY);
            } else {
                this.drawHoveringText(Collections.singletonList(I18n.format("container.vat.no_recipe")), mouseX, mouseY);
            }
        }
    }

    private boolean isMouseOverVisibleIngredientSlot(int mouseX, int mouseY) {
        for (int i = 0; i < TileEntityVat.INGREDIENT_SLOTS; i++) {
            Slot slot = this.inventorySlots.inventorySlots.get(i);
            if (slot.isEnabled() && isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpoiling() {
        FluidStack fluid = this.tileentity.getTank().getFluid();
        if (fluid == null || !(fluid.getFluid() instanceof FluidAlcohol)) {
            return false;
        }
        FluidAlcohol alcohol = (FluidAlcohol) fluid.getFluid();
        return !alcohol.isVinegar(fluid) && alcohol.getFermentation(fluid) >= FluidAlcohol.FERMENTATION_STEPS;
    }

    private boolean isHardening() {
        FluidStack fluid = this.tileentity.getTank().getFluid();
        return fluid != null && fluid.getFluid() instanceof FluidSlurry;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.vat"), 8, 6, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        if (this.tileentity.fermenting && this.tileentity.totalFermentationTime > 0) {
            int filled = (int) (24 * (long) this.tileentity.fermentationProgress / this.tileentity.totalFermentationTime);
            filled = Math.max(0, Math.min(24, filled));
            if (filled > 0) {
                this.drawTexturedModalRect(this.guiLeft + 23, this.guiTop + 14, 176, 0, filled, 17);
            }
        }

        int tankX = this.guiLeft + 60;
        int tankY = this.guiTop + 14;
        int tankWidth = 108;
        int tankHeight = 57;
        FluidStack fluid = this.tileentity.getTank().getFluid();
        int fillHeight = TileEntityVat.CAPACITY > 0
            ? (tankHeight * this.tileentity.getTank().getFluidAmount()) / TileEntityVat.CAPACITY
            : 0;
        if (fluid != null && fillHeight > 0) {
            FluidGuiRenderer.drawTiledFluidRect(fluid, tankX, tankY + tankHeight - fillHeight, tankWidth, fillHeight);
        }
    }
}