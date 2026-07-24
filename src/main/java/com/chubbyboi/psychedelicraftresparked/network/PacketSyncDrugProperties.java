package com.chubbyboi.psychedelicraftresparked.network;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.drug.IDrug;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PacketSyncDrugProperties implements IMessage {

    private Map<String, Float> drugNearValues = new HashMap<>();
    private Map<String, NBTTagCompound> drugExtraData = new HashMap<>();

    // Default Constructor
    public PacketSyncDrugProperties() {
    }

    public PacketSyncDrugProperties(IDrugProperties props) {
        if (props instanceof DrugProperties) {
            DrugProperties drugProps = (DrugProperties) props;

            // Send activeValue as nearValue for client interpolation
            for (IDrug drug : drugProps.getAllDrugs()) {
                if (drug.getActiveValue() > 0.001f) {
                    drugNearValues.put(drug.getName(), drug.getActiveValue());

                    NBTTagCompound extra = new NBTTagCompound();
                    drug.writeSyncExtraNBT(extra);
                    if (!extra.isEmpty()) {
                        drugExtraData.put(drug.getName(), extra);
                    }
                }
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(drugNearValues.size());

        for (Map.Entry<String, Float> entry : drugNearValues.entrySet()) {
            String drugType = entry.getKey();
            float nearValue = entry.getValue();

            byte[] drugTypeBytes = drugType.getBytes(StandardCharsets.UTF_8);
            buf.writeInt(drugTypeBytes.length);
            buf.writeBytes(drugTypeBytes);
            buf.writeFloat(nearValue);
        }

        buf.writeInt(drugExtraData.size());
        for (Map.Entry<String, NBTTagCompound> entry : drugExtraData.entrySet()) {
            byte[] drugTypeBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            buf.writeInt(drugTypeBytes.length);
            buf.writeBytes(drugTypeBytes);
            ByteBufUtils.writeTag(buf, entry.getValue());
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        drugNearValues.clear();
        drugExtraData.clear();

        int count = buf.readInt();

        for (int i = 0; i < count; i++) {
            int stringLength = buf.readInt();
            byte[] drugTypeBytes = new byte[stringLength];
            buf.readBytes(drugTypeBytes);
            String drugType = new String(drugTypeBytes, StandardCharsets.UTF_8);

            float nearValue = buf.readFloat();

            drugNearValues.put(drugType, nearValue);
        }

        int extraCount = buf.readInt();
        for (int i = 0; i < extraCount; i++) {
            int stringLength = buf.readInt();
            byte[] drugTypeBytes = new byte[stringLength];
            buf.readBytes(drugTypeBytes);
            String drugType = new String(drugTypeBytes, StandardCharsets.UTF_8);

            drugExtraData.put(drugType, ByteBufUtils.readTag(buf));
        }
    }

    public static class Handler implements IMessageHandler<PacketSyncDrugProperties, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketSyncDrugProperties message, MessageContext ctx) {
            // Schedule on main thread for safe world/entity access
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EntityPlayer player = Minecraft.getMinecraft().player;
                if (player == null) return;

                IDrugProperties props = player.getCapability(
                    DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY,
                    null
                );

                if (!(props instanceof DrugProperties)) return;
                DrugProperties drugProps = (DrugProperties) props;

                // Clear all drugs first
                for (IDrug drug : drugProps.getAllDrugs()) {
                    drug.setNearValue(0.0f);
                }

                // Set nearValues from packet
                for (Map.Entry<String, Float> entry : message.drugNearValues.entrySet()) {
                    IDrug drug = drugProps.getDrug(entry.getKey());
                    if (drug != null) {
                        drug.setNearValue(entry.getValue());
                    }
                }

                // Apply any drug-specific extra state (e.g. Harmonium's currentColor)
                for (Map.Entry<String, NBTTagCompound> entry : message.drugExtraData.entrySet()) {
                    IDrug drug = drugProps.getDrug(entry.getKey());
                    if (drug != null) {
                        drug.readSyncExtraNBT(entry.getValue());
                    }
                }
            });
            return null;
        }
    }
}
