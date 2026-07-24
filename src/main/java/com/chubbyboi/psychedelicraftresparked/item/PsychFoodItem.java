package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.item.ItemFood;

public class PsychFoodItem extends ItemFood {
    public PsychFoodItem(String name, int amount, float saturationMultiplier) {
        super(amount, saturationMultiplier, false);
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);

        ItemInit.ITEMS.add(this);
    }
}
