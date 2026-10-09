package com.raoulvdberge.refinedstorage.energy;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Mod-local energy storage (1.8.9 port).
 *
 * DEVIATION: 1.8.9 Forge has no net.minecraftforge.energy capability. This class preserves
 * the exact capacity/transfer math and NBT keys ("Energy") of the 1.11.2 EnergyForge.
 *
 * DEVIATION 2026-10-09 (ActuallyAdditions RF skew): this class used to extend the embedded
 * cofh RF EnergyStorage (1.7.10R1.0.2). When another mod ships a NEWER cofh RF API (e.g.
 * ActuallyAdditions' 1.8.9R1.2.0B1, which wins the classpath alphabetically and LACKS
 * setCapacity(int)), inherited calls like setCapacity crash with NoSuchMethodError and
 * controller energy never syncs (creative controller renders unlit). The class is now
 * fully self-contained (own energy/capacity/transfer fields, same math as the CoFH
 * reference implementation) with ZERO cofh references, so no RF version can break it.
 * Cross-mod IO still goes through the RF interfaces (IEnergyHandler on tiles,
 * IEnergyContainerItem on items), whose method signatures are stable across RF versions.
 */
public class EnergyStorageAdv {
    public static final String NBT_ENERGY = "Energy";

    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public EnergyStorageAdv(int capacity) {
        this(capacity, capacity, 0);
    }

    public EnergyStorageAdv(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    public EnergyStorageAdv(int capacity, int maxReceive, int maxExtract) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));

        if (!simulate) {
            energy += energyReceived;
        }

        return energyReceived;
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));

        if (!simulate) {
            energy -= energyExtracted;
        }

        return energyExtracted;
    }

    public int extractEnergyInternal(int maxExtract) {
        int energyExtracted = Math.min(energy, maxExtract);

        energy -= energyExtracted;

        return energyExtracted;
    }

    public int getEnergyStored() {
        return energy;
    }

    public int getMaxEnergyStored() {
        return capacity;
    }

    public void setEnergyStored(int energy) {
        this.energy = energy;

        if (this.energy > capacity) {
            this.energy = capacity;
        } else if (this.energy < 0) {
            this.energy = 0;
        }
    }

    public void setMaxEnergyStored(int capacity) {
        this.capacity = capacity;

        if (energy > capacity) {
            energy = capacity;
        }
    }

    public static int getStackEnergy(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_ENERGY)
            ? stack.getTagCompound().getInteger(NBT_ENERGY)
            : 0;
    }

    public static void setStackEnergy(ItemStack stack, int energy) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().setInteger(NBT_ENERGY, energy);
    }

    /**
     * Stack-backed storage with the same persist-on-change semantics as the old ItemEnergyForge.
     */
    public static EnergyStorageAdv forStack(ItemStack stack, int capacity, int maxReceive, int maxExtract) {
        return new StackEnergyStorage(stack, capacity, maxReceive, maxExtract);
    }

    private static class StackEnergyStorage extends EnergyStorageAdv {
        private final ItemStack stack;

        private StackEnergyStorage(ItemStack stack, int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);

            this.stack = stack;
            this.energy = getStackEnergy(stack);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);

            if (received > 0 && !simulate) {
                setStackEnergy(stack, getEnergyStored());
            }

            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);

            if (extracted > 0 && !simulate) {
                setStackEnergy(stack, getEnergyStored());
            }

            return extracted;
        }
    }
}
