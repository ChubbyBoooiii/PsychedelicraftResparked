package com.chubbyboi.psychedelicraftresparked.client.rendering;

import com.chubbyboi.psychedelicraftresparked.Tags;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.ResourceLocation;

public class RenderSmokeDragon extends RenderDragon {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/entities/smoke_dragon.png");

    public RenderSmokeDragon(RenderManager renderManager) {
        super(renderManager);
        this.layerRenderers.clear();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDragon entity) {
        return TEXTURE;
    }
}
