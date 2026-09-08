package com.com.chaos.Entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ChaosGolemFlightGoal extends Goal {
    private final ChaosGolemEntity golem;
    private float angle;
    private float verticalPhase;

    public ChaosGolemFlightGoal(ChaosGolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return true; // always eligible - the melee goal at higher priority takes over once a target is in range
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        BlockPos anchor = this.golem.getFlightAnchor();

        this.angle += 1.4F;          // orbit speed around the island - lower = slower, wider dragon-like loops
        this.verticalPhase += 2.0F;  // bob speed - independent of orbit speed, gives the phantom-style swerve

        double radius = 12.0D;
        double radians = Math.toRadians(this.angle);
        double targetX = anchor.getX() + 0.5D + Math.cos(radians) * radius;
        double targetZ = anchor.getZ() + 0.5D + Math.sin(radians) * radius;

        double bobble = Math.sin(Math.toRadians(this.verticalPhase)) * 4.0D; // the up/down swerve
        double targetY = anchor.getY() + 8.0D + bobble;

        this.golem.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0D);

        double dx = targetX - this.golem.getX();
        double dz = targetZ - this.golem.getZ();
        if (dx * dx + dz * dz > 0.01D) {
            float yaw = (float) (Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
            this.golem.setYRot(yaw);
            this.golem.yBodyRot = yaw;
        }
    }
}