package com.raoulvdberge.refinedstorage.block;

import com.raoulvdberge.refinedstorage.RSGui;
import com.raoulvdberge.refinedstorage.tile.TileWirelessTransmitter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockWirelessTransmitter extends BlockNode {
    // From BlockTorch
    private static final AxisAlignedBB WIRELESS_TRANSMITTER_AABB = new AxisAlignedBB(0.4000000059604645D, 0.0D, 0.4000000059604645D, 0.6000000238418579D, 0.6000000238418579D, 0.6000000238418579D);

    public BlockWirelessTransmitter() {
        super("wireless_transmitter");
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileWirelessTransmitter();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            tryOpenNetworkGui(RSGui.WIRELESS_TRANSMITTER, player, world, pos, side);
        }

        return true;
    }

    // DEVIATION: 1.8.9 has no neighborChanged; onNeighborBlockChange is the equivalent hook.
    @Override
    @SuppressWarnings("deprecation")
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block blockIn) {
        if (!canPlaceBlockAt(world, pos) && world.getBlockState(pos).getBlock() == this) {
            dropBlockAsItem(world, pos, state, 0);

            world.setBlockToAir(pos);
        }
    }

    // DEVIATION: 1.8.9 Block has no getBoundingBox; selection bounds go through setBlockBoundsBasedOnState.
    @Override
    @SuppressWarnings("deprecation")
    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
        setBlockBounds(0.4000000059604645F, 0.0F, 0.4000000059604645F, 0.6000000238418579F, 0.6000000238418579F, 0.6000000238418579F);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.getBlockState(pos.offset(EnumFacing.DOWN)).getBlock() instanceof BlockCable;
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    public boolean hasConnectivityState() {
        return true;
    }

    @Override
    @Nullable
    public Direction getDirection() {
        return null;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("block.refinedstorage:wireless_transmitter.tooltip", EnumChatFormatting.WHITE + I18n.format("block.refinedstorage:cable.name") + EnumChatFormatting.GRAY));
    }
}
