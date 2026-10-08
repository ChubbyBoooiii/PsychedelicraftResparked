package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.block.ContainerShape;
import com.chubbyboi.psychedelicraftresparked.block.PlacedContainerType;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererPlacedContainers;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.recipes.BottleWorkbenchRecipes;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBottleWorkbench;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BottleWorkbenchGui extends GuiContainer {

    private static final ResourceLocation BOTTLES_TEXTURE = new ResourceLocation(Tags.MOD_ID + ":textures/gui/gui_bottle_workbench.png");
    private static final ResourceLocation LABELS_TEXTURE = new ResourceLocation(Tags.MOD_ID + ":textures/gui/gui_bottle_workbench_labels.png");
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
    // Empty-slot hint icons, in a row on each texture (bottles: glass, dye; labels: bottle, paper, dye)
    private static final int HINT_V = 166;
    private static final int BOTTLES_GLASS_HINT_U = 14;
    private static final int BOTTLES_DYE_HINT_U = 30;
    private static final int LABELS_BOTTLE_HINT_U = 14;
    private static final int LABELS_PAPER_HINT_U = 30;
    private static final int LABELS_DYE_HINT_U = 46;
    // Mode tabs on the right edge, drawn from the same spot on the texture
    private static final int TABS_X = 176;
    private static final int TABS_Y = 0;
    private static final int TABS_WIDTH = 26;
    private static final int TABS_HEIGHT = 53;
    // Top of each tab's 20x20 icon square
    private static final int BOTTLE_TAB_Y = 3;
    private static final int LABEL_TAB_Y = 30;
    // Tab outlines for clicks and tooltips, as drawn when not selected
    private static final int BOTTLE_TAB_TOP = 0;
    private static final int LABEL_TAB_TOP = 27;
    private static final int TAB_WIDTH = 24;
    private static final int TAB_HEIGHT = 26;
    private static final ItemStack BOTTLE_TAB_ICON = new ItemStack(ItemInit.BOTTLE, 1, EnumDyeColor.GREEN.getMetadata());
    private static final ItemStack LABEL_TAB_ICON = new ItemStack(Items.PAPER);

    private final InventoryPlayer player;
    private final BottleWorkbenchContainer container;

    public BottleWorkbenchGui(InventoryPlayer player, TileEntityBottleWorkbench tileentity) {
        super(new BottleWorkbenchContainer(player, tileentity));
        this.player = player;
        this.container = (BottleWorkbenchContainer) inventorySlots;
    }

    private boolean isLabelMode() {
        return container.getMode() == TileEntityBottleWorkbench.MODE_LABELS;
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
        // Like the creative tabs, only the tab you can switch to gets a tooltip
        if (isLabelMode() && isInRect(mouseX, mouseY, TABS_X, BOTTLE_TAB_TOP, TAB_WIDTH, TAB_HEIGHT)) {
            drawHoveringText(I18n.format("container.bottle_workbench.tab.bottles"), mouseX, mouseY);
        } else if (!isLabelMode() && isInRect(mouseX, mouseY, TABS_X, LABEL_TAB_TOP, TAB_WIDTH, TAB_HEIGHT)) {
            drawHoveringText(I18n.format("container.bottle_workbench.tab.labels"), mouseX, mouseY);
        }
    }

    private boolean isInRect(int mouseX, int mouseY, int x, int y, int width, int height) {
        int left = guiLeft + x;
        int top = guiTop + y;
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("container.bottle_workbench"), 8, 4, 4210752);
        fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        ResourceLocation texture = isLabelMode() ? LABELS_TEXTURE : BOTTLES_TEXTURE;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        drawTexturedModalRect(guiLeft + TABS_X, guiTop + TABS_Y, TABS_X, TABS_Y, TABS_WIDTH, TABS_HEIGHT);

        RenderHelper.enableGUIStandardItemLighting();
        itemRender.renderItemAndEffectIntoGUI(BOTTLE_TAB_ICON, guiLeft + TABS_X + 2, guiTop + BOTTLE_TAB_Y + 2);
        itemRender.renderItemAndEffectIntoGUI(LABEL_TAB_ICON, guiLeft + TABS_X + 2, guiTop + LABEL_TAB_Y + 2);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);

        if (isLabelMode()) {
            drawHint(BottleWorkbenchContainer.BOTTLES_SLOT, LABELS_BOTTLE_HINT_U);
            drawHint(BottleWorkbenchContainer.PAPER_SLOT, LABELS_PAPER_HINT_U);
            drawHint(BottleWorkbenchContainer.LABEL_DYE_SLOT, LABELS_DYE_HINT_U);
        } else {
            drawHint(BottleWorkbenchContainer.GLASS_SLOT, BOTTLES_GLASS_HINT_U);
            drawHint(BottleWorkbenchContainer.BOTTLE_DYE_SLOT, BOTTLES_DYE_HINT_U);
            drawShapeGrid(mouseX, mouseY);
        }

        ItemStack result = container.getSlot(BottleWorkbenchContainer.OUTPUT_SLOT).getStack();
        if (!result.isEmpty()) {
            drawPreview(result, partialTicks);
        }
    }

    private void drawHint(int slotIndex, int u) {
        if (container.getStackInInput(slotIndex).isEmpty()) {
            Slot slot = container.getSlot(slotIndex);
            drawTexturedModalRect(guiLeft + slot.xPos, guiTop + slot.yPos, u, HINT_V, 16, 16);
        }
    }

    private void drawShapeGrid(int mouseX, int mouseY) {
        if (container.getGlass().isEmpty()) {
            return;
        }
        List<ContainerShape> shapes = BottleWorkbenchRecipes.getShapes();
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
        if (clicked < 0) {
            if (isLabelMode() && isInRect(mouseX, mouseY, TABS_X, BOTTLE_TAB_TOP, TAB_WIDTH, TAB_HEIGHT)) {
                clicked = BottleWorkbenchContainer.TAB_BOTTLES_ID;
            } else if (!isLabelMode() && isInRect(mouseX, mouseY, TABS_X, LABEL_TAB_TOP, TAB_WIDTH, TAB_HEIGHT)) {
                clicked = BottleWorkbenchContainer.TAB_LABELS_ID;
            }
        }
        if (clicked >= 0) {
            container.enchantItem(mc.player, clicked);
            mc.playerController.sendEnchantPacket(container.windowId, clicked);
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    // The tabs stick out past xSize; clicking them shouldn't drop the held item
    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeft, int guiTop) {
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop)
            && !isInRect(mouseX, mouseY, TABS_X, TABS_Y, TABS_WIDTH, TABS_HEIGHT);
    }

    // Screen areas outside xSize, for JEI to keep its item list clear of
    public List<Rectangle> getExtraAreas() {
        return Arrays.asList(new Rectangle(guiLeft + TABS_X, guiTop + TABS_Y, TABS_WIDTH, TABS_HEIGHT));
    }

    private int getShapeAt(int mouseX, int mouseY) {
        if (isLabelMode() || container.getGlass().isEmpty()) {
            return -1;
        }
        List<ContainerShape> shapes = BottleWorkbenchRecipes.getShapes();
        for (int i = 0; i < shapes.size(); i++) {
            if (isInRect(mouseX, mouseY, getButtonX(i), getButtonY(i), BUTTON_SIZE, BUTTON_SIZE)) {
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