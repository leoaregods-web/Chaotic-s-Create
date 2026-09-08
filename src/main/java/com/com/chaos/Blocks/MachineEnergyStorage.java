package com.com.chaos.Blocks;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.EnergyStorage;

public class MachineEnergyStorage extends EnergyStorage {
    public MachineEnergyStorage(int capacity, int maxReceive) {
        super(capacity, maxReceive, capacity);
    }

    public void loseEnergy(int energy) {
        this.energy = Math.max(0, Math.min(this.energy - energy, this.capacity));
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("energy", this.energy);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("energy")) {
            this.energy = tag.getInt("energy");
        }
    }
}
