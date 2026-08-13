package com.chubbyboi.psychedelicraftresparked.tileentities;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.client.rendering.bezier.BezierPath;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import com.chubbyboi.psychedelicraftresparked.entities.EntityRealityRift;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TileEntityRiftJar extends TileEntity implements ITickable {

    public float currentRiftFraction;
    public int ticksAliveVisual;

    public boolean isOpening;
    public float fractionOpen;

    public boolean jarBroken = false;
    public boolean suckingRifts = true;
    public float fractionHandleUp;

    public int blockRotation;

    public List<JarRiftConnection> riftConnections = new ArrayList<>();

    @Override
    public void update() {
        fractionOpen = nearValue(fractionOpen, isOpening ? 1.0f : 0.0f, 0.0f, 0.02f);
        fractionHandleUp = nearValue(fractionHandleUp, suckingRifts ? 0.0f : 1.0f, 0.0f, 0.04f);

        if (isSuckingRifts()) {
            if (fractionOpen > 0.0f && (PSConfig.riftJarOverfillingEnabled || currentRiftFraction < 1.0f)) {
                List<EntityRealityRift> rifts = getAffectedRifts();

                if (!rifts.isEmpty()) {
                    float minus = (1.0f / rifts.size()) * 0.001f * fractionOpen;
                    for (EntityRealityRift rift : rifts) {
                        currentRiftFraction += rift.takeFromRift(minus);

                        JarRiftConnection connection = createAndGetRiftConnection(rift);
                        connection.fractionUp = Math.min(connection.fractionUp + 0.02f * fractionOpen, 1.0f);
                    }
                }
            }
        } else if (fractionOpen > 0.0f) {
            float minus = Math.min(0.0004f * fractionOpen * currentRiftFraction + 0.0004f, currentRiftFraction);

            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(
                    pos.getX() - 5.0, pos.getY() - 5.0, pos.getZ() - 2.0,
                    pos.getX() + 6.0, pos.getY() + 6.0, pos.getZ() + 6.0));

            for (EntityLivingBase entity : entities) {
                double effect = (5.0 - entity.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) * 0.2 * minus;

                IDrugProperties props = entity.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
                if (props instanceof DrugProperties) {
                    DrugProperties drugProperties = (DrugProperties) props;
                    drugProperties.addDrugStrength("zero", (float) (effect * 5.0));
                    drugProperties.addDrugStrength("power", (float) (effect * 35.0));
                }
            }

            currentRiftFraction -= minus;
        }

        Iterator<JarRiftConnection> connectionIterator = riftConnections.iterator();
        while (connectionIterator.hasNext()) {
            JarRiftConnection connection = connectionIterator.next();
            connection.fractionUp -= 0.01f;

            if (connection.fractionUp <= 0.0f) {
                connectionIterator.remove();
            }
        }

        if (currentRiftFraction > 1.0f) {
            if (PSConfig.riftJarOverfillingEnabled) {
                jarBroken = true;

                releaseRift();
                world.setBlockToAir(pos);
                world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0f, false);
            } else {
                currentRiftFraction = 1.0f;
            }
        }

        ticksAliveVisual++;
    }

    public List<EntityRealityRift> getAffectedRifts() {
        return world.getEntitiesWithinAABB(EntityRealityRift.class, new AxisAlignedBB(
                pos.getX() - 2.0, pos.getY(), pos.getZ() - 2.0,
                pos.getX() + 3.0, pos.getY() + 10.0, pos.getZ() + 3.0));
    }

    public JarRiftConnection createAndGetRiftConnection(EntityRealityRift rift) {
        for (JarRiftConnection connection : riftConnections) {
            if (connection.riftID == rift.getEntityId()) {
                return connection;
            }
        }

        JarRiftConnection newConnection = new JarRiftConnection();
        newConnection.riftID = rift.getEntityId();
        newConnection.entityX = rift.posX;
        newConnection.entityY = rift.posY + rift.height * 0.5;
        newConnection.entityZ = rift.posZ;
        riftConnections.add(newConnection);

        return newConnection;
    }

    public void releaseRift() {
        if (currentRiftFraction > 0.0f) {
            List<EntityRealityRift> rifts = getAffectedRifts();

            if (!rifts.isEmpty()) {
                rifts.get(0).addToRift(currentRiftFraction);
            } else if (!world.isRemote) {
                EntityRealityRift rift = new EntityRealityRift(world);
                rift.setPosition(pos.getX() + 0.5, pos.getY() + 3.0, pos.getZ() + 0.5);
                rift.setRiftSize(currentRiftFraction);
                world.spawnEntity(rift);
            }

            currentRiftFraction = 0.0f;
        }
    }

    private static float nearValue(float current, float target, float mulSpeed, float plusSpeed) {
        current += (target - current) * mulSpeed;
        if (current > target) {
            current = Math.max(current - plusSpeed, target);
        } else if (current < target) {
            current = Math.min(current + plusSpeed, target);
        }
        return current;
    }

    public void toggleRiftJarOpen() {
        if (!world.isRemote) {
            isOpening = !isOpening;
            sync();
        }
    }

    public void toggleSuckingRifts() {
        if (!world.isRemote) {
            suckingRifts = !suckingRifts;
            sync();
        }
    }

    public boolean isSuckingRifts() {
        return suckingRifts;
    }

    private void sync() {
        markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.getX() - 10, pos.getY() - 5, pos.getZ() - 10, pos.getX() + 11, pos.getY() + 11, pos.getZ() + 11);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setFloat("currentRiftFraction", currentRiftFraction);

        compound.setBoolean("isOpening", isOpening);
        compound.setFloat("fractionOpen", fractionOpen);

        compound.setBoolean("jarBroken", jarBroken);
        compound.setBoolean("suckingRifts", suckingRifts);
        compound.setFloat("fractionHandleUp", fractionHandleUp);

        compound.setInteger("blockRotation", blockRotation);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        currentRiftFraction = compound.getFloat("currentRiftFraction");

        isOpening = compound.getBoolean("isOpening");
        fractionOpen = compound.getFloat("fractionOpen");

        jarBroken = compound.getBoolean("jarBroken");
        suckingRifts = compound.getBoolean("suckingRifts");
        fractionHandleUp = compound.getFloat("fractionHandleUp");

        blockRotation = compound.getInteger("blockRotation");
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    public static class JarRiftConnection {
        public int riftID;
        public double entityX;
        public double entityY;
        public double entityZ;

        public BezierPath bezierPath3D;
        public float fractionUp;
    }
}