package com.chubbyboi.psychedelicraftresparked.client.rendering.placedcontainers;

import com.chubbyboi.psychedelicraftresparked.block.BlockPlacedContainers;
import com.chubbyboi.psychedelicraftresparked.block.PlacedContainerType;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityPlacedContainers;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class PlacementPreviewRenderer {

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        EntityPlayer player = event.getPlayer();
        RayTraceResult target = event.getTarget();
        if (!player.isSneaking() || target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (PlacedContainerType.of(stack) == null) {
            stack = player.getHeldItem(EnumHand.OFF_HAND);
        }

        BlockPos clicked = target.getBlockPos();
        Vec3d hit = target.hitVec;
        float hitX = (float) (hit.x - clicked.getX());
        float hitZ = (float) (hit.z - clicked.getZ());
        BlockPlacedContainers.Placement placement = BlockPlacedContainers.findPlacement(player.world, clicked, target.sideHit, player, stack, hitX, hitZ);
        if (placement == null || !placement.fits) {
            return;
        }

        float partialTicks = event.getPartialTicks();
        double cameraX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double cameraY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double cameraZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        double angle = Math.toRadians(-placement.rotation * 360.0F / TileEntityPlacedContainers.ROTATION_STEPS);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double halfWidth = placement.shape.halfFootprint / 16.0;
        double originX = placement.target.getX() + placement.x / 16.0 - cameraX;
        double originY = placement.target.getY() + 0.002 - cameraY;
        double originZ = placement.target.getZ() + placement.z / 16.0 - cameraZ;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        double[][] corners = {{-halfWidth, -halfWidth}, {halfWidth, -halfWidth}, {halfWidth, halfWidth}, {-halfWidth, halfWidth}};
        for (double[] corner : corners) {
            double x = corner[0] * cos + corner[1] * sin;
            double z = -corner[0] * sin + corner[1] * cos;
            buffer.pos(originX + x, originY, originZ + z).color(1.0F, 1.0F, 1.0F, 0.6F).endVertex();
        }
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}