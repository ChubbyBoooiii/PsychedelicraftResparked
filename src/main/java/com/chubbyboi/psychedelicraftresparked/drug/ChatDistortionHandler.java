package com.chubbyboi.psychedelicraftresparked.drug;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatDistortionHandler {

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        if (!PSConfig.distortOutgoingMessages) return;

        EntityPlayerMP player = event.getPlayer();
        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (props == null) return;

        float alcohol = props.getDrugStrength("alcohol");
        float zero = props.getDrugStrength("zero");
        float cannabis = props.getDrugStrength("cannabis");
        if (alcohol <= 0.0f && zero <= 0.0f && cannabis <= 0.0f) return;

        String distorted = MessageDistorter.distort(event.getMessage(), player.getRNG(), alcohol, zero, cannabis);
        if (!distorted.equals(event.getMessage())) {
            ITextComponent component = new TextComponentTranslation("chat.type.text", player.getDisplayName(), ForgeHooks.newChatWithLinks(distorted));
            event.setComponent(component);
        }
    }
}