package com.chubbyboi.psychedelicraftresparked.tileentities;

import com.chubbyboi.psychedelicraftresparked.recipes.DryingTableRecipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class TileEntityDryingTable extends TileEntity implements ITickable, ISidedInventory {
    private NonNullList<ItemStack> dryingTableItems = NonNullList.withSize(10, ItemStack.EMPTY);
    private String customName;
    private static final int[] SLOTS_BOTTOM = new int[] {9};
    private static final int[] SLOTS_SIDES = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] SLOTS_TOP = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};

    public float dryingProgress;
    public int totalDryingTime;
    public float lightPercent;
    public float tempPercent;
    public float progressPerTick;

    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : "container.drying_table";
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }

    @Override
    public int getSizeInventory() {
        return this.dryingTableItems.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.dryingTableItems) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.dryingTableItems.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack result = ItemStackHelper.getAndSplit(this.dryingTableItems, index, count);
        this.markDirty();
        if (!world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack result = ItemStackHelper.getAndRemove(this.dryingTableItems, index);
        this.markDirty();
        if (!world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
        return result;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack itemStack = (ItemStack)this.dryingTableItems.get(index);
        boolean flag = !stack.isEmpty() && stack.isItemEqual(itemStack) && ItemStack.areItemStackTagsEqual(stack, itemStack);
        this.dryingTableItems.set(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
            if (index < 9 && !flag) {
                this.totalDryingTime = getDryingTime();
                this.dryingProgress = 0;
                this.markDirty();
            }
        }

        this.markDirty();
        if (!world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setFloat("dryingProgress", this.dryingProgress);
        compound.setInteger("totalDryingTime", this.totalDryingTime);
        compound.setFloat("lightPercent", this.lightPercent);
        compound.setFloat("tempPercent", this.tempPercent);
        compound.setFloat("progressPerTick", this.progressPerTick);
        ItemStackHelper.saveAllItems(compound, this.dryingTableItems);

        if (this.hasCustomName()) compound.setString("CustomName", this.customName);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.dryingTableItems = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.dryingTableItems);
        this.dryingProgress = compound.getFloat("dryingProgress");
        this.totalDryingTime = compound.getInteger("totalDryingTime");
        this.lightPercent = compound.getFloat("lightPercent");
        this.tempPercent = compound.getFloat("tempPercent");
        this.progressPerTick = compound.getFloat("progressPerTick");

        if (compound.hasKey("CustomName", 8)) this.setCustomName(compound.getString("CustomName"));
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void update() {
        boolean flag1 = false;

        if (!world.isRemote) {
            calculateProgressPerTick();
        }

        if (!this.world.isRemote && !(world.isRainingAt(pos.up()))) {
            if (this.canDry()) {
                this.totalDryingTime = getDryingTime();
                this.dryingProgress += this.progressPerTick;

                if (this.dryingProgress >= this.totalDryingTime) {
                    this.dryingProgress = 0;
                    this.totalDryingTime = getDryingTime();
                    this.dryItem();
                    flag1 = true;
                }
            } else {
                this.dryingProgress = 0;
                this.totalDryingTime = 0;
            }
        }
        if (flag1) this.markDirty();
    }

    public void calculateProgressPerTick() {
        // Light percent is based on light level with 15 being 100%, cap the lower end at 10%
        float light = world.getLight(pos.up(), true) / 15F;
        this.lightPercent = MathHelper.clamp(light, 0.1F, 1.0F);

        // Temp percent is based on the temperature of the biome. The lower end of the percent will be 10%, and the upper will be 100%, helps as some biomes temps are above 1 and below 0
        float biomeTemp = !world.isAirBlock(pos) ? world.getBiome(pos).getDefaultTemperature() : 0;
        this.tempPercent = MathHelper.clamp(biomeTemp, 0.1F, 1.0F);

        this.progressPerTick = MathHelper.clamp(lightPercent * tempPercent, 0.0F, 1.0F);
    }

    public int getDryingTime() {
        return 2000;
    }

    private boolean canDry() {
        for (int i = 0; i < 9; ++i) {
            if (this.dryingTableItems.get(i).isEmpty()) {
                return false;
            }
        }

        // Check all items are the same before searching for recipe
        ItemStack firstItem = this.dryingTableItems.get(0);
        for (int x = 1; x < 9; ++x) {
            if (this.dryingTableItems.get(x).getItem() != firstItem.getItem()) {
                return false;
            }
        }
        ItemStack result = DryingTableRecipes.getInstance().getDryingResult(this.dryingTableItems.get(0));

        if (result.isEmpty()) {
            return false;
        } else {
            ItemStack output = this.dryingTableItems.get(9);
            if (output.isEmpty()) return true;
            if (!output.isItemEqual(result)) return false;
            int res = output.getCount() + result.getCount();
            return res <= output.getMaxStackSize();
        }
    }

    public void dryItem() {
        if (this.canDry()) {
            ItemStack result = DryingTableRecipes.getInstance().getDryingResult(this.dryingTableItems.get(0));
            ItemStack output = (ItemStack)this.dryingTableItems.get(9);

            if (output.isEmpty()) {
                this.dryingTableItems.set(9, result.copy());
            } else if (output.getItem() == result.getItem()) {
                output.grow(result.getCount());
            }

            for (int i = 0; i < 9; i++) {
                this.dryingTableItems.get(i).shrink(1);
            }

            this.markDirty();
            if (!world.isRemote) {
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
            }
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5, (double) this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 9) {
            return dryingTableItems.get(index).isEmpty();
        }
        return false;
    }

    @Override
    public int getField(int id) {
        switch(id) {
            case 0:
                return (int)(this.dryingProgress * 1000);
            case 1:
                return this.totalDryingTime;
            case 2:
                return (int)(this.progressPerTick * 1000);
            case 3:
                return (int)(this.lightPercent * 1000);
            case 4:
                return (int)(this.tempPercent * 1000);
            default:
                return 0;
        }
    }

    @Override
    public void setField(int id, int value) {
        switch(id) {
            case 0:
                this.dryingProgress = value / 1000F;
                break;
            case 1:
                this.totalDryingTime = value;
                break;
            case 2:
                this.progressPerTick = value / 1000F;
                break;
            case 3:
                this.lightPercent = value / 1000F;
                break;
            case 4:
                this.tempPercent = value / 1000F;
                break;
        }
    }

    @Override
    public int getFieldCount() {
        return 5;
    }

    @Override
    public void clear() {
        this.dryingTableItems.clear();
    }

    public int[] getSlotsForFace(EnumFacing side) {
        if (side == EnumFacing.DOWN) {
            return SLOTS_BOTTOM;
        } else {
            return side == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES;
        }
    }

    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return direction == EnumFacing.DOWN && index == 9;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket() {
        return new net.minecraft.network.play.server.SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

}
