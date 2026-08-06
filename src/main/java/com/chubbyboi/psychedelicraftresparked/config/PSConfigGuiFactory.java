package com.chubbyboi.psychedelicraftresparked.config;

import com.chubbyboi.psychedelicraftresparked.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;

public class PSConfigGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new ConfigGui(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    public static class ConfigGui extends GuiConfig {
        public ConfigGui(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(), Tags.MOD_ID, false, false, Tags.MOD_NAME + " Config");
        }

        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> list = new ArrayList<>();
            list.add(new DummyCategoryElement("client", "psychedelicraftresparked.configgui.ctgy.client",
                new ConfigElement(PSConfig.getConfig().getCategory(PSConfig.CATEGORY_CLIENT)).getChildElements()));
            list.add(new DummyCategoryElement("server", "psychedelicraftresparked.configgui.ctgy.server",
                new ConfigElement(PSConfig.getConfig().getCategory(PSConfig.CATEGORY_SERVER)).getChildElements()));
            return list;
        }
    }
}
