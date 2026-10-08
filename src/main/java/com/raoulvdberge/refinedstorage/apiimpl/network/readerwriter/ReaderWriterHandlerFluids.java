package com.raoulvdberge.refinedstorage.apiimpl.network.readerwriter;

import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReader;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterChannel;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterHandler;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IWriter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ReaderWriterHandlerFluids implements IReaderWriterHandler {
    public static final String ID = "fluids";

    private FluidTank tank;
    private FluidTankReaderWriter tankReader, tankWriter;

    public ReaderWriterHandlerFluids(@Nullable NBTTagCompound tag) {
        this.tank = new FluidTank(4 * FluidContainerRegistry.BUCKET_VOLUME);
        this.tankReader = new FluidTankReaderWriter(tank, true, false);
        this.tankWriter = new FluidTankReaderWriter(tank, false, true);

        if (tag != null) {
            this.tank.readFromNBT(tag);
        }
    }

    @Override
    public void update(IReaderWriterChannel channel) {
        // NO OP
    }

    @Override
    public void onWriterDisabled(IWriter writer) {
        // NO OP
    }

    // DEVIATION: classic IFluidHandler is not a Forge Capability on 1.8.9, so there is no
    // fluid capability to re-export. Reader/writer tiles expose the tanks via instanceof
    // IFluidHandler instead (see TileReader/TileWriter fluid handling).
    @Override
    public boolean hasCapabilityReader(IReader reader, Capability<?> capability) {
        return false;
    }

    @Override
    public <T> T getCapabilityReader(IReader reader, Capability<T> capability) {
        return null;
    }

    @Override
    public boolean hasCapabilityWriter(IWriter writer, Capability<?> capability) {
        return false;
    }

    @Override
    public <T> T getCapabilityWriter(IWriter writer, Capability<T> capability) {
        return null;
    }

    public IFluidHandler getTankReader() {
        return tankReader;
    }

    public IFluidHandler getTankWriter() {
        return tankWriter;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tank.writeToNBT(tag);

        return tag;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<IChatComponent> getStatusReader(IReader reader, IReaderWriterChannel channel) {
        return getStatus(tankReader);
    }

    @Override
    public List<IChatComponent> getStatusWriter(IWriter writer, IReaderWriterChannel channel) {
        return getStatus(tankWriter);
    }

    private List<IChatComponent> getStatus(IFluidTank tank) {
        FluidStack stack = tank.getFluid();

        if (stack == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new ChatComponentText(RSUtils.QUANTITY_FORMATTER.format((float) stack.amount / 1000F) + "x ").appendSibling(new ChatComponentTranslation(stack.getUnlocalizedName())));
    }

    // DEVIATION: re-implemented against the classic 1.8.9 IFluidHandler (side-aware).
    private class FluidTankReaderWriter implements IFluidTank, IFluidHandler {
        private FluidTank parent;
        private boolean canFill, canDrain;

        public FluidTankReaderWriter(FluidTank parent, boolean canFill, boolean canDrain) {
            this.parent = parent;

            this.canFill = canFill;
            this.canDrain = canDrain;
        }

        @Nullable
        @Override
        public FluidStack getFluid() {
            return parent.getFluid();
        }

        @Override
        public int getFluidAmount() {
            return parent.getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return parent.getCapacity();
        }

        @Override
        public FluidTankInfo getInfo() {
            return parent.getInfo();
        }

        @Override
        public FluidTankInfo[] getTankInfo(EnumFacing from) {
            return new FluidTankInfo[] { parent.getInfo() };
        }

        @Override
        public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
            return canFill ? parent.fill(resource, doFill) : 0;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return fill(null, resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
            if (!canDrain || resource == null || parent.getFluid() == null || !resource.isFluidEqual(parent.getFluid())) {
                return null;
            }

            return parent.drain(resource.amount, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
            return canDrain ? parent.drain(maxDrain, doDrain) : null;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return drain(null, maxDrain, doDrain);
        }

        @Override
        public boolean canFill(EnumFacing from, Fluid fluid) {
            return canFill;
        }

        @Override
        public boolean canDrain(EnumFacing from, Fluid fluid) {
            return canDrain;
        }
    }
}
