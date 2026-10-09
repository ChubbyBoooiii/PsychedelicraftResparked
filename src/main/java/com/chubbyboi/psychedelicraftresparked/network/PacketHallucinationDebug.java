package com.chubbyboi.psychedelicraftresparked.network;

import com.chubbyboi.psychedelicraftresparked.client.rendering.HallucinationManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketHallucinationDebug implements IMessage {

    public enum Action {
        SET, CLEAR, CLEAR_ALL, STATUS, MIND_COLOR, MIND_COLOR_CLEAR
    }

    private Action action;
    private String type;
    private float x, y, z;

    public PacketHallucinationDebug() {
    }

    public PacketHallucinationDebug(Action action, String type, float x, float y, float z) {
        this.action = action;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(action.ordinal());
        ByteBufUtils.writeUTF8String(buf, type);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = Action.values()[buf.readByte()];
        type = ByteBufUtils.readUTF8String(buf);
        x = buf.readFloat();
        y = buf.readFloat();
        z = buf.readFloat();
    }

    public static class Handler implements IMessageHandler<PacketHallucinationDebug, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketHallucinationDebug message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> apply(message));
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void apply(PacketHallucinationDebug message) {
            HallucinationManager manager = HallucinationManager.getInstance();

            switch (message.action) {
                case SET:
                    manager.setDebugOverride(HallucinationManager.HallucinationType.valueOf(message.type), message.x);
                    break;
                case CLEAR:
                    manager.clearDebugOverride(HallucinationManager.HallucinationType.valueOf(message.type));
                    break;
                case CLEAR_ALL:
                    manager.clearAllDebugOverrides();
                    break;
                case MIND_COLOR:
                    manager.setDebugMindColor(message.x, message.y, message.z);
                    break;
                case MIND_COLOR_CLEAR:
                    manager.clearDebugMindColor();
                    break;
                case STATUS:
                    printStatus(manager);
                    break;
            }
        }

        @SideOnly(Side.CLIENT)
        private static void printStatus(HallucinationManager manager) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.player == null) {
                return;
            }

            for (HallucinationManager.Pool pool : HallucinationManager.Pool.values()) {
                mc.player.sendMessage(new TextComponentString(TextFormatting.AQUA + pool.name() + " pool: " + TextFormatting.WHITE + String.format("%.3f", manager.getPoolValue(pool))));
            }

            float[] mindColor = manager.getCurrentMindColor();
            mc.player.sendMessage(new TextComponentString(TextFormatting.AQUA + "mind colour: " + TextFormatting.WHITE + String.format("(%.3f, %.3f, %.3f)", mindColor[0], mindColor[1], mindColor[2]) + (manager.isMindColorForced() ? TextFormatting.LIGHT_PURPLE + " [forced]" : "")));

            for (HallucinationManager.HallucinationType type : HallucinationManager.HallucinationType.values()) {
                boolean active = manager.isActive(type);
                boolean forced = manager.isDebugForced(type);
                TextFormatting color = forced ? TextFormatting.LIGHT_PURPLE : (active ? TextFormatting.YELLOW : TextFormatting.GRAY);

                mc.player.sendMessage(new TextComponentString(color + "  " + type.name() + ": " + String.format("%.3f", manager.getEffectValue(type)) + (active ? " [active]" : "") + (forced ? " [forced]" : "")));
            }
        }
    }
}