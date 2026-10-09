package com.chubbyboi.psychedelicraftresparked.util.compat.universalmodcore;

import zone.rong.mixinbooter.Context;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

public class UniversalModCoreMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.psychedelicraftresparked.umc.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(Context context) {
        return context.isModPresent("universalmodcore");
    }
}