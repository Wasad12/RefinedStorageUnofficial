package com.raoulvdberge.refinedstorage.inventory;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;

/**
 * 1.8.9 tank with fill/drain gating and a contents-changed hook.
 *
 * DEVIATION: mirrors the later-Forge FluidTank API used by the source (canFill/canDrain flags,
 * fillInternal/drainInternal bypassing the flags, onContentsChanged callback), which 1.8.9
 * vanilla FluidTank lacks. RS wiring only fills tankIn and drains tankOut externally, so the
 * observable behavior matches the source on all reachable paths.
 */
public class FluidTankAdv extends FluidTank {
    private boolean canFill = true;
    private boolean canDrain = true;

    public FluidTankAdv(int capacity) {
        super(capacity);
    }

    public void setCanFill(boolean canFill) {
        this.canFill = canFill;
    }

    public void setCanDrain(boolean canDrain) {
        this.canDrain = canDrain;
    }

    public int fillInternal(FluidStack resource, boolean doFill) {
        int filled = super.fill(resource, doFill);

        if (doFill && filled > 0) {
            onContentsChanged();
        }

        return filled;
    }

    @Nullable
    public FluidStack drainInternal(int maxDrain, boolean doDrain) {
        FluidStack drained = super.drain(maxDrain, doDrain);

        if (doDrain && drained != null) {
            onContentsChanged();
        }

        return drained;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (!canFill) {
            return 0;
        }

        int filled = super.fill(resource, doFill);

        if (doFill && filled > 0) {
            onContentsChanged();
        }

        return filled;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (!canDrain) {
            return null;
        }

        FluidStack drained = super.drain(maxDrain, doDrain);

        if (doDrain && drained != null) {
            onContentsChanged();
        }

        return drained;
    }

    @Override
    public void setFluid(FluidStack fluid) {
        super.setFluid(fluid);

        onContentsChanged();
    }

    protected void onContentsChanged() {
    }
}
