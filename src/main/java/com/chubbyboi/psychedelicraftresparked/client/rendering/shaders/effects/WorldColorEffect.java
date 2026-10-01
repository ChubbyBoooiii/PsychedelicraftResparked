package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.effects;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.client.rendering.HallucinationManager;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.PingPongBuffer;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderEffect;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderPipeline;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderUtils;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class WorldColorEffect implements ShaderEffect {

    private static final int SKY_NONE = 0;
    private static final int SKY_FAKE_BOX = 1;
    private static final int SKY_SURFACE = 2;
    private static final int SKY_END = 3;

    private int shaderProgram = 0;

    @Override
    public void init() {
        try {
            String vertexSource = ShaderUtils.loadShader("shader_basic.vert");
            String fragmentSource = ShaderUtils.loadShaderWithUtils("shader_world_color.frag");

            int vertexShader = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);
            int fragmentShader = compileShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);

            shaderProgram = linkProgram(vertexShader, fragmentShader);

            GL20.glDeleteShader(vertexShader);
            GL20.glDeleteShader(fragmentShader);
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to initialize World Color shader!", e);
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
        return shaderProgram != 0 && getStrength(partialTicks) > 0.001f;
    }

    @Override
    public float getStrength(float partialTicks) {
        float[] pulseColor = getPulseColor();
        float[] contrastColor = getContrastColor();
        return Math.max(getSurfaceFractal(), Math.max(pulseColor[3], contrastColor[3]));
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return shouldApply(partialTicks);
    }

    @Override
    public boolean wantsHandDepthBuffer(float partialTicks) {
        return shouldApply(partialTicks);
    }

    @Override
    public void apply(PingPongBuffer buffer, float partialTicks) {
        ShaderPipeline pipeline = ShaderPipeline.getInstance();
        int worldDepthTexture = pipeline.getDepthTexture();
        int handDepthTexture = pipeline.getHandDepthTexture();
        if (worldDepthTexture == 0 || handDepthTexture == 0 || !pipeline.hasWorldMatrices()) {
            return;
        }

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
        float surfaceFractal = getSurfaceFractal();
        float[] pulseColor = getPulseColor();
        float[] contrastColor = getContrastColor();

        GL20.glUniform1i(uniform("tex0"), 0);
        GL20.glUniform1i(uniform("worldDepthTex"), 1);
        GL20.glUniform1i(uniform("handDepthTex"), 2);
        GL20.glUniform1i(uniform("fractalTex"), 3);

        GL20.glUniformMatrix4(uniform("invWorldProjection"), false, pipeline.getInverseWorldProjection());
        GL20.glUniformMatrix4(uniform("invWorldModelView"), false, pipeline.getInverseWorldModelView());
        GL20.glUniformMatrix4(uniform("invHandProjection"), false, pipeline.getInverseHandProjection());
        double[] cameraPos = pipeline.getWorldCameraPos();
        GL20.glUniform3f(uniform("cameraPosMod4"), (float) mod4(cameraPos[0]), (float) mod4(cameraPos[1]), (float) mod4(cameraPos[2]));

        GL20.glUniform1f(uniform("ticks"), ticks);
        GL20.glUniform1f(uniform("surfaceFractal"), surfaceFractal);
        GL20.glUniform4f(uniform("pulses"), pulseColor[0], pulseColor[1], pulseColor[2], pulseColor[3]);
        GL20.glUniform4f(uniform("worldColorization"), contrastColor[0], contrastColor[1], contrastColor[2], contrastColor[3]);

        GL20.glUniform1i(uniform("skyMode"), getSkyMode(mc));
        GL20.glUniform1f(uniform("skyHorizonOffset"),
            mc.player != null && mc.world != null ? (float) (mc.player.getPositionEyes(partialTicks).y - mc.world.getHorizon()) : 0.0f);
        GL20.glUniform1f(uniform("fakeSkyboxDistance"), mc.gameSettings.renderDistanceChunks * 16 * 0.75f * (float) Math.sqrt(3.0));

        TextureAtlasSprite sprite = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.PORTAL.getDefaultState());
        GL20.glUniform4f(uniform("fractal0TexCoords"), sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE3);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE2);
        GlStateManager.bindTexture(handDepthTexture);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.bindTexture(worldDepthTexture);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(buffer.getReadTexture());

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, buffer.getWriteBuffer().framebufferObject);

        renderFullScreenQuad();

        GL20.glUseProgram(0);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE3);
        GlStateManager.bindTexture(0);
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
        return "World Color (Fractal, Pulses, Colorization)";
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    private static int getSkyMode(Minecraft mc) {
        World world = mc.world;
        if (!PSConfig.skyDrugEffects || world == null) {
            return SKY_NONE;
        }

        if (mc.gameSettings.renderDistanceChunks < 4 || world.provider.getSkyRenderer() != null) {
            return SKY_FAKE_BOX;
        }
        if (world.provider.getDimensionType().getId() == 1) {
            return SKY_END;
        }
        return world.provider.isSurfaceWorld() ? SKY_SURFACE : SKY_FAKE_BOX;
    }

    private static float getSurfaceFractal() {
        return clamp01(HallucinationManager.getInstance().getSurfaceFractalStrength());
    }

    private static float[] getPulseColor() {
        float[] pulseColor = new float[4];
        HallucinationManager.getInstance().getPulseColor(pulseColor);
        pulseColor[3] = clamp01(pulseColor[3]);
        return pulseColor;
    }

    private static float[] getContrastColor() {
        float[] contrastColor = {1.0f, 1.0f, 1.0f, 0.0f};
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            IDrugProperties props = mc.player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
            if (props instanceof DrugProperties) {
                HallucinationManager.getInstance().applyContrastColorization((DrugProperties) props, contrastColor);
            }
        }
        contrastColor[3] = clamp01(contrastColor[3]);
        return contrastColor;
    }

    private static double mod4(double value) {
        return value - 4.0 * Math.floor(value / 4.0);
    }

    private static float clamp01(float value) {
        return value < 0.0f ? 0.0f : (value > 1.0f ? 1.0f : value);
    }

    private int uniform(String name) {
        return GL20.glGetUniformLocation(shaderProgram, name);
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