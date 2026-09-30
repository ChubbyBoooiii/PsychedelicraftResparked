package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.client.rendering.HallucinationManager;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class WorldShaderEffect {

    private static final WorldShaderEffect INSTANCE = new WorldShaderEffect();

    public static WorldShaderEffect getInstance() {
        return INSTANCE;
    }

    private WorldShaderEffect() {
    }

    private static final int MAX_LIGHTS = 8;

    private int shaderProgram = 0;

    private boolean active = false;
    private boolean bound = false;
    private boolean wantBound = false;
    private boolean foreignProgram = false;
    private boolean lightingEnabled = false;
    private final boolean[] lightEnabled = new boolean[MAX_LIGHTS];
    private final int[] lightEnabledUniforms = new int[MAX_LIGHTS];
    private boolean colorMaterialEnabled = false;
    private int colorMaterialMode = GL11.GL_AMBIENT_AND_DIFFUSE;
    // S, T, R, Q
    private static final int[] TEX_GEN_COORDS = {GL11.GL_S, GL11.GL_T, GL11.GL_R, GL11.GL_Q};
    private static final int[] TEX_GEN_ENABLE_CAPS = {GL11.GL_TEXTURE_GEN_S, GL11.GL_TEXTURE_GEN_T, GL11.GL_TEXTURE_GEN_R, GL11.GL_TEXTURE_GEN_Q};
    private final boolean[] texGenEnabled = new boolean[4];
    private final int[] texGenMode = new int[4];
    private boolean pausedForTexGen = false;

    public void init() {
        try {
            String vertexSource = ShaderUtils.loadShader("shader_world.vert");

            int vertexShader = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);

            shaderProgram = linkProgram(vertexShader);

            for (int i = 0; i < MAX_LIGHTS; i++) {
                lightEnabledUniforms[i] = uniform("lightEnabled[" + i + "]");
            }

            GL20.glDeleteShader(vertexShader);
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to initialize World shader!", e);
            shaderProgram = 0;
        }
    }

    public void activate(float partialTicks) {
        if (shaderProgram == 0 || !PSConfig.advancedShadersEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null) {
            return;
        }

        active = true;
        wantBound = true;
        foreignProgram = false;
        pausedForTexGen = false;

        lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        for (int i = 0; i < MAX_LIGHTS; i++) {
            lightEnabled[i] = GL11.glIsEnabled(GL11.GL_LIGHT0 + i);
        }
        colorMaterialEnabled = GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL);
        colorMaterialMode = GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_PARAMETER);
        for (int i = 0; i < 4; i++) {
            texGenEnabled[i] = GL11.glIsEnabled(TEX_GEN_ENABLE_CAPS[i]);
            texGenMode[i] = GL11.glGetTexGeni(TEX_GEN_COORDS[i], GL11.GL_TEXTURE_GEN_MODE);
        }

        bind();

        float ticks = mc.ingameGUI.getUpdateCounter() + partialTicks;
        GL20.glUniform1f(uniform("ticks"), ticks);
        GL20.glUniform3f(uniform("playerPos"), (float) player.posX, (float) player.posY, (float) player.posZ);

        HallucinationManager manager = HallucinationManager.getInstance();

        GL20.glUniform1f(uniform("bigWaves"), manager.getBigWaveStrength());
        GL20.glUniform1f(uniform("smallWaves"), manager.getSmallWaveStrength());
        GL20.glUniform1f(uniform("wiggleWaves"), manager.getWiggleWaveStrength());
        GL20.glUniform1f(uniform("distantWorldDeformation"), manager.getDistantWorldDeformationStrength());

        updateTexGen();
    }

    public void deactivate() {
        active = false;
        bound = false;
        wantBound = false;
        foreignProgram = false;
        pausedForTexGen = false;
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

    public void setLightEnabled(int light, boolean enabled) {
        if (shaderProgram == 0 || light < 0 || light >= MAX_LIGHTS) {
            return;
        }
        lightEnabled[light] = enabled;
        if (bound) {
            GL20.glUniform1i(lightEnabledUniforms[light], enabled ? 1 : 0);
        }
    }

    public void setColorMaterialEnabled(boolean enabled) {
        if (shaderProgram == 0) {
            return;
        }
        colorMaterialEnabled = enabled;
        if (bound) {
            uploadColorMaterial();
        }
    }

    public void setColorMaterialMode(int mode) {
        if (shaderProgram == 0) {
            return;
        }
        colorMaterialMode = mode;
        if (bound) {
            uploadColorMaterial();
        }
    }

    public void setTexGenEnabled(GlStateManager.TexGen coord, boolean enabled) {
        if (shaderProgram == 0) {
            return;
        }
        texGenEnabled[coord.ordinal()] = enabled;
        updateTexGen();
    }

    public void setTexGenMode(GlStateManager.TexGen coord, int mode) {
        if (shaderProgram == 0) {
            return;
        }
        texGenMode[coord.ordinal()] = mode;
        updateTexGen();
    }

    private void updateTexGen() {
        if (!active) {
            return;
        }
        boolean unsupported = false;
        for (int i = 0; i < 4; i++) {
            if (texGenEnabled[i] && texGenMode[i] != GL11.GL_OBJECT_LINEAR && texGenMode[i] != GL11.GL_EYE_LINEAR) {
                unsupported = true;
            }
        }

        if (unsupported && !pausedForTexGen) {
            pausedForTexGen = true;
            wantBound = false;
            if (!foreignProgram) {
                GL20.glUseProgram(0);
                bound = false;
            }
        } else if (!unsupported && pausedForTexGen) {
            pausedForTexGen = false;
            wantBound = true;
            if (!foreignProgram) {
                bind();
            }
        } else if (bound) {
            uploadTexGen();
        }
    }

    private int shaderTexGenMode(int i) {
        if (!texGenEnabled[i]) {
            return 0;
        }
        return texGenMode[i] == GL11.GL_OBJECT_LINEAR ? 1 : 2;
    }

    private void uploadTexGen() {
        GL20.glUniform4i(uniform("texGenMode"), shaderTexGenMode(0), shaderTexGenMode(1), shaderTexGenMode(2), shaderTexGenMode(3));
    }

    private void uploadColorMaterial() {
        GL20.glUniform1i(uniform("colorMaterialMode"), colorMaterialEnabled ? colorMaterialMode : 0);
    }

    // Another mod's shader owns the GL program until it releases it; never bind over it or upload uniforms into it
    public void onExternalProgramChange(int program) {
        if (!active || shaderProgram == 0) {
            return;
        }
        if (program == shaderProgram) {
            foreignProgram = false;
            if (wantBound) {
                bind();
            }
        } else if (program != 0) {
            foreignProgram = true;
            bound = false;
        } else if (foreignProgram) {
            foreignProgram = false;
            if (wantBound) {
                bind();
            }
        } else {
            bound = false;
        }
    }

    private void bind() {
        GL20.glUseProgram(shaderProgram);
        bound = true;
        GL20.glUniform1i(uniform("lightingEnabled"), lightingEnabled ? 1 : 0);
        for (int i = 0; i < MAX_LIGHTS; i++) {
            GL20.glUniform1i(lightEnabledUniforms[i], lightEnabled[i] ? 1 : 0);
        }
        uploadColorMaterial();
        uploadTexGen();
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

    private int linkProgram(int vertexShader) throws Exception {
        int program = GL20.glCreateProgram();

        GL20.glAttachShader(program, vertexShader);
        GL20.glLinkProgram(program);

        int status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(program, 1024);
            throw new Exception("Shader program linking failed:\n" + log);
        }

        return program;
    }
}