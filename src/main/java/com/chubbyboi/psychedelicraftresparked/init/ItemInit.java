package com.chubbyboi.psychedelicraftresparked.init;

import com.chubbyboi.psychedelicraftresparked.drug.DrugInfluence;
import com.chubbyboi.psychedelicraftresparked.drug.DrugInfluenceHarmonium;
import com.chubbyboi.psychedelicraftresparked.item.ItemBarrel;
import com.chubbyboi.psychedelicraftresparked.item.ItemBottle;
import com.chubbyboi.psychedelicraftresparked.item.ItemDistillery;
import com.chubbyboi.psychedelicraftresparked.item.ItemDrinkable;
import com.chubbyboi.psychedelicraftresparked.item.ItemFlask;
import com.chubbyboi.psychedelicraftresparked.item.ItemHarmonium;
import com.chubbyboi.psychedelicraftresparked.item.ItemIngestable;
import com.chubbyboi.psychedelicraftresparked.item.ItemVat;
import com.chubbyboi.psychedelicraftresparked.item.ItemPipeOfSmokeMonsters;
import com.chubbyboi.psychedelicraftresparked.item.ItemSmokable;
import com.chubbyboi.psychedelicraftresparked.item.ItemSmokingTool;
import com.chubbyboi.psychedelicraftresparked.item.ItemGrapes;
import com.chubbyboi.psychedelicraftresparked.item.PsychFoodItem;
import com.chubbyboi.psychedelicraftresparked.item.PsychItem;
import com.chubbyboi.psychedelicraftresparked.item.PsychSeeds;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemInit {
    public static final List<Item> ITEMS = new ArrayList<>();

    // ==================== FUNCTIONAL BLOCKS ====================
    public static final Item DRYING_TABLE_ITEM = createItemBlock(BlockInit.DRYING_TABLE);
    public static final ItemVat VAT = new ItemVat("vat");
    public static final ItemBarrel BARREL_ITEM = new ItemBarrel(BlockInit.BARREL);
    public static final ItemDistillery DISTILLERY_ITEM = new ItemDistillery(BlockInit.DISTILLERY);
    public static final ItemFlask FLASK_ITEM = new ItemFlask(BlockInit.FLASK);
    public static final Item LATTICE_ITEM = createItemBlock(BlockInit.LATTICE);



    // ==================== OTHER BLOCKS ====================
    public static final Item JUNIPER_SAPLING_ITEM = createItemBlock(BlockInit.JUNIPER_SAPLING);
    public static final Item JUNIPER_LOG_ITEM = createItemBlock(BlockInit.JUNIPER_LOG);
    public static final Item JUNIPER_LEAVES_ITEM = createItemBlock(BlockInit.JUNIPER_LEAVES);



    // ==================== DRUG DELIVERY ITEMS ====================
    public static final ItemSmokingTool PIPE = new ItemSmokingTool("pipe", 50, 25);
    public static final ItemSmokingTool BONG = new ItemSmokingTool("bong", 128, 30).setBubblingSound(true);
    public static final ItemPipeOfSmokeMonsters PIPE_OF_SMOKE_MONSTERS = new ItemPipeOfSmokeMonsters("pipe_of_smoke_monsters", 50, 25);
    public static final ItemDrinkable SYRINGE = new ItemDrinkable("syringe", 10, 10, 25, ItemDrinkable.ConsumptionType.INJECT)
        .setFinishSound(SoundEvents.ENTITY_PLAYER_HURT);



    // ==================== CANNABIS ITEMS ====================
    // Raw/dried materials
    public static final PsychSeeds CANNABIS_SEEDS = new PsychSeeds("cannabis_seeds");
    public static final Item CANNABIS_LEAF = new PsychItem("cannabis_leaf");
    public static final Item DRIED_CANNABIS_LEAVES = new PsychItem("dried_cannabis_leaves");
    public static final Item CANNABIS_BUD = new PsychItem("cannabis_bud");
    public static final Item DRIED_CANNABIS_BUDS = new PsychItem("dried_cannabis_buds");

    // Cannabis Drugs
    static {
        PIPE.addConsumable(new ItemSmokingTool.Consumable(new ItemStack(DRIED_CANNABIS_BUDS), new DrugInfluence[]{
            new DrugInfluence("cannabis", 20, 0.002, 0.001, 0.25)
        }));
        BONG.addConsumable(new ItemSmokingTool.Consumable(new ItemStack(DRIED_CANNABIS_BUDS), new DrugInfluence[]{
            new DrugInfluence("cannabis", 20, 0.002, 0.001, 0.20)
        }));
        PIPE_OF_SMOKE_MONSTERS.addConsumable(new ItemSmokingTool.Consumable(new ItemStack(DRIED_CANNABIS_BUDS), new DrugInfluence[]{
            new DrugInfluence("cannabis", 20, 0.002, 0.001, 0.25)
        }));
    }

    // Joint
    public static final Item JOINT = new ItemSmokable("joint")
        .addDrugInfluence("cannabis",20, 0.002, 0.001, 0.20)
        .setSmokeDuration(40);

    // Blunt
    public static final Item BLUNT = new ItemSmokable("blunt")
        .addDrugInfluence("tobacco", 0, 0.1, 0.02, 0.7)
        .addDrugInfluence("cannabis", 20, 0.002, 0.001, 0.7)
        .setSmokeDuration(40)
        .setUseStages(4);

    // Hash Muffin
    public static final Item HASH_MUFFIN = new ItemIngestable("hash_muffin", 5, 0.2F)
        .setDrugInfluence("cannabis", 120, 0.004, 0.002, 0.8);



    // ==================== COCAINE ITEMS ====================
    // Raw/dried materials
    public static final PsychSeeds COCA_SEEDS = new PsychSeeds("coca_seeds");
    public static final Item COCA_LEAF = new PsychItem("coca_leaf");
    public static final Item DRIED_COCA_LEAVES = new PsychItem("dried_coca_leaves");

    // Cocaine Drugs
    public static final Item COCAINE_POWDER = new ItemIngestable("cocaine_powder", 0, 0.0F)
        .setDrugInfluence("cocaine", 0, 0.002, 0.003, 0.35)
        .setUseAction(EnumAction.BOW)
        .setFinishSound(SoundEvents.ENTITY_LEASHKNOT_PLACE) ; // Leash kinda sounds snorty



    // ==================== TOBACCO ITEMS ====================
    // Raw/dried materials
    public static final PsychSeeds TOBACCO_SEEDS = new PsychSeeds("tobacco_seeds");
    public static final Item TOBACCO_LEAF = new PsychItem("tobacco_leaf");
    public static final Item DRIED_TOBACCO = new PsychItem("dried_tobacco");

    // Tobacco Drugs
    static {
        PIPE.addConsumable(new ItemSmokingTool.Consumable(new ItemStack(DRIED_TOBACCO), new DrugInfluence[]{
            new DrugInfluence("tobacco", 0, 0.1, 0.02, 0.8)
        }));
        PIPE_OF_SMOKE_MONSTERS.addConsumable(new ItemSmokingTool.Consumable(new ItemStack(DRIED_TOBACCO), new DrugInfluence[]{
            new DrugInfluence("tobacco", 0, 0.1, 0.02, 0.8)
        }));
        BONG.addConsumable(new ItemSmokingTool.Consumable(new ItemStack(DRIED_TOBACCO), new DrugInfluence[]{
            new DrugInfluence("tobacco", 0, 0.1, 0.02, 0.6)
        }));
    }

    public static final Item CIGARETTE = new ItemSmokable("cigarette")
        .addDrugInfluence("tobacco", 0, 0.1, 0.02, 0.7)
        .setSmokeDuration(40);

    public static final Item CIGAR = new ItemSmokable("cigar")
        .addDrugInfluence("tobacco", 0, 0.1, 0.02, 0.7)
        .setSmokeDuration(40)
        .setUseStages(4);



    // ==================== SHROOMS ITEMS ====================
    public static final Item BROWN_SHROOMS = new ItemIngestable("brown_magic_mushrooms", 3, 0.1F)
        .setDrugInfluence("brownshrooms", 15, 0.005, 0.003, 0.5)
        .setConsumeDuration(16);

    public static final Item RED_SHROOMS = new ItemIngestable("red_magic_mushrooms", 3, 0.1F)
        .setDrugInfluence("redshrooms", 15, 0.005, 0.003, 0.5)
        .setConsumeDuration(16);



    // ==================== COFFEE RELATED ====================
    // Raw/dried materials
    public static final PsychSeeds COFFEA_CHERRIES = new PsychSeeds("coffea_cherries");
    public static final Item COFFEE_BEANS = new PsychItem("coffee_beans");



    // ==================== PEYOTE ITEMS ====================
    // Raw/dried materials
    public static final Item PEYOTE = createItemBlock(BlockInit.PEYOTE_PLANT);

    // Peyote Drugs
    public static final Item DRIED_PEYOTE = new ItemIngestable("dried_peyote", 1, 0.1F)
        .setDrugInfluence("peyote", 15, 0.005, 0.003, 0.5)
        .setConsumeDuration(16);
    public static final Item PEYOTE_JOINT = new ItemSmokable("peyote_joint")
        .addDrugInfluence("peyote",20, 0.003, 0.0015, 0.4)
        .setSmokeDuration(40);



    // ==================== HOPS ITEMS ====================
    public static final PsychSeeds HOP_SEEDS = new PsychSeeds("hop_seeds");
    public static final Item HOP_CONES = new PsychItem("hop_cones");



    // ==================== GRAPES ITEMS ====================
    public static final Item GRAPES = new ItemGrapes("grapes", 1, 0.5F);



    // ==================== JUNIPER ITEMS ====================
    public static final Item JUNIPER_BERRIES = new PsychFoodItem("juniper_berries", 1, 0.5F);



    // ==================== HARMONIUM ITEMS ====================
    public static final ItemHarmonium HARMONIUM = new ItemHarmonium("harmonium");

    // Harmonium Drug
    static {
        for (EnumDyeColor color : EnumDyeColor.values()) {
            PIPE.addConsumable(new ItemSmokingTool.Consumable(
                    new ItemStack(HARMONIUM, 1, color.getMetadata()),
                    new DrugInfluence[]{
                            new DrugInfluenceHarmonium("harmonium", 0, 0.04, 0.01, 0.65, color.getColorComponentValues()),
                            new DrugInfluence("tobacco", 0, 0.1, 0.02, 0.7)
                    },
                    color.getColorComponentValues()
            ));

            PIPE_OF_SMOKE_MONSTERS.addConsumable(new ItemSmokingTool.Consumable(
                    new ItemStack(HARMONIUM, 1, color.getMetadata()),
                    new DrugInfluence[]{
                            new DrugInfluenceHarmonium("harmonium", 0, 0.04, 0.01, 0.65, color.getColorComponentValues()),
                            new DrugInfluence("tobacco", 0, 0.1, 0.02, 0.7)
                    },
                    color.getColorComponentValues())
            );
        }
    }



    // ==================== FLUID RELATED ====================
    public static final Item TAP = new PsychItem("tap");
    public static final ItemDrinkable WOODEN_MUG = new ItemDrinkable("wooden_mug", 500, 250, 32, ItemDrinkable.ConsumptionType.DRINK);
    public static final ItemDrinkable GLASS_CHALICE = new ItemDrinkable("glass_chalice", 250, 250, 32, ItemDrinkable.ConsumptionType.DRINK);
    public static final ItemDrinkable SHOT_GLASS = new ItemDrinkable("shot_glass", 40, 250, 8, ItemDrinkable.ConsumptionType.DRINK);
    public static final ItemBottle BOTTLE = new ItemBottle("bottle", 1000, 250, 32);



    // ==================== HELPER METHODS ====================
    // Need to call this after all the blocks / seeds are registered
    public static void setCropForSeeds() {
        CANNABIS_SEEDS.setBlockCrop(BlockInit.CANNABIS_PLANT);
        TOBACCO_SEEDS.setBlockCrop(BlockInit.TOBACCO_PLANT);
        COCA_SEEDS.setBlockCrop(BlockInit.COCA_PLANT);
        COFFEA_CHERRIES.setBlockCrop(BlockInit.COFFEA_PLANT);
        HOP_SEEDS.setBlockCrop(BlockInit.HOPS_PLANT);
    }

    private static Item createItemBlock(net.minecraft.block.Block block) {
        ItemBlock itemBlock = new ItemBlock(block);
        itemBlock.setRegistryName(block.getRegistryName());
        ITEMS.add(itemBlock);
        return itemBlock;
    }
}