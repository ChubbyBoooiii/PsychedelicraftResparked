package com.chubbyboi.psychedelicraftresparked.tabs;

import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class PsychedelicraftResparkedTab extends CreativeTabs {

    public PsychedelicraftResparkedTab(String label) {
        super("psychedelicrafResparkedTab");
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(ItemInit.CANNABIS_LEAF, 1);
    }
}
