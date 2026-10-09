package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.client.rendering.ambient.AmbientEnvironment;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class ShaderPipeline {

    private static ShaderPipeline instance;
    private PingPongBuffer buffer;
    private DepthCaptureBuffer depthBuffer;
    private DepthCaptureBuffer handDepthBuffer;
    private List<ShaderEffect> effects;
    private boolean initialized = false;

    private final FloatBuffer matrixScratch = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer inverseWorldProjection = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer inverseWorldModelView = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer inverseHandProjection = BufferUtils.createFloatBuffer(16);
    private final double[] worldCameraPos = new double[3];
    private boolean hasWorldMatrices = false;

    public static ShaderPipeline getInstance() {
        if (instance == null) {
            instance = new ShaderPipeline();
        }
        return instance;
    }

    private ShaderPipeline() {
        buffer = new PingPongBuffer();
        depthBuffer = new DepthCaptureBuffer();
        handDepthBuffer = new DepthCaptureBuffer();
        effects = new ArrayList<>();
    }

    public void init() {
        if (initialized) {
            return;
        }

        try {
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.WorldColorEffect());
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.HeatDistortionEffect("Heat Distortion Effect", 0.15f,
                () -> PSConfig.biomeHeatDistortion ? AmbientEnvironment.getInstance().getCurrentHeatDistortion() : 0.0f));
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.HeatDistortionEffect("Underwater Distortion Effect", 0.03f,
                () -> PSConfig.waterDistortion ? AmbientEnvironment.getInstance().getCurrentWaterDistortion() : 0.0f));
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.SimpleEffectsShader());
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.BloomEffect());
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.MotionBlurEffect());
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.DoubleVisionEffect());
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.ColorBloomEffect());
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.BlurNoiseEffect());
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.DigitalEffect());

            for (ShaderEffect effect : effects) {
                effect.init();
            }
            initialized = true;
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to initialize shader pipeline!", e);
            initialized = false;
        }
    }

    public int getDepthTexture() {
        return depthBuffer.isReady() ? depthBuffer.getDepthTexture() : 0;
    }

    public void registerEffect(ShaderEffect effect) {
        if (!initialized) {
            effects.add(effect);
        } else {
            PsychedelicraftResparked.LOGGER.warn("Cannot register effects after pipeline initialization!");
        }
    }

    public int getHandDepthTexture() {
        return handDepthBuffer.isReady() ? handDepthBuffer.getDepthTexture() : 0;
    }

    public boolean hasWorldMatrices() {
        return hasWorldMatrices;
    }

    public FloatBuffer getInverseWorldProjection() {
        return inverseWorldProjection;
    }

    public FloatBuffer getInverseWorldModelView() {
        return inverseWorldModelView;
    }

    public FloatBuffer getInverseHandProjection() {
        return inverseHandProjection;
    }

    public double[] getWorldCameraPos() {
        return worldCameraPos;
    }

    public void captureDepth(float partialTicks) {
        if (!anyWants(partialTicks, false)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        depthBuffer.setup(mc.displayWidth, mc.displayHeight);
        if (depthBuffer.isReady()) {
            depthBuffer.captureFrom(mc.getFramebuffer());
        }

        captureInverseMatrix(GL11.GL_PROJECTION_MATRIX, inverseWorldProjection);
        captureInverseMatrix(GL11.GL_MODELVIEW_MATRIX, inverseWorldModelView);
        RenderManager renderManager = mc.getRenderManager();
        worldCameraPos[0] = renderManager.viewerPosX;
        worldCameraPos[1] = renderManager.viewerPosY;
        worldCameraPos[2] = renderManager.viewerPosZ;
        hasWorldMatrices = true;
    }

    public void captureHandProjection(float partialTicks) {
        if (!anyWants(partialTicks, true)) {
            return;
        }
        captureInverseMatrix(GL11.GL_PROJECTION_MATRIX, inverseHandProjection);
    }

    public void captureHandDepth(float partialTicks) {
        if (!anyWants(partialTicks, true)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        handDepthBuffer.setup(mc.displayWidth, mc.displayHeight);
        if (handDepthBuffer.isReady()) {
            handDepthBuffer.captureFrom(mc.getFramebuffer());
        }
    }

    private boolean anyWants(float partialTicks, boolean hand) {
        if (!initialized) {
            return false;
        }
        for (ShaderEffect effect : effects) {
            if (isCategoryEnabled(effect) && effect.shouldApply(partialTicks)
                    && (hand ? effect.wantsHandDepthBuffer(partialTicks) : effect.wantsDepthBuffer(partialTicks))) {
                return true;
            }
        }
        return false;
    }

    private void captureInverseMatrix(int matrix, FloatBuffer out) {
        matrixScratch.clear();
        GL11.glGetFloat(matrix, matrixScratch);
        matrixScratch.rewind();
        Matrix4f inverse = new Matrix4f();
        inverse.load(matrixScratch);
        inverse.invert();
        out.clear();
        inverse.store(out);
        out.rewind();
    }

    public void render(float partialTicks) {
        if (!initialized) {
            return;
        }

        boolean anyActive = false;
        for (ShaderEffect effect : effects) {
            if (isCategoryEnabled(effect) && effect.shouldApply(partialTicks)) {
                anyActive = true;
                break;
            }
        }

        if (!anyActive) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth;
        int height = mc.displayHeight;

        buffer.setup(width, height);

        if (!buffer.isReady()) {
            PsychedelicraftResparked.LOGGER.error("PingPong buffer not ready!");
            return;
        }

        buffer.copyFromScreen();

        // Apply each active effect in sequence
        for (ShaderEffect effect : effects) {
            if (isCategoryEnabled(effect) && effect.shouldApply(partialTicks)) {
                buffer.bindWriteBuffer();

                effect.apply(buffer, partialTicks);

                buffer.swap();
            }
        }

        buffer.renderToScreen();
    }

    private boolean isCategoryEnabled(ShaderEffect effect) {
        if (effect.isAmbient()) {
            return true;
        }
        return effect.isAdvanced() ? PSConfig.advancedShadersEnabled : PSConfig.simpleShadersEnabled;
    }
}