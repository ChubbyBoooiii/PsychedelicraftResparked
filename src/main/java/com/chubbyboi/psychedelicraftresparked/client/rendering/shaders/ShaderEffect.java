package com.chubbyboi.psychedelicraftresparked.client.rendering.shaders;

public interface ShaderEffect {

    void init();

    void cleanup();

    boolean shouldApply(float partialTicks);

    float getStrength(float partialTicks);

    void apply(PingPongBuffer buffer, float partialTicks);

    String getName();

    default boolean wantsDepthBuffer(float partialTicks) {
        return false;
    }

    default boolean isAdvanced() {
        return false;
    }
}
