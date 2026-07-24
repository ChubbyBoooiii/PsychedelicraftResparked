package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class ShaderPipeline {

    private static ShaderPipeline instance;
    private PingPongBuffer buffer;
    private List<ShaderEffect> effects;
    private boolean initialized = false;

    public static ShaderPipeline getInstance() {
        if (instance == null) {
            instance = new ShaderPipeline();
        }
        return instance;
    }

    private ShaderPipeline() {
        buffer = new PingPongBuffer();
        effects = new ArrayList<>();
    }

    public void init() {
        if (initialized) {
            return;
        }

        try {
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.SimpleEffectsShader());
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.BloomEffect());
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.MotionBlurEffect());
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.DoubleVisionEffect());
            registerEffect(new com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects.ColorBloomEffect());

            for (ShaderEffect effect : effects) {
                effect.init();
            }
            initialized = true;
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to initialize shader pipeline!", e);
            initialized = false;
        }
    }

    public void registerEffect(ShaderEffect effect) {
        if (!initialized) {
            effects.add(effect);
        } else {
            PsychedelicraftResparked.LOGGER.warn("Cannot register effects after pipeline initialization!");
        }
    }

    public void render(float partialTicks) {
        if (!initialized) {
            return;
        }

        boolean anyActive = false;
        for (ShaderEffect effect : effects) {
            if (effect.shouldApply(partialTicks)) {
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
            if (effect.shouldApply(partialTicks)) {
                buffer.bindWriteBuffer();

                effect.apply(buffer, partialTicks);

                buffer.swap();
            }
        }

        buffer.renderToScreen();
    }
}