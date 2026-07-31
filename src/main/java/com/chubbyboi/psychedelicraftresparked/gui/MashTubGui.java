package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityMashTub;
import com.chubbyboi.psychedelicraftresparked.util.TimeHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class MashTubGui extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID + ":textures/gui/gui_mash_tub.png");

    private final TileEntityMashTub tileentity;
    private GuiButton startFermentingButton;
    private GuiButton transferDirectionButton;

    public MashTubGui(InventoryPlayer player, TileEntityMashTub tileentity) {
        super(new MashTubContainer(player, tileentity));
        this.tileentity = tileentity;
        this.xSize = 176;
        this.ySize = 183;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        this.transferDirectionButton = new GuiButton(MashTubContainer.TOGGLE_DIRECTION_BUTTON_ID, this.guiLeft + 7, this.guiTop + 60, 50, 20, "");
        this.buttonList.add(this.transferDirectionButton);
        updateTransferButtonTitle();

        this.startFermentingButton = new GuiButton(MashTubContainer.START_FERMENTING_BUTTON_ID,
            this.guiLeft + 66, this.guiTop + 76, 96, 20, I18n.format("container.mash_tub.start_fermenting")
        );
        this.buttonList.add(this.startFermentingButton);
    }

    private void updateTransferButtonTitle() {
        this.transferDirectionButton.displayString = I18n.format(this.tileentity.drainingMode ? "container.mash_tub.drain" : "container.mash_tub.fill");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == MashTubContainer.START_FERMENTING_BUTTON_ID) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, button.id);
        } else if (button.id == MashTubContainer.TOGGLE_DIRECTION_BUTTON_ID) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, button.id);
            this.tileentity.drainingMode = !this.tileentity.drainingMode;
            updateTransferButtonTitle();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.startFermentingButton.enabled = !this.tileentity.fermenting && this.tileentity.hasMatchingRecipe();

        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        int tankX = this.guiLeft + 60;
        int tankY = this.guiTop + 14;
        if (mouseX >= tankX && mouseX < tankX + 108 && mouseY >= tankY && mouseY < tankY + 57
                && !isMouseOverVisibleIngredientSlot(mouseX, mouseY)) {
            FluidStack fluid = this.tileentity.getTank().getFluid();
            String name = fluid != null ? fluid.getLocalizedName() : I18n.format("container.mash_tub.empty");
            int amount = fluid != null ? fluid.amount : 0;
            this.drawHoveringText(Collections.singletonList(
                name + " (" + amount + "mB / " + TileEntityMashTub.CAPACITY + "mB)"
            ), mouseX, mouseY);
        }

        if (this.tileentity.fermenting && this.tileentity.totalFermentationTime > 0) {
            int arrowX = this.guiLeft + 23;
            int arrowY = this.guiTop + 14;
            if (mouseX >= arrowX && mouseX < arrowX + 24 && mouseY >= arrowY && mouseY < arrowY + 17) {
                int progress = this.tileentity.fermentationProgress;
                int total = this.tileentity.totalFermentationTime;
                int percent = Math.min(100, (int) (progress * 100.0 / total));
                int ticksLeft = Math.max(0, total - progress);
                this.drawHoveringText(Arrays.asList(
                    I18n.format(isSpoiling() ? "container.mash_tub.spoiling" : "container.mash_tub.fermenting", percent),
                    I18n.format("container.mash_tub.time_left", TimeHelper.formatTicksAsTime(ticksLeft))
                ), mouseX, mouseY);
            }
        }
    }

    private boolean isMouseOverVisibleIngredientSlot(int mouseX, int mouseY) {
        for (int i = 0; i < TileEntityMashTub.INGREDIENT_SLOTS; i++) {
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

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.mash_tub"), 8, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 91, 4210752);
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
        int fillHeight = TileEntityMashTub.CAPACITY > 0
            ? (tankHeight * this.tileentity.getTank().getFluidAmount()) / TileEntityMashTub.CAPACITY
            : 0;
        if (fluid != null && fillHeight > 0) {
            drawTiledFluidRect(fluid, tankX, tankY + tankHeight - fillHeight, tankWidth, fillHeight);
        }
    }

    private void drawTiledFluidRect(FluidStack fluid, int x, int y, int width, int height) {
        TextureAtlasSprite sprite = this.mc.getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill(fluid).toString());

        int color = FluidHelper.getDisplayColor(fluid);
        float a = ((color >> 24) & 0xFF) / 255.0F;
        if (a <= 0.0F) {
            a = 1.0F;
        }
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(r, g, b, a);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);

        int tileSize = 32;
        for (int dx = 0; dx < width; dx += tileSize) {
            int drawWidth = Math.min(tileSize, width - dx);
            float u1 = sprite.getInterpolatedU(0.0);
            float u2 = sprite.getInterpolatedU(drawWidth * 16.0 / tileSize);
            for (int dy = 0; dy < height; dy += tileSize) {
                int drawHeight = Math.min(tileSize, height - dy);
                float v1 = sprite.getInterpolatedV(0.0);
                float v2 = sprite.getInterpolatedV(drawHeight * 16.0 / tileSize);

                int x1 = x + dx, x2 = x1 + drawWidth;
                int y1 = y + dy, y2 = y1 + drawHeight;

                buffer.pos(x1, y2, 0).tex(u1, v2).endVertex();
                buffer.pos(x2, y2, 0).tex(u2, v2).endVertex();
                buffer.pos(x2, y1, 0).tex(u2, v1).endVertex();
                buffer.pos(x1, y1, 0).tex(u1, v1).endVertex();
            }
        }
        tessellator.draw();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
    }
}