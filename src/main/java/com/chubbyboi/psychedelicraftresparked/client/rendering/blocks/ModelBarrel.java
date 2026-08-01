package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelBarrel extends ModelBase {
    private final ModelRenderer base;
    private final ModelRenderer top;
    private final ModelRenderer bottom;
    private final ModelRenderer right;
    private final ModelRenderer left;
    private final ModelRenderer corner1;
    private final ModelRenderer corner2;
    private final ModelRenderer corner3;
    private final ModelRenderer corner4;
    private final ModelRenderer rack1;
    private final ModelRenderer rack2;
    private final ModelRenderer rack3;
    private final ModelRenderer tap1;
    private final ModelRenderer tap2;
    private final ModelRenderer tap3;

    public ModelBarrel() {
        textureWidth = 128;
        textureHeight = 64;

        base = new ModelRenderer(this, 0, 0);
        base.addBox(-4F, -4F, 0F, 8, 8, 14);
        base.setRotationPoint(0F, 15F, -7F);
        base.mirror = true;

        top = new ModelRenderer(this, 45, 0);
        top.addBox(-4F, -6F, -1F, 8, 2, 16);
        top.setRotationPoint(0F, 15F, -7F);
        top.mirror = true;

        bottom = new ModelRenderer(this, 45, 19);
        bottom.addBox(-4F, 4F, -1F, 8, 2, 16);
        bottom.setRotationPoint(0F, 15F, -7F);
        bottom.mirror = true;

        right = new ModelRenderer(this, 45, 38);
        right.addBox(-6F, -4F, -1F, 2, 8, 16);
        right.setRotationPoint(0F, 15F, -7F);
        right.mirror = true;

        left = new ModelRenderer(this, 82, 38);
        left.addBox(4F, -4F, -1F, 2, 8, 16);
        left.setRotationPoint(0F, 15F, -7F);
        left.mirror = true;

        corner1 = new ModelRenderer(this, 0, 23);
        corner1.addBox(-4F, -4F, -1F, 1, 1, 16);
        corner1.setRotationPoint(0F, 15F, -7F);
        corner1.mirror = true;

        corner2 = new ModelRenderer(this, 0, 26);
        corner2.addBox(3F, -4F, -1F, 1, 1, 16);
        corner2.setRotationPoint(0F, 15F, -7F);
        corner2.mirror = true;

        corner3 = new ModelRenderer(this, 0, 28);
        corner3.addBox(-4F, 3F, -1F, 1, 1, 16);
        corner3.setRotationPoint(0F, 15F, -7F);
        corner3.mirror = true;

        corner4 = new ModelRenderer(this, 0, 30);
        corner4.addBox(3F, 3F, -1F, 1, 1, 16);
        corner4.setRotationPoint(0F, 15F, -7F);
        corner4.mirror = true;

        rack1 = new ModelRenderer(this, 94, 12);
        rack1.addBox(-5F, 0F, -1F, 10, 4, 2);
        rack1.setRotationPoint(0F, 20.5F, -5F);
        rack1.rotateAngleX = -0.1487144F;
        rack1.mirror = true;

        rack2 = new ModelRenderer(this, 94, 19);
        rack2.addBox(-5F, 0F, 0F, 10, 4, 2);
        rack2.setRotationPoint(0F, 20.5F, 4F);
        rack2.rotateAngleX = 0.1487195F;
        rack2.mirror = true;

        rack3 = new ModelRenderer(this, 94, 0);
        rack3.addBox(-1F, 7F, 2F, 2, 1, 10);
        rack3.setRotationPoint(0F, 15F, -7F);
        rack3.mirror = true;

        tap1 = new ModelRenderer(this, 0, 50);
        tap1.addBox(-0.5F, 2F, -1.5F, 1, 1, 2);
        tap1.setRotationPoint(0F, 15F, -7F);
        tap1.mirror = true;

        tap2 = new ModelRenderer(this, 7, 50);
        tap2.addBox(-0.5F, 1.8F, -2.5F, 1, 2, 1);
        tap2.setRotationPoint(0F, 15F, -7F);
        tap2.mirror = true;

        tap3 = new ModelRenderer(this, 12, 50);
        tap3.addBox(-1.5F, -0.21F, -0.5F, 3, 0, 1);
        tap3.setRotationPoint(0F, 17F, -9F);
        tap3.mirror = true;
    }

    public void render(float scale, float tapRotation) {
        tap3.rotateAngleY = tapRotation;

        base.render(scale);
        top.render(scale);
        bottom.render(scale);
        right.render(scale);
        left.render(scale);
        corner1.render(scale);
        corner2.render(scale);
        corner3.render(scale);
        corner4.render(scale);
        rack1.render(scale);
        rack2.render(scale);
        rack3.render(scale);
        if (hasTap) {
            tap1.render(scale);
            tap2.render(scale);
            tap3.render(scale);
        }
    }
}