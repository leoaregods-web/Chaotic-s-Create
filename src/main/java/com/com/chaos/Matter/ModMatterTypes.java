package com.com.chaos.Matter;

import com.buuz135.replication.ReplicationRegistry;
import com.buuz135.replication.api.IMatterType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.com.chaos.ChaoticsCreate.MODID;

public class ModMatterTypes {

    public static final DeferredRegister<IMatterType> MATTER_TYPES =
            DeferredRegister.create(ReplicationRegistry.MATTER_TYPES_KEY, MODID);

    public static final DeferredHolder<IMatterType, IMatterType> GASEOUS =
            MATTER_TYPES.register("gaseous", () -> new IMatterType() {
                private final Supplier<float[]> color = () -> new float[]{0.75f, 0.9f, 0.9f, 0.55f};

                @Override
                public String getName() {
                    return "gaseous";
                }

                @Override
                public Supplier<float[]> getColor() {
                    return color;
                }

                @Override
                public int getMax() {
                    return 1000;
                }
            });

    public static void register(IEventBus modBus) {
        MATTER_TYPES.register(modBus);
    }
}