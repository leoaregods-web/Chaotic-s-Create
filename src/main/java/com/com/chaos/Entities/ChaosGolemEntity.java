package com.com.chaos.Entities;

import com.com.chaos.Entities.ChaosGolemAttackGoal;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerBossEvent;

public class ChaosGolemEntity extends PathfinderMob implements Enemy {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private int attackCooldown = 0;

    public int getAttackCooldown() {
        return this.attackCooldown;
    }

    public void setAttackCooldown(int ticks) {
        this.attackCooldown = ticks;
    }

    private BlockPos flightAnchor;
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    public ChaosGolemEntity(EntityType<? extends ChaosGolemEntity> type, Level level) {
        super(type, level);
        this.moveControl = new HoverMoveControl(this);
        this.setNoGravity(true);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 250.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 6.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ChaosGolemAttackGoal(this));
        this.goalSelector.addGoal(2, new ChaosGolemFlightGoal(this));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    public BlockPos getFlightAnchor() {
        if (this.flightAnchor == null) {
            this.flightAnchor = this.blockPosition(); // defaults to spawn point - the arena platform the structure placed it on
        }
        return this.flightAnchor;
    }

    public void setFlightAnchor(BlockPos pos) {
        this.flightAnchor = pos;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    public boolean isFlapping() {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    public static boolean checkChaosGolemSpawnRules(EntityType<ChaosGolemEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, net.minecraft.util.RandomSource random) {
        return false; // this boss is placed by the arena structure, not natural spawns
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.fallDistance = 0.0F;
        if (!this.level().isClientSide()) {
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        } else {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }

        if (this.isDeadOrDying()) {
            this.deathAnimationState.startIfStopped(this.tickCount);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.bossEvent.removeAllPlayers();
    }

    static class HoverMoveControl extends MoveControl {
        private final ChaosGolemEntity golem;

        public HoverMoveControl(ChaosGolemEntity golem) {
            super(golem);
            this.golem = golem;
        }

        @Override
        public void tick() {
            if (this.operation == Operation.MOVE_TO) {
                double dx = this.wantedX - this.golem.getX();
                double dy = this.wantedY - this.golem.getY();
                double dz = this.wantedZ - this.golem.getZ();
                double distSqr = dx * dx + dy * dy + dz * dz;
                if (distSqr < 2.5000003E-7) {
                    this.operation = Operation.WAIT;
                    this.golem.setDeltaMovement(this.golem.getDeltaMovement().scale(0.5D));
                } else {
                    float speed = (float) (this.speedModifier * this.golem.getAttributeValue(Attributes.FLYING_SPEED));
                    this.golem.setDeltaMovement(this.golem.getDeltaMovement().add(
                            dx / Math.sqrt(distSqr) * 0.1D * speed,
                            dy / Math.sqrt(distSqr) * 0.1D * speed,
                            dz / Math.sqrt(distSqr) * 0.1D * speed
                    ));
                }
            }
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isControlledByLocalInstance() && this.isEffectiveAi()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        }
        this.calculateEntityAnimation(false);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false; // flying boss - fall damage/knockdown should never apply
    }
}