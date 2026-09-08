package com.com.chaos.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataGenerators {

    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();

        generator.addProvider(event.includeServer(),
                new ModMatterValueProvider(packOutput));
        generator.addProvider(event.includeServer(),
                new CreateCompatMatterValueProvider(packOutput));
    }
}