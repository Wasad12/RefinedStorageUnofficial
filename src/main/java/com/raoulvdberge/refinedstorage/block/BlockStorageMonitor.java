package com.raoulvdberge.refinedstorage.block;

import com.raoulvdberge.refinedstorage.RSUtils;

import com.raoulvdberge.refinedstorage.RSGui;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeStorageMonitor;
import com.raoulvdberge.refinedstorage.tile.TileStorageMonitor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;

public class BlockStorageMonitor extends BlockNode {
    public BlockStorageMonitor() {
        super("storage_monitor");
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            ItemStack held = player.inventory.getCurrentItem();

            if (player.isSneaking()) {
                tryOpenNetworkGui(RSGui.STORAGE_MONITOR, player, world, pos, side);
            } else {
                NetworkNodeStorageMonitor storageMonitor = ((TileStorageMonitor) world.getTileEntity(pos)).getNode();

                if (!RSUtils.isEmpty(held)) {
                    return storageMonitor.deposit(player, held);
                } else {
                    return storageMonitor.depositAll(player);
                }
            }
        }

        return true;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        super.onBlockClicked(world, pos, player);

        if (!world.isRemote) {
            // DEVIATION: 1.8.9 EntityPlayerMP exposes theItemInWorldManager, not interactionManager.
            MovingObjectPosition rayResult = ForgeHooks.rayTraceEyes(player, ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance() + 1);

            if (rayResult == null) {
                return;
            }

            ((TileStorageMonitor) world.getTileEntity(pos)).getNode().extract(player, rayResult.sideHit);
        }
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileStorageMonitor();
    }

    @Override
    public boolean hasConnectivityState() {
        return true;
    }
}
