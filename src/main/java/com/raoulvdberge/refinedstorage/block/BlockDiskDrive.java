package com.raoulvdberge.refinedstorage.block;

import com.raoulvdberge.refinedstorage.RSGui;
import com.raoulvdberge.refinedstorage.tile.TileDiskDrive;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.ExtendedBlockState;

import java.util.List;

public class BlockDiskDrive extends BlockNode {
    public static final PropertyObject<Integer[]> DISK_STATE = new PropertyObject<>("disk_state", Integer[].class);

    public BlockDiskDrive() {
        super("disk_drive");
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileDiskDrive();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            tryOpenNetworkGui(RSGui.DISK_DRIVE, player, world, pos, side);
        }

        return true;
    }

    // DEVIATION: 1.8.9 ExtendedBlockState replaces the builder idiom; DISK_STATE stays unlisted.
    @Override
    protected BlockState createBlockState() {
        List<IProperty> properties = super.getStateProperties();

        return new ExtendedBlockState(this, properties.toArray(new IProperty[properties.size()]), new IUnlistedProperty[] {DISK_STATE});
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IBlockState s = super.getExtendedState(state, world, pos);

        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileDiskDrive) {
            s = ((IExtendedBlockState) s).withProperty(DISK_STATE, ((TileDiskDrive) tile).getDiskState());
        }

        return s;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        ((TileDiskDrive) world.getTileEntity(pos)).getNode().onBreak();

        super.breakBlock(world, pos, state);
    }
}
