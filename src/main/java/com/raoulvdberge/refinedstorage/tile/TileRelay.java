package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeRelay;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TileRelay extends TileNode<NetworkNodeRelay> {
    @Override
    @Nonnull
    public NetworkNodeRelay createNode(World worldIn, BlockPos pos) {
        return new NetworkNodeRelay(worldIn, pos);
    }

    @Override
    public String getNodeId() {
        return NetworkNodeRelay.ID;
    }
}
