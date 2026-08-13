package com.chubbyboi.psychedelicraftresparked.drug.drugs;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.drug.DrugBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class DrugPower extends DrugBase {

    private static final ResourceLocation POWER_PARTICLE = new ResourceLocation(Tags.MOD_ID, "textures/particles/power_particle.png");
    private static final ResourceLocation[] LIGHTNING_TEXTURES = new ResourceLocation[4];
    static {
        for (int i = 0; i < LIGHTNING_TEXTURES.length; i++) {
            LIGHTNING_TEXTURES[i] = new ResourceLocation(Tags.MOD_ID, "textures/particles/lightning_" + i + ".png");
        }
    }

    public DrugPower() {
        super(0.95, 0.0001);
    }

    @Override
    public String getName() {
        return "power";
    }

    @Override
    public float getMotionBlurStrength() {
        return getActiveValue() * 0.3f;
    }

    @Override
    public float getColourDesaturationModifier() {
        return getActiveValue() * 0.75f;
    }

    @Override
    public float getSoundVolumeModifier() {
        return 1.0f - getActiveValue();
    }

    @Override
    public void drawOverlays(float partialTicks, int width, int height) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) return;

        float power = getActiveValue();
        int ticksExisted = player.ticksExisted;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        Random powerR = new Random(ticksExisted);
        int powerParticles = MathHelper.floor(powerR.nextFloat() * 200.0f * power);
        if (powerParticles > 0) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Minecraft.getMinecraft().getTextureManager().bindTexture(POWER_PARTICLE);
            renderRandomParticles(buffer, tessellator, powerParticles,
                height / 10, MathHelper.ceil(height / 10.0f * power), width, height, powerR);
        }

        Random powerLR = new Random((ticksExisted / 2) * 21124871824L);
        float lightningChance = (power - 0.5f) * 0.1f;
        int powerLightnings = 0;
        while (powerLR.nextFloat() < lightningChance && powerLightnings < 3) {
            powerLightnings++;
        }

        if (powerLightnings > 0) {
            int lightningW = height;

            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
            for (int i = 0; i < powerLightnings; i++) {
                float lX = powerLR.nextInt(width + lightningW) - lightningW;
                lX += (powerLR.nextFloat() - 0.5f) * lightningW * partialTicks * 2.0f;
                int lIndex = powerLR.nextInt(LIGHTNING_TEXTURES.length);
                boolean upsideDown = powerLR.nextBoolean();
                float lightningTime = ((ticksExisted % 2) + partialTicks) * 0.5f;

                GlStateManager.color(1.0f, 1.0f, 1.0f, (0.05f + power * 0.1f) * (1.0f - lightningTime));
                Minecraft.getMinecraft().getTextureManager().bindTexture(LIGHTNING_TEXTURES[lIndex]);

                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(lX, height, -90.0).tex(0.0, upsideDown ? 0.0 : 1.0).endVertex();
                buffer.pos(lX + lightningW, height, -90.0).tex(1.0, upsideDown ? 0.0 : 1.0).endVertex();
                buffer.pos(lX + lightningW, 0.0, -90.0).tex(1.0, upsideDown ? 1.0 : 0.0).endVertex();
                buffer.pos(lX, 0.0, -90.0).tex(0.0, upsideDown ? 1.0 : 0.0).endVertex();
                tessellator.draw();
            }
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void renderRandomParticles(BufferBuilder buffer, Tessellator tessellator, int number,
            int quadWidth, int quadHeight, int screenWidth, int screenHeight, Random rand) {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for (int i = 0; i < number; i++) {
            int x = rand.nextInt(screenWidth + quadWidth) - quadWidth;
            int y = rand.nextInt(screenHeight + quadHeight) - quadHeight;

            buffer.pos(x, y + quadHeight, -90.0).tex(0.0, 1.0).endVertex();
            buffer.pos(x + quadWidth, y + quadHeight, -90.0).tex(1.0, 1.0).endVertex();
            buffer.pos(x + quadWidth, y, -90.0).tex(1.0, 0.0).endVertex();
            buffer.pos(x, y, -90.0).tex(0.0, 0.0).endVertex();
        }
        tessellator.draw();
    }
}