package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.client.rendering.HallucinationManager;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.PingPongBuffer;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderEffect;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderUtils;
import com.chubbyboi.psychedelicraftresparked.drug.IDrug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class SimpleEffectsShader implements ShaderEffect {

    private int shaderProgram = 0;
    private int uniformTexture;
    private int uniformColorIntensification;
    private int uniformDesaturation;
    private int uniformTicks;
    private int uniformSlowColorRotation;
    private int uniformQuickColorRotation;

    @Override
    public void init() {
        try {
            String vertexSource = ShaderUtils.loadShader("shader_basic.vert");
            String fragmentSource = ShaderUtils.loadShaderWithUtils("shader_simple_effects.frag");

            int vertexShader = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);
            int fragmentShader = compileShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);

            shaderProgram = linkProgram(vertexShader, fragmentShader);

            uniformTexture = GL20.glGetUniformLocation(shaderProgram, "tex0");
            uniformColorIntensification = GL20.glGetUniformLocation(shaderProgram, "colorIntensification");
            uniformDesaturation = GL20.glGetUniformLocation(shaderProgram, "desaturation");
            uniformTicks = GL20.glGetUniformLocation(shaderProgram, "ticks");
            uniformSlowColorRotation = GL20.glGetUniformLocation(shaderProgram, "slowColorRotation");
            uniformQuickColorRotation = GL20.glGetUniformLocation(shaderProgram, "quickColorRotation");

            GL20.glDeleteShader(vertexShader);
            GL20.glDeleteShader(fragmentShader);
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to initialize Simple Effects shader!", e);
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
        if (shaderProgram == 0) return false;

        float[] strengths = getStrengths(partialTicks);
        return Math.abs(strengths[0]) > 0.001f || Math.abs(strengths[1]) > 0.001f || Math.abs(strengths[2]) > 0.001f || Math.abs(strengths[3]) > 0.001f;
    }

    private float[] getStrengths(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return new float[]{0.0f, 0.0f, 0.0f, 0.0f};

        IDrugProperties props = mc.player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) return new float[]{0.0f, 0.0f, 0.0f, 0.0f};

        DrugProperties drugProps = (DrugProperties) props;

        // Aggregate saturation and desaturation from ALL drugs
        float totalSaturation = 0.0f;
        float totalDesaturation = 0.0f;

        for (IDrug drug : drugProps.getAllDrugs()) {
            if (drug.getActiveValue() > 0.001f) {
                totalSaturation += drug.getColourSaturationModifier();
                totalDesaturation += drug.getColourDesaturationModifier();
            }
        }

        HallucinationManager hallucinationManager = HallucinationManager.getInstance();
        totalSaturation += hallucinationManager.getSuperSaturation();
        float slowColorRotation = hallucinationManager.getSlowColorRotation();
        float quickColorRotation = hallucinationManager.getQuickColorRotation();

        return new float[]{totalSaturation, totalDesaturation, slowColorRotation, quickColorRotation};
    }

    @Override
    public float getStrength(float partialTicks) {
        float[] strengths = getStrengths(partialTicks);
        float max = 0.0f;
        for (float s : strengths) {
            max = Math.max(max, Math.abs(s));
        }
        return max;
    }

    @Override
    public void apply(PingPongBuffer buffer, float partialTicks) {
        float[] strengths = getStrengths(partialTicks);
        float saturation = strengths[0];
        float desaturation = strengths[1];
        float slowColorRotation = strengths[2];
        float quickColorRotation = strengths[3];

        if (Math.abs(saturation) <= 0.001f && Math.abs(desaturation) <= 0.001f && Math.abs(slowColorRotation) <= 0.001f && Math.abs(quickColorRotation) <= 0.001f) return;

        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth;
        int height = mc.displayHeight;

        GL11.glViewport(0, 0, width, height);

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

        float ticks = mc.ingameGUI.getUpdateCounter() + partialTicks;

        GL20.glUniform1f(uniformColorIntensification, saturation);
        GL20.glUniform1f(uniformDesaturation, desaturation);
        GL20.glUniform1f(uniformTicks, ticks);
        GL20.glUniform1f(uniformSlowColorRotation, slowColorRotation);
        GL20.glUniform1f(uniformQuickColorRotation, quickColorRotation);
        GL20.glUniform1i(uniformTexture, 0);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(buffer.getReadTexture());

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, buffer.getWriteBuffer().framebufferObject);

        renderFullScreenQuad();

        GL20.glUseProgram(0);

        GL11.glDepthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableCull();
    }

    @Override
    public String getName() {
        return "Simple Effects (Colour Modification)";
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