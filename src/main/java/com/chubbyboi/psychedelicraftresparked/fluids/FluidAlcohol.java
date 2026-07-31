package com.chubbyboi.psychedelicraftresparked.fluids;

import com.chubbyboi.psychedelicraftresparked.drug.DrugInfluence;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

// One fluid class per ingredient, not drink.
public class FluidAlcohol extends FluidDrug implements FermentableFluid, DistillableFluid {

    public static final int FERMENTATION_STEPS = 2;
    private static final int MINUTE = 20 * 60;

    private static final String NBT_FERMENTATION = "fermentation";
    private static final String NBT_DISTILLATION = "distillation";
    private static final String NBT_MATURATION = "maturation";
    private static final String NBT_VINEGAR = "isVinegar";

    public static class TickInfo {
        public final int ticksPerFermentation;
        public final int ticksPerDistillation;
        public final int ticksPerMaturation;
        public final int ticksUntilAcetification;

        public TickInfo(int ticksPerFermentation, int ticksPerDistillation, int ticksPerMaturation, int ticksUntilAcetification) {
            this.ticksPerFermentation = ticksPerFermentation;
            this.ticksPerDistillation = ticksPerDistillation;
            this.ticksPerMaturation = ticksPerMaturation;
            this.ticksUntilAcetification = ticksUntilAcetification;
        }

        public static TickInfo ofMinutes(int ferment, int distill, int mature, int acetify) {
            return new TickInfo(ferment * MINUTE, distill * MINUTE, mature * MINUTE, acetify * MINUTE);
        }
    }

    private final double fermentationAlcohol;
    private final double distillationAlcohol;
    private final double maturationAlcohol;
    private final TickInfo tickInfo;

    public FluidAlcohol(String fluidName, ResourceLocation still, ResourceLocation flowing,
                         double fermentationAlcohol, double distillationAlcohol, double maturationAlcohol,
                         TickInfo tickInfo) {
        super(fluidName, still, flowing);
        this.fermentationAlcohol = fermentationAlcohol;
        this.distillationAlcohol = distillationAlcohol;
        this.maturationAlcohol = maturationAlcohol;
        this.tickInfo = tickInfo;
        setDrinkable(true);
    }

    // ==================== NBT-backed processing state ====================

    public int getFermentation(FluidStack stack) {
        return stack.tag != null ? clampInt(stack.tag.getInteger(NBT_FERMENTATION), 0, FERMENTATION_STEPS) : 0;
    }

    public int getDistillation(FluidStack stack) {
        return stack.tag != null ? Math.max(stack.tag.getInteger(NBT_DISTILLATION), 0) : 0;
    }

    public int getMaturation(FluidStack stack) {
        return stack.tag != null ? Math.max(stack.tag.getInteger(NBT_MATURATION), 0) : 0;
    }

    public boolean isVinegar(FluidStack stack) {
        return stack.tag != null && stack.tag.getBoolean(NBT_VINEGAR);
    }

    public void setFermentation(FluidStack stack, int value) {
        tag(stack).setInteger(NBT_FERMENTATION, value);
    }

    public void setDistillation(FluidStack stack, int value) {
        tag(stack).setInteger(NBT_DISTILLATION, value);
    }

    public void setMaturation(FluidStack stack, int value) {
        tag(stack).setInteger(NBT_MATURATION, value);
    }

    public void setVinegar(FluidStack stack, boolean value) {
        tag(stack).setBoolean(NBT_VINEGAR, value);
    }

    private NBTTagCompound tag(FluidStack stack) {
        if (stack.tag == null) {
            stack.tag = new NBTTagCompound();
        }
        return stack.tag;
    }

    private static int clampInt(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }

    // ==================== Fermentation (Mash Tub, open) / Maturation (Barrel, closed) ====================

    @Override
    public int fermentationTime(FluidStack stack, boolean openContainer) {
        if (isVinegar(stack)) return UNFERMENTABLE;

        int fermentation = getFermentation(stack);
        if (fermentation < FERMENTATION_STEPS) {
            return openContainer ? tickInfo.ticksPerFermentation : UNFERMENTABLE;
        }
        return openContainer ? tickInfo.ticksUntilAcetification : tickInfo.ticksPerMaturation;
    }

    @Override
    public void fermentStep(FluidStack stack, boolean openContainer) {
        int fermentation = getFermentation(stack);
        if (openContainer) {
            if (fermentation < FERMENTATION_STEPS) {
                setFermentation(stack, fermentation + 1);
            } else {
                setVinegar(stack, true);
            }
        } else {
            setMaturation(stack, getMaturation(stack) + 1);
        }
    }

    // ==================== Distillation (Distillery) ====================

    @Override
    public int distillationTime(FluidStack stack) {
        if (getFermentation(stack) < FERMENTATION_STEPS) return UNDISTILLABLE;
        if (getMaturation(stack) != 0) return UNDISTILLABLE;
        return tickInfo.ticksPerDistillation;
    }

    @Override
    public FluidStack distillStep(FluidStack stack) {
        if (getFermentation(stack) < FERMENTATION_STEPS) return null;

        int distillation = getDistillation(stack);
        setDistillation(stack, distillation + 1);

        int distilledAmount = (int) Math.floor(stack.amount * (1.0 - 0.5 / (distillation + 1.0)));
        FluidStack slurry = new FluidStack(this, stack.amount - distilledAmount);
        stack.amount = distilledAmount;
        return slurry.amount > 0 ? slurry : null;
    }

    // ==================== Alcohol content -> Alcohol drug dose ====================

    @Override
    public void getDrugInfluences(FluidStack fluidStack, List<DrugInfluence> list) {
        if (isVinegar(fluidStack)) return;

        int fermentation = getFermentation(fluidStack);
        int distillation = getDistillation(fluidStack);
        int maturation = getMaturation(fluidStack);

        double alcohol = ((double) fermentation / FERMENTATION_STEPS) * fermentationAlcohol
                + distillationAlcohol * (1.0 - 1.0 / (1.0 + distillation))
                + maturationAlcohol * (1.0 - 1.0 / (1.0 + maturation * 0.2));

        double scaled = alcohol * fluidStack.amount / FluidHelper.BUCKET_VOLUME;
        list.add(new DrugInfluence("alcohol", 20, 0.003, 0.002, scaled));
    }

    // ==================== Drink identity (name) resolution ====================

    @Override
    public String getLocalizedName(FluidStack stack) {
        String baseName = getUnlocalizedName(stack);

        if (isVinegar(stack)) {
            return I18n.translateToLocalFormatted(baseName + ".vinegar");
        }

        int fermentation = getFermentation(stack);
        int distillation = getDistillation(stack);
        int maturation = getMaturation(stack);

        if (distillation == 0) {
            if (maturation > 0) {
                return I18n.translateToLocalFormatted(baseName + ".mature", maturation);
            } else if (fermentation > 0) {
                return I18n.translateToLocalFormatted(baseName + ".ferment_" + fermentation);
            } else {
                return I18n.translateToLocal(baseName);
            }
        } else {
            if (maturation > 0) {
                return I18n.translateToLocalFormatted(baseName + ".dmature", maturation, distillation);
            } else {
                return I18n.translateToLocalFormatted(baseName + ".distill", distillation);
            }
        }
    }
}