package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeSecurityManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TileSecurityManager extends TileNode<NetworkNodeSecurityManager> {
    @Override
    @Nonnull
    public NetworkNodeSecurityManager createNode(World worldIn, BlockPos pos) {
        return new NetworkNodeSecurityManager(worldIn, pos);
    }

    @Override
    public String getNodeId() {
        return NetworkNodeSecurityManager.ID;
    }
}
