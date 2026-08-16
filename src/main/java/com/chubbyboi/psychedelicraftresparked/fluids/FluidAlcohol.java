package com.chubbyboi.psychedelicraftresparked.fluids;

import com.chubbyboi.psychedelicraftresparked.drug.DrugInfluence;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

// One fluid class per ingredient, not drink.
public class FluidAlcohol extends FluidDrug implements FermentableFluid, DistillableFluid, UntintedFluid, ExplodingFluid {

    public static final int FERMENTATION_STEPS = 2;
    private static final float FIRE_STRENGTH_MULTIPLIER = 2.0f;
    private static final float EXPLOSION_STRENGTH_MULTIPLIER = 0.6f;
    private static final int SECOND = 20;
    private static final int MINUTE = SECOND * 60;

    private static final String NBT_FERMENTATION = "fermentation";
    private static final String NBT_DISTILLATION = "distillation";
    private static final String NBT_MATURATION = "maturation";
    private static final String NBT_VINEGAR = "isVinegar";

    public static class TickInfo {
        public int ticksPerFermentation;
        public int ticksPerDistillation;
        public int ticksPerMaturation;
        public int ticksUntilAcetification;

        public TickInfo(int ticksPerFermentation, int ticksPerDistillation, int ticksPerMaturation, int ticksUntilAcetification) {
            this.ticksPerFermentation = ticksPerFermentation;
            this.ticksPerDistillation = ticksPerDistillation;
            this.ticksPerMaturation = ticksPerMaturation;
            this.ticksUntilAcetification = ticksUntilAcetification;
        }

        public static TickInfo ofMinutes(int ferment, int distill, int mature, int acetify) {
            return new TickInfo(ferment * MINUTE, distill * MINUTE, mature * MINUTE, acetify * MINUTE);
        }

        public static TickInfo ofSeconds(int ferment, int distill, int mature, int acetify) {
            return new TickInfo(ferment * SECOND, distill * SECOND, mature * SECOND, acetify * SECOND);
        }
    }

    private final double fermentationAlcohol;
    private final double distillationAlcohol;
    private final double maturationAlcohol;
    private final TickInfo tickInfo;

    private int matureColor = 0xcc592518;
    private int distilledColor = 0x33ffffff;

    private final List<IconRange> iconRanges = new ArrayList<>();

    private static class IconRange {
        final int maturationMin, maturationMax, distillationMin, distillationMax;
        final ResourceLocation still, flowing;

        IconRange(int maturationMin, int maturationMax, int distillationMin, int distillationMax, ResourceLocation still, ResourceLocation flowing) {
            this.maturationMin = maturationMin;
            this.maturationMax = maturationMax;
            this.distillationMin = distillationMin;
            this.distillationMax = distillationMax;
            this.still = still;
            this.flowing = flowing;
        }

        boolean matches(int maturation, int distillation) {
            return inRange(maturation, maturationMin, maturationMax) && inRange(distillation, distillationMin, distillationMax);
        }

        private static boolean inRange(int value, int min, int max) {
            return (min < 0 || value >= min) && (max < 0 || value <= max);
        }
    }

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

    public TickInfo getTickInfo() {
        return tickInfo;
    }

    public FluidAlcohol addIcon(int maturationMin, int maturationMax, int distillationMin, int distillationMax, ResourceLocation still, ResourceLocation flowing) {
        iconRanges.add(new IconRange(maturationMin, maturationMax, distillationMin, distillationMax, still, flowing));
        return this;
    }

    @Override
    public ResourceLocation getStill(FluidStack stack) {
        IconRange range = findIconRange(stack);
        return range != null ? range.still : getStill();
    }

    @Override
    public ResourceLocation getFlowing(FluidStack stack) {
        IconRange range = findIconRange(stack);
        return range != null ? range.flowing : getFlowing();
    }

    private IconRange findIconRange(FluidStack stack) {
        int maturation = getMaturation(stack);
        int distillation = getDistillation(stack);
        for (IconRange range : iconRanges) {
            if (range.matches(maturation, distillation)) {
                return range;
            }
        }
        return null;
    }

    public FluidAlcohol setMatureColor(int matureColor) {
        this.matureColor = matureColor;
        return this;
    }

