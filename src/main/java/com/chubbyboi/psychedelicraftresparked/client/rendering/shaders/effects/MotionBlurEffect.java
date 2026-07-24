package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.PingPongBuffer;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderEffect;
import com.chubbyboi.psychedelicraftresparked.drug.IDrug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

public class MotionBlurEffect implements ShaderEffect {

    private static final int SAMPLE_COUNT = 30;
    private static final float SAMPLE_FREQUENCY = 0.5f;
    private static final float ALPHA_STEP = 0.02f;
    private static final float ALPHA_CAP = 0.1f;

    private int[] cacheTextures;
    private boolean[] cacheInitialized;
    private int cacheIndex = 0;
    private float previousTicks = 0.0f;
    private int currentWidth = -1;
    private int currentHeight = -1;

    @Override
    public void init() {
        // No shader to compile - pure texture accumulation.
    }

    @Override
    public void cleanup() {
        destructTextures();
    }

    @Override
    public boolean shouldApply(float partialTicks) {
        return getStrength(partialTicks) > 0.001f;
    }

    @Override
    public float getStrength(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) {
            return 0.0f;
        }

        IDrugProperties props = mc.player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) {
            return 0.0f;
        }

        float value = 0.0f;
        for (IDrug drug : ((DrugProperties) props).getAllDrugs()) {
            if (drug.getActiveValue() > 0.001f) {
                value += (1.0f - value) * drug.getMotionBlurStrength();
            }
        }
        return value;
    }

    @Override
    public void apply(PingPongBuffer buffer, float partialTicks) {
        float motionBlur = getStrength(partialTicks);

        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth;
        int height = mc.displayHeight;

        if (motionBlur <= 0.001f) {
            if (cacheInitialized != null) {
                cacheIndex = (cacheIndex + 1) % cacheTextures.length;
                cacheInitialized[cacheIndex] = false;
            }
            return;
        }

        if (width != currentWidth || height != currentHeight) {
            setUpTextures(width, height);
        }

        GlStateManager.disableDepth();
        GL11.glDepthMask(false);
        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, buffer.getWriteBuffer().framebufferObject);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(buffer.getReadTexture());
        renderFullScreenQuad(width, height);

        float ticks = mc.ingameGUI.getUpdateCounter() + partialTicks;
        if (previousTicks > ticks) {
            previousTicks = ticks;
        } else if (previousTicks + SAMPLE_FREQUENCY * cacheTextures.length < ticks) {
            previousTicks = ticks - SAMPLE_FREQUENCY * cacheTextures.length;
        }

        while (previousTicks + SAMPLE_FREQUENCY <= ticks) {
            cacheIndex = (cacheIndex + 1) % cacheTextures.length;
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
            GlStateManager.bindTexture(cacheTextures[cacheIndex]);
            GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0, width, height, 0);
            cacheInitialized[cacheIndex] = true;
            previousTicks += SAMPLE_FREQUENCY;
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );

        for (int i = 0; i < cacheTextures.length; i++) {
            int index = (i + cacheIndex) % cacheTextures.length;
            if (!cacheInitialized[index]) {
                continue;
            }

            float alpha = i * ALPHA_STEP * motionBlur;
            if (alpha > ALPHA_CAP) {
                alpha = ALPHA_CAP;
            }
            if (alpha <= 0.0f) {
                continue;
            }

            GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
            GlStateManager.bindTexture(cacheTextures[index]);
            renderFullScreenQuad(width, height);
        }

        GlStateManager.disableBlend();
        GL11.glDepthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public String getName() {
        return "Motion Blur Effect";
    }

    private void setUpTextures(int width, int height) {
        destructTextures();

        cacheTextures = new int[SAMPLE_COUNT];
        cacheInitialized = new boolean[SAMPLE_COUNT];

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        for (int i = 0; i < cacheTextures.length; i++) {
            cacheTextures[i] = GlStateManager.generateTexture();
            GlStateManager.bindTexture(cacheTextures[i]);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        }

        currentWidth = width;
        currentHeight = height;
    }

    private void destructTextures() {
        if (cacheTextures != null) {
            for (int texture : cacheTextures) {
                if (texture > 0) {
                    GlStateManager.deleteTexture(texture);
                }
            }
        }
        cacheTextures = null;
        cacheInitialized = null;
        cacheIndex = 0;
        currentWidth = -1;
        currentHeight = -1;
    }

    private void renderFullScreenQuad(int width, int height) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, -1, 1);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(0, height);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2f(width, height);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2f(width, 0);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2f(0, 0);
        GL11.glEnd();

        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }
}
