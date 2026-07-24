package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.network.NetworkHandler;
import com.chubbyboi.psychedelicraftresparked.network.PacketSpawnSmokeMonster;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class ItemPipeOfSmokeMonsters extends ItemSmokingTool {

    private static final int DURATION_TICKS = 60; // Longer than normal pipe

    public ItemPipeOfSmokeMonsters(String name, int maxDamage, int useDuration) {
        super(name, maxDamage, useDuration);
    }

    @Override
    protected int getBreathingSmokeDuration(World world) {
        return DURATION_TICKS;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        Consumable usedConsumable = entityLiving instanceof EntityPlayer ? getUsedConsumable((EntityPlayer) entityLiving) : null;
        ItemStack result = super.onItemUseFinish(stack, worldIn, entityLiving);

        if (worldIn.isRemote || usedConsumable == null || !(entityLiving instanceof EntityPlayer)) {
            return result;
        }
        EntityPlayer player = (EntityPlayer) entityLiving;

        Vec3d look = player.getLookVec();
        double x = player.posX + look.x * 0.5;
        double y = player.posY + player.getEyeHeight() - 0.2;
        double z = player.posZ + look.z * 0.5;

        double velX = look.x * 0.02;
        double velY = 0.01;
        double velZ = look.z * 0.02;

        // Random creature each use
        int creatureIndex = worldIn.rand.nextInt(128);
        PacketSpawnSmokeMonster packet = new PacketSpawnSmokeMonster( x, y, z, velX, velY, velZ, player.rotationYaw, DURATION_TICKS, creatureIndex, usedConsumable.smokeColor);

        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 64.0);
        NetworkHandler.INSTANCE.sendToAllAround(packet, point);

        return result;
    }
}
