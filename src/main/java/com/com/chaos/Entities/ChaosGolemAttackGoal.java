package com.com.chaos.Entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ChaosGolemAttackGoal extends Goal {
    private final ChaosGolemEntity golem;
    private int attackTimeoutTicks; // safety cap so it doesn't chase forever if it can't close the gap

    public ChaosGolemAttackGoal(ChaosGolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.golem.getTarget();
        return target != null && target.isAlive() && this.golem.getAttackCooldown() <= 0;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.golem.getTarget();
        return target != null && target.isAlive() && this.golem.getAttackCooldown() <= 0 && this.attackTimeoutTicks > 0;
    }

    @Override
    public void start() {
        this.attackTimeoutTicks = 200; // ~10s safety cap
    }

    @Override
    public void stop() {
        this.golem.getNavigation().stop();
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

        this.attackTimeoutTicks--;
        this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distSqr = this.golem.distanceToSqr(target);
        double reachSqr = 9.0D; // ~3 blocks - tune to taste once you see it in-game

        if (distSqr > reachSqr) {
            this.golem.getMoveControl().setWantedPosition(
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 1.3D);
        } else {
            // close enough - land the hit, then immediately hand control back to the flight goal
            this.golem.swing(InteractionHand.MAIN_HAND);
            this.golem.doHurtTarget(target);
            this.spawnImpactParticles(target);
            this.golem.setAttackCooldown(70); // ~3.5s before it's willing to re-engage
            this.attackTimeoutTicks = 0;
        }
    }

    private void spawnImpactParticles(LivingEntity target) {
        if (!(this.golem.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                12, 0.3D, 0.3D, 0.3D, 0.2D);
    }
}