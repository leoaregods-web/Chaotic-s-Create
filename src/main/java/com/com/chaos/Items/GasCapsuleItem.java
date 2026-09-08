package com.com.chaos.Items;

import com.com.chaos.Entities.ThrownCapsuleEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GasCapsuleItem extends Item {

    private final int capacityMb;

    public GasCapsuleItem(Properties properties, int capacityMb) {
        super(properties);
        this.capacityMb = capacityMb;
    }

    public int getCapacityMb() {
        return this.capacityMb;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        if (!level.isClientSide) {
            ThrownCapsuleEntity thrown = new ThrownCapsuleEntity(level, player);
            thrown.setItem(stack);
            thrown.shootFromRotation((Entity) player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity(thrown);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1); // swap to stack.grow(1) elsewhere if you want the emptied capsule handed back instead of consumed - see note below
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}