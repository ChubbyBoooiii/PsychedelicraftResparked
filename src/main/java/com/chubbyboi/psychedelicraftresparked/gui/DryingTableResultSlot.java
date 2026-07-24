package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.recipes.DryingTableRecipes;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class DryingTableResultSlot extends Slot {

    private final EntityPlayer player;
    private int removeCount;

    public DryingTableResultSlot(EntityPlayer player, IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.player = player;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
        this.onCrafting(stack);
        super.onTake(thePlayer, stack);
        return stack;
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        if (this.getHasStack()) this.removeCount += Math.min(amount, this.getStack().getCount());
        return super.decrStackSize(amount);
    }

    @Override
    protected void onCrafting(ItemStack stack, int amount) {
        this.removeCount += amount;
        this.onCrafting(stack);
    }

    @Override
    protected void onCrafting(ItemStack stack) {
        stack.onCrafting(this.player.world, this.player, this.removeCount);

        if (!this.player.world.isRemote) {
            int count = this.removeCount;
            float experience = DryingTableRecipes.getInstance().getDryingExperience(stack);

            if (experience == 0.0F) {
                count = 0;
            } else if (experience < 1.0F) {
                int wholeCount = MathHelper.floor((float) count * experience);

                if (wholeCount < MathHelper.ceil((float) count * experience) && Math.random() < (double) ((float) count * experience - (float) wholeCount)) {
                    ++wholeCount;
                }

                count = wholeCount;
            }

            while (count > 0) {
                int split = EntityXPOrb.getXPSplit(count);
                count -= split;
                this.player.world.spawnEntity(new EntityXPOrb(this.player.world, this.player.posX, this.player.posY + 0.5D, this.player.posZ + 0.5D, split));
            }
        }

        this.removeCount = 0;
    }
}
