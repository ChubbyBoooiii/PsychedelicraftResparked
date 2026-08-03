package com.chubbyboi.psychedelicraftresparked.client.rendering;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ParticleFluidSplash extends Particle {

    public ParticleFluidSplash(World world, double x, double y, double z, double motionX, double motionY, double motionZ, TextureAtlasSprite sprite) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.motionX *= 0.3D;
        this.motionY = Math.random() * 0.2D + 0.1D;
        this.motionZ *= 0.3D;
        particleRed = 1.0F;
        particleGreen = 1.0F;
        particleBlue = 1.0F;
        setParticleTexture(sprite);
        setSize(0.01F, 0.01F);
        particleGravity = 0.06F;
        particleMaxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D));

        if (motionY == 0.0D && (motionX != 0.0D || motionZ != 0.0D)) {
            this.motionX = motionX;
            this.motionY = motionY + 0.1D;
            this.motionZ = motionZ;
        }
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
        motionY -= particleGravity;
        move(motionX, motionY, motionZ);
        motionX *= 0.98D;
        motionY *= 0.98D;
        motionZ *= 0.98D;

        if (particleMaxAge-- <= 0) {
            setExpired();
        }

        if (onGround) {
            if (Math.random() < 0.5D) {
                setExpired();
            }
            motionX *= 0.7D;
            motionZ *= 0.7D;
        }

        BlockPos blockpos = new BlockPos(posX, posY, posZ);
        IBlockState state = world.getBlockState(blockpos);
        Material material = state.getMaterial();

        if (material.isLiquid() || material.isSolid()) {
            double surfaceY;
            if (state.getBlock() instanceof BlockLiquid) {
                surfaceY = 1.0F - BlockLiquid.getLiquidHeightPercent(state.getValue(BlockLiquid.LEVEL));
            } else {
                surfaceY = state.getBoundingBox(world, blockpos).maxY;
            }

            double top = MathHelper.floor(posY) + surfaceY;
            if (posY < top) {
                setExpired();
            }
        }
    }
}