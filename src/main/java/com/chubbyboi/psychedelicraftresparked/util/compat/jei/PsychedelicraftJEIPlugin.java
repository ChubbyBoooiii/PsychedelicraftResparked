package com.chubbyboi.psychedelicraftresparked.util.compat.jei;

import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.dryingtable.DryingTableRecipeCategory;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.dryingtable.DryingTableRecipeMaker;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.util.Collections;

@JEIPlugin
public class PsychedelicraftJEIPlugin implements IModPlugin {

    @Nullable
    private static IJeiRuntime runtime;

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new DryingTableRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipes(DryingTableRecipeMaker.getRecipes(), PsychedelicraftRecipeCategoryUid.DRYING_TABLE);
        registry.addRecipeCatalyst(new ItemStack(BlockInit.DRYING_TABLE), PsychedelicraftRecipeCategoryUid.DRYING_TABLE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    public static boolean tryOpenRecipes(int mouseX, int mouseY, int areaX, int areaY, int areaWidth, int areaHeight, String categoryUid) {
        if (!Loader.isModLoaded("jei")) {
            return false;
        }
        if (mouseX < areaX || mouseX >= areaX + areaWidth || mouseY < areaY || mouseY >= areaY + areaHeight) {
            return false;
        }
        if (runtime != null) {
            runtime.getRecipesGui().showCategories(Collections.singletonList(categoryUid));
        }
        return true;
    }
}