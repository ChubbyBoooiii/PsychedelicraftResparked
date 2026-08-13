package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public class DepthCaptureBuffer {

    private int framebufferObject = -1;
    private int depthTexture = -1;
    private int width = -1;
    private int height = -1;

    public void setup(int screenWidth, int screenHeight) {
        if (framebufferObject >= 0 && width == screenWidth && height == screenHeight) {
            return;
        }

        cleanup();

        width = screenWidth;
        height = screenHeight;

        depthTexture = GlStateManager.generateTexture();
        GlStateManager.bindTexture(depthTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0,
            GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (java.nio.FloatBuffer) null);
        GlStateManager.bindTexture(0);

        framebufferObject = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferObject);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTexture, 0);
        GL11.glDrawBuffer(GL11.GL_NONE);
        GL11.glReadBuffer(GL11.GL_NONE);

        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            cleanup();
        }
    }

    public boolean captureFrom(Framebuffer source) {
        if (framebufferObject < 0 || source.framebufferObject < 0) {
            return false;
        }

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, source.framebufferObject);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebufferObject);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        return true;
    }

    public int getDepthTexture() {
        return depthTexture;
    }

    public boolean isReady() {
        return framebufferObject >= 0 && depthTexture >= 0;
    }

    public void cleanup() {
        if (depthTexture >= 0) {
            GlStateManager.deleteTexture(depthTexture);
            depthTexture = -1;
        }
        if (framebufferObject >= 0) {
            GL30.glDeleteFramebuffers(framebufferObject);
            framebufferObject = -1;
        }
        width = -1;
        height = -1;
    }
}