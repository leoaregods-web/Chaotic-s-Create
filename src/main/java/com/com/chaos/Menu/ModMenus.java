package com.com.chaos.Menu;

import com.com.chaos.ChaoticsCreate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ChaoticsCreate.MODID);

    public static final Supplier<MenuType<AtmosphereLiquifierMenu>> ATMOSPHERE_LIQUIFIER_MENU =
            MENUS.register("atmosphere_liquifier", () -> new MenuType<>(AtmosphereLiquifierMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
