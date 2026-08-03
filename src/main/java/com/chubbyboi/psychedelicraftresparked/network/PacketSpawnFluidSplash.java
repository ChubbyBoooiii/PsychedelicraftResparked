package com.chubbyboi.psychedelicraftresparked.network;

import com.chubbyboi.psychedelicraftresparked.client.rendering.ParticleFluidBubble;
import com.chubbyboi.psychedelicraftresparked.client.rendering.ParticleFluidSplash;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class PacketSpawnFluidSplash implements IMessage {
    private float x;
    private float y;
    private float z;
    private float motionX;
    private float motionY;
    private float motionZ;
    private float width;
    private float colorR;
    private float colorG;
    private float colorB;

    // Default constructor
    public PacketSpawnFluidSplash() {
    }

    public PacketSpawnFluidSplash(double x, double y, double z, double motionX, double motionY, double motionZ, float width, float colorR, float colorG, float colorB) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        this.motionX = (float) motionX;
        this.motionY = (float) motionY;
        this.motionZ = (float) motionZ;
        this.width = width;
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(motionX);
        buf.writeFloat(motionY);
        buf.writeFloat(motionZ);
        buf.writeFloat(width);
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
        width = buf.readFloat();
        colorR = buf.readFloat();
        colorG = buf.readFloat();
        colorB = buf.readFloat();
    }

    public static class Handler implements IMessageHandler<PacketSpawnFluidSplash, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketSpawnFluidSplash message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handleMessage(message));
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void handleMessage(PacketSpawnFluidSplash message) {
            if (Minecraft.getMinecraft().world == null) {
                return;
            }

            Random rand = new Random();
            int count = (int) (1.0F + message.width * 20.0F);
            TextureAtlasSprite bubbleSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                    .getAtlasSprite(new ResourceLocation("psychedelicraftresparked:particles/fluid_bubble").toString());
            TextureAtlasSprite splashSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                    .getAtlasSprite(new ResourceLocation("psychedelicraftresparked:particles/fluid_splash").toString());

            for (int i = 0; i < count; i++) {
                float ox = (rand.nextFloat() * 2.0F - 1.0F) * message.width;
                float oz = (rand.nextFloat() * 2.0F - 1.0F) * message.width;
                Particle particle = new ParticleFluidBubble(Minecraft.getMinecraft().world,
                        message.x + ox, message.y, message.z + oz,
                        message.motionX, message.motionY - rand.nextFloat() * 0.2F, message.motionZ, bubbleSprite);
                particle.setRBGColorF(message.colorR, message.colorG, message.colorB);
                Minecraft.getMinecraft().effectRenderer.addEffect(particle);
            }
            for (int i = 0; i < count; i++) {
                float ox = (rand.nextFloat() * 2.0F - 1.0F) * message.width;
                float oz = (rand.nextFloat() * 2.0F - 1.0F) * message.width;
                Particle particle = new ParticleFluidSplash(Minecraft.getMinecraft().world,
                        message.x + ox, message.y, message.z + oz,
                        message.motionX, message.motionY, message.motionZ, splashSprite);
                particle.setRBGColorF(message.colorR, message.colorG, message.colorB);
                Minecraft.getMinecraft().effectRenderer.addEffect(particle);
            }
        }
    }
}