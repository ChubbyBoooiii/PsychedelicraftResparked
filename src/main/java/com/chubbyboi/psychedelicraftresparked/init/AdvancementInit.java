package com.chubbyboi.psychedelicraftresparked.init;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.advancements.CustomEventTrigger;
import com.chubbyboi.psychedelicraftresparked.advancements.DrinkTrigger;
import com.chubbyboi.psychedelicraftresparked.advancements.DrugThresholdTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.util.ResourceLocation;

public class AdvancementInit {

    public static final CustomEventTrigger CUSTOM = new CustomEventTrigger(new ResourceLocation(Tags.MOD_ID, "custom"));
    public static final DrinkTrigger DRUNK_FLUID = new DrinkTrigger(new ResourceLocation(Tags.MOD_ID, "drunk_fluid"));
    public static final DrugThresholdTrigger DRUG_THRESHOLD = new DrugThresholdTrigger(new ResourceLocation(Tags.MOD_ID, "drug_threshold"));

    public static void register() {
        CriteriaTriggers.register(CUSTOM);
        CriteriaTriggers.register(DRUNK_FLUID);
        CriteriaTriggers.register(DRUG_THRESHOLD);
    }
}