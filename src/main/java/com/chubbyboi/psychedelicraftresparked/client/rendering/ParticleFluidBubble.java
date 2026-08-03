package com.chubbyboi.psychedelicraftresparked.client.rendering;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;

public class ParticleFluidBubble extends Particle {

    public ParticleFluidBubble(World world, double x, double y, double z, double motionX, double motionY, double motionZ, TextureAtlasSprite sprite) {
        super(world, x, y, z);
        particleRed = 1.0F;
        particleGreen = 1.0F;
        particleBlue = 1.0F;
        setParticleTexture(sprite);
        setSize(0.02F, 0.02F);
        particleScale *= rand.nextFloat() * 0.6F + 0.2F;
        this.motionX = motionX * 0.2F + (Math.random() * 2.0D - 1.0D) * 0.02D;
        this.motionY = motionY * 0.2F + (Math.random() * 2.0D - 1.0D) * 0.02D;
        this.motionZ = motionZ * 0.2F + (Math.random() * 2.0D - 1.0D) * 0.02D;
        particleMaxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        motionY += 0.002D;
        move(motionX, motionY, motionZ);
        motionX *= 0.85D;
        motionY *= 0.85D;
        motionZ *= 0.85D;

        if (particleMaxAge-- <= 0) {
            setExpired();
        }
    }
}