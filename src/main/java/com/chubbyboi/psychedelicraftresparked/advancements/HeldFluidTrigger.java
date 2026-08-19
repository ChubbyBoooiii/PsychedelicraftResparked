package com.chubbyboi.psychedelicraftresparked.advancements;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HeldFluidTrigger implements ICriterionTrigger<HeldFluidTrigger.Instance> {

    private final ResourceLocation id;
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

    public HeldFluidTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<Instance> listener) {
        listeners.computeIfAbsent(playerAdvancementsIn, Listeners::new).add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<Instance> listener) {
        Listeners l = listeners.get(playerAdvancementsIn);
        if (l != null) {
            l.remove(listener);
            if (l.isEmpty()) {
                listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
        listeners.remove(playerAdvancementsIn);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        String fluidName = JsonUtils.getString(json, "fluid");
        MinMaxBounds fermentation = json.has("fermentation") ? MinMaxBounds.deserialize(json.get("fermentation")) : MinMaxBounds.UNBOUNDED;
        MinMaxBounds maturation = json.has("maturation") ? MinMaxBounds.deserialize(json.get("maturation")) : MinMaxBounds.UNBOUNDED;
        MinMaxBounds distillation = json.has("distillation") ? MinMaxBounds.deserialize(json.get("distillation")) : MinMaxBounds.UNBOUNDED;
        return new Instance(id, fluidName, fermentation, maturation, distillation);
    }

    public void trigger(EntityPlayerMP player) {
        Listeners l = listeners.get(player.getAdvancements());
        if (l != null) {
            l.trigger(player);
        }
    }

    public static class Instance extends AbstractCriterionInstance {
        private final String fluidName;
        private final MinMaxBounds fermentation;
        private final MinMaxBounds maturation;
        private final MinMaxBounds distillation;

        public Instance(ResourceLocation id, String fluidName, MinMaxBounds fermentation, MinMaxBounds maturation, MinMaxBounds distillation) {
            super(id);
            this.fluidName = fluidName;
            this.fermentation = fermentation;
            this.maturation = maturation;
            this.distillation = distillation;
        }

        public boolean test(EntityPlayerMP player) {
            for (ItemStack stack : player.inventory.mainInventory) {
                if (matches(stack)) {
                    return true;
                }
            }
            for (ItemStack stack : player.inventory.offHandInventory) {
                if (matches(stack)) {
                    return true;
                }
            }
            return false;
        }

        private boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (handler == null) {
                return false;
            }
            FluidStack fluidStack = handler.drain(Integer.MAX_VALUE, false);
            if (fluidStack == null) {
                return false;
            }
            Fluid fluid = fluidStack.getFluid();
            if (fluid == null || !fluid.getName().equals(fluidName)) {
                return false;
            }
            if (!(fluid instanceof FluidAlcohol)) {
                return true;
            }
            FluidAlcohol alcohol = (FluidAlcohol) fluid;
            return fermentation.test(alcohol.getFermentation(fluidStack))
                && maturation.test(alcohol.getMaturation(fluidStack))
                && distillation.test(alcohol.getDistillation(fluidStack));
        }
    }

    private static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<ICriterionTrigger.Listener<Instance>> listeners = Sets.newHashSet();

        Listeners(PlayerAdvancements playerAdvancements) {
            this.playerAdvancements = playerAdvancements;
        }

        boolean isEmpty() {
            return listeners.isEmpty();
        }

        void add(ICriterionTrigger.Listener<Instance> listener) {
            listeners.add(listener);
        }

        void remove(ICriterionTrigger.Listener<Instance> listener) {
            listeners.remove(listener);
        }

        void trigger(EntityPlayerMP player) {
            List<ICriterionTrigger.Listener<Instance>> matched = null;
            for (ICriterionTrigger.Listener<Instance> listener : listeners) {
                if (listener.getCriterionInstance().test(player)) {
                    if (matched == null) {
                        matched = Lists.newArrayList();
                    }
                    matched.add(listener);
                }
            }
            if (matched != null) {
                for (ICriterionTrigger.Listener<Instance> listener : matched) {
                    listener.grantCriterion(playerAdvancements);
                }
            }
        }
    }
}