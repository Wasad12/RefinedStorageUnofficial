package com.raoulvdberge.refinedstorage.apiimpl.network;

import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
import com.raoulvdberge.refinedstorage.api.network.security.Permission;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.proxy.CapabilityNetworkNodeProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NetworkNodeListener {
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent e) {
        if (!e.world.isRemote) {
            if (e.phase == TickEvent.Phase.END) {
                // DEVIATION: 1.8.9 World has no profiler field; section markers dropped (profiling only).
                for (INetworkNode node : API.instance().getNetworkNodeManager(e.world).all()) {
                    node.update();
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent e) {
        // DEVIATION: 1.8.9 BlockEvent exposes world/pos/player/blockSnapshot as fields.
        if (!e.world.isRemote) {
            TileEntity placed = e.world.getTileEntity(e.pos);

            if (placed != null && placed.hasCapability(CapabilityNetworkNodeProxy.NETWORK_NODE_PROXY_CAPABILITY, null)) {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    TileEntity tile = e.world.getTileEntity(e.blockSnapshot.pos.offset(facing));

                    if (tile != null && tile.hasCapability(CapabilityNetworkNodeProxy.NETWORK_NODE_PROXY_CAPABILITY, facing.getOpposite())) {
                        INetworkNodeProxy nodeProxy = tile.getCapability(CapabilityNetworkNodeProxy.NETWORK_NODE_PROXY_CAPABILITY, facing.getOpposite());
                        INetworkNode node = nodeProxy.getNode();

                        if (node.getNetwork() != null && !node.getNetwork().getSecurityManager().hasPermission(Permission.BUILD, e.player)) {
                            RSUtils.sendNoPermissionMessage(e.player);

                            e.setCanceled(true);

                            return;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent e) {
        if (!e.world.isRemote) {
            TileEntity tile = e.world.getTileEntity(e.pos);

            if (tile != null && tile.hasCapability(CapabilityNetworkNodeProxy.NETWORK_NODE_PROXY_CAPABILITY, null)) {
                INetworkNodeProxy nodeProxy = tile.getCapability(CapabilityNetworkNodeProxy.NETWORK_NODE_PROXY_CAPABILITY, null);
                INetworkNode node = nodeProxy.getNode();

                if (node.getNetwork() != null && !node.getNetwork().getSecurityManager().hasPermission(Permission.BUILD, e.getPlayer())) {
                    RSUtils.sendNoPermissionMessage(e.getPlayer());

                    e.setCanceled(true);
                }
            }
        }
    }
}
