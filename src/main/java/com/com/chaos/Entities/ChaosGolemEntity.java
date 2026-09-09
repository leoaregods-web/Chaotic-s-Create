package com.com.chaos.Entities;

import com.com.chaos.Entities.ChaosGolemAttackGoal;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
    private int rangedAttackCooldown = 0;
    private int flightParticleTimer = 0; // throttles trail particles so it's not spamming packets every tick
    private int phase = 1; // 1 = calm, 2 = aggressive (<=66% hp), 3 = enraged (<=33% hp)

    public int getAttackCooldown() {
        return this.attackCooldown;
    }

    public void setAttackCooldown(int ticks) {
        // scaling here (rather than in the goal classes) means both attack goals automatically
        // speed up in later phases without either of them needing to know about phases at all
        this.attackCooldown = Math.round(ticks * this.getPhaseCooldownMultiplier());
    }

    public int getRangedAttackCooldown() {
        return this.rangedAttackCooldown;
    }

    public void setRangedAttackCooldown(int ticks) {
        this.rangedAttackCooldown = Math.round(ticks * this.getPhaseCooldownMultiplier());
    }

    public int getPhase() {
        return this.phase;
    }

    // phase 1 = 100% of the goals' normal cooldowns, phase 2 = 75%, phase 3 = 55% - tune to taste
    private float getPhaseCooldownMultiplier() {
        return switch (this.phase) {
            case 3 -> 0.55F;
            case 2 -> 0.75F;
            default -> 1.0F;
        };
    }

    private void updatePhase() {
        float healthFraction = this.getHealth() / this.getMaxHealth();
        int newPhase = 1;
        if (healthFraction <= 0.33F) {
            newPhase = 3;
        } else if (healthFraction <= 0.66F) {
            newPhase = 2;
        }

        if (newPhase != this.phase) {
            this.phase = newPhase;
            this.onPhaseChange(newPhase);
        }
    }

    private void onPhaseChange(int newPhase) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                    40, 1.0D, 1.0D, 1.0D, 0.2D);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 0.7F + (newPhase - 1) * 0.15F);
        }
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
        this.goalSelector.addGoal(2, new ChaosGolemRangedAttackGoal(this));
        this.goalSelector.addGoal(3, new ChaosGolemFlightGoal(this));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
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
        // dragon-scale ambience reads as "big flying boss" without borrowing the Warden's identity
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        // ties the hurt sound back to "golem" even though the ambient sound is dragon-y
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        // pitched down from the default random 0.8-1.2 range - makes it sound heavier/older, less "mob-y"
        return 0.6F + this.random.nextFloat() * 0.15F;
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
            if (this.rangedAttackCooldown > 0) {
                this.rangedAttackCooldown--;
            }
            this.updatePhase();
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            if (--this.flightParticleTimer <= 0) {
                this.flightParticleTimer = 3; // every 3 ticks - visible trail without flooding nearby clients
                this.spawnFlightTrailParticles();
            }
        } else {
            this.setupAnimationStates();
        }
    }

    private void spawnFlightTrailParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        // two symmetric points near the "shoulders" so the trail reads as coming off the body, not the center
        double offsetX = Math.cos(Math.toRadians(this.getYRot() + 90.0F)) * 1.2D;
        double offsetZ = Math.sin(Math.toRadians(this.getYRot() + 90.0F)) * 1.2D;
        double baseY = this.getY() + this.getBbHeight() * 0.6D;

        serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                this.getX() + offsetX, baseY, this.getZ() + offsetZ,
                1, 0.05D, 0.05D, 0.05D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                this.getX() - offsetX, baseY, this.getZ() - offsetZ,
                1, 0.05D, 0.05D, 0.05D, 0.0D);
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
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                    60, 1.2D, 1.0D, 1.2D, 0.15D);
        }
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