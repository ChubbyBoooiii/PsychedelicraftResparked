package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ContainerShape {

    public static final class FluidBox {
        public final float halfWidth;
        public final float bottom;
        public final float top;

        private FluidBox(float halfWidth, float bottom, float top) {
            this.halfWidth = halfWidth;
            this.bottom = bottom;
            this.top = top;
        }

        public float getHeight() {
            return top - bottom;
        }
    }

    private static final Map<String, ContainerShape> SHAPES = new HashMap<>();

    public final String name;
    public final ResourceLocation model;
    public final int footprint;
    public final float halfFootprint;
    public final float height;
    public final int capacity;
    public final List<FluidBox> fluid;
    public final float fluidHeight;
    public final Map<String, String> clearTextures;
    @Nullable
    public final ResourceLocation molotovModel;

    private ContainerShape(String name, ResourceLocation model, int footprint, float height, int capacity, List<FluidBox> fluid, Map<String, String> clearTextures, @Nullable ResourceLocation molotovModel) {
        this.name = name;
        this.model = model;
        this.footprint = footprint;
        this.halfFootprint = footprint / 2.0F;
        this.height = height;
        this.capacity = capacity;
        this.fluid = Collections.unmodifiableList(fluid);
        this.clearTextures = Collections.unmodifiableMap(clearTextures);
        this.molotovModel = molotovModel;
        float total = 0.0F;
        for (FluidBox box : fluid) {
            total += box.getHeight();
        }
        this.fluidHeight = total;
    }

    public static synchronized ContainerShape get(String name) {
        return SHAPES.computeIfAbsent(name, ContainerShape::load);
    }

    private static ContainerShape load(String name) {
        String path = "/assets/" + Tags.MOD_ID + "/container_shapes/" + name + ".json";
        try (InputStream in = ContainerShape.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing container shape " + path);
            }
            JsonObject json = new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();

            int footprint = JsonUtils.getInt(json, "footprint");
            if (footprint <= 0 || footprint > 16) {
                throw new IllegalStateException("footprint must be 1-16 whole pixels, got " + footprint);
            }

            List<FluidBox> fluid = new ArrayList<>();
            JsonArray boxes = JsonUtils.getJsonArray(json, "fluid");
            for (JsonElement element : boxes) {
                JsonObject box = element.getAsJsonObject();
                float bottom = JsonUtils.getFloat(box, "bottom");
                float top = JsonUtils.getFloat(box, "top");
                if (top <= bottom) {
                    throw new IllegalStateException("fluid box top must be above its bottom");
                }
                fluid.add(new FluidBox(JsonUtils.getFloat(box, "halfWidth"), bottom, top));
            }
            if (fluid.isEmpty()) {
                throw new IllegalStateException("needs at least one fluid box");
            }

            Map<String, String> clearTextures = new HashMap<>();
            if (json.has("clearTextures")) {
                for (Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(json, "clearTextures").entrySet()) {
                    clearTextures.put(entry.getKey(), entry.getValue().getAsString());
                }
            }

            return new ContainerShape(name, new ResourceLocation(JsonUtils.getString(json, "model")), footprint,
                JsonUtils.getFloat(json, "height"), JsonUtils.getInt(json, "capacity", 0), fluid, clearTextures,
                json.has("molotovModel") ? new ResourceLocation(JsonUtils.getString(json, "molotovModel")) : null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load container shape " + path, e);
        }
    }
}