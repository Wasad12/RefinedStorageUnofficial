package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterChannel;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterHandler;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterHandlerFactory;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IWriter;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeWriter;
import com.raoulvdberge.refinedstorage.apiimpl.network.readerwriter.ReaderWriterHandlerFluids;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileWriter extends TileNode<NetworkNodeWriter> implements IFluidHandler {
    public static final TileDataParameter<String> CHANNEL = TileReader.createChannelParameter();

    public TileWriter() {
        dataManager.addWatchedParameter(CHANNEL);
    }

    private <T> T getDummyCapabilityForClient(IWriter writer, Capability<T> capability) {
        for (IReaderWriterHandlerFactory factory : API.instance().getReaderWriterHandlerRegistry().all()) {
            T dummy = factory.create(null).getCapabilityWriter(writer, capability);

            if (dummy != null) {
                return dummy;
            }
        }

        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (super.hasCapability(capability, facing)) {
            return true;
        }

        IWriter writer = getNode();

        if (facing != getDirection()) {
            return false;
        }

        if (writer.getNetwork() == null || !writer.canUpdate()) {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                return getDummyCapabilityForClient(writer, capability) != null;
            }

            return false;
        }

        IReaderWriterChannel channel = writer.getNetwork().getReaderWriterChannel(writer.getChannel());

        if (channel == null) {
            return false;
        }

        for (IReaderWriterHandler handler : channel.getHandlers()) {
            if (handler.hasCapabilityWriter(writer, capability)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        T foundCapability = super.getCapability(capability, facing);

        if (foundCapability == null) {
            IWriter writer = getNode();

            if (facing != getDirection()) {
                return null;
            }

            if (writer.getNetwork() == null || !writer.canUpdate()) {
                if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                    return getDummyCapabilityForClient(writer, capability);
                }

                return null;
            }

            IReaderWriterChannel channel = writer.getNetwork().getReaderWriterChannel(writer.getChannel());

            if (channel == null) {
                return null;
            }

            for (IReaderWriterHandler handler : channel.getHandlers()) {
                foundCapability = handler.getCapabilityWriter(writer, capability);

                if (foundCapability != null) {
                    return foundCapability;
                }
            }
        }

        return foundCapability;
    }

    @Override
    @Nonnull
    public NetworkNodeWriter createNode(World worldIn, BlockPos pos) {
        return new NetworkNodeWriter(worldIn, pos);
    }

    @Override
    public String getNodeId() {
        return NetworkNodeWriter.ID;
    }

    // DEVIATION: classic IFluidHandler (instanceof-checked by 1.8.9 pipes) instead of the fluid
    // capability. Delegates to the channel's fluids-handler tankWriter, same facing gate as capabilities.
    @Nullable
    private IFluidHandler getFluidHandler(EnumFacing facing) {
        if (facing != getDirection() || worldObj == null) {
            return null;
        }

        IWriter writer = getNode();

        if (writer.getNetwork() == null || !writer.canUpdate()) {
            return null;
        }

        IReaderWriterChannel channel = writer.getNetwork().getReaderWriterChannel(writer.getChannel());

        if (channel == null) {
            return null;
        }

        for (IReaderWriterHandler handler : channel.getHandlers()) {
            if (handler instanceof ReaderWriterHandlerFluids) {
                return ((ReaderWriterHandlerFluids) handler).getTankWriter();
            }
        }

        return null;
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        IFluidHandler handler = getFluidHandler(from);

        return handler != null ? handler.fill(from, resource, doFill) : 0;
    }

    @Override
    @Nullable
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        IFluidHandler handler = getFluidHandler(from);

        return handler != null ? handler.drain(from, resource, doDrain) : null;
    }

    @Override
    @Nullable
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        IFluidHandler handler = getFluidHandler(from);

        return handler != null ? handler.drain(from, maxDrain, doDrain) : null;
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        IFluidHandler handler = getFluidHandler(from);

        return handler != null && handler.canFill(from, fluid);
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        IFluidHandler handler = getFluidHandler(from);

        return handler != null && handler.canDrain(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        IFluidHandler handler = getFluidHandler(from);

        return handler != null ? handler.getTankInfo(from) : new FluidTankInfo[0];
    }
}
