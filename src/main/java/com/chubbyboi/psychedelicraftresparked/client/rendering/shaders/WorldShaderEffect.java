package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.client.rendering.HallucinationManager;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public class WorldShaderEffect {

    private static final WorldShaderEffect INSTANCE = new WorldShaderEffect();

    public static WorldShaderEffect getInstance() {
        return INSTANCE;
    }

    private WorldShaderEffect() {
    }

    private int shaderProgram = 0;

    private boolean active = false;
    private boolean bound = false;
    private boolean lightingEnabled = false;

    public void init() {
        try {
            String vertexSource = ShaderUtils.loadShader("shader_world.vert");
            String fragmentSource = ShaderUtils.loadShaderWithUtils("shader_world.frag");

            int vertexShader = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);
            int fragmentShader = compileShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);

            shaderProgram = linkProgram(vertexShader, fragmentShader);

            GL20.glDeleteShader(vertexShader);
            GL20.glDeleteShader(fragmentShader);
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to initialize World shader!", e);
            shaderProgram = 0;
        }
    }

    public void activate(float partialTicks) {
        if (shaderProgram == 0 || !PSConfig.shader3DEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null) {
            return;
        }

        active = true;
        bound = true;
        GL20.glUseProgram(shaderProgram);

        float ticks = mc.ingameGUI.getUpdateCounter() + partialTicks;

        GL20.glUniform1i(uniform("texture"), 0);
        GL20.glUniform1i(uniform("lightmapTex"), 1);
        GL20.glUniform1i(uniform("texFractal0"), 2);

        GL20.glUniform1f(uniform("ticks"), ticks);
        GL20.glUniform2f(uniform("pixelSize"), 1.0f / mc.displayWidth, 1.0f / mc.displayHeight);
        GL20.glUniform1i(uniform("useScreenTexCoords"), 0);
        GL20.glUniform4f(uniform("overrideColor"), 1.0f, 1.0f, 1.0f, 1.0f);
        GL20.glUniform1f(uniform("depthMultiplier"), 1.0f);

        GL20.glUniform3f(uniform("playerPos"), (float) player.posX, (float) player.posY, (float) player.posZ);

        boolean texture2DEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        GL20.glUniform1i(uniform("texture2DEnabled"), texture2DEnabled ? 1 : 0);

        lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        GL20.glUniform1i(uniform("lightingEnabled"), lightingEnabled ? 1 : 0);

        GL20.glUniform1i(uniform("lightmapEnabled"), 1);

        GL20.glUniform1i(uniform("fogEnabled"), GL11.glIsEnabled(GL11.GL_FOG) ? 1 : 0);
        GL20.glUniform1i(uniform("fogMode"), GL11.glGetInteger(GL11.GL_FOG_MODE));

        boolean colorSafeMode = GL11.glIsEnabled(GL11.GL_BLEND) && GL11.glGetInteger(GL11.GL_BLEND_DST) != GL11.GL_ONE_MINUS_SRC_ALPHA;
        GL20.glUniform1i(uniform("colorSafeMode"), colorSafeMode ? 1 : 0);

        HallucinationManager manager = HallucinationManager.getInstance();

        GL20.glUniform1f(uniform("bigWaves"), manager.getBigWaveStrength());
        GL20.glUniform1f(uniform("smallWaves"), manager.getSmallWaveStrength());
        GL20.glUniform1f(uniform("wiggleWaves"), manager.getWiggleWaveStrength());
        GL20.glUniform1f(uniform("distantWorldDeformation"), manager.getDistantWorldDeformationStrength());

        float surfaceFractal = clamp01(manager.getSurfaceFractalStrength());
        GL20.glUniform1f(uniform("surfaceFractal"), surfaceFractal);
        if (surfaceFractal > 0.0f) {
            registerFractals();
        }

        float[] pulseColor = new float[4];
        manager.getPulseColor(pulseColor);
        pulseColor[3] = clamp01(pulseColor[3]);
        GL20.glUniform4f(uniform("pulses"), pulseColor[0], pulseColor[1], pulseColor[2], pulseColor[3]);

        float[] contrastColor = {1.0f, 1.0f, 1.0f, 0.0f};
        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (props instanceof DrugProperties) {
            manager.applyContrastColorization((DrugProperties) props, contrastColor);
        }
        contrastColor[3] = clamp01(contrastColor[3]);
        GL20.glUniform4f(uniform("worldColorization"), contrastColor[0], contrastColor[1], contrastColor[2], contrastColor[3]);
    }

    public void deactivate() {
        active = false;
        bound = false;
        if (shaderProgram == 0) {
            return;
        }
        GL20.glUseProgram(0);
    }

    public void setLightingEnabled(boolean enabled) {
        if (shaderProgram == 0) {
            return;
        }
        lightingEnabled = enabled;
        if (bound) {
            GL20.glUniform1i(uniform("lightingEnabled"), enabled ? 1 : 0);
        }
    }

    public void pauseForUntexturedDraw() {
        if (active && shaderProgram != 0) {
            GL20.glUseProgram(0);
            bound = false;
        }
    }

    public void resumeAfterUntexturedDraw() {
        if (active && shaderProgram != 0) {
            GL20.glUseProgram(shaderProgram);
            bound = true;
            GL20.glUniform1i(uniform("lightingEnabled"), lightingEnabled ? 1 : 0);
        }
    }

    private void registerFractals() {
        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE2);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);

        TextureAtlasSprite sprite = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.PORTAL.getDefaultState());
        GL20.glUniform4f(uniform("fractal0TexCoords"), sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
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
}