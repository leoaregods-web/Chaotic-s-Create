package com.com.chaos.Entities;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ChaosGolemRangedAttackGoal extends Goal {
    private final ChaosGolemEntity golem;
    private int shotsRemaining;
    private int shotIntervalTicks;

    private static final double MIN_RANGE_SQR = 36.0D;   // ~6 blocks - below this, the melee goal (higher priority) takes over
    private static final double DROP_RANGE_SQR = 9.0D;   // if the target closes to ~3 blocks mid-burst, bail early to melee
    private static final int SHOTS_PER_BURST = 3;
    private static final int TICKS_BETWEEN_SHOTS = 8;    // ~0.4s between bolts in a burst
    private static final int COOLDOWN_AFTER_BURST = 50;  // ~2.5s before the next burst is allowed

    public ChaosGolemRangedAttackGoal(ChaosGolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK)); // no MOVE flag - flight goal keeps orbiting/flying while this fires
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.golem.getTarget();
        return target != null && target.isAlive()
                && this.golem.distanceToSqr(target) > MIN_RANGE_SQR
                && this.golem.getRangedAttackCooldown() <= 0;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.golem.getTarget();
        return target != null && target.isAlive()
                && this.shotsRemaining > 0
                && this.golem.distanceToSqr(target) > DROP_RANGE_SQR;
    }

    @Override
    public void start() {
        this.shotsRemaining = SHOTS_PER_BURST;
        this.shotIntervalTicks = 0;
    }

    @Override
    public void stop() {
        this.golem.setRangedAttackCooldown(COOLDOWN_AFTER_BURST);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.golem.getTarget();
        if (target == null) {
            return;
        }

        this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.shotIntervalTicks-- > 0) {
            return;
        }
        this.shotIntervalTicks = TICKS_BETWEEN_SHOTS;
        this.fireBolt(target);
        this.shotsRemaining--;
    }

    private void fireBolt(LivingEntity target) {
        double dx = target.getX() - this.golem.getX();
        double dy = target.getY(0.5D) - (this.golem.getEyeY() - 0.3D); // aim center-mass, not feet
        double dz = target.getZ() - this.golem.getZ();

        ChaosBoltEntity bolt = new ChaosBoltEntity(this.golem.level(), this.golem, dx, dy, dz);
        this.golem.level().addFreshEntity(bolt);
        this.golem.level().playSound(null, this.golem.getX(), this.golem.getY(), this.golem.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 1.0F);
    }
}
