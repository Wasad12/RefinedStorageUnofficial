package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeCrafter;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TileCrafter extends TileNode<NetworkNodeCrafter> {
    @Override
    @Nonnull
    public NetworkNodeCrafter createNode(World worldIn, BlockPos pos) {
        return new NetworkNodeCrafter(worldIn, pos);
    }

    @Override
    public String getNodeId() {
        return NetworkNodeCrafter.ID;
    }
}
