package com.chubbyboi.psychedelicraftresparked.util;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.entities.EntityMolotovCocktail;
import com.chubbyboi.psychedelicraftresparked.entities.EntityRealityRift;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.init.OreDictionaryInit;
import com.chubbyboi.psychedelicraftresparked.init.SoundInit;
import com.chubbyboi.psychedelicraftresparked.recipes.CraftingRecipes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

@Mod.EventBusSubscriber
public class RegistryHandler {
    @SubscribeEvent
    public static void onBlockRegister(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(BlockInit.BLOCKS.toArray(new Block[0]));
        TileEntityHandler.registerTileEntities();
    }

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        // Register all items (includes ItemBlocks that blocks created themselves)
        event.getRegistry().registerAll(ItemInit.ITEMS.toArray(new Item[0]));

        // Now set the seed and drop references on crops, as well as which seeds make each crop
        ItemInit.setCropForSeeds();
        BlockInit.setCropDropsAndSeeds();

        OreDictionaryInit.registerOres();
    }

    @SubscribeEvent
    public static void onSoundRegister(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(SoundInit.HEARTBEAT, SoundInit.BREATH);
    }

    @SubscribeEvent
    public static void onEntityRegister(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().register(EntityEntryBuilder.create()
            .entity(EntityMolotovCocktail.class)
            .id(new ResourceLocation(Tags.MOD_ID, "molotov_cocktail"), 0)
            .name("molotov_cocktail")
            .tracker(64, 10, true)
            .build());

        event.getRegistry().register(EntityEntryBuilder.create()
            .entity(EntityRealityRift.class)
            .id(new ResourceLocation(Tags.MOD_ID, "reality_rift"), 1)
            .name("reality_rift")
            .tracker(80, 3, false)
            .build());
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        for (Item item : ItemInit.ITEMS) {
            if (item == ItemInit.HARMONIUM || item == ItemInit.BOTTLE || item == ItemInit.MOLOTOV_COCKTAIL) {
                // 16 dye-coloured variants all share the one model (harmonium.json/bottle.json/molotov_cocktail.json). The tint handler (ClientProxy) is what actually differentiates them, not separate models.
                for (int meta = 0; meta < 16; meta++) {
                    PsychedelicraftResparked.proxy.registerItemRenderer(item, meta, "inventory");
                }
            } else if (item == ItemInit.VAT) {
                for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
                    PsychedelicraftResparked.proxy.registerItemRenderer(item, type.getMetadata(), "vat_" + type.getName(), "inventory");
                }
            } else if (item == ItemInit.BARREL_ITEM) {
                for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
                    PsychedelicraftResparked.proxy.registerItemRenderer(item, type.getMetadata(), "barrel_" + type.getName(), "inventory");
                }
            } else {
                PsychedelicraftResparked.proxy.registerItemRenderer(item, 0, "inventory");
            }
        }
    }

    public static void preInitRegistries() {
        FluidInit.registerFluids();
    }

    public static void initRegistries() {
        NetworkRegistry.INSTANCE.registerGuiHandler(PsychedelicraftResparked.instance, new GuiHandler());
        CraftingRecipes.registerCraftingRecipes();
    }

    public static void postInitRegistries() {

    }

    public static void serverRegistries(FMLServerStartingEvent event) {

    }
}