package com.chubbyboi.psychedelicraftresparked.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class DrugPropertiesStorage implements Capability.IStorage<IDrugProperties> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IDrugProperties> capability, IDrugProperties instance, EnumFacing side) {
        NBTTagCompound compound = new NBTTagCompound();

        if (instance instanceof DrugProperties) {
            ((DrugProperties) instance).writeToNBT(compound);
        }

        return compound;
    }

    @Override
    public void readNBT(Capability<IDrugProperties> capability, IDrugProperties instance, EnumFacing side, NBTBase nbt) {
        if (nbt instanceof NBTTagCompound && instance instanceof DrugProperties) {
            ((DrugProperties) instance).readFromNBT((NBTTagCompound) nbt);
        }
    }
}