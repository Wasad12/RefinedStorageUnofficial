package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeFluidInterface;
import com.raoulvdberge.refinedstorage.tile.config.IComparable;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataProducer;
import com.raoulvdberge.refinedstorage.tile.data.RSSerializers;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// DEVIATION: classic IFluidHandler (instanceof-checked by 1.8.9 pipes) instead of the fluid
// capability. Delegates to the node's split tank, as before.
public class TileFluidInterface extends TileNode<NetworkNodeFluidInterface> implements IFluidHandler {
    public static final TileDataParameter<Integer> COMPARE = IComparable.createParameter();

    public static final TileDataParameter<FluidStack> TANK_IN = new TileDataParameter<>(RSSerializers.FLUID_STACK_SERIALIZER, null, new ITileDataProducer<FluidStack, TileFluidInterface>() {
        @Override
        public FluidStack getValue(TileFluidInterface tile) {
            return tile.getNode().getTankIn().getFluid();
        }
    });

    public static final TileDataParameter<FluidStack> TANK_OUT = new TileDataParameter<>(RSSerializers.FLUID_STACK_SERIALIZER, null, new ITileDataProducer<FluidStack, TileFluidInterface>() {
        @Override
        public FluidStack getValue(TileFluidInterface tile) {
            return tile.getNode().getTankOut().getFluid();
        }
    });

    public TileFluidInterface() {
        dataManager.addWatchedParameter(COMPARE);
        dataManager.addParameter(TANK_IN);
        dataManager.addParameter(TANK_OUT);
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        return getNode().getTank().fill(from, resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        return getNode().getTank().drain(from, resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return getNode().getTank().drain(from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return getNode().getTank().canFill(from, fluid);
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return getNode().getTank().canDrain(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        return getNode().getTank().getTankInfo(from);
    }

    @Override
    @Nonnull
    public NetworkNodeFluidInterface createNode(World worldIn, BlockPos pos) {
        return new NetworkNodeFluidInterface(worldIn, pos);
    }

    @Override
    public String getNodeId() {
        return NetworkNodeFluidInterface.ID;
    }
}
