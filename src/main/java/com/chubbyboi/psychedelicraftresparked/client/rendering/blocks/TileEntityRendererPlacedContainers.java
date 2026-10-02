package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.block.PlacedContainerType;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.item.ItemDrinkable;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityPlacedContainers;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileEntityRendererPlacedContainers extends TileEntitySpecialRenderer<TileEntityPlacedContainers> {

    private static final class SplitModel {
        private final List<BakedQuad> solid = new ArrayList<>();
        private final List<BakedQuad> seeThrough = new ArrayList<>();
    }

    private static final Map<PlacedContainerType, SplitModel> MODELS = new EnumMap<>(PlacedContainerType.class);
    private static final float GLASS_FILTER_STRENGTH = 0.85F;

    public static void registerTextures(TextureMap map) {
        for (PlacedContainerType type : PlacedContainerType.values()) {
            for (ResourceLocation texture : ModelLoaderRegistry.getModelOrMissing(type.model).getTextures()) {
                map.registerSprite(texture);
            }
        }
    }

    public static void bakeModels() {
        MODELS.clear();
        Map<String, BufferedImage> images = new HashMap<>();
        for (PlacedContainerType type : PlacedContainerType.values()) {
            try {
                IModel model = ModelLoaderRegistry.getModel(type.model);
                IBakedModel baked = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                List<BakedQuad> quads = new ArrayList<>();
                for (EnumFacing side : EnumFacing.values()) {
                    quads.addAll(baked.getQuads(null, side, 0L));
                }
                quads.addAll(baked.getQuads(null, null, 0L));

                SplitModel split = new SplitModel();
                for (BakedQuad quad : quads) {
                    (isSolid(quad, images) ? split.solid : split.seeThrough).add(quad);
                }
                MODELS.put(type, split);
            } catch (Exception e) {
                PsychedelicraftResparked.LOGGER.error("Failed to load placed container model {}", type.model, e);
            }
        }
    }

    private static boolean isSolid(BakedQuad quad, Map<String, BufferedImage> images) {
        TextureAtlasSprite sprite = quad.getSprite();
        BufferedImage image = images.computeIfAbsent(sprite.getIconName(), name -> {
            ResourceLocation location = new ResourceLocation(name);
            ResourceLocation file = new ResourceLocation(location.getNamespace(), "textures/" + location.getPath() + ".png");
            try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(file)) {
                return TextureUtil.readBufferedImage(resource.getInputStream());
            } catch (Exception e) {
                return null;
            }
        });
        if (image == null) {
            return false;
        }

        VertexFormat format = quad.getFormat();
        int uvIndex = -1;
        for (int i = 0; i < format.getElementCount(); i++) {
            VertexFormatElement element = format.getElement(i);
            if (element.getUsage() == VertexFormatElement.EnumUsage.UV && element.getIndex() == 0) {
                uvIndex = i;
            }
        }
        if (uvIndex < 0) {
            return false;
        }

        float minU = Float.MAX_VALUE, maxU = -Float.MAX_VALUE, minV = Float.MAX_VALUE, maxV = -Float.MAX_VALUE;
        float[] data = new float[4];
        for (int vertex = 0; vertex < 4; vertex++) {
            LightUtil.unpack(quad.getVertexData(), data, format, vertex, uvIndex);
            float u = sprite.getUnInterpolatedU(data[0]);
            float v = sprite.getUnInterpolatedV(data[1]);
            minU = Math.min(minU, u);
            maxU = Math.max(maxU, u);
            minV = Math.min(minV, v);
            maxV = Math.max(maxV, v);
        }

        int width = image.getWidth();
        int height = Math.min(image.getHeight(), width);
        int x0 = MathHelper.clamp((int) Math.floor(minU / 16.0F * width + 0.01F), 0, width - 1);
        int x1 = MathHelper.clamp((int) Math.ceil(maxU / 16.0F * width - 0.01F), x0 + 1, width);
        int y0 = MathHelper.clamp((int) Math.floor(minV / 16.0F * height + 0.01F), 0, height - 1);
        int y1 = MathHelper.clamp((int) Math.ceil(maxV / 16.0F * height - 0.01F), y0 + 1, height);
        for (int py = y0; py < y1; py++) {
            for (int px = x0; px < x1; px++) {
                if ((image.getRGB(px, py) >>> 24) != 0xFF) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void render(TileEntityPlacedContainers tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (MinecraftForgeClient.getRenderPass() == 1) {
            collect(tileEntity, x, y, z);
            drawPending();
            return;
        }

        GlStateManager.disableCull();
        for (TileEntityPlacedContainers.Entry entry : tileEntity.getEntries()) {
            GlStateManager.pushMatrix();
            // Lifted a little to avoid z-fighting with the top of the block below
            GlStateManager.translate(x + entry.x / 16.0, y + 0.001, z + entry.z / 16.0);
            GlStateManager.rotate(-entry.rotation * 360.0F / TileEntityPlacedContainers.ROTATION_STEPS, 0.0F, 1.0F, 0.0F);
            renderSolidQuads(entry);
            GlStateManager.popMatrix();
        }
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableCull();
    }

    @Override
    public void renderTileEntityFast(TileEntityPlacedContainers tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
        collect(tileEntity, x, y, z);
    }

    private static final class Pending {
        private final TileEntityPlacedContainers.Entry entry;
        private final Placement placement;
        private final int skyLight;
        private final int blockLight;
        private double distanceSq;

        private Pending(TileEntityPlacedContainers.Entry entry, Placement placement, int skyLight, int blockLight) {
            this.entry = entry;
            this.placement = placement;
            this.skyLight = skyLight;
            this.blockLight = blockLight;
        }
    }

    private static final List<Pending> PENDING = new ArrayList<>();

    private static void collect(TileEntityPlacedContainers tileEntity, double x, double y, double z) {
        int light = tileEntity.getWorld().getCombinedLight(tileEntity.getPos(), 0);
        for (TileEntityPlacedContainers.Entry entry : tileEntity.getEntries()) {
            PENDING.add(new Pending(entry, new Placement(entry, x, y, z), light >> 16 & 0xFFFF, light & 0xFFFF));
        }
    }

    public static void drawPending() {
        if (PENDING.isEmpty()) {
            return;
        }
        Vec3d camera = ActiveRenderInfo.getCameraPosition();
        for (Pending pending : PENDING) {
            double dx = pending.placement.originX - camera.x;
            double dy = pending.placement.originY + pending.entry.type.height / 32.0 - camera.y;
            double dz = pending.placement.originZ - camera.z;
            pending.distanceSq = dx * dx + dy * dy + dz * dz;
        }
        PENDING.sort(Comparator.comparingDouble((Pending pending) -> pending.distanceSq).reversed());

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        for (Pending pending : PENDING) {
            SplitModel model = MODELS.get(pending.entry.type);
            FluidStack fluid = getFluid(pending.entry);

            boolean hasNeck = pending.entry.type.neckFluidHalfWidth > 0.0F;
            boolean cameraAboveNeckStart = camera.y > pending.placement.originY + getNeckStart(pending.entry.type) / 16.0;
            boolean[] sections = !hasNeck ? new boolean[]{false} : cameraAboveNeckStart ? new boolean[]{false, true} : new boolean[]{true, false};
            for (boolean neck : sections) {
                if (model != null) {
                    addModelQuads(buffer, pending, model.seeThrough, hasNeck, neck, false, camera);
                }
                if (fluid != null) {
                    addFluid(buffer, pending, fluid, neck, false, camera);
                    addFluid(buffer, pending, fluid, neck, true, camera);
                }
                if (model != null) {
                    addModelQuads(buffer, pending, model.seeThrough, hasNeck, neck, true, camera);
                }
            }
        }
        tessellator.draw();
        PENDING.clear();

        GlStateManager.enableCull();
        RenderHelper.enableStandardItemLighting();
    }

    private static final class Placement {
        private final double originX;
        private final double originY;
        private final double originZ;
        private final float cos;
        private final float sin;

        private Placement(TileEntityPlacedContainers.Entry entry, double x, double y, double z) {
            originX = x + entry.x / 16.0;
            originY = y + 0.001;
            originZ = z + entry.z / 16.0;
            double angle = Math.toRadians(-entry.rotation * 360.0F / TileEntityPlacedContainers.ROTATION_STEPS);
            cos = (float) Math.cos(angle);
            sin = (float) Math.sin(angle);
        }

        private double worldX(float localX, float localZ) {
            return originX + rotateX(localX, localZ);
        }

        private double worldZ(float localX, float localZ) {
            return originZ + rotateZ(localX, localZ);
        }

        private float rotateX(float localX, float localZ) {
            return localX * cos + localZ * sin;
        }

        private float rotateZ(float localX, float localZ) {
            return -localX * sin + localZ * cos;
        }

        private boolean faces(Vec3d camera, float normalX, float normalY, float normalZ, float centreX, float centreY, float centreZ) {
            return rotateX(normalX, normalZ) * (camera.x - worldX(centreX, centreZ))
                + normalY * (camera.y - (originY + centreY))
                + rotateZ(normalX, normalZ) * (camera.z - worldZ(centreX, centreZ)) > 0.0;
        }
    }

    // Where the neck section of bottle starts
    private static float getNeckStart(PlacedContainerType type) {
        return type.fluidBottom + type.fluidMaxHeight;
    }

    private static void addModelQuads(BufferBuilder buffer, Pending pending, List<BakedQuad> quads, boolean hasNeck, boolean neckSection, boolean towardCamera, Vec3d camera) {
        float neckStart = (getNeckStart(pending.entry.type) + 0.25F) / 16.0F;
        Placement placement = pending.placement;
        float[] data = new float[4];
        float[][] positions = new float[4][3];
        float[][] uvs = new float[4][2];
        for (BakedQuad quad : quads) {
            VertexFormat format = quad.getFormat();
            int positionIndex = -1, uvIndex = -1;
            for (int i = 0; i < format.getElementCount(); i++) {
                VertexFormatElement element = format.getElement(i);
                if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
                    positionIndex = i;
                } else if (element.getUsage() == VertexFormatElement.EnumUsage.UV && element.getIndex() == 0) {
                    uvIndex = i;
                }
            }
            if (positionIndex < 0 || uvIndex < 0) {
                continue;
            }

            int[] vertexData = quad.getVertexData();
            float centreX = 0.0F, centreY = 0.0F, centreZ = 0.0F;
            for (int vertex = 0; vertex < 4; vertex++) {
                LightUtil.unpack(vertexData, data, format, vertex, positionIndex);
                positions[vertex][0] = data[0] - 0.5F;
                positions[vertex][1] = data[1];
                positions[vertex][2] = data[2] - 0.5F;
                centreX += positions[vertex][0] / 4.0F;
                centreY += positions[vertex][1] / 4.0F;
                centreZ += positions[vertex][2] / 4.0F;
                LightUtil.unpack(vertexData, data, format, vertex, uvIndex);
                uvs[vertex][0] = data[0];
                uvs[vertex][1] = data[1];
            }

            if (hasNeck && (centreY > neckStart) != neckSection) {
                continue;
            }

            EnumFacing face = quad.getFace();
            float normalX = face.getXOffset();
            float normalY = face.getYOffset();
            float normalZ = face.getZOffset();
            if (placement.faces(camera, normalX, normalY, normalZ, centreX, centreY, centreZ) != towardCamera) {
                continue;
            }

            int tint = quad.hasTintIndex() ? getTint(pending.entry.stack, quad.getTintIndex()) : 0xFFFFFF;
            float shade = LightUtil.diffuseLight(placement.rotateX(normalX, normalZ), normalY, placement.rotateZ(normalX, normalZ));
            int red = (int) (((tint >> 16) & 0xFF) * shade);
            int green = (int) (((tint >> 8) & 0xFF) * shade);
            int blue = (int) ((tint & 0xFF) * shade);

            for (int vertex = 0; vertex < 4; vertex++) {
                float[] position = positions[vertex];
                buffer.pos(placement.worldX(position[0], position[2]), placement.originY + position[1], placement.worldZ(position[0], position[2]))
                    .color(red, green, blue, 255)
                    .tex(uvs[vertex][0], uvs[vertex][1])
                    .lightmap(pending.skyLight, pending.blockLight)
                    .endVertex();
            }
        }
    }

    private static void addFluid(BufferBuilder buffer, Pending pending, FluidStack fluid, boolean neckSection, boolean towardCamera, Vec3d camera) {
        PlacedContainerType type = pending.entry.type;
        int capacity = ((ItemDrinkable) pending.entry.stack.getItem()).getCapacity();
        float fill = MathHelper.clamp((float) fluid.amount / capacity, 0.0F, 1.0F);
        float level = (type.fluidMaxHeight + type.neckFluidMaxHeight) * fill;

        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill(fluid).toString());
        int color = FluidHelper.getWorldRenderColor(fluid);
        int alpha = (color >> 24) & 0xFF;
        if (alpha == 0) {
            alpha = 255;
        }
        int[] rgba = {(color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, alpha};

        int glass = getTint(pending.entry.stack, 0);
        for (int channel = 0; channel < 3; channel++) {
            int glassChannel = (glass >> (16 - channel * 8)) & 0xFF;
            float filtered = rgba[channel] * glassChannel / 255.0F;
            rgba[channel] = (int) (rgba[channel] + (filtered - rgba[channel]) * GLASS_FILTER_STRENGTH);
        }

        if (!neckSection) {
            float bodyTop = type.fluidBottom + Math.min(level, type.fluidMaxHeight);
            addFluidBox(buffer, pending, sprite, rgba, type.fluidHalfWidth, type.fluidBottom, bodyTop, towardCamera, camera);
        } else if (level > type.fluidMaxHeight) {
            addFluidBox(buffer, pending, sprite, rgba, type.neckFluidHalfWidth, getNeckStart(type), type.fluidBottom + level, towardCamera, camera);
        }
    }

    private static void addFluidBox(BufferBuilder buffer, Pending pending, TextureAtlasSprite sprite, int[] rgba, float halfWidth, float bottom, float top, boolean towardCamera, Vec3d camera) {
        Placement placement = pending.placement;
        float x0 = -halfWidth / 16.0F, x1 = halfWidth / 16.0F;
        float y0 = bottom / 16.0F, y1 = top / 16.0F;
        float z0 = x0, z1 = x1;
        float yMid = (y0 + y1) / 2.0F;
        float u0 = sprite.getMinU(), u1 = sprite.getMaxU(), v0 = sprite.getMinV(), v1 = sprite.getMaxV();

        // Per face: normal, centre, then four vertices of x, y, z, u, v
        float[][][] faces = {
            {{0, 0, -1}, {0, yMid, z0}, {x0, y0, z0, u0, v0}, {x0, y1, z0, u0, v1}, {x1, y1, z0, u1, v1}, {x1, y0, z0, u1, v0}},
            {{0, 0, 1}, {0, yMid, z1}, {x0, y0, z1, u0, v0}, {x1, y0, z1, u1, v0}, {x1, y1, z1, u1, v1}, {x0, y1, z1, u0, v1}},
            {{1, 0, 0}, {x1, yMid, 0}, {x1, y0, z0, u0, v0}, {x1, y1, z0, u1, v0}, {x1, y1, z1, u1, v1}, {x1, y0, z1, u0, v1}},
            {{-1, 0, 0}, {x0, yMid, 0}, {x0, y0, z0, u0, v0}, {x0, y0, z1, u1, v0}, {x0, y1, z1, u1, v1}, {x0, y1, z0, u0, v1}},
            {{0, 1, 0}, {0, y1, 0}, {x0, y1, z0, u0, v0}, {x0, y1, z1, u0, v1}, {x1, y1, z1, u1, v1}, {x1, y1, z0, u1, v0}},
        };
        for (float[][] face : faces) {
            float[] normal = face[0];
            float[] centre = face[1];
            if (placement.faces(camera, normal[0], normal[1], normal[2], centre[0], centre[1], centre[2]) != towardCamera) {
                continue;
            }
            for (int vertex = 2; vertex < 6; vertex++) {
                float[] v = face[vertex];
                buffer.pos(placement.worldX(v[0], v[2]), placement.originY + v[1], placement.worldZ(v[0], v[2]))
                    .color(rgba[0], rgba[1], rgba[2], rgba[3])
                    .tex(v[3], v[4])
                    .lightmap(pending.skyLight, pending.blockLight)
                    .endVertex();
            }
        }
    }

    @Nullable
    private static FluidStack getFluid(TileEntityPlacedContainers.Entry entry) {
        IFluidHandlerItem handler = entry.stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null || !(entry.stack.getItem() instanceof ItemDrinkable)) {
            return null;
        }
        FluidStack fluid = handler.drain(((ItemDrinkable) entry.stack.getItem()).getCapacity(), false);
        return fluid != null && fluid.amount > 0 ? fluid : null;
    }

    private void renderSolidQuads(TileEntityPlacedContainers.Entry entry) {
        SplitModel model = MODELS.get(entry.type);
        if (model == null || model.solid.isEmpty()) {
            return;
        }

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.99F);
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5, 0.0, -0.5);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        for (BakedQuad quad : model.solid) {
            int color = quad.hasTintIndex() ? getTint(entry.stack, quad.getTintIndex()) | 0xFF000000 : 0xFFFFFFFF;
            LightUtil.renderQuadColor(buffer, quad, color);
        }
        tessellator.draw();

        GlStateManager.popMatrix();
    }

    private static int getTint(ItemStack stack, int tintIndex) {
        if (tintIndex == 0 && PlacedContainerType.of(stack) == PlacedContainerType.BOTTLE) {
            return MapColor.getBlockColor(EnumDyeColor.byMetadata(stack.getMetadata())).colorValue;
        }
        return 0xFFFFFF;
    }
}