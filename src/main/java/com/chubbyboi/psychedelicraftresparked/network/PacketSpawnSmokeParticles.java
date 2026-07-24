package com.chubbyboi.psychedelicraftresparked.network;

import com.chubbyboi.psychedelicraftresparked.client.rendering.ParticleDrugSmoke;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSpawnSmokeParticles implements IMessage {
    private float x;
    private float y;
    private float z;
    private float motionX;
    private float motionY;
    private float motionZ;
    private float size;
    private float colorR;
    private float colorG;
    private float colorB;

    // Default Constructor
    public PacketSpawnSmokeParticles() {
    }

    public PacketSpawnSmokeParticles(double x, double y, double z, double motionX, double motionY, double motionZ, float size, float[] color) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        this.motionX = (float) motionX;
        this.motionY = (float) motionY;
        this.motionZ = (float) motionZ;
        this.size = size;
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
        buf.writeFloat(size);
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
        size = buf.readFloat();
        colorR = buf.readFloat();
        colorG = buf.readFloat();
        colorB = buf.readFloat();
    }

    public static class Handler implements IMessageHandler<PacketSpawnSmokeParticles, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketSpawnSmokeParticles message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handleMessage(message));
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void handleMessage(PacketSpawnSmokeParticles message) {
            World world = Minecraft.getMinecraft().world;
            if (world == null) return;

            Particle particle = new ParticleDrugSmoke(world, message.x, message.y, message.z, message.motionX, message.motionY, message.motionZ, message.size);
            particle.setRBGColorF(message.colorR, message.colorG, message.colorB);
            particle.setAlphaF(0.6f);
            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
        }
    }
}
