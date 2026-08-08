package com.chubbyboi.psychedelicraftresparked.entities;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class EntityRealityRift extends Entity {

    private static final DataParameter<Float> RIFT_SIZE = EntityDataManager.createKey(EntityRealityRift.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> RIFT_CLOSING = EntityDataManager.createKey(EntityRealityRift.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> CRITICAL_STATUS = EntityDataManager.createKey(EntityRealityRift.class, DataSerializers.FLOAT);

    public float visualRiftSize;

    public EntityRealityRift(World world) {
        super(world);
        setSize(2.0F, 2.0F);
        ignoreFrustumCheck = true;
    }

    @Override
    protected void entityInit() {
        dataManager.register(RIFT_SIZE, 0.0F);
        dataManager.register(RIFT_CLOSING, false);
        dataManager.register(CRITICAL_STATUS, 0.0F);

        setRiftSize(world.rand.nextFloat() * 0.5F + 0.5F);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote && com.chubbyboi.psychedelicraftresparked.config.PSConfig.randomTicksUntilRiftSpawn == 0) {
            setDead();
            return;
        }

        motionX = 0.0;
        motionY = 0.0;
        motionZ = 0.0;

        boolean critical = isCritical();

        if (world.isRemote) {
            int smokeParticles = rand.nextInt(3);
            for (int i = 0; i < smokeParticles; i++) {
                float distance = rand.nextFloat() * rand.nextFloat();
                double xP = (rand.nextFloat() * 8.0 - 4.0) * distance;
                double yP = (rand.nextFloat() * 8.0 - 4.0) * distance;
                double zP = (rand.nextFloat() * 8.0 - 4.0) * distance;
                world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_LARGE,
                        posX + xP, posY + yP + height / 2.0, posZ + zP, 0.0, 0.0, 0.0);
            }

            int stationarySmoke = rand.nextInt(2);
            for (int i = 0; i < stationarySmoke; i++) {
                float distance = rand.nextFloat() * rand.nextFloat();
                double xP = (rand.nextFloat() * 8.0 - 4.0) * distance;
                double yP = (rand.nextFloat() * 8.0 - 4.0) * distance;
                double zP = (rand.nextFloat() * 8.0 - 4.0) * distance;

                world.spawnParticle(net.minecraft.util.EnumParticleTypes.REDSTONE,
                        posX + xP, posY + yP + height / 2.0, posZ + zP, 0.001, 0.001, 0.001);
            }

            double xP = rand.nextFloat() * 8.0 - 4.0;
            double yP = rand.nextFloat() * 8.0 - 4.0;
            double zP = rand.nextFloat() * 8.0 - 4.0;
            world.spawnParticle(net.minecraft.util.EnumParticleTypes.ENCHANTMENT_TABLE,
                    posX, posY + 1.0 + height / 2.0, posZ, xP, yP, zP);
        }

        float searchDistance = 5.0F + getCriticalStatus() * 50.0F;
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                getEntityBoundingBox().grow(searchDistance, searchDistance, searchDistance));

        for (EntityLivingBase entity : entities) {
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                if (player.isSpectator() || player.capabilities.isCreativeMode) {
                    continue;
                }
            }

            double dist = entity.getDistance(this);
            double effect = (searchDistance - dist) * 0.0005 * getRiftSize();

            if (effect <= 0.0) {
                continue;
            }

            IDrugProperties props = entity instanceof EntityPlayer
                    ? entity.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null)
                    : null;

            if (props instanceof DrugProperties) {
                DrugProperties drugProps = (DrugProperties) props;
                drugProps.addDrugStrength("zero", (float) effect * 20.0F);
                drugProps.addDrugStrength("power", (float) effect * 200.0F);
            } else if (critical) {
                entity.attackEntityFrom(DamageSource.MAGIC, (float) effect * 20.0F);
            }
        }

        if (critical) {
            setCriticalStatus(Math.min(getCriticalStatus() + 0.001F, 1.0F));
        }

        if (isRiftClosing()) {
            setRiftSize(getRiftSize() - 1.0F / 20.0F);
        } else if (!critical) {
            setRiftSize(getRiftSize() - 1.0F / 20.0F / 20.0F / 60.0F);
        }

        visualRiftSize = nearValue(visualRiftSize, getRiftSize(), 0.05F, 0.005F);

        if (!world.isRemote) {
            if (getCriticalStatus() >= 0.9F) {
                setRiftClosing(true);
            }

            if (visualRiftSize <= 0.0F && getRiftSize() <= 0.0F) {
                setDead();
            }
        }
    }

    private static float nearValue(float current, float target, float easeSpeed, float minSpeed) {
        float diff = target - current;
        float speed = Math.max(Math.abs(diff) * easeSpeed, minSpeed);
        if (Math.abs(diff) <= speed) {
            return target;
        }
        return current + Math.signum(diff) * speed;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setRiftSize(compound.getFloat("riftSize"));
        setRiftClosing(compound.getBoolean("isRiftClosing"));
        setCriticalStatus(compound.getFloat("criticalStatus"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setFloat("riftSize", getRiftSize());
        compound.setBoolean("isRiftClosing", isRiftClosing());
        compound.setFloat("criticalStatus", getCriticalStatus());
    }

    public float getRiftSize() {
        return dataManager.get(RIFT_SIZE);
    }

    public void setRiftSize(float size) {
        dataManager.set(RIFT_SIZE, Math.max(size, 0.0F));
    }

    public float getCriticalStatus() {
        return dataManager.get(CRITICAL_STATUS);
    }

    public void setCriticalStatus(float status) {
        dataManager.set(CRITICAL_STATUS, Math.max(status, 0.0F));
    }

    public void addToRift(float size) {
        setRiftSize(getRiftSize() + size);
    }

    public float takeFromRift(float size) {
        if (isCritical()) {
            return 0.2F;
        }

        float riftSize = getRiftSize();
        float newVal = Math.max(riftSize - size, 0.0F);
        setRiftSize(newVal);
        return riftSize - newVal;
    }

    public boolean isRiftClosing() {
        return dataManager.get(RIFT_CLOSING);
    }

    public void setRiftClosing(boolean closing) {
        dataManager.set(RIFT_CLOSING, closing);
    }

    public boolean isCritical() {
        return (getCriticalStatus() > 0.0F || getRiftSize() > 3.0F) && !isRiftClosing();
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    public String getName() {
        return TextFormatting.OBFUSCATED + super.getName();
    }
}