package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.client.rendering.FluidGuiRenderer;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import com.chubbyboi.psychedelicraftresparked.util.TimeHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class BarrelGui extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID + ":textures/gui/gui_barrel.png");

    private final TileEntityBarrel tileentity;
    private GuiButton sealButton;

    public BarrelGui(InventoryPlayer player, TileEntityBarrel tileentity) {
        super(new BarrelContainer(player, tileentity));
        this.tileentity = tileentity;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        // Halfway between the arrow's bottom (31) and the tank's bottom (14+57=71), centred for the
        // button's 20px height.
        this.sealButton = new GuiButton(BarrelContainer.SEAL_BUTTON_ID, this.guiLeft + 7, this.guiTop + 41, 50, 20, "");
        this.buttonList.add(this.sealButton);
        updateSealButton();
    }

    // A real toggle - sealing is reversible so a barrel can be unsealed and reused once its aged
    // batch has been drained out through the tap.
    private void updateSealButton() {
        this.sealButton.displayString = I18n.format(this.tileentity.isSealed() ? "container.barrel.unseal" : "container.barrel.seal");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BarrelContainer.SEAL_BUTTON_ID) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, button.id);
            this.tileentity.toggleSealed();
            updateSealButton();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        updateSealButton();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        int tankX = this.guiLeft + 60;
        int tankY = this.guiTop + 14;
        if (mouseX >= tankX && mouseX < tankX + 108 && mouseY >= tankY && mouseY < tankY + 57) {
            FluidStack fluid = this.tileentity.getTank().getFluid();
            String name = fluid != null ? fluid.getLocalizedName() : I18n.format("container.barrel.empty");
            int amount = fluid != null ? fluid.amount : 0;
            this.drawHoveringText(Collections.singletonList(
                name + " (" + amount + "mB / " + TileEntityBarrel.CAPACITY + "mB)"
            ), mouseX, mouseY);
        }

        int arrowX = this.guiLeft + 23;
        int arrowY = this.guiTop + 14;
        if (mouseX >= arrowX && mouseX < arrowX + 24 && mouseY >= arrowY && mouseY < arrowY + 17) {
            if (this.tileentity.isMaturing()) {
                int progress = this.tileentity.getTimeFermented();
                int total = this.tileentity.getNeededMaturationTime();
                int percent = total > 0 ? Math.min(100, (int) (progress * 100.0 / total)) : 0;
                int ticksLeft = Math.max(0, total - progress);
                this.drawHoveringText(Arrays.asList(
                    I18n.format("container.barrel.maturing", percent),
                    I18n.format("container.barrel.time_left", TimeHelper.formatTicksAsTime(ticksLeft))
                ), mouseX, mouseY);
            } else if (!this.tileentity.isSealed()) {
                this.drawHoveringText(Collections.singletonList(I18n.format("container.barrel.needs_sealing")), mouseX, mouseY);
            } else if (this.tileentity.hasTap()) {
                this.drawHoveringText(Collections.singletonList(I18n.format("container.barrel.tapped_paused")), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.barrel"), 8, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 74, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        if (this.tileentity.isMaturing()) {
            int total = this.tileentity.getNeededMaturationTime();
            if (total > 0) {
                int filled = (int) (24 * (long) this.tileentity.getTimeFermented() / total);
                filled = Math.max(0, Math.min(24, filled));
                if (filled > 0) {
                    this.drawTexturedModalRect(this.guiLeft + 23, this.guiTop + 14, 176, 0, filled, 17);
                }
            }
        }

        int tankX = this.guiLeft + 60;
        int tankY = this.guiTop + 14;
        int tankWidth = 108;
        int tankHeight = 57;
        FluidStack fluid = this.tileentity.getTank().getFluid();
        int fillHeight = TileEntityBarrel.CAPACITY > 0
            ? (tankHeight * this.tileentity.getTank().getFluidAmount()) / TileEntityBarrel.CAPACITY
            : 0;
        if (fluid != null && fillHeight > 0) {
            FluidGuiRenderer.drawTiledFluidRect(fluid, tankX, tankY + tankHeight - fillHeight, tankWidth, fillHeight);
        }
    }
}