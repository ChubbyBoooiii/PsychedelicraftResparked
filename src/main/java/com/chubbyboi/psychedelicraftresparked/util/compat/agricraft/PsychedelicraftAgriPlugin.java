package com.chubbyboi.psychedelicraftresparked.util.compat.agricraft;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.infinityraider.agricraft.api.v1.misc.IAgriRegistry;
import com.infinityraider.agricraft.api.v1.plant.IAgriPlant;
import com.infinityraider.agricraft.api.v1.plugin.AgriPlugin;
import com.infinityraider.agricraft.api.v1.plugin.IAgriPlugin;
import com.chubbyboi.psychedelicraftresparked.util.compat.agricraft.PsychedelicraftAgriPlant.HarvestProduct;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

@AgriPlugin
public class PsychedelicraftAgriPlugin implements IAgriPlugin {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getId() {
        return Tags.MOD_ID;
    }

    @Override
    public String getName() {
        return "Psychedelicraft Resparked Integration";
    }

    @Override
    public void registerPlants(IAgriRegistry<IAgriPlant> plantRegistry) {
        register(plantRegistry, new PsychedelicraftAgriPlant(
            Tags.MOD_ID + "_cannabis_plant", "Cannabis", ItemInit.CANNABIS_SEEDS,
            Arrays.asList(
                // Leaf is the main product
                new HarvestProduct(ItemInit.CANNABIS_LEAF, 1, 3, 0.95),
                // Bud is a rarer bonus drop
                new HarvestProduct(ItemInit.CANNABIS_BUD, 1, 2, 0.4)
            ),
            primaryTextures("cannabis_plant", 3)
        ));

        register(plantRegistry, new PsychedelicraftAgriPlant(
            Tags.MOD_ID + "_tobacco_plant", "Tobacco", ItemInit.TOBACCO_SEEDS,
            Arrays.asList(new HarvestProduct(ItemInit.TOBACCO_LEAF, 1, 3, 0.95)),
            primaryTextures("tobacco_plant_top", 4)
        ));
        register(plantRegistry, new PsychedelicraftAgriPlant(
            Tags.MOD_ID + "_coca_plant", "Coca", ItemInit.COCA_SEEDS,
            Arrays.asList(new HarvestProduct(ItemInit.COCA_LEAF, 1, 3, 0.95)),
            primaryTextures("coca_plant", 3)
        ));
        register(plantRegistry, new PsychedelicraftAgriPlant(
            Tags.MOD_ID + "_hops_plant", "Hops", ItemInit.HOP_SEEDS,
            Arrays.asList(new HarvestProduct(ItemInit.HOP_CONES, 1, 3, 0.95)),
            primaryTextures("hops_plant", 3)
        ));

        register(plantRegistry, new PsychedelicraftAgriPlant(
            Tags.MOD_ID + "_coffea_plant", "Coffea", ItemInit.COFFEA_CHERRIES,
            Arrays.asList(new HarvestProduct(ItemInit.COFFEA_CHERRIES, 1, 3, 0.95)),
            primaryTextures("coffea_plant", 8)
        ));
    }

    private static void register(IAgriRegistry<IAgriPlant> plantRegistry, PsychedelicraftAgriPlant plant) {
        plantRegistry.add(plant);
    }

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(Tags.MOD_ID, "blocks/" + name);
    }

    private static ResourceLocation[] primaryTextures(String baseName, int stageCount) {
        ResourceLocation[] result = new ResourceLocation[8];
        for (int meta = 0; meta < 8; meta++) {
            int stage = Math.min(stageCount - 1, meta * stageCount / 8);
            result[meta] = tex(baseName + "_" + stage);
        }
        return result;
    }
}