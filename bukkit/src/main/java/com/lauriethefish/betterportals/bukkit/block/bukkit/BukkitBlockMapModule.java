package com.lauriethefish.betterportals.bukkit.block.bukkit;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.lauriethefish.betterportals.bukkit.block.IMultiBlockChangeManager;
import com.lauriethefish.betterportals.bukkit.block.IBlockMap;

public class BukkitBlockMapModule extends AbstractModule {

    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(IBlockMap.class, BukkitBlockMap.class)
                .build(IBlockMap.Factory.class)
        );

        install(new FactoryModuleBuilder()
            .implement(IMultiBlockChangeManager.class, MultiBlockChangeManager_1_16_2.class)
            .build(IMultiBlockChangeManager.Factory.class)
        );
    }
}
