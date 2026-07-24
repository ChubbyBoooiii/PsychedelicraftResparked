package com.chubbyboi.psychedelicraftresparked.capabilities;

import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityRegistry {
    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(
                IDrugProperties.class,           // The capability interface
                new DrugPropertiesStorage(),     // The storage handler (NBT serialization)
                DrugProperties::new              // Factory method to create new instances
        );
    }
}