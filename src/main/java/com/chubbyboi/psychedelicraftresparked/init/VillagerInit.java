package com.chubbyboi.psychedelicraftresparked.init;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

public class VillagerInit {

    public static VillagerRegistry.VillagerProfession HERBALIST;
    public static VillagerRegistry.VillagerCareer HERBALIST_CAREER;

    public static void registerCareer(VillagerRegistry.VillagerProfession herbalistProfession) {
        HERBALIST = herbalistProfession;
        HERBALIST_CAREER = new VillagerRegistry.VillagerCareer(HERBALIST, "herbalist");

        HERBALIST_CAREER.addTrade(1,
            new EntityVillager.ListItemForEmeralds(ItemInit.GRAPES, new EntityVillager.PriceInfo(1, 3)),
            new EntityVillager.ListItemForEmeralds(ItemInit.TOBACCO_SEEDS, new EntityVillager.PriceInfo(2, 8))
        );
        HERBALIST_CAREER.addTrade(2,
            new EntityVillager.ListItemForEmeralds(ItemInit.CANNABIS_SEEDS, new EntityVillager.PriceInfo(2, 8)),
            new EntityVillager.ListItemForEmeralds(ItemInit.COFFEA_CHERRIES, new EntityVillager.PriceInfo(2, 8))
        );
        HERBALIST_CAREER.addTrade(3,
            new EntityVillager.ListItemForEmeralds(ItemInit.HOP_SEEDS, new EntityVillager.PriceInfo(3, 8))
        );
        HERBALIST_CAREER.addTrade(4,
            new EntityVillager.ListItemForEmeralds(ItemInit.COCA_SEEDS, new EntityVillager.PriceInfo(5, 10))
        );
    }
}