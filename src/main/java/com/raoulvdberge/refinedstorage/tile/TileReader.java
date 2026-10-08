package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReader;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterChannel;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterHandler;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterHandlerFactory;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.IGuiReaderWriter;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeReader;
import com.raoulvdberge.refinedstorage.apiimpl.network.readerwriter.ReaderWriterHandlerFluids;
import com.raoulvdberge.refinedstorage.gui.GuiReaderWriter;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataConsumer;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataProducer;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.client.Minecraft;
import com.raoulvdberge.refinedstorage.tile.data.DataSerializers;
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

public class TileReader extends TileNode<NetworkNodeReader> implements IFluidHandler {
    static <T extends TileNode> TileDataParameter<String> createChannelParameter() {
        return new TileDataParameter<>(DataSerializers.STRING, "", new ITileDataProducer<String, T>() {
            @Override
            public String getValue(T tile) {
                return ((IGuiReaderWriter) tile.getNode()).getChannel();
            }
        }, new ITileDataConsumer<String, T>() {
            @Override
            public void setValue(T tile, String value) {
                ((IGuiReaderWriter) tile.getNode()).setChannel(value);

                tile.getNode().markDirty();
            }
        }, parameter -> {
            if (Minecraft.getMinecraft().currentScreen instanceof GuiReaderWriter) {
                ((GuiReaderWriter) Minecraft.getMinecraft().currentScreen).updateSelection(parameter.getValue());
            }
        });
    }

    public static final TileDataParameter<String> CHANNEL = createChannelParameter();

    public TileReader() {
        dataManager.addWatchedParameter(CHANNEL);
    }

    private <T> T getDummyCapabilityForClient(IReader reader, Capability<T> capability) {
        for (IReaderWriterHandlerFactory factory : API.instance().getReaderWriterHandlerRegistry().all()) {
            T dummy = factory.create(null).getCapabilityReader(reader, capability);

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

        IReader reader = getNode();

        if (facing != getDirection()) {
            return false;
        }

        if (reader.getNetwork() == null || !reader.canUpdate()) {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                return getDummyCapabilityForClient(reader, capability) != null;
            }

            return false;
        }

        IReaderWriterChannel channel = reader.getNetwork().getReaderWriterChannel(reader.getChannel());

        if (channel == null) {
            return false;
        }

        for (IReaderWriterHandler handler : channel.getHandlers()) {
            if (handler.hasCapabilityReader(reader, capability)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        T foundCapability = super.getCapability(capability, facing);

        if (foundCapability == null) {
            IReader reader = getNode();

            if (facing != getDirection()) {
                return null;
            }

            if (reader.getNetwork() == null || !reader.canUpdate()) {
                if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                    return getDummyCapabilityForClient(reader, capability);
                }

                return null;
            }

            IReaderWriterChannel channel = reader.getNetwork().getReaderWriterChannel(reader.getChannel());

            if (channel == null) {
                return null;
            }

            for (IReaderWriterHandler handler : channel.getHandlers()) {
                foundCapability = handler.getCapabilityReader(reader, capability);

                if (foundCapability != null) {
                    return foundCapability;
                }
            }
        }

        return foundCapability;
    }

    @Override
    @Nonnull
    public NetworkNodeReader createNode(World worldIn, BlockPos pos) {
        return new NetworkNodeReader(worldIn, pos);
    }

    @Override
    public String getNodeId() {
        return NetworkNodeReader.ID;
    }

    // DEVIATION: classic IFluidHandler (instanceof-checked by 1.8.9 pipes) instead of the fluid
    // capability. Delegates to the channel's fluids-handler tankReader, same facing gate as capabilities.
    @Nullable
    private IFluidHandler getFluidHandler(EnumFacing facing) {
        if (facing != getDirection() || worldObj == null) {
            return null;
        }

        IReader reader = getNode();

        if (reader.getNetwork() == null || !reader.canUpdate()) {
            return null;
        }

        IReaderWriterChannel channel = reader.getNetwork().getReaderWriterChannel(reader.getChannel());

        if (channel == null) {
            return null;
        }

        for (IReaderWriterHandler handler : channel.getHandlers()) {
            if (handler instanceof ReaderWriterHandlerFluids) {
                return ((ReaderWriterHandlerFluids) handler).getTankReader();
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
