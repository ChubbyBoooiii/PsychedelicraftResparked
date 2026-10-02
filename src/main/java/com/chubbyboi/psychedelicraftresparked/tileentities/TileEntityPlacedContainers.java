package com.chubbyboi.psychedelicraftresparked.tileentities;

import com.chubbyboi.psychedelicraftresparked.block.PlacedContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileEntityPlacedContainers extends TileEntity {

    public static final int ROTATION_STEPS = 16;

    public static class Entry {
        public final ItemStack stack;
        public final PlacedContainerType type;
        public final float x;
        public final float z;
        public int rotation;

        private Entry(ItemStack stack, PlacedContainerType type, float x, float z, int rotation) {
            this.stack = stack;
            this.type = type;
            this.x = x;
            this.z = z;
            this.rotation = rotation;
        }

        public AxisAlignedBB getBox() {
            return new AxisAlignedBB(
                (x - type.hitHalfWidth) / 16.0, 0.0, (z - type.hitHalfWidth) / 16.0,
                (x + type.hitHalfWidth) / 16.0, type.height / 16.0, (z + type.hitHalfWidth) / 16.0);
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public static final float GAP = 1.0F;

    public static float snapToBlock(float pixel, PlacedContainerType type) {
        float snapped = Math.round(pixel);
        return Math.max(type.hitHalfWidth, Math.min(16.0F - type.hitHalfWidth, snapped));
    }

    public boolean canPlace(PlacedContainerType type, float x, float z) {
        for (Entry entry : entries) {
            float minDistance = entry.type.hitHalfWidth + type.hitHalfWidth + GAP - 0.001F;
            if (Math.abs(entry.x - x) < minDistance && Math.abs(entry.z - z) < minDistance) {
                return false;
            }
        }
        return true;
    }

    public void add(ItemStack stack, PlacedContainerType type, float x, float z, int rotation) {
        entries.add(new Entry(stack, type, x, z, rotation));
        sync();
    }

    public ItemStack remove(int index) {
        ItemStack stack = entries.remove(index).stack;
        sync();
        return stack;
    }

    public void rotate(int index) {
        Entry entry = entries.get(index);
        entry.rotation = (entry.rotation + 1) % ROTATION_STEPS;
        sync();
    }

    public int findEntry(double hitX, double hitY, double hitZ) {
        Vec3d hit = new Vec3d(hitX, hitY, hitZ);
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (!entry.getBox().grow(0.01).contains(hit)) {
                continue;
            }
            double dx = entry.x / 16.0 - hitX;
            double dz = entry.z / 16.0 - hitZ;
            double distance = dx * dx + dz * dz;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return best;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 || pass == 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasFastRenderer() {
        return MinecraftForgeClient.getRenderPass() == 1;
    }

    private void sync() {
        markDirty();
        if (world != null && !world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList list = new NBTTagList();
        for (Entry entry : entries) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("Item", entry.stack.writeToNBT(new NBTTagCompound()));
            tag.setFloat("X", entry.x);
            tag.setFloat("Z", entry.z);
            tag.setByte("Rotation", (byte) entry.rotation);
            list.appendTag(tag);
        }
        compound.setTag("Containers", list);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        entries.clear();
        NBTTagList list = compound.getTagList("Containers", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            ItemStack stack = new ItemStack(tag.getCompoundTag("Item"));
            PlacedContainerType type = PlacedContainerType.of(stack);
            if (type != null) {
                entries.add(new Entry(stack, type, tag.getFloat("X"), tag.getFloat("Z"), tag.getByte("Rotation") & 0xF));
            }
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}