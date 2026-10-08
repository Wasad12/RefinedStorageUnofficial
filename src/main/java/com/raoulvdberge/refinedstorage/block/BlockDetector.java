package com.raoulvdberge.refinedstorage.block;

import com.raoulvdberge.refinedstorage.RSGui;
import com.raoulvdberge.refinedstorage.tile.TileDetector;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.util.List;

public class BlockDetector extends BlockNode {
    private static final AxisAlignedBB AABB_DETECTOR = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 5D / 16D, 1.0D);

    private static final PropertyBool POWERED = PropertyBool.create("powered");

    public BlockDetector() {
        super("detector");
    }

    @Override
    protected List<IProperty> getStateProperties() {
        List<IProperty> properties = super.getStateProperties();

        properties.add(POWERED);

        return properties;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return super.getActualState(state, world, pos)
            .withProperty(POWERED, ((TileDetector) world.getTileEntity(pos)).getNode().isPowered());
    }

    // DEVIATION: 1.8.9 Block has no getBoundingBox; selection bounds go through setBlockBoundsBasedOnState.
    @Override
    @SuppressWarnings("deprecation")
    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 5F / 16F, 1.0F);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileDetector();
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        return ((TileDetector) world.getTileEntity(pos)).getNode().isPowered() ? 15 : 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            tryOpenNetworkGui(RSGui.DETECTOR, player, world, pos, side);
        }

        return true;
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
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    @Nullable
    public Direction getDirection() {
        return null;
    }
}
