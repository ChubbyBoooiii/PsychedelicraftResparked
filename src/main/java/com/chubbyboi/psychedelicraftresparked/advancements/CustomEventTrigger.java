package com.chubbyboi.psychedelicraftresparked.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomEventTrigger implements ICriterionTrigger<CustomEventTrigger.Instance> {

    private final ResourceLocation id;
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

    public CustomEventTrigger(ResourceLocation id) {
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
        String event = JsonUtils.getString(json, "event");
        EntityPredicate entity = json.has("entity") ? EntityPredicate.deserialize(json.get("entity")) : EntityPredicate.ANY;
        return new Instance(id, event, entity);
    }

    public void trigger(EntityPlayerMP player, String event) {
        trigger(player, event, null);
    }

    public void trigger(EntityPlayerMP player, String event, @Nullable Entity target) {
        Listeners l = listeners.get(player.getAdvancements());
        if (l != null) {
            l.trigger(player, event, target);
        }
    }

    public static class Instance extends AbstractCriterionInstance {
        private final String event;
        private final EntityPredicate entity;

        public Instance(ResourceLocation id, String event, EntityPredicate entity) {
            super(id);
            this.event = event;
            this.entity = entity;
        }

        public boolean test(EntityPlayerMP player, String event, @Nullable Entity target) {
            return this.event.equals(event) && entity.test(player, target);
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

        void trigger(EntityPlayerMP player, String event, @Nullable Entity target) {
            List<ICriterionTrigger.Listener<Instance>> matched = null;
            for (ICriterionTrigger.Listener<Instance> listener : listeners) {
                if (listener.getCriterionInstance().test(player, event, target)) {
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