package com.chubbyboi.psychedelicraftresparked.util.compat.agricraft;

import com.infinityraider.agricraft.api.v1.AgriApi;
import com.infinityraider.agricraft.api.v1.crop.IAgriCrop;
import com.infinityraider.agricraft.api.v1.render.RenderMethod;
import com.infinityraider.agricraft.api.v1.requirement.IGrowthReqBuilder;
import com.infinityraider.agricraft.api.v1.requirement.IGrowthRequirement;
import com.infinityraider.agricraft.api.v1.stat.IAgriStat;
import com.infinityraider.agricraft.api.v1.util.FuzzyStack;
import com.infinityraider.agricraft.farming.growthrequirement.GrowthReqBuilder;
import com.infinityraider.agricraft.renderers.PlantRenderer;
import com.infinityraider.infinitylib.render.tessellation.ITessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public class PsychedelicraftAgriPlant implements com.infinityraider.agricraft.api.v1.plant.IAgriPlant {

    public static final class HarvestProduct {
        private final Item item;
        private final int min;
        private final int max;
        private final double chance;

        public HarvestProduct(Item item, int min, int max, double chance) {
            this.item = item;
            this.min = min;
            this.max = max;
            this.chance = chance;
        }
    }

    private final String id;
    private final String plantName;
    private final Item seedItem;
    private final List<HarvestProduct> harvestProducts;
    private final ResourceLocation[] primaryTextures;

    public PsychedelicraftAgriPlant(String id, String plantName, Item seedItem, List<HarvestProduct> harvestProducts,
                                     ResourceLocation[] primaryTextures) {
        this.id = id;
        this.plantName = plantName;
        this.seedItem = seedItem;
        this.harvestProducts = harvestProducts;
        this.primaryTextures = primaryTextures;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPlantName() {
        return plantName;
    }

    @Override
    public String getSeedName() {
        return plantName + " Seeds";
    }

    @Override
    public Collection<FuzzyStack> getSeedItems() {
        return Collections.singletonList(new FuzzyStack(new ItemStack(seedItem), false, false, "*"));
    }

    @Override
    public boolean isWeed() {
        return false;
    }

    @Override
    public boolean isFertilizable() {
        return true;
    }

    @Override
    public double getSpreadChance() {
        return 0.0;
    }

    @Override
    public double getSpawnChance() {
        return 0.0;
    }

    @Override
    public double getGrowthChanceBase() {
        return 0.9;
    }

    @Override
    public double getGrowthChanceBonus() {
        return 0.025;
    }

    @Override
    public double getSeedDropChanceBase() {
        return 1.0;
    }

    @Override
    public double getSeedDropChanceBonus() {
        return 0.0;
    }

    @Override
    public double getGrassDropChance() {
        return 0.0;
    }

    @Override
    public int getGrowthStages() {
        return 8;
    }

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public String getInformation() {
        return "A Psychedelicraft Resparked crop.";
    }

    @Override
    public ItemStack getSeed() {
        return new ItemStack(seedItem);
    }

    @Override
    public IGrowthRequirement getGrowthRequirement() {
        IGrowthReqBuilder builder = new GrowthReqBuilder().setMinLight(9).setMaxLight(16);
        AgriApi.getSoilRegistry().get(Blocks.FARMLAND.getDefaultState()).ifPresent(builder::addSoil);
        return builder.build();
    }

    @Override
    public void getPossibleProducts(Consumer<ItemStack> products) {
        harvestProducts.forEach(product -> products.accept(new ItemStack(product.item, product.max)));
    }

    @Override
    public void getHarvestProducts(Consumer<ItemStack> products, IAgriCrop crop, IAgriStat stat, Random rand) {
        for (HarvestProduct product : harvestProducts) {
            if (product.chance > rand.nextDouble()) {
                int amount = product.min + rand.nextInt(product.max - product.min + 1);
                products.accept(new ItemStack(product.item, amount));
            }
        }
    }

    @Nullable
    @Override
    public ResourceLocation getSeedTexture() {
        return null;
    }

    @Override
    public float getHeight(int meta) {
        return 0.5f + 0.5f * (meta / (float) (getGrowthStages() - 1));
    }

    @Nullable
    @Override
    public RenderMethod getRenderMethod() {
        return RenderMethod.HASHTAG;
    }

    @Nullable
    @Override
    public ResourceLocation getPrimaryPlantTexture(int meta) {
        return primaryTextures[meta];
    }

    @Nullable
    @Override
    public ResourceLocation getSecondaryPlantTexture(int meta) {
        return null;
    }

    @Override
    public List<BakedQuad> getPlantQuads(IExtendedBlockState state, int growthStage, EnumFacing direction, Function<ResourceLocation, TextureAtlasSprite> textureToIcon) {
        if (textureToIcon instanceof ITessellator) {
            PlantRenderer.renderPlant((ITessellator) textureToIcon, this, growthStage);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PsychedelicraftAgriPlant && ((PsychedelicraftAgriPlant) obj).id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}