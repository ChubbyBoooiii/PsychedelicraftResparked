package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.block.ContainerShape;
import com.chubbyboi.psychedelicraftresparked.block.PlacedContainerType;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererPlacedContainers;
import com.chubbyboi.psychedelicraftresparked.recipes.BottleWorkbenchRecipes;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.List;

public class BottleWorkbenchGui extends GuiContainer {

    private static final ResourceLocation TEXTURES = new ResourceLocation(Tags.MOD_ID + ":textures/gui/gui_bottle_workbench.png");
    // Shape grid: 4x4 buttons of 14x14, like the Loom's pattern grid
    private static final int GRID_X = 60;
    private static final int GRID_Y = 13;
    private static final int GRID_COLUMNS = 4;
    private static final int BUTTON_SIZE = 14;
    // Preview box (inner area)
    private static final int PREVIEW_X = 139;
    private static final int PREVIEW_Y = 9;
    private static final int PREVIEW_WIDTH = 24;
    private static final int PREVIEW_HEIGHT = 38;
    // Pixels per block for the preview bottle
    private static final float PREVIEW_SCALE = 34.0F;

    private final InventoryPlayer player;
    private final BottleWorkbenchContainer container;

    public BottleWorkbenchGui(InventoryPlayer player, World world, BlockPos pos) {
        super(new BottleWorkbenchContainer(player, world, pos));
        this.player = player;
        this.container = (BottleWorkbenchContainer) inventorySlots;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);

        int hovered = getShapeAt(mouseX, mouseY);
        if (hovered >= 0) {
            drawHoveringText(I18n.format(Tags.MOD_ID + ".shape." + BottleWorkbenchRecipes.getShapes().get(hovered).name), mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("container.bottle_workbench"), 8, 4, 4210752);
        fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURES);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (container.getGlass().isEmpty()) {
            drawTexturedModalRect(guiLeft + 13, guiTop + 32, 176, 0, 16, 16);
        }
        if (container.getDye().isEmpty()) {
            drawTexturedModalRect(guiLeft + 33, guiTop + 32, 192, 0, 16, 16);
        }

        List<ContainerShape> shapes = BottleWorkbenchRecipes.getShapes();
        if (!container.getGlass().isEmpty()) {
            int hovered = getShapeAt(mouseX, mouseY);
            for (int i = 0; i < shapes.size(); i++) {
                int v = i == container.getSelectedShape() ? ySize + BUTTON_SIZE : i == hovered ? ySize + BUTTON_SIZE * 2 : ySize;
                drawTexturedModalRect(guiLeft + getButtonX(i), guiTop + getButtonY(i), 0, v, BUTTON_SIZE, BUTTON_SIZE);
            }
            for (int i = 0; i < shapes.size(); i++) {
                ItemStack icon = BottleWorkbenchRecipes.getResult(container.getGlass(), container.getDye(), shapes.get(i));
                GlStateManager.pushMatrix();
                GlStateManager.translate(guiLeft + getButtonX(i) + 1, guiTop + getButtonY(i) + 1, 0.0F);
                GlStateManager.scale(0.75F, 0.75F, 1.0F);
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemAndEffectIntoGUI(icon, 0, 0);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
            }
        }

        ItemStack result = container.getSlot(BottleWorkbenchContainer.OUTPUT_SLOT).getStack();
        if (!result.isEmpty()) {
            drawPreview(result, partialTicks);
        }
    }

    // The picked bottle in 3D turning
    private void drawPreview(ItemStack result, float partialTicks) {
        ContainerShape shape = PlacedContainerType.BOTTLE.getShape(result);
        float spin = (mc.player.ticksExisted + partialTicks) * 2.0F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft + PREVIEW_X + PREVIEW_WIDTH / 2.0F, guiTop + PREVIEW_Y + PREVIEW_HEIGHT / 2.0F, 100.0F);
        GlStateManager.scale(PREVIEW_SCALE, -PREVIEW_SCALE, PREVIEW_SCALE);
        GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(spin, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -shape.height / 32.0F, -0.5F);
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        TileEntityRendererPlacedContainers.renderItem(result);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int clicked = getShapeAt(mouseX, mouseY);
        if (clicked >= 0) {
            container.enchantItem(mc.player, clicked);
            mc.playerController.sendEnchantPacket(container.windowId, clicked);
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private int getShapeAt(int mouseX, int mouseY) {
        if (container.getGlass().isEmpty()) {
            return -1;
        }
        List<ContainerShape> shapes = BottleWorkbenchRecipes.getShapes();
        for (int i = 0; i < shapes.size(); i++) {
            int x = guiLeft + getButtonX(i);
            int y = guiTop + getButtonY(i);
            if (mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= y && mouseY < y + BUTTON_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private static int getButtonX(int index) {
        return GRID_X + (index % GRID_COLUMNS) * BUTTON_SIZE;
    }

    private static int getButtonY(int index) {
        return GRID_Y + (index / GRID_COLUMNS) * BUTTON_SIZE;
    }
}