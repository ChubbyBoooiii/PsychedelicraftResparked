package com.chubbyboi.psychedelicraftresparked.network;

import com.chubbyboi.psychedelicraftresparked.client.rendering.SmokeMonsterSpawner;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSpawnSmokeMonster implements IMessage {
    private float x;
    private float y;
    private float z;
    private float motionX;
    private float motionY;
    private float motionZ;
    private float yaw;
    private int maxTicks;
    private byte creatureIndex;
    private float colorR;
    private float colorG;
    private float colorB;

    // DEfault Constructor
    public PacketSpawnSmokeMonster() {
    }

    public PacketSpawnSmokeMonster(double x, double y, double z, double motionX, double motionY, double motionZ, float yaw, int maxTicks, int creatureIndex, float[] color) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        this.motionX = (float) motionX;
        this.motionY = (float) motionY;
        this.motionZ = (float) motionZ;
        this.yaw = yaw;
        this.maxTicks = maxTicks;
        this.creatureIndex = (byte) creatureIndex;
        this.colorR = color[0];
        this.colorG = color[1];
        this.colorB = color[2];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(motionX);
        buf.writeFloat(motionY);
        buf.writeFloat(motionZ);
        buf.writeFloat(yaw);
        buf.writeInt(maxTicks);
        buf.writeByte(creatureIndex);
        buf.writeFloat(colorR);
        buf.writeFloat(colorG);
        buf.writeFloat(colorB);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readFloat();
        y = buf.readFloat();
        z = buf.readFloat();
        motionX = buf.readFloat();
        motionY = buf.readFloat();
        motionZ = buf.readFloat();
        yaw = buf.readFloat();
        maxTicks = buf.readInt();
        creatureIndex = buf.readByte();
        colorR = buf.readFloat();
        colorG = buf.readFloat();
        colorB = buf.readFloat();
    }

    public static class Handler implements IMessageHandler<PacketSpawnSmokeMonster, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketSpawnSmokeMonster message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handleMessage(message));
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void handleMessage(PacketSpawnSmokeMonster message) {
            World world = Minecraft.getMinecraft().world;
            if (world == null) return;

            SmokeMonsterSpawner.getInstance().spawn(world, message.x, message.y, message.z, message.motionX, message.motionY, message.motionZ, message.yaw, message.maxTicks, message.creatureIndex, message.colorR, message.colorG, message.colorB);
        }
    }
}
