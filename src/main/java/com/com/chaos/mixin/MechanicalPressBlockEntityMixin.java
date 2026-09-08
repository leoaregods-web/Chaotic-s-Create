package com.com.chaos.mixin;

import com.com.chaos.Effects.CapsuleReleaseEffects;
import com.com.chaos.Effects.DelayedCapsuleEffects;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MechanicalPressBlockEntity.class)
public class MechanicalPressBlockEntityMixin {

    @Unique
    private ItemStack chaoticscreate$worldOriginal = ItemStack.EMPTY;

    @Unique
    private ItemStack chaoticscreate$beltOriginal = ItemStack.EMPTY;

    @Inject(method = "tryProcessInWorld", at = @At("HEAD"))
    private void chaoticscreate$captureWorldInput(ItemEntity itemEntity, boolean simulate,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (!simulate) {
            this.chaoticscreate$worldOriginal = itemEntity.getItem().copyWithCount(1);
        }
    }

    @Inject(method = "tryProcessInWorld", at = @At("RETURN"))
    private void chaoticscreate$afterWorldPress(ItemEntity itemEntity, boolean simulate,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (simulate || !cir.getReturnValue()) return;

        MechanicalPressBlockEntity self = (MechanicalPressBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide) return;

        DelayedCapsuleEffects.queue(level, itemEntity.position(), chaoticscreate$worldOriginal);
        chaoticscreate$worldOriginal = ItemStack.EMPTY;
    }

    @Inject(method = "tryProcessOnBelt", at = @At("HEAD"))
    private void chaoticscreate$captureBeltInput(TransportedItemStack input, java.util.List<ItemStack> outputList,
                                                 boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (!simulate) {
            this.chaoticscreate$beltOriginal = input.stack.copyWithCount(1);
        }
    }

    @Inject(method = "tryProcessOnBelt", at = @At("RETURN"))
    private void chaoticscreate$afterBeltPress(TransportedItemStack input, java.util.List<ItemStack> outputList,
                                               boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (simulate || !cir.getReturnValue()) return;

        MechanicalPressBlockEntity self = (MechanicalPressBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide) return;

        Vec3 pos = Vec3.atCenterOf(self.getBlockPos().below(2));
        DelayedCapsuleEffects.queue(level, pos, chaoticscreate$beltOriginal);
        chaoticscreate$beltOriginal = ItemStack.EMPTY;
    }
}