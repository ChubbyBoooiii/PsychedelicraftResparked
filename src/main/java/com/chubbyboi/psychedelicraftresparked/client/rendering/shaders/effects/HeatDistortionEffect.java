package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.PingPongBuffer;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderEffect;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderPipeline;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.function.Supplier;

public class HeatDistortionEffect implements ShaderEffect {

    private static final ResourceLocation NOISE_TEXTURE =
        new ResourceLocation(Tags.MOD_ID, "textures/effects/heat_distortion_noise.png");

    private final String name;
    private final float wobbleSpeed;
    private final Supplier<Float> strength;

    private int shaderProgram = 0;

    public HeatDistortionEffect(String name, float wobbleSpeed, Supplier<Float> strength) {
        this.name = name;
        this.wobbleSpeed = wobbleSpeed;
        this.strength = strength;
    }

    @Override
    public void init() {
        try {
            String vertexSource = ShaderUtils.loadShader("shader_basic.vert");
            String fragmentSource = ShaderUtils.loadShaderWithUtils("shader_heat_distortion.frag");

            int vertexShader = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);
            int fragmentShader = compileShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);

            shaderProgram = linkProgram(vertexShader, fragmentShader);

            GL20.glDeleteShader(vertexShader);
            GL20.glDeleteShader(fragmentShader);
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to initialize " + name + " shader!", e);
            shaderProgram = 0;
        }
    }

    @Override
    public void cleanup() {
        if (shaderProgram != 0) {
            GL20.glDeleteProgram(shaderProgram);
            shaderProgram = 0;
        }
    }

    @Override
    public boolean shouldApply(float partialTicks) {
        return shaderProgram != 0 && getStrength(partialTicks) > 0.0f;
    }

    @Override
    public float getStrength(float partialTicks) {
        if (Minecraft.getMinecraft().player == null) return 0.0f;
        return strength.get();
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return getStrength(partialTicks) > 0.0f;
    }

    @Override
    public boolean isAmbient() {
        return true;
    }

    @Override
    public void apply(PingPongBuffer buffer, float partialTicks) {
        float distortion = getStrength(partialTicks);
        if (distortion <= 0.0f) return;

        Minecraft mc = Minecraft.getMinecraft();
        Entity viewEntity = mc.getRenderViewEntity();
        float ticks = (viewEntity != null ? viewEntity.ticksExisted : 0) + partialTicks;

        // 0 until the first capture lands; an unbound depth sampler reads 0, which zeroes depthMul (no-op pass)
        int depthTexture = ShaderPipeline.getInstance().getDepthTexture();

        GlStateManager.disableDepth();
        GL11.glDepthMask(false);
        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        GL20.glUseProgram(shaderProgram);

        GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "tex0"), 0);
        GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "noiseTex"), 1);
        GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "depthTex"), 2);
        GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "totalAlpha"), 1.0f);
        GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "ticks"), ticks * wobbleSpeed);
        GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "strength"), distortion);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE2);
        GlStateManager.bindTexture(depthTexture);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        mc.getTextureManager().bindTexture(NOISE_TEXTURE);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(buffer.getReadTexture());

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, buffer.getWriteBuffer().framebufferObject);

        renderFullScreenQuad();

        GL20.glUseProgram(0);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE2);
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);

        GL11.glDepthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableCull();
    }

    @Override
    public String getName() {
        return name;
    }

    private int compileShader(String source, int type) throws Exception {
        String shaderType = (type == GL20.GL_VERTEX_SHADER) ? "VERTEX" : "FRAGMENT";

        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        int status = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS);
        if (status == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader, 1024);
            throw new Exception(shaderType + " shader compilation failed:\n" + log);
        }

        return shader;
    }

    private int linkProgram(int vertexShader, int fragmentShader) throws Exception {
        int program = GL20.glCreateProgram();

        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);

        int status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(program, 1024);
            throw new Exception("Shader program linking failed:\n" + log);
        }

        return program;
    }

    private void renderFullScreenQuad() {
        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth;
        int height = mc.displayHeight;

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