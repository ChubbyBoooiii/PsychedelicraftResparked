package com.chubbyboi.psychedelicraftresparked.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class TileEntityMashTubCompanion extends TileEntity {
    @Nullable
    private BlockPos masterPos;

    public void setMasterPos(BlockPos masterPos) {
        this.masterPos = masterPos;
    }

    @Nullable
    public BlockPos getMasterPos() {
        return masterPos;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (masterPos != null) {
            compound.setInteger("MasterX", masterPos.getX());
            compound.setInteger("MasterY", masterPos.getY());
            compound.setInteger("MasterZ", masterPos.getZ());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("MasterX")) {
            masterPos = new BlockPos(compound.getInteger("MasterX"), compound.getInteger("MasterY"), compound.getInteger("MasterZ"));
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket() {
        return new net.minecraft.network.play.server.SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}