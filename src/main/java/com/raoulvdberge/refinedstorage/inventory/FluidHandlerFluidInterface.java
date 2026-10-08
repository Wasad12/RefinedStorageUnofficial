package com.raoulvdberge.refinedstorage.inventory;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

// DEVIATION: re-implemented against the classic 1.8.9 IFluidHandler (side-aware). Input/output split preserved.
public class FluidHandlerFluidInterface implements IFluidHandler {
    private FluidTank input;
    private FluidTank output;

    public FluidHandlerFluidInterface(FluidTank input, FluidTank output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        return new FluidTankInfo[] {
            input.getInfo(),
            output.getInfo()
        };
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        return input.fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        if (resource == null || output.getFluid() == null || !resource.isFluidEqual(output.getFluid())) {
            return null;
        }

        return output.drain(resource.amount, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return output.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return true;
    }
}
