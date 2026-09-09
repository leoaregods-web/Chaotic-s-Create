package com.com.chaos.Entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The golem's ranged attack. Reuses vanilla fireball-style physics (AbstractHurtingProjectile
 * already handles motion/acceleration each tick) but skips the explosion/block-breaking part -
 * this just deals flat damage on hit and disappears, so it's safe to spam near the arena build.
 */
public class ChaosBoltEntity extends AbstractHurtingProjectile implements ItemSupplier {

    private float damage = 6.0F;

    // required no-arg-ish constructor for EntityType registration/deserialization
    public ChaosBoltEntity(EntityType<? extends ChaosBoltEntity> type, Level level) {
        super(type, level);
    }

    // convenience constructor used by ChaosGolemRangedAttackGoal to actually fire one
    public ChaosBoltEntity(Level level, LivingEntity shooter, double dx, double dy, double dz) {
        // 1.21+ AbstractHurtingProjectile takes the direction as a Vec3 rather than raw doubles;
        // this constructor variant positions the bolt at the shooter automatically
        super(ModEntities.CHAOS_BOLT.get(), shooter, new Vec3(dx, dy, dz), level);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public ItemStack getItem() {
        // stand-in icon so it renders as something instead of crashing - swap for a
        // dedicated "chaos_bolt" item + texture later once you've settled on the look
        return new ItemStack(Items.FIRE_CHARGE);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide()) {
            return;
        }
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        DamageSource damageSource = owner instanceof LivingEntity livingOwner
                ? this.level().damageSources().mobAttack(livingOwner)
                : this.level().damageSources().magic();
        target.hurt(damageSource, this.damage);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        // dispatches to onHitEntity/onHitBlock above (handles damage + discard), then we layer
        // the shared impact VFX on top regardless of what was hit
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY(), this.getZ(), 20, 0.2D, 0.2D, 0.2D, 0.05D);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 0.7F, 1.4F);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true; // flies straight, doesn't arc like an arrow/snowball
    }
}
