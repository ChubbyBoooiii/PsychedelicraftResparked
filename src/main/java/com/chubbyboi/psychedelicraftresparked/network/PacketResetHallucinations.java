package com.chubbyboi.psychedelicraftresparked.network;

import com.chubbyboi.psychedelicraftresparked.client.rendering.HallucinationManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketResetHallucinations implements IMessage {

    public PacketResetHallucinations() {
    }

    @Override
    public void toBytes(io.netty.buffer.ByteBuf buf) {
        // No payload - this is a pure signal.
    }

    @Override
    public void fromBytes(io.netty.buffer.ByteBuf buf) {
        // No payload - this is a pure signal.
    }

    public static class Handler implements IMessageHandler<PacketResetHallucinations, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketResetHallucinations message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(HallucinationManager.getInstance()::reset);
            return null;
        }
    }
}
