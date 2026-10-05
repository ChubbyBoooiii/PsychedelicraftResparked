package com.chubbyboi.psychedelicraftresparked.proxy;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.client.rendering.DrugVisualRenderer;
import com.chubbyboi.psychedelicraftresparked.client.rendering.HallucinationEntitySpawner;
import com.chubbyboi.psychedelicraftresparked.client.rendering.RenderMolotovCocktail;
import com.chubbyboi.psychedelicraftresparked.client.rendering.RenderRealityRift;
import com.chubbyboi.psychedelicraftresparked.client.rendering.SmokeMonsterSpawner;
import com.chubbyboi.psychedelicraftresparked.entities.EntityMolotovCocktail;
import com.chubbyboi.psychedelicraftresparked.entities.EntityRealityRift;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererDryingTable;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererBarrel;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererDistillery;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityItemStackRendererDistillery;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererFlask;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityItemStackRendererFlask;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityItemStackRendererBottle;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererVat;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererPeyote;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererPlacedContainers;
import com.chubbyboi.psychedelicraftresparked.client.rendering.placedcontainers.PlacementPreviewRenderer;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererRiftJar;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderPipeline;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.WorldShaderEffect;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.item.ItemBottle;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDistillery;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDryingTable;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityFlask;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityPeyote;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityPlacedContainers;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityRiftJar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.function.IntUnaryOperator;

public class ClientProxy extends CommonProxy {

    private Boolean lastKnownFancyGraphics;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().world == null) {
            return;
        }

        boolean fancy = Minecraft.getMinecraft().gameSettings.fancyGraphics;
        if (lastKnownFancyGraphics == null || lastKnownFancyGraphics != fancy) {
            lastKnownFancyGraphics = fancy;
            BlockInit.JUNIPER_LEAVES.setGraphicsLevel(fancy);
        }
    }

    @Override
    public void registerItemRenderer(Item item, int meta, String id) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
    }

    @Override
    public void registerItemRenderer(Item item, int meta, String modelName, String id) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(Tags.MOD_ID + ":" + modelName, id));
    }

    @Override
    public void registerRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDryingTable.class, new TileEntityRendererDryingTable());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPeyote.class, new TileEntityRendererPeyote());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityVat.class, new TileEntityRendererVat());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrel.class, new TileEntityRendererBarrel());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDistillery.class, new TileEntityRendererDistillery());
        ItemInit.DISTILLERY_ITEM.setTileEntityItemStackRenderer(new TileEntityItemStackRendererDistillery());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFlask.class, new TileEntityRendererFlask());
        ItemInit.FLASK_ITEM.setTileEntityItemStackRenderer(new TileEntityItemStackRendererFlask());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRiftJar.class, new TileEntityRendererRiftJar());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPlacedContainers.class, new TileEntityRendererPlacedContainers());
        ItemInit.BOTTLE.setTileEntityItemStackRenderer(new TileEntityItemStackRendererBottle());

        RenderingRegistry.registerEntityRenderingHandler(EntityMolotovCocktail.class,
            manager -> new RenderMolotovCocktail(manager, ItemInit.MOLOTOV_COCKTAIL, Minecraft.getMinecraft().getRenderItem()));

        RenderingRegistry.registerEntityRenderingHandler(EntityRealityRift.class, RenderRealityRift::new);

        ShaderPipeline.getInstance().init();
        WorldShaderEffect.getInstance().init();
        com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ZeroMatterShader.getInstance().init();
        MinecraftForge.EVENT_BUS.register(new DrugVisualRenderer());
        MinecraftForge.EVENT_BUS.register(new HallucinationEntitySpawner());
        MinecraftForge.EVENT_BUS.register(SmokeMonsterSpawner.getInstance());
        MinecraftForge.EVENT_BUS.register(new PlacementPreviewRenderer());

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onItemColorHandler(ColorHandlerEvent.Item event) {
        event.getItemColors().registerItemColorHandler(
            (stack, tintIndex) -> tintIndex == 0 ? EnumDyeColor.byMetadata(stack.getMetadata()).getColorValue() : 0xFFFFFF,
            ItemInit.HARMONIUM
        );

        event.getItemColors().registerItemColorHandler(
            (stack, tintIndex) -> tintIndex == 1 ? FluidHelper.getFluidColor(stack) : 0xFFFFFF,
            ItemInit.WOODEN_MUG, ItemInit.GLASS_CHALICE, ItemInit.SHOT_GLASS, ItemInit.SYRINGE
        );

        event.getItemColors().registerItemColorHandler(
            bottleColors(ItemBottle::getGlassColor), ItemInit.BOTTLE);
        event.getItemColors().registerItemColorHandler(
            bottleColors(meta -> ItemBottle.isClear(meta) ? 0xFFFFFF : EnumDyeColor.byMetadata(meta).getColorValue()), ItemInit.MOLOTOV_COCKTAIL);
    }

    private static IItemColor bottleColors(IntUnaryOperator glassColor) {
        return (stack, tintIndex) -> {
            boolean filled = FluidHelper.hasFluid(stack);
            if (tintIndex == 0) {
                return filled ? FluidHelper.getFluidColor(stack) : glassColor.applyAsInt(stack.getMetadata());
            }
            if (tintIndex == 1 && filled) {
                return glassColor.applyAsInt(stack.getMetadata());
            }
            return 0xFFFFFF;
        };
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/clear_still"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/clear_flow"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/beer_still"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/beer_flow"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_still"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_flow"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_still"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_flow"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/slurry_still"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/slurry_flow"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/rice_wine_still"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/rice_wine_flow"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/tea_still"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/tea_flow"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/cider_still"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/cider_flow"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/wine_still"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/wine_flow"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/mead_still"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:blocks/mead_flow"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:particles/fluid_bubble"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:particles/fluid_splash"));
        TileEntityRendererPlacedContainers.registerTextures(event.getMap());
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        TileEntityRendererPlacedContainers.bakeModels();
    }
}