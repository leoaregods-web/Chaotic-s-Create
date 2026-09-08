package com.com.chaos.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeltRenderer.class)
public class BeltRendererMixin {

    @Inject(method = "renderItem", at = @At("TAIL"))
    private void chaoticscreate$spawnCapsuleBeltParticles(
            BeltBlockEntity be,
            float partialTicks,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay,
            Direction beltFacing,
            Vec3i directionVec,
            BeltSlope slope,
            int verticality,
            boolean slopeAlongX,
            boolean onContraption,
            TransportedItemStack transported,
            Vec3 beltStartOffset,
            CallbackInfo ci
    ) {
        if (!(be.getLevel() instanceof ClientLevel level)) {
            return;
        }

        if (level.getGameTime() % 4L != 0L) {
            return;
        }

        float offset = Mth.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition);
        float sideOffset = Mth.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset);

        if (be.getSpeed() == 0) {
            offset = transported.beltPosition;
            sideOffset = transported.sideOffset;
        }

        float verticalMovement = 0.0F;
        if (verticality != 0 && offset >= 0.5F) {
            verticalMovement = verticality * (offset - 0.5F);
        }

        Vec3 offsetVec = Vec3.atLowerCornerOf(directionVec).scale(offset);
        if (verticalMovement != 0.0F) {
            offsetVec = offsetVec.add(0.0D, verticalMovement, 0.0D);
        }

        boolean alongX = beltFacing.getClockWise().getAxis() == Direction.Axis.X;
        if (!alongX) {
            sideOffset *= -1.0F;
        }

        Vec3 itemPos = beltStartOffset
                .add(be.getBlockPos().getX(), be.getBlockPos().getY(), be.getBlockPos().getZ())
                .add(offsetVec)
                .add(alongX ? sideOffset : 0.0D, 0.06D, alongX ? 0.0D : sideOffset);
    }
}