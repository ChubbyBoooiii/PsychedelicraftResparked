package com.chubbyboi.psychedelicraftresparked.util;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class LootHandler {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        int weight = getPipeOfSmokeMonstersWeight(event.getName());
        if (weight <= 0) {
            return;
        }

        LootPool pool = event.getTable().getPool("main");
        if (pool == null) {
            return;
        }

        pool.addEntry(new LootEntryItem(
            ItemInit.PIPE_OF_SMOKE_MONSTERS,
            weight,
            0,
            new LootFunction[0],
            new LootCondition[0],
            Tags.MOD_ID + ":pipe_of_smoke_monsters"
        ));
    }

    private static int getPipeOfSmokeMonstersWeight(ResourceLocation lootTable) {
        if (lootTable.equals(LootTableList.CHESTS_SIMPLE_DUNGEON)) {
            return 3;
        }
        if (lootTable.equals(LootTableList.CHESTS_ABANDONED_MINESHAFT)) {
            return 4;
        }
        if (lootTable.equals(LootTableList.CHESTS_JUNGLE_TEMPLE)) {
            return 2;
        }
        return 0;
    }
}