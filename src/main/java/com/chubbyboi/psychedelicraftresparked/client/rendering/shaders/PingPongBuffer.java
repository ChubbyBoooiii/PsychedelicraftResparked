package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
public class PingPongBuffer {

    private Framebuffer[] buffers = new Framebuffer[2];
    private int activeBuffer = 0;
    private int width = -1;
    private int height = -1;

    public void setup(int screenWidth, int screenHeight) {
        if (width != screenWidth || height != screenHeight) {
            cleanup();

            width = screenWidth;
            height = screenHeight;

            buffers[0] = new Framebuffer(width, height, true);
            buffers[1] = new Framebuffer(width, height, true);
        }
    }

    public void swap() {
        activeBuffer = 1 - activeBuffer;
    }

    public Framebuffer getReadBuffer() {
        return buffers[activeBuffer];
    }

    public Framebuffer getWriteBuffer() {
        return buffers[1 - activeBuffer];
    }

    public int getReadTexture() {
        return buffers[activeBuffer].framebufferTexture;
    }

    public void copyFromScreen() {
        Framebuffer mcBuffer = Minecraft.getMinecraft().getFramebuffer();

        // Bind the buffer as the render target
        buffers[activeBuffer].bindFramebuffer(false);
        buffers[activeBuffer].framebufferClear();

        mcBuffer.unbindFramebuffer();
        buffers[activeBuffer].bindFramebuffer(true);

        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        mcBuffer.framebufferRenderExt(width, height, false);
    }

    public void bindWriteBuffer() {
        getWriteBuffer().bindFramebuffer(false);
        getWriteBuffer().framebufferClear();
    }

    public void unbind() {
        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
    }

    public int getActiveBuffer() {
        return activeBuffer;
    }

    public void renderToScreen() {
        Minecraft mc = Minecraft.getMinecraft();
        int mcFramebuffer = 0;
        if (mc.getFramebuffer() != null && mc.getFramebuffer().framebufferObject >= 0) {
            mcFramebuffer = mc.getFramebuffer().framebufferObject;
        }

        unbind();
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, mcFramebuffer);

        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        getReadBuffer().framebufferRenderExt(width, height, false);
    }

    public void cleanup() {
        for (int i = 0; i < 2; i++) {
            if (buffers[i] != null) {
                buffers[i].deleteFramebuffer();
                buffers[i] = null;
            }
        }
        width = -1;
        height = -1;
    }

    public boolean isReady() {
        return buffers[0] != null && buffers[1] != null;
    }
}