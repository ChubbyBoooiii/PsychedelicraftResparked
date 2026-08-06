package com.chubbyboi.psychedelicraftresparked.init;

import net.minecraft.init.Items;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictionaryInit {

    public static void registerOres() {
        OreDictionary.registerOre("cropCoffee", ItemInit.COFFEE_BEANS);
        OreDictionary.registerOre("leafCannabis", ItemInit.CANNABIS_LEAF);
        OreDictionary.registerOre("leafCoca", ItemInit.COCA_LEAF);
        OreDictionary.registerOre("peyoteDried", ItemInit.DRIED_PEYOTE);
        OreDictionary.registerOre("cropHops", ItemInit.HOP_CONES);
        OreDictionary.registerOre("cropGrape", ItemInit.GRAPES);
        OreDictionary.registerOre("cropJuniperberry", ItemInit.JUNIPER_BERRIES);

        OreDictionary.registerOre("cropApple", Items.APPLE);
        OreDictionary.registerOre("listAllsugar", Items.SUGAR);
    }
}