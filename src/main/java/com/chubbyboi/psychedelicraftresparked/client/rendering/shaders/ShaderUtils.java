package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ShaderUtils {
    
    private static String utilsSource = null;
    private static boolean utilsLoaded = false;

    private static void loadUtils() {
        if (utilsLoaded) return;
        
        try {
            ResourceLocation location = new ResourceLocation(Tags.MOD_ID, "shaders/shader_utils.frag");
            
            InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
            );
            
            StringBuilder source = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                source.append(line).append("\n");
            }
            
            IOUtils.closeQuietly(reader);
            
            utilsSource = source.toString();
            utilsLoaded = true;
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to load shader_utils.frag!", e);
            utilsSource = ""; // Empty fallback
            utilsLoaded = true;
        }
    }

    public static String loadShaderWithUtils(String filename) throws Exception {
        loadUtils();

        ResourceLocation location = new ResourceLocation(Tags.MOD_ID, "shaders/" + filename);
        
        InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8)
        );
        
        StringBuilder source = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            source.append(line).append("\n");
        }
        
        IOUtils.closeQuietly(reader);
        
        String shaderSource = source.toString();
        
        // Find the #version line
        int versionIndex = shaderSource.indexOf("#version");
        if (versionIndex < 0) {
            return utilsSource + shaderSource;
        }
        
        // Find the end of the #version line
        int versionEnd = shaderSource.indexOf("\n", versionIndex);
        if (versionEnd < 0) {
            versionEnd = shaderSource.length();
        } else {
            versionEnd++;
        }
        
        // Insert utils after #version
        return shaderSource.substring(0, versionEnd) + utilsSource + shaderSource.substring(versionEnd);
    }

    public static String loadShader(String filename) throws Exception {
        ResourceLocation location = new ResourceLocation(Tags.MOD_ID, "shaders/" + filename);
        
        InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8)
        );
        
        StringBuilder source = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            source.append(line).append("\n");
        }
        
        IOUtils.closeQuietly(reader);
        
        return source.toString();
    }
}