    public FluidAlcohol setDistilledColor(int distilledColor) {
        this.distilledColor = distilledColor;
        return this;
    }

    @Override
    public int getColor(FluidStack stack) {
        return 0xFFFFFFFF;
    }

    public int getFlatTintColor(FluidStack stack) {
        int distillation = getDistillation(stack);
        int maturation = getMaturation(stack);

        int baseFluidColor = mixColors(getColor(), distilledColor, (float) (1.0 - 1.0 / (1.0 + distillation)));
        return mixColors(baseFluidColor, matureColor, (float) (1.0 - 1.0 / (1.0 + maturation * 0.2)));
    }

    private static int mixColors(int left, int right, float progress) {
        float alphaL = (left >> 24 & 255) / 255.0F;
        float redL = (left >> 16 & 255) / 255.0F;
        float greenL = (left >> 8 & 255) / 255.0F;
        float blueL = (left & 255) / 255.0F;

        float alphaR = (right >> 24 & 255) / 255.0F;
        float redR = (right >> 16 & 255) / 255.0F;
        float greenR = (right >> 8 & 255) / 255.0F;
        float blueR = (right & 255) / 255.0F;

        float alpha = alphaL * (1.0F - progress) + alphaR * progress;
        float red = redL * (1.0F - progress) + redR * progress;
        float green = greenL * (1.0F - progress) + greenR * progress;
        float blue = blueL * (1.0F - progress) + blueR * progress;

        return (Math.round(alpha * 255.0F) << 24)
            | (Math.round(red * 255.0F) << 16)
            | (Math.round(green * 255.0F) << 8)
            | Math.round(blue * 255.0F);
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

    // ==================== Fermentation (Vat, open) / Maturation (Barrel, closed) ====================

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
    public ItemStack fermentStep(FluidStack stack, boolean openContainer) {
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
        return null;
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
        FluidStack slurry = new FluidStack(FluidInit.SLURRY, stack.amount - distilledAmount);
        stack.amount = distilledAmount;
        return slurry.amount > 0 ? slurry : null;
    }

    // ==================== Alcohol content -> Alcohol drug dose / Molotov potency ====================

    /** Alcohol content per liter (unscaled by volume) - reflects fermentation/distillation/maturation stage. */
    public double getAlcoholContent(FluidStack fluidStack) {
        if (isVinegar(fluidStack)) return 0.0;

        int fermentation = getFermentation(fluidStack);
        int distillation = getDistillation(fluidStack);
        int maturation = getMaturation(fluidStack);

        return ((double) fermentation / FERMENTATION_STEPS) * fermentationAlcohol
                + distillationAlcohol * (1.0 - 1.0 / (1.0 + distillation))
                + maturationAlcohol * (1.0 - 1.0 / (1.0 + maturation * 0.2));
    }

    @Override
    public void getDrugInfluences(FluidStack fluidStack, List<DrugInfluence> list) {
        if (isVinegar(fluidStack)) return;

        double scaled = getAlcoholContent(fluidStack) * fluidStack.amount / FluidHelper.BUCKET_VOLUME;
        list.add(new DrugInfluence("alcohol", 20, 0.003, 0.002, scaled));
    }

    private double getClampedAlcoholDose(FluidStack fluidStack) {
        List<DrugInfluence> influences = new ArrayList<>();
        getDrugInfluences(fluidStack, influences);

        double alcohol = 0.0;
        for (DrugInfluence influence : influences) {
            if (influence.getDrugName().equals("alcohol")) {
                alcohol += influence.getMaxInfluence();
            }
        }
        return Math.max(0.0, Math.min(1.0, alcohol));
    }

    @Override
    public float fireStrength(FluidStack fluidStack) {
        return (float) (getClampedAlcoholDose(fluidStack) * fluidStack.amount / FluidHelper.BUCKET_VOLUME) * FIRE_STRENGTH_MULTIPLIER;
    }

    @Override
    public float explosionStrength(FluidStack fluidStack) {
        return (float) (getClampedAlcoholDose(fluidStack) * fluidStack.amount / FluidHelper.BUCKET_VOLUME) * EXPLOSION_STRENGTH_MULTIPLIER;
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