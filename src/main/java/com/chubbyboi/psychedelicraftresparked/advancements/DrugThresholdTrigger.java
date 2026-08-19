package com.chubbyboi.psychedelicraftresparked.advancements;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DrugThresholdTrigger implements ICriterionTrigger<DrugThresholdTrigger.Instance> {

    private final ResourceLocation id;
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

    public DrugThresholdTrigger(ResourceLocation id) {
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
        JsonArray array = JsonUtils.getJsonArray(json, "drugs");
        List<DrugPredicate> predicates = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject entry = JsonUtils.getJsonObject(array.get(i), "drugs[" + i + "]");
            String drug = JsonUtils.getString(entry, "drug");
            MinMaxBounds value = entry.has("value") ? MinMaxBounds.deserialize(entry.get("value")) : MinMaxBounds.UNBOUNDED;
            predicates.add(new DrugPredicate(drug, value));
        }
        return new Instance(id, predicates);
    }

    public void trigger(EntityPlayerMP player) {
        Listeners l = listeners.get(player.getAdvancements());
        if (l != null) {
            l.trigger(player);
        }
    }

    private static class DrugPredicate {
        final String drug;
        final MinMaxBounds value;

        DrugPredicate(String drug, MinMaxBounds value) {
            this.drug = drug;
            this.value = value;
        }

        boolean test(DrugProperties properties) {
            return value.test(properties.getDrugStrength(drug));
        }
    }

    public static class Instance extends AbstractCriterionInstance {
        private final List<DrugPredicate> drugs;

        private Instance(ResourceLocation id, List<DrugPredicate> drugs) {
            super(id);
            this.drugs = drugs;
        }

        public boolean test(EntityPlayerMP player) {
            IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
            if (!(props instanceof DrugProperties)) {
                return false;
            }
            DrugProperties properties = (DrugProperties) props;
            for (DrugPredicate predicate : drugs) {
                if (!predicate.test(properties)) {
                    return false;
                }
            }
            return true;
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