package com.com.chaos.Blocks;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.EnergyStorage;

public class AstraAbyssGenEnergyStorage extends EnergyStorage {
    public AstraAbyssGenEnergyStorage(int capacity, int maxExtract) {
        super(capacity, 0, maxExtract); // 0 receive rate to prevent external items from putting energy in
    }

    public void addEnergy(int energy) {
        this.energy = Math.min(this.energy + energy, this.capacity);
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
