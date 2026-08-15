package com.chubbyboi.psychedelicraftresparked.util.compat.jei;

import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.barrel.BarrelGuiHandler;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.barrel.BarrelRecipeCategory;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.barrel.BarrelRecipeMaker;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.distillery.DistilleryRecipeCategory;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.distillery.DistilleryRecipeMaker;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.dryingtable.DryingTableRecipeCategory;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.dryingtable.DryingTableRecipeMaker;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat.VatGuiHandler;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat.VatRecipeCategory;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat.VatRecipeMaker;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.block.BlockPlanks;
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
        registry.addRecipeCategories(new VatRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new DistilleryRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new BarrelRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipes(DryingTableRecipeMaker.getRecipes(), PsychedelicraftRecipeCategoryUid.DRYING_TABLE);
        registry.addRecipeCatalyst(new ItemStack(BlockInit.DRYING_TABLE), PsychedelicraftRecipeCategoryUid.DRYING_TABLE);

        registry.addRecipes(VatRecipeMaker.getRecipes(), PsychedelicraftRecipeCategoryUid.VAT);
        for (BlockPlanks.EnumType woodType : BlockPlanks.EnumType.values()) {
            registry.addRecipeCatalyst(new ItemStack(ItemInit.VAT, 1, woodType.getMetadata()), PsychedelicraftRecipeCategoryUid.VAT);
        }

        registry.addAdvancedGuiHandlers(new VatGuiHandler());

        registry.addRecipes(DistilleryRecipeMaker.getRecipes(), PsychedelicraftRecipeCategoryUid.DISTILLERY);
        registry.addRecipeCatalyst(new ItemStack(BlockInit.DISTILLERY), PsychedelicraftRecipeCategoryUid.DISTILLERY);

        registry.addRecipes(BarrelRecipeMaker.getRecipes(), PsychedelicraftRecipeCategoryUid.BARREL);
        for (BlockPlanks.EnumType woodType : BlockPlanks.EnumType.values()) {
            registry.addRecipeCatalyst(new ItemStack(ItemInit.BARREL_ITEM, 1, woodType.getMetadata()), PsychedelicraftRecipeCategoryUid.BARREL);
        }
        registry.addAdvancedGuiHandlers(new BarrelGuiHandler());
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