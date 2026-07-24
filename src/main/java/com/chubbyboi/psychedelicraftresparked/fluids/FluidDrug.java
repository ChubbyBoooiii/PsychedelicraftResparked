package com.chubbyboi.psychedelicraftresparked.fluids;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.drug.DrugInfluence;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FluidDrug extends Fluid implements DrinkableFluid, InjectableFluid {

    private final List<DrugInfluence> drugInfluencesPerBucket = new ArrayList<>();

    private boolean drinkable;
    private boolean injectable;

    public FluidDrug(String fluidName, ResourceLocation still, ResourceLocation flowing) {
        super(fluidName, still, flowing);
    }

    public FluidDrug setDrinkable(boolean drinkable) {
        this.drinkable = drinkable;
        return this;
    }

    public FluidDrug setInjectable(boolean injectable) {
        this.injectable = injectable;
        return this;
    }

    public FluidDrug addDrugInfluencePerBucket(String drugName, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluencePerBucket) {
        drugInfluencesPerBucket.add(new DrugInfluence(drugName, delay, influenceSpeed, influenceSpeedPlus, maxInfluencePerBucket));
        return this;
    }

    public void getDrugInfluences(FluidStack fluidStack, List<DrugInfluence> list) {
        for (DrugInfluence influence : drugInfluencesPerBucket) {
            double scaledMax = influence.getMaxInfluence() * fluidStack.amount / (double) FluidHelper.BUCKET_VOLUME;
            list.add(new DrugInfluence(influence.getDrugName(), influence.getDelay(), influence.getInfluenceSpeed(), influence.getInfluenceSpeedPlus(), scaledMax));
        }
    }

    @Override
    public boolean canDrink(FluidStack fluidStack, EntityLivingBase entity) {
        return drinkable;
    }

    @Override
    public void drink(FluidStack fluidStack, EntityLivingBase entity) {
        applyInfluences(fluidStack, entity);
    }

    @Override
    public boolean canInject(FluidStack fluidStack, EntityLivingBase entity) {
        return injectable;
    }

    @Override
    public void inject(FluidStack fluidStack, EntityLivingBase entity) {
        applyInfluences(fluidStack, entity);
    }

    private void applyInfluences(FluidStack fluidStack, EntityLivingBase entity) {
        @Nullable IDrugProperties props = entity.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (props instanceof DrugProperties) {
            List<DrugInfluence> influences = new ArrayList<>();
            getDrugInfluences(fluidStack, influences);
            for (DrugInfluence influence : influences) {
                ((DrugProperties) props).addInfluence(influence);
            }
        }
    }
}
