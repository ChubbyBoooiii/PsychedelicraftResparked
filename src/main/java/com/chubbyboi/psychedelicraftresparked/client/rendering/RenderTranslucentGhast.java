package com.chubbyboi.psychedelicraftresparked.client.rendering;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderGhast;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityGhast;

public class RenderTranslucentGhast extends RenderGhast {

    private float alpha = 1.0f;
    private float colorR = 1.0f;
    private float colorG = 1.0f;
    private float colorB = 1.0f;

    public RenderTranslucentGhast(RenderManager renderManager) {
        super(renderManager);
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setColor(float colorR, float colorG, float colorB) {
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
    }

    @Override
    protected void preRenderCallback(EntityGhast entity, float partialTickTime) {
        GlStateManager.scale(4.5F, 4.5F, 4.5F);
        GlStateManager.color(colorR, colorG, colorB, alpha);
    }
}
