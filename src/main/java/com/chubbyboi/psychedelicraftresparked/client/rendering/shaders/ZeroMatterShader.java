package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class ZeroMatterShader {

    private static final ZeroMatterShader INSTANCE = new ZeroMatterShader();

    public static ZeroMatterShader getInstance() {
        return INSTANCE;
    }

    private ZeroMatterShader() {
    }

    private int shaderProgram = 0;

    public void init() {
        try {
            String vertexSource = ShaderUtils.loadShader("zero_matter.vert");
            String fragmentSource = ShaderUtils.loadShader("zero_matter.frag");

            int vertexShader = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);
            int fragmentShader = compileShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);

            shaderProgram = linkProgram(vertexShader, fragmentShader);

            GL20.glDeleteShader(vertexShader);
            GL20.glDeleteShader(fragmentShader);
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to initialize Zero Matter shader!", e);
            shaderProgram = 0;
        }
    }

    private static final float PIXEL_SIZE_X = 1.0F / 70.0F;
    private static final float PIXEL_SIZE_Y = -1.0F / 112.0F;

    private int previousProgram = 0;

    public boolean isAvailable() {
        return shaderProgram != 0;
    }

    public void activate(float cellOffsetX, float cellOffsetY) {
        if (shaderProgram == 0) {
            return;
        }
        previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        OpenGlHelper.glUseProgram(shaderProgram);
        GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "tex0"), 0);
        GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "pixelSize"), PIXEL_SIZE_X, PIXEL_SIZE_Y);
        GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "cellOffset"), cellOffsetX, cellOffsetY);
    }

    public void deactivate() {
        if (shaderProgram == 0) {
            return;
        }
        OpenGlHelper.glUseProgram(previousProgram);
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