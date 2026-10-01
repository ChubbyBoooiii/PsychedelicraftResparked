package com.chubbyboi.psychedelicraftresparked.client.rendering.ambient;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import com.chubbyboi.psychedelicraftresparked.mixins.EntityRendererAccessor;
import com.chubbyboi.psychedelicraftresparked.util.PsychMathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

@SideOnly(Side.CLIENT)
public class LensFlareEffect {

    private static final LensFlareEffect INSTANCE = new LensFlareEffect();

    private static final float[] FLARE_SIZES = {0.15f, 0.24f, 0.12f, 0.036f, 0.06f, 0.048f, 0.006f, 0.012f, 0.5f, 0.09f, 0.036f, 0.09f, 0.06f, 0.05f, 0.6f};
    private static final float[] FLARE_INFLUENCES = {-1.3f, -2.0f, 0.2f, 0.4f, 0.25f, -0.25f, -0.7f, -1.0f, 1.0f, 1.4f, -1.31f, -1.2f, -1.5f, -1.55f, -3.0f};
    private static final ResourceLocation[] FLARE_TEXTURES = new ResourceLocation[FLARE_SIZES.length];
    private static final ResourceLocation SUN_BLINDNESS_TEXTURE =
        new ResourceLocation(Tags.MOD_ID, "textures/effects/lens_flare/sun_blindness.png");

    static {
        for (int i = 0; i < FLARE_TEXTURES.length; i++) {
            FLARE_TEXTURES[i] = new ResourceLocation(Tags.MOD_ID, "textures/effects/lens_flare/flare" + i + ".png");
        }
    }

    private float actualSunAlpha = 0.0f;

    public static LensFlareEffect getInstance() {
        return INSTANCE;
    }

    public void update(Entity entity) {
        if (PSConfig.sunFlareIntensity <= 0.0f) {
            return;
        }

        World world = entity.world;

        if (isDisabledDimension(world)) {
            actualSunAlpha = 0.0f;
            return;
        }

        float sunSizeRadians = -5.0f / 180.0f * 3.1315926f;
        float sunWidth = 20.0f;
        float sunRadians = world.getCelestialAngleRadians(1.0f);

        Vec3d sunVecTopLeft = new Vec3d(-MathHelper.sin(sunRadians - sunSizeRadians) * 120.0f, MathHelper.cos(sunRadians - sunSizeRadians) * 120.0f, -sunWidth);
        Vec3d sunVecTopRight = new Vec3d(-MathHelper.sin(sunRadians - sunSizeRadians) * 120.0f, MathHelper.cos(sunRadians - sunSizeRadians) * 120.0f, sunWidth);
        Vec3d sunVecBottomLeft = new Vec3d(-MathHelper.sin(sunRadians + sunSizeRadians) * 120.0f, MathHelper.cos(sunRadians + sunSizeRadians) * 120.0f, -sunWidth);
        Vec3d sunVecBottomRight = new Vec3d(-MathHelper.sin(sunRadians + sunSizeRadians) * 120.0f, MathHelper.cos(sunRadians + sunSizeRadians) * 120.0f, sunWidth);

        Vec3d playerPos = entity.getPositionEyes(1.0f);

        RayTraceResult sunTopLeft = world.rayTraceBlocks(playerPos, playerPos.add(sunVecTopLeft), true, true, true);
        RayTraceResult sunTopRight = world.rayTraceBlocks(playerPos, playerPos.add(sunVecTopRight), true, true, true);
        RayTraceResult sunBottomLeft = world.rayTraceBlocks(playerPos, playerPos.add(sunVecBottomLeft), true, true, true);
        RayTraceResult sunBottomRight = world.rayTraceBlocks(playerPos, playerPos.add(sunVecBottomRight), true, true, true);

        float newSunAlpha = (1.0f - world.getRainStrength(1.0f)) * ((sunTopLeft == null ? 0.25f : 0.0f) + (sunTopRight == null ? 0.25f : 0.0f)
            + (sunBottomLeft == null ? 0.25f : 0.0f) + (sunBottomRight == null ? 0.25f : 0.0f));
        actualSunAlpha = Math.min(PsychMathHelper.nearValue(actualSunAlpha, newSunAlpha, 0.1f, 0.01f), 1.0f);
    }

