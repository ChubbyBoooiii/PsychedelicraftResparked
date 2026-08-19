package com.chubbyboi.psychedelicraftresparked.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class EntityRayTrace {

    public static EntityLivingBase rayTraceLivingEntity(EntityLivingBase source, double range) {
        Vec3d eyePos = source.getPositionEyes(1.0F);
        Vec3d look = source.getLook(1.0F);
        Vec3d endPos = eyePos.add(look.x * range, look.y * range, look.z * range);

        AxisAlignedBB searchBox = source.getEntityBoundingBox()
                .expand(look.x * range, look.y * range, look.z * range)
                .grow(1.0D, 1.0D, 1.0D);

        List<Entity> candidates = source.world.getEntitiesInAABBexcluding(source, searchBox,
                candidate -> candidate instanceof EntityLivingBase && candidate.canBeCollidedWith());

        EntityLivingBase closest = null;
        double closestDistanceSq = range * range;

        for (Entity candidate : candidates) {
            AxisAlignedBB candidateBox = candidate.getEntityBoundingBox().grow(candidate.getCollisionBorderSize());
            RayTraceResult hit = candidateBox.calculateIntercept(eyePos, endPos);

            if (hit != null) {
                double distanceSq = eyePos.squareDistanceTo(hit.hitVec);
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closest = (EntityLivingBase) candidate;
                }
            }
        }

        return closest;
    }
}