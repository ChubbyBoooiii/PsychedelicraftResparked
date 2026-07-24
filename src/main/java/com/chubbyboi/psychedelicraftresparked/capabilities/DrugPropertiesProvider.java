package com.chubbyboi.psychedelicraftresparked.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DrugPropertiesProvider implements ICapabilitySerializable<NBTBase> {

    @CapabilityInject(IDrugProperties.class)
    public static final Capability<IDrugProperties> DRUG_PROPERTIES_CAPABILITY = null;

    private IDrugProperties instance = new DrugProperties();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == DRUG_PROPERTIES_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == DRUG_PROPERTIES_CAPABILITY) {
            return DRUG_PROPERTIES_CAPABILITY.cast(instance);
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return DRUG_PROPERTIES_CAPABILITY.getStorage().writeNBT(DRUG_PROPERTIES_CAPABILITY, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        DRUG_PROPERTIES_CAPABILITY.getStorage().readNBT(DRUG_PROPERTIES_CAPABILITY, instance, null, nbt);
    }
}