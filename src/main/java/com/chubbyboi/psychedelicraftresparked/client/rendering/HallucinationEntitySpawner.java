package com.chubbyboi.psychedelicraftresparked.client.rendering;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class HallucinationEntitySpawner {

    private static final double MIN_SPAWN_DISTANCE = 5.0;
    private static final double MAX_SPAWN_DISTANCE = 25.0;
    private static final int FORCE_FADE_OUT_TICKS = 20;
    private static List<ResourceLocation> candidateEntities;

    private static List<ResourceLocation> getCandidateEntities() {
        if (candidateEntities == null) {
            List<ResourceLocation> list = new ArrayList<>();
            for (EntityEntry entry : ForgeRegistries.ENTITIES) {
                Class<? extends Entity> entityClass = entry.getEntityClass();
                if (IMob.class.isAssignableFrom(entityClass) || EntityAnimal.class.isAssignableFrom(entityClass) || EntityAmbientCreature.class.isAssignableFrom(entityClass) || EntityWaterMob.class.isAssignableFrom(entityClass)) {
                    list.add(entry.getRegistryName());
                }
            }
            candidateEntities = list;
        }
        return candidateEntities;
    }

    private static final class HallucinatedEntity {
        final EntityLivingBase entity;
        final int spawnTick;
        int maxTicks;
        final float scale;
        final float tintR;
        final float tintG;
        final float tintB;
        final double velX;
        final double velY;
        final double velZ;
        final float rotationYawPlus;

        HallucinatedEntity(EntityLivingBase entity, int spawnTick, int maxTicks, float scale, float tintR, float tintG, float tintB, double velX, double velY, double velZ, float rotationYawPlus) {
            this.entity = entity;
            this.spawnTick = spawnTick;
            this.maxTicks = maxTicks;
            this.scale = scale;
            this.tintR = tintR;
            this.tintG = tintG;
            this.tintB = tintB;
            this.velX = velX;
            this.velY = velY;
            this.velZ = velZ;
            this.rotationYawPlus = rotationYawPlus;
        }

        float getFadeAlpha(int currentTick) {
            float progress = (currentTick - spawnTick) / (float) maxTicks;
            return MathHelper.clamp(MathHelper.sin(progress * (float) Math.PI) * 18.0f, 0.0f, 1.0f);
        }

        boolean isDead(int currentTick) {
            return (currentTick - spawnTick) >= maxTicks;
        }

        void forceFadeOut(int currentTick) {
            maxTicks = Math.min(maxTicks, (currentTick - spawnTick) + FORCE_FADE_OUT_TICKS);
        }
    }

    private static final class RastaHead {
        final int spawnTick;
        int maxTicks;

        double posX, posY, posZ;
        double lastTickPosX, lastTickPosY, lastTickPosZ;
        double motionX, motionY, motionZ;
        float rotationYawHead, prevRotationYawHead;
        float rotationPitch, prevRotationPitch;

        RastaHead(int spawnTick, int maxTicks, double posX, double posY, double posZ) {
            this.spawnTick = spawnTick;
            this.maxTicks = maxTicks;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.lastTickPosX = posX;
            this.lastTickPosY = posY;
            this.lastTickPosZ = posZ;
        }

        float getFadeAlpha(int currentTick) {
            float progress = (currentTick - spawnTick) / (float) maxTicks;
            return MathHelper.clamp(MathHelper.sin(progress * (float) Math.PI) * 18.0f, 0.0f, 1.0f);
        }

        boolean isDead(int currentTick) {
            return (currentTick - spawnTick) >= maxTicks;
        }

        void forceFadeOut(int currentTick) {
            maxTicks = Math.min(maxTicks, (currentTick - spawnTick) + FORCE_FADE_OUT_TICKS);
        }
    }

    private static final ResourceLocation RASTA_HEAD_TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/entities/rasta_head.png");
    private static final ModelRastaHead RASTA_HEAD_MODEL = new ModelRastaHead();

    private final List<HallucinatedEntity> active = new ArrayList<>();
    private RastaHead activeRastaHead;
    private int ticksExisted = 0;
    private final Set<Class<?>> loggedFailureClasses = new HashSet<>();

    private void logRenderFailureOnce(EntityLivingBase entity, Exception e) {
        if (loggedFailureClasses.add(entity.getClass())) {
            PsychedelicraftResparked.LOGGER.error("Hallucination entity render failed for " + entity.getClass().getName() + " - this entity type will silently be skipped from now on", e);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) return;

        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) return;

        ticksExisted++;

        HallucinationManager hallucinationManager = HallucinationManager.getInstance();
        float entityStrength = PSConfig.hallucinationEntitiesEnabled ? hallucinationManager.getEntityHallucinationStrength() : 0.0f;

        if (entityStrength <= 0.0f) {
            for (HallucinatedEntity hallucination : active) {
                hallucination.forceFadeOut(ticksExisted);
            }
            if (activeRastaHead != null) {
                activeRastaHead.forceFadeOut(ticksExisted);
            }
        }

        Iterator<HallucinatedEntity> iterator = active.iterator();
        while (iterator.hasNext()) {
            HallucinatedEntity hallucination = iterator.next();
            if (hallucination.isDead(ticksExisted)) {
                iterator.remove();
                continue;
            }
            updateHallucination(hallucination);
        }

        if (activeRastaHead != null) {
            if (activeRastaHead.isDead(ticksExisted)) {
                activeRastaHead = null;
            } else {
                updateRastaHead(activeRastaHead, player);
            }
        }

        float chance = entityStrength * 0.05f;
        if (chance <= 0.0f) return;

        int denominator = MathHelper.floor(1.0f / chance);
        if (denominator <= 0) return;

        Random random = player.getRNG();
        if (random.nextInt(denominator) != 0) return;

        if (activeRastaHead == null && random.nextFloat() < 0.1f && props.getDrugStrength("cannabis") > 0.4f) {
            spawnRastaHead(player, random);
        } else {
            spawnRandomEntity(player, random);
        }
    }

    private void updateHallucination(HallucinatedEntity hallucination) {
        EntityLivingBase entity = hallucination.entity;

        entity.ticksExisted++;

        entity.lastTickPosX = entity.posX;
        entity.lastTickPosY = entity.posY;
        entity.lastTickPosZ = entity.posZ;
        entity.prevRotationYaw = entity.rotationYaw;
        entity.prevRotationPitch = entity.rotationPitch;

        entity.prevRenderYawOffset = entity.renderYawOffset;
        entity.prevRotationYawHead = entity.rotationYawHead;
        entity.rotationYawHead = entity.renderYawOffset;

        entity.posX += hallucination.velX;
        entity.posY += hallucination.velY;
        entity.posZ += hallucination.velZ;

        entity.rotationYaw += hallucination.rotationYawPlus;

        double dx = entity.posX - entity.prevPosX;
        double dz = entity.posZ - entity.prevPosZ;
        float distance = MathHelper.sqrt(dx * dx + dz * dz) * 4.0F;
        if (distance > 1.0F) distance = 1.0F;

        entity.prevLimbSwingAmount = entity.limbSwingAmount;
        entity.limbSwingAmount += (distance / 3.0f - entity.limbSwingAmount) * 0.4F;
        entity.limbSwing += entity.limbSwingAmount;
    }

    private void updateRastaHead(RastaHead rastaHead, EntityPlayerSP player) {
        rastaHead.lastTickPosX = rastaHead.posX;
        rastaHead.lastTickPosY = rastaHead.posY;
        rastaHead.lastTickPosZ = rastaHead.posZ;
        rastaHead.prevRotationYawHead = rastaHead.rotationYawHead;
        rastaHead.prevRotationPitch = rastaHead.rotationPitch;

        double dx = player.posX - rastaHead.posX;
        double dy = (player.posY + player.getEyeHeight()) - (rastaHead.posY + 0.5);
        double dz = player.posZ - rastaHead.posZ;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float targetPitch = (float) -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        rastaHead.rotationPitch = clampedStep(0.0F, targetPitch, 3.0F);
        rastaHead.rotationYawHead = clampedStep(rastaHead.rotationYawHead, targetYaw, 3.0F);

        double wantedX = player.posX + Math.sin(player.ticksExisted / 50.0) * 5.0;
        double wantedY = player.posY;
        double wantedZ = player.posZ + Math.cos(player.ticksExisted / 50.0) * 5.0;

        double ddx = wantedX - rastaHead.posX;
        double ddy = wantedY - rastaHead.posY;
        double ddz = wantedZ - rastaHead.posZ;
        double totalDist = Math.sqrt(ddx * ddx + ddy * ddy + ddz * ddz);

        if (totalDist > 3.0) {
            rastaHead.motionX = ddx / totalDist * 0.05;
            rastaHead.motionY = ddy / totalDist * 0.05;
            rastaHead.motionZ = ddz / totalDist * 0.05;
        } else {
            rastaHead.motionX *= 0.9;
            rastaHead.motionY *= 0.9;
            rastaHead.motionZ *= 0.9;
        }

        rastaHead.posX += rastaHead.motionX;
        rastaHead.posY += rastaHead.motionY;
        rastaHead.posZ += rastaHead.motionZ;
    }

    private static float clampedStep(float prev, float target, float maxDelta) {
        float delta = MathHelper.clamp(MathHelper.wrapDegrees(target - prev), -maxDelta, maxDelta);
        return prev + delta;
    }

    private void spawnRastaHead(EntityPlayerSP player, Random random) {
        int maxTicks = (random.nextInt(59) + 120) * 20;
        activeRastaHead = new RastaHead(ticksExisted, maxTicks, player.posX, player.posY, player.posZ);
    }

    private void spawnRandomEntity(EntityPlayerSP player, Random random) {
        World world = player.world;

        List<ResourceLocation> candidates = getCandidateEntities();
        if (candidates.isEmpty()) return;
        ResourceLocation type = candidates.get(random.nextInt(candidates.size()));
        Entity spawned = EntityList.createEntityByIDFromName(type, world);
        if (!(spawned instanceof EntityLivingBase)) return;

        EntityLivingBase entity = (EntityLivingBase) spawned;

        double angle = random.nextDouble() * Math.PI * 2.0;
        double horizontalDistance = MIN_SPAWN_DISTANCE + random.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
        double x = player.posX + Math.cos(angle) * horizontalDistance;
        double z = player.posZ + Math.sin(angle) * horizontalDistance;
        double y = player.posY + random.nextDouble() * 10.0 - 5.0;
        entity.setPosition(x, y, z);
        entity.prevPosX = x;
        entity.prevPosY = y;
        entity.prevPosZ = z;
        entity.lastTickPosX = x;
        entity.lastTickPosY = y;
        entity.lastTickPosZ = z;

        double velX = (random.nextDouble() - 0.5) / 10.0;
        double velY = (random.nextDouble() - 0.5) / 10.0;
        double velZ = (random.nextDouble() - 0.5) / 10.0;

        entity.rotationYaw = random.nextInt(360);
        entity.prevRotationYaw = entity.rotationYaw;

        float rotationYawPlus = random.nextFloat() * 10.0f;
        if (random.nextBoolean()) rotationYawPlus = 0.0f;

        float scale = 1.0f;
        while (random.nextFloat() < 0.3f && scale < 20.0f) {
            scale *= random.nextFloat() * 2.7f + 0.3f;
        }
        scale = Math.min(scale, 20.0f);

        int maxTicks = (random.nextInt(59) + 3) * 20;

        float tintR = random.nextFloat();
        float tintG = random.nextFloat();
        float tintB = random.nextFloat();

        active.add(new HallucinatedEntity(entity, ticksExisted, maxTicks, scale, tintR, tintG, tintB, velX, velY, velZ, rotationYawPlus));
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (active.isEmpty() && activeRastaHead == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        RenderManager renderManager = mc.getRenderManager();
        float partialTicks = event.getPartialTicks();

        for (HallucinatedEntity hallucination : active) {
            float alpha = hallucination.getFadeAlpha(ticksExisted);
            if (alpha <= 0.001f) continue;

            EntityLivingBase entity = hallucination.entity;
            double interpX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
            double interpY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
            double interpZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
            float interpYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;

            GlStateManager.pushMatrix();

            try {
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

                GlStateManager.disableLighting();

                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.color(hallucination.tintR, hallucination.tintG, hallucination.tintB, alpha);

                GlStateManager.colorMask(true, true, true, false);

                GlStateManager.translate(interpX - renderManager.viewerPosX, interpY - renderManager.viewerPosY, interpZ - renderManager.viewerPosZ);
                GlStateManager.rotate(interpYaw, 0.0f, 1.0f, 0.0f);
                GlStateManager.scale(hallucination.scale, hallucination.scale, hallucination.scale);

                Render<Entity> render = (Render<Entity>) renderManager.getEntityRenderObject(entity);
                render.doRender(entity, 0.0, 0.0, 0.0, 0.0f, partialTicks);
            } catch (Exception e) {
                logRenderFailureOnce(entity, e);
            } finally {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                GlStateManager.disableBlend();

                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

                GlStateManager.enableLighting();
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.popMatrix();
            }
        }

        if (activeRastaHead != null) {
            renderRastaHead(activeRastaHead, renderManager, partialTicks);
        }
    }

    private void renderRastaHead(RastaHead rastaHead, RenderManager renderManager, float partialTicks) {
        float alpha = rastaHead.getFadeAlpha(ticksExisted);
        if (alpha <= 0.001f) return;

        double interpX = rastaHead.lastTickPosX + (rastaHead.posX - rastaHead.lastTickPosX) * partialTicks;
        double interpY = rastaHead.lastTickPosY + (rastaHead.posY - rastaHead.lastTickPosY) * partialTicks;
        double interpZ = rastaHead.lastTickPosZ + (rastaHead.posZ - rastaHead.lastTickPosZ) * partialTicks;
        float interpYaw = rastaHead.prevRotationYawHead + (rastaHead.rotationYawHead - rastaHead.prevRotationYawHead) * partialTicks;
        float interpPitch = rastaHead.prevRotationPitch + (rastaHead.rotationPitch - rastaHead.prevRotationPitch) * partialTicks;

        GlStateManager.pushMatrix();

        try {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.disableLighting();

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
            GlStateManager.colorMask(true, true, true, false);

            GlStateManager.translate(interpX - renderManager.viewerPosX, interpY + 1.0 - renderManager.viewerPosY, interpZ - renderManager.viewerPosZ);
            GlStateManager.rotate(interpPitch, 0.0f, 0.0f, 1.0f);
            GlStateManager.rotate(-interpYaw, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);

            Minecraft.getMinecraft().getTextureManager().bindTexture(RASTA_HEAD_TEXTURE);
            RASTA_HEAD_MODEL.render(null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Rasta-Head hallucination render failed", e);
        } finally {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.disableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableLighting();
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.popMatrix();
        }
    }
}
