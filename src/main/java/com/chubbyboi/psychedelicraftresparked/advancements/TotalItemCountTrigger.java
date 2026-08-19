package com.chubbyboi.psychedelicraftresparked.advancements;

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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TotalItemCountTrigger implements ICriterionTrigger<TotalItemCountTrigger.Instance> {

    private final ResourceLocation id;
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

    public TotalItemCountTrigger(ResourceLocation id) {
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
        ResourceLocation itemId = new ResourceLocation(JsonUtils.getString(json, "item"));
        Item item = Item.REGISTRY.getObject(itemId);
        if (item == null) {
            throw new com.google.gson.JsonSyntaxException("Unknown item id '" + itemId + "'");
        }
        MinMaxBounds count = json.has("count") ? MinMaxBounds.deserialize(json.get("count")) : MinMaxBounds.UNBOUNDED;
        return new Instance(id, item, count);
    }

    public void trigger(EntityPlayerMP player) {
        Listeners l = listeners.get(player.getAdvancements());
        if (l != null) {
            l.trigger(player);
        }
    }

    public static class Instance extends AbstractCriterionInstance {
        private final Item item;
        private final MinMaxBounds count;

        public Instance(ResourceLocation id, Item item, MinMaxBounds count) {
            super(id);
            this.item = item;
            this.count = count;
        }

        public boolean test(EntityPlayerMP player) {
            int total = 0;
            for (ItemStack stack : player.inventory.mainInventory) {
                if (stack.getItem() == item) {
                    total += stack.getCount();
                }
            }
            for (ItemStack stack : player.inventory.offHandInventory) {
                if (stack.getItem() == item) {
                    total += stack.getCount();
                }
            }
            return count.test(total);
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