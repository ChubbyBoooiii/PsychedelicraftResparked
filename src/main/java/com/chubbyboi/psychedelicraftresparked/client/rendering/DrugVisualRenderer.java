package com.chubbyboi.psychedelicraftresparked.client.rendering;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ShaderPipeline;
import com.chubbyboi.psychedelicraftresparked.drug.IDrug;
import com.chubbyboi.psychedelicraftresparked.mixins.EntityLivingBaseAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class DrugVisualRenderer {

    private final ShaderPipeline shaderPipeline;
    private final Random random = new Random();

    public DrugVisualRenderer() {
        this.shaderPipeline = ShaderPipeline.getInstance();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) return;

        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) return;

        DrugProperties drugProps = (DrugProperties) props;

        drugProps.update(player);

        drugProps.updateHeartbeatAndBreath(player);

        HallucinationManager.getInstance().update(drugProps, player.getRNG(), drugProps.getTicksExisted());

        float totalStumble = 0.0f;
        for (IDrug drug : drugProps.getAllDrugs()) {
            if (drug.getActiveValue() > 0.001f) {
                totalStumble += drug.getStumbleStrength();
            }
        }
        if (totalStumble > 0.001f) {
            int ticksExisted = drugProps.getTicksExisted();
            Random rng = player.getRNG();

            float pitchDelta = MathHelper.sin(ticksExisted / 600.0f * (float) Math.PI) / 2.0f * totalStumble * (rng.nextFloat() + 0.5f)
                    + MathHelper.sin(ticksExisted / 180.0f * (float) Math.PI) / 3.0f * totalStumble * (rng.nextFloat() + 0.5f);
            float yawDelta = MathHelper.cos(ticksExisted / 500.0f * (float) Math.PI) / 1.3f * totalStumble * (rng.nextFloat() + 0.5f)
                    + MathHelper.cos(ticksExisted / 150.0f * (float) Math.PI) / 2.0f * totalStumble * (rng.nextFloat() + 0.5f);

            player.rotationYaw += yawDelta;
            player.rotationPitch = MathHelper.clamp(player.rotationPitch + pitchDelta, -90.0f, 90.0f);
        }

        boolean canActInvoluntarily = !player.isPlayerSleeping() && !player.isRiding();

        if (canActInvoluntarily && player.onGround) {
            float jumpChance = 0.0f;
            for (IDrug drug : drugProps.getAllDrugs()) {
                if (drug.getActiveValue() > 0.001f) {
                    jumpChance += drug.getRandomJumpChance();
                }
            }
            if (jumpChance > 0.0f && player.getRNG().nextFloat() < jumpChance) {
                ((EntityLivingBaseAccessor) player).callJump();
            }
        }

        if (canActInvoluntarily && !player.isSwingInProgress) {
            float punchChance = 0.0f;
            for (IDrug drug : drugProps.getAllDrugs()) {
                if (drug.getActiveValue() > 0.001f) {
                    punchChance += drug.getRandomPunchChance();
                }
            }
            if (punchChance > 0.0f && player.getRNG().nextFloat() < punchChance) {
                player.swingArm(EnumHand.MAIN_HAND);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        shaderPipeline.captureDepth(event.getPartialTicks());
    }

    @SubscribeEvent
    public void onRenderHand(RenderSpecificHandEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) return;

        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) return;

        DrugProperties drugProps = (DrugProperties) props;

        // Calculate hand tremble amplitude
        float amplitude = 0.0f;
        for (IDrug drug : drugProps.getAllDrugs()) {
            if (drug.getActiveValue() > 0.001f) {
                float strength = drug.getHandTrembleStrength();
                amplitude += (1.0f - amplitude) * strength;
            }
        }

        if (amplitude > 0.001f) {
            Minecraft mc = Minecraft.getMinecraft();
            float ticks = mc.ingameGUI.getUpdateCounter() + event.getPartialTicks();

            Random handRandom = new Random((long) (ticks * 1000.0f));
            float shiftX = (handRandom.nextFloat() - 0.5f) * 0.015f * amplitude;
            float shiftY = (handRandom.nextFloat() - 0.5f) * 0.015f * amplitude;

            org.lwjgl.opengl.GL11.glTranslatef(shiftX, shiftY, 0.0f);
        }
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) return;

        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) return;

        DrugProperties drugProps = (DrugProperties) props;

        float totalTremble = 0.0f;

        for (IDrug drug : drugProps.getAllDrugs()) {
            if (drug.getActiveValue() > 0.001f) {
                totalTremble = Math.max(totalTremble, drug.getViewTrembleStrength());
            }
        }

        // Apply tremble
        if (totalTremble > 0.001f) {
            float trembleIntensity = totalTremble * 0.5f;

            float trembleX = (random.nextFloat() - 0.5f) * 2.0f * trembleIntensity;
            float trembleY = (random.nextFloat() - 0.5f) * 2.0f * trembleIntensity;
            float trembleZ = (random.nextFloat() - 0.5f) * 2.0f * trembleIntensity;

            event.setYaw(event.getYaw() + trembleX);
            event.setPitch(event.getPitch() + trembleY);
            event.setRoll(event.getRoll() + trembleZ);
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) return;

        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) return;

        DrugProperties drugProps = (DrugProperties) props;

        boolean anyActive = false;
        for (IDrug drug : drugProps.getAllDrugs()) {
            if (drug.getActiveValue() > 0.001f) {
                anyActive = true;
                break;
            }
        }
        if (!anyActive) {
            anyActive = HallucinationManager.getInstance().hasAnyEffect();
        }

        if (anyActive) {
            shaderPipeline.render(event.getPartialTicks());
        }
    }
}