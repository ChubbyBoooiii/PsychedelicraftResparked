package com.chubbyboi.psychedelicraftresparked.proxy;

import com.chubbyboi.psychedelicraftresparked.client.rendering.DrugVisualRenderer;
import com.chubbyboi.psychedelicraftresparked.client.rendering.HallucinationEntitySpawner;
import com.chubbyboi.psychedelicraftresparked.client.rendering.SmokeMonsterSpawner;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererDryingTable;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererBarrel;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererDistillery;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityItemStackRendererDistillery;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererFlask;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityItemStackRendererFlask;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererVat;
import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererPeyote;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderPipeline;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.WorldShaderEffect;
import com.chubbyboi.psychedelicraftresparked.commands.CommandHallucinationDebug;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDistillery;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDryingTable;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityFlask;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityPeyote;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerItemRenderer(Item item, int meta, String id) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
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

        ShaderPipeline.getInstance().init();
        WorldShaderEffect.getInstance().init();
        MinecraftForge.EVENT_BUS.register(new DrugVisualRenderer());
        MinecraftForge.EVENT_BUS.register(new HallucinationEntitySpawner());
        MinecraftForge.EVENT_BUS.register(SmokeMonsterSpawner.getInstance());
        ClientCommandHandler.instance.registerCommand(new CommandHallucinationDebug());

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
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:particles/fluid_bubble"));
        event.getMap().registerSprite(new ResourceLocation("psychedelicraftresparked:particles/fluid_splash"));
    }
}