    private static boolean isDisabledDimension(World world) {
        int dimension = world.provider.getDimension();
        for (int disabled : PSConfig.sunFlareDisabledDimensions) {
            if (disabled == dimension) {
                return true;
            }
        }
        return false;
    }

    public void render(float partialTicks) {
        float sunFlareIntensity = PSConfig.sunFlareIntensity;
        if (sunFlareIntensity <= 0.0f || actualSunAlpha <= 0.0f) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        Entity renderEntity = mc.getRenderViewEntity();
        if (world == null || renderEntity == null || isDisabledDimension(world)) {
            return;
        }

        int screenWidth = mc.displayWidth;
        int screenHeight = mc.displayHeight;

        float sunRadians = world.getCelestialAngleRadians(partialTicks);
        Vector3f sunVecCenter = new Vector3f(-MathHelper.sin(sunRadians) * 120.0f, MathHelper.cos(sunRadians) * 120.0f, 0.0f);

        float genSize = screenWidth > screenHeight ? screenWidth : screenHeight;

        Vector3f sunPositionOnScreen = projectPointCurrentView(mc, renderEntity, sunVecCenter, partialTicks);

        if (sunPositionOnScreen.z <= 0.0f) {
            return;
        }

        Vector3f normSunPos = new Vector3f();
        sunPositionOnScreen.normalise(normSunPos);
        float xDist = normSunPos.x * screenWidth;
        float yDist = normSunPos.y * screenHeight;

        Vec3d color = world.getFogColor(1.0f);
        float red = (float) color.x - 0.1f;
        float green = (float) color.y - 0.1f;
        float blue = (float) color.z - 0.1f;

        float alpha = Math.min(sunPositionOnScreen.z, 1.0f);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0, screenWidth, screenHeight, 0.0, 1000.0, 3000.0);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0f, 0.0f, -2000.0f);

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.enableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.disableAlpha();

        float screenCenterX = screenWidth * 0.5f;
        float screenCenterY = screenHeight * 0.5f;

        for (int i = 0; i < FLARE_SIZES.length; i++) {
            float flareSizeHalf = FLARE_SIZES[i] * genSize * 0.5f;
            float flareCenterX = screenCenterX + xDist * FLARE_INFLUENCES[i];
            float flareCenterY = screenCenterY + yDist * FLARE_INFLUENCES[i];

            GlStateManager.color(red, green, blue, (alpha * i == 8 ? 1.0f : 0.5f) * actualSunAlpha * sunFlareIntensity);

            mc.getTextureManager().bindTexture(FLARE_TEXTURES[i]);
            drawQuad(flareCenterX, flareCenterY, flareSizeHalf);
        }

        float genDist = 1.0f - (normSunPos.x * normSunPos.x + normSunPos.y * normSunPos.y);
        float blendingSize = (genDist - 0.1f) * sunFlareIntensity * 250.0f * genSize;

        if (blendingSize > 0.0f) {
            float blendAlpha = Math.min(blendingSize / genSize / 150.0f, 1.0f);

            GlStateManager.color(red, green, blue, blendAlpha * actualSunAlpha);

            mc.getTextureManager().bindTexture(SUN_BLINDNESS_TEXTURE);
            drawQuad(screenCenterX + xDist, screenCenterY + yDist, blendingSize * 0.5f);
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    private static void drawQuad(float centerX, float centerY, float sizeHalf) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(centerX - sizeHalf, centerY + sizeHalf, 0.0).tex(0.0, 1.0).endVertex();
        buffer.pos(centerX + sizeHalf, centerY + sizeHalf, 0.0).tex(1.0, 1.0).endVertex();
        buffer.pos(centerX + sizeHalf, centerY - sizeHalf, 0.0).tex(1.0, 0.0).endVertex();
        buffer.pos(centerX - sizeHalf, centerY - sizeHalf, 0.0).tex(0.0, 0.0).endVertex();
        tessellator.draw();
    }

    private static Vector3f projectPointCurrentView(Minecraft mc, Entity entity, Vector3f point, float partialTicks) {
        float farPlaneDistance = (float) (mc.gameSettings.renderDistanceChunks * 16);
        float fov = ((EntityRendererAccessor) mc.entityRenderer).callGetFOVModifier(partialTicks, true);
        Matrix4f transformMatrix = getProjectionMatrix((float) Math.toRadians(fov), (float) mc.displayWidth / (float) mc.displayHeight, 0.05f, farPlaneDistance * 2.0f);

        if (mc.gameSettings.thirdPersonView > 0) {
            Vec3d eyes = entity.getPositionEyes(partialTicks);
            double d0 = eyes.x;
            double d1 = eyes.y;
            double d2 = eyes.z;

            double d7 = 4.0;
            float f6 = entity.rotationYaw;
            float f2 = entity.rotationPitch;

            if (mc.gameSettings.thirdPersonView == 2) {
                f2 += 180.0f;
            }

            double d3 = (double) (-MathHelper.sin(f6 / 180.0f * (float) Math.PI) * MathHelper.cos(f2 / 180.0f * (float) Math.PI)) * d7;
            double d4 = (double) (MathHelper.cos(f6 / 180.0f * (float) Math.PI) * MathHelper.cos(f2 / 180.0f * (float) Math.PI)) * d7;
            double d5 = (double) (-MathHelper.sin(f2 / 180.0f * (float) Math.PI)) * d7;

            for (int k = 0; k < 8; ++k) {
                float f3 = (float) ((k & 1) * 2 - 1) * 0.1f;
                float f4 = (float) ((k >> 1 & 1) * 2 - 1) * 0.1f;
                float f5 = (float) ((k >> 2 & 1) * 2 - 1) * 0.1f;
                RayTraceResult hit = mc.world.rayTraceBlocks(new Vec3d(d0 + f3, d1 + f4, d2 + f5), new Vec3d(d0 - d3 + f3 + f5, d1 - d5 + f4, d2 - d4 + f5));

                if (hit != null) {
                    double d6 = hit.hitVec.distanceTo(new Vec3d(d0, d1, d2));

                    if (d6 < d7) {
                        d7 = d6;
                    }
                }
            }

            if (mc.gameSettings.thirdPersonView == 2) {
                Matrix4f.rotate((float) Math.toRadians(180.0f), new Vector3f(0.0f, 1.0f, 0.0f), transformMatrix, transformMatrix);
            }

            Matrix4f.rotate((float) Math.toRadians(entity.rotationPitch - f2), new Vector3f(1.0f, 0.0f, 0.0f), transformMatrix, transformMatrix);
            Matrix4f.rotate((float) Math.toRadians(entity.rotationYaw - f6), new Vector3f(0.0f, 1.0f, 0.0f), transformMatrix, transformMatrix);
            Matrix4f.translate(new Vector3f(0.0f, 0.0f, (float) (-d7)), transformMatrix, transformMatrix);
            Matrix4f.rotate((float) Math.toRadians(f6 - entity.rotationYaw), new Vector3f(0.0f, 1.0f, 0.0f), transformMatrix, transformMatrix);
            Matrix4f.rotate((float) Math.toRadians(f2 - entity.rotationPitch), new Vector3f(1.0f, 0.0f, 0.0f), transformMatrix, transformMatrix);
        }

        Matrix4f.rotate((float) Math.toRadians(entity.rotationPitch), new Vector3f(1.0f, 0.0f, 0.0f), transformMatrix, transformMatrix);
        Matrix4f.rotate((float) Math.toRadians(entity.rotationYaw + 180.0f), new Vector3f(0.0f, 1.0f, 0.0f), transformMatrix, transformMatrix);

        Vector4f clippedPoint = new Vector4f(point.x, point.y, point.z, 1.0f);
        Matrix4f.transform(transformMatrix, clippedPoint, clippedPoint);
        return new Vector3f(clippedPoint.x, -clippedPoint.y, clippedPoint.z);
    }

    private static Matrix4f getProjectionMatrix(float fov, float aspect, float nearPlane, float farPlane) {
        Matrix4f projectionMatrix = new Matrix4f();

        float yScale = 1.0f / (float) Math.tan(fov / 2.0f);
        float xScale = yScale / aspect;
        float frustumLength = farPlane - nearPlane;

        projectionMatrix.m00 = xScale;
        projectionMatrix.m11 = yScale;
        projectionMatrix.m22 = -((farPlane + nearPlane) / frustumLength);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * nearPlane * farPlane) / frustumLength);
        projectionMatrix.m33 = 0;

        return projectionMatrix;
    }
}