package com.com.chaos.Entities;

import com.com.chaos.Effects.CapsuleReleaseEffects;
import com.com.chaos.Items.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownCapsuleEntity extends ThrowableItemProjectile {

    // Required factory constructor - used by EntityType.Builder / when loading from disk or spawn packets.
    public ThrownCapsuleEntity(EntityType<? extends ThrownCapsuleEntity> type, Level level) {
        super(type, level);
    }

    // Used when a player throws a capsule.
    public ThrownCapsuleEntity(Level level, LivingEntity owner) {
        super(ModEntities.THROWN_CAPSULE.get(), owner, level);
    }

    // Handy if you ever want to spawn one from code (dispensers, commands, etc.) without an owner.
    public ThrownCapsuleEntity(Level level, double x, double y, double z) {
        super(ModEntities.THROWN_CAPSULE.get(), x, y, z, level);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        Level level = this.level();
        if (level.isClientSide) {
            return;
        }

        ItemStack thrownStack = this.getItem();
        boolean released = CapsuleReleaseEffects.trigger(level, this.position(), thrownStack);

        if (!released && !thrownStack.isEmpty()) {
            // No burst effect defined for this capsule (e.g. a reinforced capsule, which already
            // resists being crushed by a press) - let it survive the throw instead of just vanishing.
            ItemEntity drop = new ItemEntity(level, this.getX(), this.getY(), this.getZ(), thrownStack.copy());
            drop.setDeltaMovement(0.0D, 0.05D, 0.0D);
            level.addFreshEntity(drop);
        }

        this.discard();
    }

    protected Item getDefaultItem() {
        return ModItems.CAPSULE.get();
    }
}