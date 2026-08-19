package com.chubbyboi.psychedelicraftresparked.init;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.advancements.CustomEventTrigger;
import com.chubbyboi.psychedelicraftresparked.advancements.DrugThresholdTrigger;
import com.chubbyboi.psychedelicraftresparked.advancements.HeldFluidTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.util.ResourceLocation;

public class AdvancementInit {

    public static final CustomEventTrigger CUSTOM = new CustomEventTrigger(new ResourceLocation(Tags.MOD_ID, "custom"));
    public static final DrugThresholdTrigger DRUG_THRESHOLD = new DrugThresholdTrigger(new ResourceLocation(Tags.MOD_ID, "drug_threshold"));
    public static final HeldFluidTrigger HELD_FLUID = new HeldFluidTrigger(new ResourceLocation(Tags.MOD_ID, "held_fluid"));

    public static void register() {
        CriteriaTriggers.register(CUSTOM);
        CriteriaTriggers.register(DRUG_THRESHOLD);
        CriteriaTriggers.register(HELD_FLUID);
    }
}