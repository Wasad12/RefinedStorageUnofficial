package com.raoulvdberge.refinedstorage.energy;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Mod-local energy storage (1.8.9 port).
 *
 * DEVIATION: 1.8.9 Forge has no net.minecraftforge.energy capability. This class preserves
 * the exact capacity/transfer math and NBT keys ("Energy") of the 1.11.2 EnergyForge, built on
 * the embedded RF 1.8 API base (cofh.api.energy.EnergyStorage). Cross-mod IO goes through the
 * RF interfaces (IEnergyHandler on tiles, IEnergyContainerItem on items).
 */
public class EnergyStorageAdv extends cofh.api.energy.EnergyStorage {
    public static final String NBT_ENERGY = "Energy";

    public EnergyStorageAdv(int capacity) {
        super(capacity, capacity, 0);
    }

    public EnergyStorageAdv(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public EnergyStorageAdv(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public int extractEnergyInternal(int maxExtract) {
        int energyExtracted = Math.min(energy, maxExtract);

        energy -= energyExtracted;

        return energyExtracted;
    }

    public void setMaxEnergyStored(int capacity) {
        setCapacity(capacity);
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
