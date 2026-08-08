package com.chubbyboi.psychedelicraftresparked.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;

public class TileEntityRiftJar extends TileEntity implements ITickable {

    public float currentRiftFraction;
    public int ticksAliveVisual;

    public boolean isOpening;
    public float fractionOpen;

    public boolean jarBroken = false;
    public boolean suckingRifts = true;
    public float fractionHandleUp;

    public int blockRotation;

    public List<JarRiftConnection> riftConnections = new ArrayList<>();

    @Override
    public void update() {
        fractionOpen = nearValue(fractionOpen, isOpening ? 1.0f : 0.0f, 0.0f, 0.02f);
        fractionHandleUp = nearValue(fractionHandleUp, suckingRifts ? 0.0f : 1.0f, 0.0f, 0.04f);

        ticksAliveVisual++;
    }

    private static float nearValue(float current, float target, float mulSpeed, float plusSpeed) {
        current += (target - current) * mulSpeed;
        if (current > target) {
            current = Math.max(current - plusSpeed, target);
        } else if (current < target) {
            current = Math.min(current + plusSpeed, target);
        }
        return current;
    }

    public void toggleRiftJarOpen() {
        if (!world.isRemote) {
            isOpening = !isOpening;
            sync();
        }
    }

    public void toggleSuckingRifts() {
        if (!world.isRemote) {
            suckingRifts = !suckingRifts;
            sync();
        }
    }

    public boolean isSuckingRifts() {
        return suckingRifts;
    }

    private void sync() {
        markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.getX() - 10, pos.getY() - 5, pos.getZ() - 10, pos.getX() + 11, pos.getY() + 11, pos.getZ() + 11);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setFloat("currentRiftFraction", currentRiftFraction);

        compound.setBoolean("isOpening", isOpening);
        compound.setFloat("fractionOpen", fractionOpen);

        compound.setBoolean("jarBroken", jarBroken);
        compound.setBoolean("suckingRifts", suckingRifts);
        compound.setFloat("fractionHandleUp", fractionHandleUp);

        compound.setInteger("blockRotation", blockRotation);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        currentRiftFraction = compound.getFloat("currentRiftFraction");

        isOpening = compound.getBoolean("isOpening");
        fractionOpen = compound.getFloat("fractionOpen");

        jarBroken = compound.getBoolean("jarBroken");
        suckingRifts = compound.getBoolean("suckingRifts");
        fractionHandleUp = compound.getFloat("fractionHandleUp");

        blockRotation = compound.getInteger("blockRotation");
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

    public static class JarRiftConnection {
        public int riftID;
        public double entityX;
        public double entityY;
        public double entityZ;

        public Object bezierPath3D;
        public float fractionUp;
    }
}