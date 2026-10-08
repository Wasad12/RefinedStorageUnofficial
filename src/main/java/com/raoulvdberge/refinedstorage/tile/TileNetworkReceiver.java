package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeNetworkReceiver;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TileNetworkReceiver extends TileNode<NetworkNodeNetworkReceiver> {
    @Override
    @Nonnull
    public NetworkNodeNetworkReceiver createNode(World worldIn, BlockPos pos) {
        return new NetworkNodeNetworkReceiver(worldIn, pos);
    }

    @Override
    public String getNodeId() {
        return NetworkNodeNetworkReceiver.ID;
    }
}
