package com.chubbyboi.psychedelicraftresparked.entities;

import com.chubbyboi.psychedelicraftresparked.fluids.ExplodingFluid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import java.util.List;

public class EntityMolotovCocktail extends EntityThrowable {

    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityMolotovCocktail.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Integer> THROWER_ID = EntityDataManager.createKey(EntityMolotovCocktail.class, DataSerializers.VARINT);

    public EntityMolotovCocktail(World world) {
        super(world);
    }

    public EntityMolotovCocktail(World world, EntityLivingBase thrower) {
        super(world, thrower);
        getDataManager().set(THROWER_ID, thrower.getEntityId());
    }

    public EntityMolotovCocktail(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        getDataManager().register(ITEM, ItemStack.EMPTY);
        getDataManager().register(THROWER_ID, -1);
    }

    private void resolveThrowerClientSide() {
        if (thrower != null || !world.isRemote) {
            return;
        }
        int throwerId = getDataManager().get(THROWER_ID);
        if (throwerId < 0) {
            return;
        }
        Entity resolved = world.getEntityByID(throwerId);
        if (resolved instanceof EntityLivingBase) {
            thrower = (EntityLivingBase) resolved;
        }
    }

    public ItemStack getMolotovStack() {
        return getDataManager().get(ITEM);
    }

    public void setMolotovStack(ItemStack stack) {
        getDataManager().set(ITEM, stack);
        getDataManager().setDirty(ITEM);
    }

    @Nullable
    private FluidStack getExplodingFluid() {
        ItemStack stack = getMolotovStack();
        if (stack.isEmpty()) {
            return null;
        }
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) {
            return null;
        }
        FluidStack fluidStack = handler.drain(Integer.MAX_VALUE, false);
        return fluidStack != null && fluidStack.getFluid() instanceof ExplodingFluid ? fluidStack : null;
    }

    @Override
    public void onUpdate() {
        resolveThrowerClientSide();

        super.onUpdate();

        if (getFireStrength() > 0) {
            world.spawnParticle(EnumParticleTypes.FLAME, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
            world.spawnParticle(EnumParticleTypes.FLAME, posX + motionX / 2, posY + motionY / 2, posZ + motionZ / 2, 0.0D, 0.0D, 0.0D);
        }
    }

    private float getFireStrength() {
        FluidStack fluidStack = getExplodingFluid();
        return fluidStack == null ? 0.0F : ((ExplodingFluid) fluidStack.getFluid()).fireStrength(fluidStack);
    }

    private float getExplosionStrength() {
        FluidStack fluidStack = getExplodingFluid();
        return fluidStack == null ? 0.0F : ((ExplodingFluid) fluidStack.getFluid()).explosionStrength(fluidStack);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        float fireStrength = getFireStrength();
        float explosionStrength = getExplosionStrength();

        if (!world.isRemote && result.entityHit != null) {
            damageEntity(result.entityHit, 1.0F, fireStrength, explosionStrength);

            if (explosionStrength > 0) {
                AxisAlignedBB splashBox = getEntityBoundingBox().grow(explosionStrength);
                List<Entity> nearby = world.getEntitiesWithinAABBExcludingEntity(this, splashBox);
                for (Entity entity : nearby) {
                    if (entity == result.entityHit) {
                        continue;
                    }
                    double distance = entity.getDistance(posX, posY, posZ);
                    if (distance <= explosionStrength) {
                        damageEntity(entity, 1.0F - (float) (distance / explosionStrength), fireStrength, explosionStrength);
                    }
                }
            }
        }

        world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.NEUTRAL, 1.0F, 1.0F);

        if (!world.isRemote) {
            if (explosionStrength > 0 || fireStrength > 0) {
                world.newExplosion(this, posX, posY, posZ, explosionStrength, false, false);
                spreadFire(fireStrength);
            } else {
                world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
        }

        setDead();
    }

    private void spreadFire(float fireStrength) {
        if (fireStrength <= 0) {
            return;
        }

        int range = MathHelper.ceil(fireStrength);
        BlockPos center = new BlockPos(posX, posY, posZ);

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (x * x + y * y + z * z > fireStrength * fireStrength) {
                        continue;
                    }
                    BlockPos pos = center.add(x, y, z);
                    if (rand.nextInt(2) == 0
                        && world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                        && Blocks.FIRE.canPlaceBlockAt(world, pos)) {
                        world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
                    }
                }
            }
        }
    }

    private void damageEntity(Entity entity, float percentageScale, float fireStrength, float explosionStrength) {
        entity.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()),
            percentageScale * Math.max(4.0F, explosionStrength * 0.6F + fireStrength * 0.3F));

        if (fireStrength > 0) {
            int fireTicks = (int) Math.max(10, 3 * fireStrength);
            entity.setFire(Math.max(1, fireTicks / 20));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        ItemStack stack = getMolotovStack();
        if (!stack.isEmpty()) {
            compound.setTag("MolotovStack", stack.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("MolotovStack", Constants.NBT.TAG_COMPOUND)) {
            setMolotovStack(new ItemStack(compound.getCompoundTag("MolotovStack")));
        }
    }
}