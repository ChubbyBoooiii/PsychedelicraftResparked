package com.chubbyboi.psychedelicraftresparked.client.rendering;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class SmokeMonsterSpawner {

    private static final SmokeMonsterSpawner INSTANCE = new SmokeMonsterSpawner();

    public static SmokeMonsterSpawner getInstance() {
        return INSTANCE;
    }

    private SmokeMonsterSpawner() {
    }

    private static final class CreatureType {
        final ResourceLocation entityId;
        final float baseScale;
        final boolean useTexture;

        CreatureType(ResourceLocation entityId, float baseScale) {
            this(entityId, baseScale, false);
        }

        CreatureType(ResourceLocation entityId, float baseScale, boolean useTexture) {
            this.entityId = entityId;
            this.baseScale = baseScale;
            this.useTexture = useTexture;
        }
    }

    private static final CreatureType[] CREATURE_TYPES = {
            new CreatureType(new ResourceLocation("minecraft", "ender_dragon"), 0.06f, true),
            new CreatureType(new ResourceLocation("minecraft", "ghast"), 0.06f),
            new CreatureType(new ResourceLocation("minecraft", "horse"), 0.2f),
    };

    private static final float MAX_ALPHA = 0.4f;

    private static final class SummonedMonster {
        final EntityLivingBase entity;
        final int spawnTick;
        final int maxTicks;
        final float scale;
        final double velX;
        final double velY;
        final double velZ;
        final boolean isDragon;
        final boolean isGhast;
        final boolean useTexture;
        final float colorR;
        final float colorG;
        final float colorB;

        SummonedMonster(EntityLivingBase entity, int spawnTick, int maxTicks, float scale, double velX, double velY, double velZ, boolean isDragon, boolean isGhast, boolean useTexture, float colorR, float colorG, float colorB) {
            this.entity = entity;
            this.spawnTick = spawnTick;
            this.maxTicks = maxTicks;
            this.scale = scale;
            this.velX = velX;
            this.velY = velY;
            this.velZ = velZ;
            this.isDragon = isDragon;
            this.useTexture = useTexture;
            this.isGhast = isGhast;
            this.colorR = colorR;
            this.colorG = colorG;
            this.colorB = colorB;
        }

        float getFadeAlpha(int currentTick) {
            float progress = (currentTick - spawnTick) / (float) maxTicks;
            float fade = MathHelper.clamp(MathHelper.sin(progress * (float) Math.PI) * 6.0f, 0.0f, 1.0f);
            return fade * MAX_ALPHA;
        }

        boolean isDead(int currentTick) {
            return (currentTick - spawnTick) >= maxTicks;
        }
    }

    private final List<SummonedMonster> active = new ArrayList<>();
    private int ticksExisted = 0;
    private final Set<Class<?>> loggedFailureClasses = new HashSet<>();
    private RenderSmokeDragon dragonRenderer;
    private RenderTranslucentGhast ghastRenderer;

    private void logRenderFailureOnce(EntityLivingBase entity, Exception e) {
        if (loggedFailureClasses.add(entity.getClass())) {
            PsychedelicraftResparked.LOGGER.error("Smoke monster render failed for " + entity.getClass().getName() + " - this creature type will silently be skipped from now on", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Render<Entity> getRenderer(SummonedMonster monster, RenderManager renderManager, float alpha) {
        if (monster.isDragon) {
            if (dragonRenderer == null) {
                dragonRenderer = new RenderSmokeDragon(renderManager);
            }
            return (Render<Entity>) (Render<?>) dragonRenderer;
        }
        if (monster.isGhast) {
            if (ghastRenderer == null) {
                ghastRenderer = new RenderTranslucentGhast(renderManager);
            }
            ghastRenderer.setAlpha(alpha);
            ghastRenderer.setColor(monster.colorR, monster.colorG, monster.colorB);
            return (Render<Entity>) (Render<?>) ghastRenderer;
        }
        return (Render<Entity>) renderManager.getEntityRenderObject(monster.entity);
    }

    public void spawn(World world, double x, double y, double z, double velX, double velY, double velZ, float yaw, int maxTicks, int creatureIndex, float colorR, float colorG, float colorB) {
        CreatureType creatureType = CREATURE_TYPES[Math.floorMod(creatureIndex, CREATURE_TYPES.length)];
        Entity spawned = EntityList.createEntityByIDFromName(creatureType.entityId, world);
        if (!(spawned instanceof EntityLivingBase)) return;

        EntityLivingBase entity = (EntityLivingBase) spawned;
        entity.setPosition(x, y, z);
        entity.prevPosX = entity.lastTickPosX = x;
        entity.prevPosY = entity.lastTickPosY = y;
        entity.prevPosZ = entity.lastTickPosZ = z;
        entity.rotationYaw = yaw;
        entity.prevRotationYaw = yaw;
        entity.renderYawOffset = yaw;
        entity.prevRenderYawOffset = yaw;

        boolean isDragon = entity instanceof EntityDragon;
        if (isDragon) {
            EntityDragon dragon = (EntityDragon) entity;
            float dragonYaw = yaw + 180.0f;
            for (double[] entry : dragon.ringBuffer) {
                entry[0] = dragonYaw;
                entry[1] = y;
            }
            dragon.ringBufferIndex = 0;
        }

        boolean isGhast = entity instanceof EntityGhast;

        active.add(new SummonedMonster(entity, ticksExisted, maxTicks, creatureType.baseScale, velX, velY, velZ, isDragon, isGhast, creatureType.useTexture, colorR, colorG, colorB));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (active.isEmpty()) return;

        ticksExisted++;

        Iterator<SummonedMonster> iterator = active.iterator();
        while (iterator.hasNext()) {
            SummonedMonster monster = iterator.next();
            if (monster.isDead(ticksExisted)) {
                iterator.remove();
                continue;
            }
            update(monster);
        }
    }

    private void update(SummonedMonster monster) {
        EntityLivingBase entity = monster.entity;

        entity.ticksExisted++;

        entity.lastTickPosX = entity.posX;
        entity.lastTickPosY = entity.posY;
        entity.lastTickPosZ = entity.posZ;

        entity.posX += monster.velX;
        entity.posY += monster.velY;
        entity.posZ += monster.velZ;

        double dx = entity.posX - entity.prevPosX;
        double dz = entity.posZ - entity.prevPosZ;
        float distance = MathHelper.sqrt(dx * dx + dz * dz) * 4.0F;
        if (distance > 1.0F) distance = 1.0F;

        entity.prevLimbSwingAmount = entity.limbSwingAmount;
        entity.limbSwingAmount += (distance / 3.0f - entity.limbSwingAmount) * 0.4F;
        entity.limbSwing += entity.limbSwingAmount;

        if (monster.isDragon) {
            EntityDragon dragon = (EntityDragon) entity;
            dragon.prevAnimTime = dragon.animTime;
            dragon.animTime += 0.1F; // matches source's own "stationary phase" flap rate - see class javadoc
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (active.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        RenderManager renderManager = mc.getRenderManager();
        float partialTicks = event.getPartialTicks();

        for (SummonedMonster monster : active) {
            float alpha = monster.getFadeAlpha(ticksExisted);
            if (alpha <= 0.001f) continue;

            EntityLivingBase entity = monster.entity;
            double interpX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
            double interpY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
            double interpZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

            GlStateManager.pushMatrix();

            try {
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                GlStateManager.disableLighting();

                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                if (!monster.useTexture) {
                    GlStateManager.disableTexture2D();
                }
                GlStateManager.color(monster.colorR, monster.colorG, monster.colorB, alpha);
                GlStateManager.colorMask(true, true, true, false);

                GlStateManager.translate(interpX - renderManager.viewerPosX, interpY - renderManager.viewerPosY, interpZ - renderManager.viewerPosZ);
                GlStateManager.scale(monster.scale, monster.scale, monster.scale);

                Render<Entity> render = getRenderer(monster, renderManager, alpha);
                render.doRender(entity, 0.0, 0.0, 0.0, 0.0f, partialTicks);
            } catch (Exception e) {
                logRenderFailureOnce(entity, e);
            } finally {
                GlStateManager.enableTexture2D();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                GlStateManager.disableBlend();

                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

                GlStateManager.enableLighting();
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.popMatrix();
            }
        }
    }
}
