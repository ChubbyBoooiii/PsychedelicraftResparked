package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.PingPongBuffer;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderEffect;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderPipeline;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderUtils;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class DigitalEffect implements ShaderEffect {

    private static final ResourceLocation DIGITAL_TEXT_TEXTURE =
        new ResourceLocation(Tags.MOD_ID, "textures/particles/digital_text.png");

    private int digitalProgram = 0;
    private int digitalDepthProgram = 0;

    @Override
    public void init() {
        String vertexSource;
        try {
            vertexSource = ShaderUtils.loadShader("shader_basic.vert");
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to load shader_basic.vert for Digital shader!", e);
            return;
        }

        try {
            String fragmentSource = ShaderUtils.loadShaderWithUtils("shader_digital.frag");
            digitalProgram = compileProgram(vertexSource, fragmentSource);
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to initialize Digital shader (no-depth variant)!", e);
            digitalProgram = 0;
        }

        try {
            String fragmentSource = ShaderUtils.loadShaderWithUtils("shader_digital_depth.frag");
            digitalDepthProgram = compileProgram(vertexSource, fragmentSource);
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to initialize Digital shader (depth variant)!", e);
            digitalDepthProgram = 0;
        }
    }

    @Override
    public void cleanup() {
        if (digitalProgram != 0) {
            GL20.glDeleteProgram(digitalProgram);
            digitalProgram = 0;
        }
        if (digitalDepthProgram != 0) {
            GL20.glDeleteProgram(digitalDepthProgram);
            digitalDepthProgram = 0;
        }
    }

    @Override
    public boolean shouldApply(float partialTicks) {
        return (digitalProgram != 0 || digitalDepthProgram != 0) && getStrength(partialTicks) > 0.001f;
    }

    @Override
    public float getStrength(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return 0.0f;

        IDrugProperties props = mc.player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) return 0.0f;

        return ((DrugProperties) props).getDrugStrength("zero");
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return digitalDepthProgram != 0 && getStrength(partialTicks) > 0.001f;
    }

    @Override
    public void apply(PingPongBuffer buffer, float partialTicks) {
        float digital = getStrength(partialTicks);
        if (digital <= 0.001f) return;

        int depthTexture = ShaderPipeline.getInstance().getDepthTexture();
        boolean useDepth = digitalDepthProgram != 0 && depthTexture != 0;
        int program = useDepth ? digitalDepthProgram : digitalProgram;
        if (program == 0) return;

        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth;
        int height = mc.displayHeight;

        float downscale = mixEaseInOut(0.0f, 0.95f, Math.min(digital * 3.0f, 1.0f));
        downscale += digital * 0.05f;
        float textProgress = easeZeroToOne((digital - 0.2f) * 5.0f);
        float maxColors = digital > 0.4f ? Math.max(256.0f / ((digital - 0.4f) * 640.0f + 1.0f), 2.0f) : -1.0f;
        float saturation = 1.0f - easeZeroToOne((digital - 0.6f) * 5.0f);
        float binaryProgress = easeZeroToOne((digital - 0.8f) * 10.0f);
        textProgress += binaryProgress;

        float newResX = width * (1.0f + (PSConfig.digitalEffectPixelRescaleX - 1.0f) * downscale);
        float newResY = height * (1.0f + (PSConfig.digitalEffectPixelRescaleY - 1.0f) * downscale);

        GlStateManager.disableDepth();
        GL11.glDepthMask(false);
        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        GL20.glUseProgram(program);

        GL20.glUniform1i(GL20.glGetUniformLocation(program, "tex0"), 0);
        GL20.glUniform1i(GL20.glGetUniformLocation(program, "asciiTex"), 1);
        GL20.glUniform2f(GL20.glGetUniformLocation(program, "newResolution"), newResX, newResY);
        GL20.glUniform1f(GL20.glGetUniformLocation(program, "textProgress"), textProgress);
        GL20.glUniform1f(GL20.glGetUniformLocation(program, "maxColors"), maxColors);
        GL20.glUniform1f(GL20.glGetUniformLocation(program, "saturation"), saturation);
        GL20.glUniform1f(GL20.glGetUniformLocation(program, "totalAlpha"), 1.0f);

        if (useDepth) {
            GL20.glUniform1i(GL20.glGetUniformLocation(program, "depthTex"), 2);
            float zNear = 0.05f;
            float zFar = mc.gameSettings.renderDistanceChunks * 16.0f;
            GL20.glUniform2f(GL20.glGetUniformLocation(program, "depthRange"), zNear, zFar);

            GlStateManager.setActiveTexture(GL13.GL_TEXTURE2);
            GlStateManager.bindTexture(depthTexture);
        }

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        mc.getTextureManager().bindTexture(DIGITAL_TEXT_TEXTURE);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(buffer.getReadTexture());

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, buffer.getWriteBuffer().framebufferObject);

        renderFullScreenQuad();

        GL20.glUseProgram(0);

        if (useDepth) {
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE2);
            GlStateManager.bindTexture(0);
        }
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
        return "Digital Effect";
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    private static float mix(float value1, float value2, float progress) {
        return value1 + (value2 - value1) * progress;
    }

    private static float quadraticMix(float value1, float value2, float value3, float progress) {
        return mix(mix(value1, value2, progress), mix(value2, value3, progress), progress);
    }

    private static float cubicMix(float value1, float value2, float value3, float value4, float progress) {
        return mix(quadraticMix(value1, value2, value3, progress), quadraticMix(value2, value3, value4, progress), progress);
    }

    private static float clamp(float min, float value, float max) {
        return value < min ? min : value > max ? max : value;
    }

    private static float mixEaseInOut(float value1, float value2, float progress) {
        return cubicMix(value1, value1, value2, value2, progress);
    }

    private static float easeZeroToOne(float progress) {
        return cubicMix(0.0f, 0.0f, 1.0f, 1.0f, clamp(0.0f, progress, 1.0f));
    }

    private int compileProgram(String vertexSource, String fragmentSource) throws Exception {
        int vertexShader = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);

        int program = linkProgram(vertexShader, fragmentShader);

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        return program;
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