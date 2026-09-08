package com.com.chaos.Integration.Apoth;

import net.neoforged.fml.ModList;

public class ApothCompat {
    public static final String MODID = "apotheosis";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MODID);
    }
}