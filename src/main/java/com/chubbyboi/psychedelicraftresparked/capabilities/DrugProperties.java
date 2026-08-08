package com.chubbyboi.psychedelicraftresparked.capabilities;

import com.chubbyboi.psychedelicraftresparked.drug.IDrug;
import com.chubbyboi.psychedelicraftresparked.drug.DrugInfluence;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugAlcohol;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugBrownShrooms;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugCaffeine;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugCannabis;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugCocaine;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugHarmonium;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugPeyote;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugPower;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugRedShrooms;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugTobacco;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugZero;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import com.chubbyboi.psychedelicraftresparked.init.SoundInit;
import com.chubbyboi.psychedelicraftresparked.network.NetworkHandler;
import com.chubbyboi.psychedelicraftresparked.network.PacketSpawnSmokeParticles;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DrugProperties implements IDrugProperties {

    private final Map<String, IDrug> drugs = new HashMap<>();
    private final List<DrugInfluence> influences = new ArrayList<>();
    private int ticksExisted = 0;

    public int getTicksExisted() {
        return ticksExisted;
    }

    private int sleepBlockedTicks = 0;

    private int delayUntilHeartbeat = 0;
    private int delayUntilBreath = 0;
    private boolean lastBreathWasIn = false;

    private int timeBreathingSmoke = 0;
    private float[] breathSmokeColor = {1.0f, 1.0f, 1.0f};
    private float smokeSpeedMultiplier = 1.0f;

    public DrugProperties() {
        registerDrug(new DrugCannabis());
        registerDrug(new DrugCocaine());
        registerDrug(new DrugAlcohol());
        registerDrug(new DrugBrownShrooms());
        registerDrug(new DrugRedShrooms());
        registerDrug(new DrugCaffeine());
        registerDrug(new DrugTobacco());
        registerDrug(new DrugPeyote());
        registerDrug(new DrugHarmonium());
        registerDrug(new DrugZero());
        registerDrug(new DrugPower());
        // Warmth ?? (Some way of heating any drink? Instead of a furnace, hot wine would be cool - big pot over fire?)
    }

    private void registerDrug(IDrug drug) {
        drugs.put(drug.getName(), drug);
    }

    public IDrug getDrug(String name) {
        return drugs.get(name);
    }

    public Collection<IDrug> getAllDrugs() {
        return drugs.values();
    }

    @Override
    public float getDrugStrength(String drugType) {
        IDrug drug = drugs.get(drugType);
        return drug != null ? drug.getActiveValue() : 0.0f;
    }

    @Override
    public void setDrugStrength(String drugType, float strength) {
        IDrug drug = drugs.get(drugType);
        if (drug != null) {
            drug.setDesiredValue(strength);
        }
    }

    public float getDesiredDrugStrength(String drugType) {
        IDrug drug = drugs.get(drugType);
        return drug != null ? drug.getDesiredValue() : 0.0f;
    }

    public void addDrugStrength(String drugType, float amount) {
        IDrug drug = drugs.get(drugType);
        if (drug != null) {
            drug.addToDesiredValue(amount);
        }
    }

    public void addInfluence(DrugInfluence influence) {
        influences.add(influence);
    }

    public boolean hasActiveInfluences() {
        return !influences.isEmpty();
    }

    public void wakeUp() {
        for (IDrug drug : drugs.values()) {
            drug.setDesiredValue(0.0f);
        }
        influences.clear();
    }

    public void clearAll() {
        for (IDrug drug : drugs.values()) {
            drug.setActiveValue(0.0f);
            drug.setDesiredValue(0.0f);
        }
        influences.clear();
    }

    public float getSpeedModifier() {
        float modifier = 1.0f;
        for (IDrug drug : drugs.values()) {
            if (drug.getActiveValue() > 0.001f) {
                modifier *= drug.getSpeedModifier();
            }
        }
        return modifier;
    }

    public float getDigSpeedModifier() {
        float modifier = 1.0f;
        for (IDrug drug : drugs.values()) {
            if (drug.getActiveValue() > 0.001f) {
                modifier *= drug.getDigSpeedModifier();
            }
        }
        return modifier;
    }

    public boolean isSleepBlocked() {
        for (IDrug drug : drugs.values()) {
            if (drug.getActiveValue() > 0.001f && drug.isSleepBlocked()) {
                return true;
            }
        }
        return false;
    }

    public int incrementSleepBlockedTicks() {
        return ++sleepBlockedTicks;
    }

    public void resetSleepBlockedTicks() {
        sleepBlockedTicks = 0;
    }

    public void startBreathingSmoke(int time, float[] color) {
        startBreathingSmoke(time, color, 1.0f);
    }

    public void startBreathingSmoke(int time, float[] color, float speedMultiplier) {
        this.breathSmokeColor = color != null ? color : new float[]{1.0f, 1.0f, 1.0f};
        this.timeBreathingSmoke = time + 10; // 10 is the time spent breathing in/recovering, no particles
        this.smokeSpeedMultiplier = speedMultiplier;
    }

    public boolean isBreathingSmoke() {
        return timeBreathingSmoke > 0;
    }

    private void updateBreathingSmoke(EntityPlayer player) {
        timeBreathingSmoke--;

        if (timeBreathingSmoke > 10) {
            java.util.Random random = player.world.rand;

            if (random.nextInt(2) == 0) {
                sendSmokeParticle(player, (random.nextFloat() * 0.03f + 0.05f) * smokeSpeedMultiplier, 1.0f);
            }
            if (random.nextInt(5) == 0) {
                sendSmokeParticle(player, (random.nextFloat() * 0.03f + 0.05f) * smokeSpeedMultiplier, 2.5f);
            }
        }
    }

    private void sendSmokeParticle(EntityPlayer player, float speed, float size) {
        double yawRad = Math.toRadians(player.rotationYaw);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        // Nudged half a block along the facing direction so it spawns in front of the face
        double x = player.posX + dirX * 0.5;
        double y = player.posY + player.getEyeHeight() - 0.2;
        double z = player.posZ + dirZ * 0.5;

        double motionX = dirX * speed;
        double motionY = 0;
        double motionZ = dirZ * speed;

        PacketSpawnSmokeParticles packet = new PacketSpawnSmokeParticles(x, y, z, motionX, motionY, motionZ, size, breathSmokeColor);

        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 64.0);
        NetworkHandler.INSTANCE.sendToAllAround(packet, point);
    }

    public void updateHeartbeatAndBreath(EntityPlayer player) {
        if (delayUntilHeartbeat > 0) {
            delayUntilHeartbeat--;
        }
        if (delayUntilBreath > 0) {
            delayUntilBreath--;
        }

        if (delayUntilHeartbeat == 0) {
            float heartbeatVolume = 0.0f;
            for (IDrug drug : drugs.values()) {
                if (drug.getActiveValue() > 0.001f) {
                    heartbeatVolume += drug.getHeartbeatVolume();
                }
            }

            if (heartbeatVolume > 0.0f) {
                float speed = 1.0f;
                for (IDrug drug : drugs.values()) {
                    if (drug.getActiveValue() > 0.001f) {
                        speed += drug.getHeartbeatSpeed();
                    }
                }

                delayUntilHeartbeat = MathHelper.floor(35.0f / (speed - 1.0f));
                player.playSound(SoundInit.HEARTBEAT, heartbeatVolume, speed);
            }
        }

        if (delayUntilBreath == 0) {
            float breathVolume = 0.0f;
            for (IDrug drug : drugs.values()) {
                if (drug.getActiveValue() > 0.001f) {
                    breathVolume += drug.getBreathVolume();
                }
            }

            lastBreathWasIn = !lastBreathWasIn;

            if (breathVolume > 0.0f) {
                float speed = 1.0f;
                for (IDrug drug : drugs.values()) {
                    if (drug.getActiveValue() > 0.001f) {
                        speed += drug.getBreathSpeed();
                    }
                }

                delayUntilBreath = MathHelper.floor(30.0f / speed);
                float pitch = speed * 0.1f + 0.9f + (lastBreathWasIn ? 0.15f : 0.0f);
                player.playSound(SoundInit.BREATH, breathVolume, pitch);
            }
        }
    }

    public void update(EntityPlayer player) {
        ticksExisted++;

        if (!player.world.isRemote) {
            if (!PSConfig.drugEffectsEnabled) {
                clearAll();
            }

            if (!influences.isEmpty() && ticksExisted % 5 == 0) {
                Iterator<DrugInfluence> iterator = influences.iterator();
                while (iterator.hasNext()) {
                    DrugInfluence influence = iterator.next();
                    influence.update(this);

                    if (influence.isDone()) {
                        iterator.remove();
                    }
                }
            }

            // Update all drugs
            for (IDrug drug : drugs.values()) {
                drug.update(player, player.world, ticksExisted);
            }

            if (timeBreathingSmoke > 0) {
                updateBreathingSmoke(player);
            }
        }

        if (player.world.isRemote) {
            for (IDrug drug : drugs.values()) {
                float nearValue = drug.getNearValue();
                float activeValue = drug.getActiveValue();

                // Smooth interpolation towards nearValue
                if (Math.abs(activeValue - nearValue) > 0.001f) {
                    float newActive = activeValue + (nearValue - activeValue) * 0.2f;
                    drug.setActiveValue(newActive);
                }
            }

            if (timeBreathingSmoke > 0) {
                timeBreathingSmoke--;
            }
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        NBTTagCompound drugsTag = new NBTTagCompound();

        for (Map.Entry<String, IDrug> entry : drugs.entrySet()) {
            NBTTagCompound drugTag = new NBTTagCompound();
            entry.getValue().writeToNBT(drugTag);
            drugsTag.setTag(entry.getKey(), drugTag);
        }

        compound.setTag("drugs", drugsTag);
        compound.setInteger("ticksExisted", ticksExisted);

        net.minecraft.nbt.NBTTagList influencesTag = new net.minecraft.nbt.NBTTagList();
        for (DrugInfluence influence : influences) {
            NBTTagCompound influenceTag = new NBTTagCompound();
            influence.writeToNBT(influenceTag);
            influenceTag.setString("influenceClass", influence.getClass().getName());
            influencesTag.appendTag(influenceTag);
        }
        compound.setTag("influences", influencesTag);
    }

    public void readFromNBT(NBTTagCompound compound) {
        NBTTagCompound drugsTag = compound.getCompoundTag("drugs");

        for (String drugName : drugsTag.getKeySet()) {
            IDrug drug = drugs.get(drugName);
            if (drug != null) {
                drug.readFromNBT(drugsTag.getCompoundTag(drugName));
            }
        }

        ticksExisted = compound.getInteger("ticksExisted");

        influences.clear();
        net.minecraft.nbt.NBTTagList influencesTag = compound.getTagList("influences", net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < influencesTag.tagCount(); i++) {
            NBTTagCompound influenceTag = influencesTag.getCompoundTagAt(i);
            DrugInfluence influence = createInfluence(influenceTag.getString("influenceClass"));
            influence.readFromNBT(influenceTag);
            influences.add(influence);
        }
    }

    private static DrugInfluence createInfluence(String className) {
        if (com.chubbyboi.psychedelicraftresparked.drug.DrugInfluenceHarmonium.class.getName().equals(className)) {
            return new com.chubbyboi.psychedelicraftresparked.drug.DrugInfluenceHarmonium();
        }
        return new DrugInfluence();
    }

    @Override
    public float getDecayRate(String drugType) {
        // Legacy - decay is now handled inside drug objects
        return 0.001f;
    }

    @Override
    public void setDecayRate(String drugType, float decayRate) {
        // Legacy - decay is now handled inside drug objects
    }

    @Override
    public void copyFrom(IDrugProperties source) {
        if (source instanceof DrugProperties) {
            DrugProperties other = (DrugProperties) source;

            // Copy drug states
            for (Map.Entry<String, IDrug> entry : other.drugs.entrySet()) {
                IDrug ourDrug = this.drugs.get(entry.getKey());
                IDrug theirDrug = entry.getValue();

                if (ourDrug != null && theirDrug != null) {
                    ourDrug.setActiveValue(theirDrug.getActiveValue());
                    ourDrug.setDesiredValue(theirDrug.getDesiredValue());
                    ourDrug.setNearValue(theirDrug.getNearValue());
                }
            }

            this.ticksExisted = other.ticksExisted;

            this.influences.clear();
            for (DrugInfluence influence : other.influences) {
                NBTTagCompound influenceTag = new NBTTagCompound();
                influence.writeToNBT(influenceTag);
                DrugInfluence copy = createInfluence(influence.getClass().getName());
                copy.readFromNBT(influenceTag);
                this.influences.add(copy);
            }
        }
    }
}