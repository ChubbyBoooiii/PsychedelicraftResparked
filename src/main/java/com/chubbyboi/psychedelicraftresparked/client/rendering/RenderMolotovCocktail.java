package com.chubbyboi.psychedelicraftresparked.client.rendering;

import com.chubbyboi.psychedelicraftresparked.entities.EntityMolotovCocktail;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMolotovCocktail extends RenderSnowball<EntityMolotovCocktail> {

    public RenderMolotovCocktail(RenderManager renderManager, Item item, RenderItem itemRenderer) {
        super(renderManager, item, itemRenderer);
    }

    @Override
    public ItemStack getStackToRender(EntityMolotovCocktail entity) {
        ItemStack stack = entity.getMolotovStack();
        return stack.isEmpty() ? new ItemStack(this.item) : stack;
    }
}