package com.com.chaos.Effects;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = "chaoticscreate")
public final class DelayedCapsuleEffects {

    private static final List<PendingEffect> PENDING = new ArrayList<>();

    private DelayedCapsuleEffects() {}

    public static void queue(Level level, Vec3 pos, ItemStack originalInput) {
        if (level.isClientSide || !(level instanceof ServerLevel server)) {
            return;
        }

        PENDING.add(new PendingEffect(
                server.dimension(),
                pos,
                originalInput.copyWithCount(1),
                2 // 2 here = truly next tick when processed in ServerTickEvent.Post
        ));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<PendingEffect> it = PENDING.iterator();

        while (it.hasNext()) {
            PendingEffect pending = it.next();

            pending.ticksRemaining--;

            if (pending.ticksRemaining > 0) {
                continue;
            }

            ServerLevel level = event.getServer().getLevel(pending.dimension);
            if (level != null) {
                CapsuleReleaseEffects.trigger(level, pending.pos, pending.input);
            }

            it.remove();
        }
    }

    private static final class PendingEffect {
        private final ResourceKey<Level> dimension;
        private final Vec3 pos;
        private final ItemStack input;
        private int ticksRemaining;

        private PendingEffect(ResourceKey<Level> dimension, Vec3 pos, ItemStack input, int ticksRemaining) {
            this.dimension = dimension;
            this.pos = pos;
            this.input = input;
            this.ticksRemaining = ticksRemaining;
        }
    }
}