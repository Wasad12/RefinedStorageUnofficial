package com.raoulvdberge.refinedstorage.block;

import com.raoulvdberge.refinedstorage.RSBlocks;
import com.raoulvdberge.refinedstorage.RSGui;
import com.raoulvdberge.refinedstorage.api.network.readerwriter.IReaderWriterChannel;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeReader;
import com.raoulvdberge.refinedstorage.tile.TileReader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Map;
import javax.annotation.Nullable;
import java.util.List;

public class BlockReader extends BlockCable {
    public BlockReader() {
        super("reader");
    }

    @Override
    public List<AxisAlignedBB> getNonUnionizedCollisionBoxes(IBlockState state) {
        return RSBlocks.CONSTRUCTOR.getNonUnionizedCollisionBoxes(state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (hitCablePart(state, world, pos, hitX, hitY, hitZ)) {
            return false;
        }

        if (!world.isRemote) {
            NetworkNodeReader reader = ((TileReader) world.getTileEntity(pos)).getNode();

            if (player.isSneaking()) {
                if (reader.getNetwork() != null) {
                    IReaderWriterChannel channel = reader.getNetwork().getReaderWriterChannel(reader.getChannel());

                    if (channel != null) {
                        channel.getHandlers().stream().map(h -> h.getStatusReader(reader, channel)).flatMap(List::stream).forEach(player::addChatMessage);
                    }
                }
            } else if (tryOpenNetworkGui(RSGui.READER_WRITER, player, world, pos, side)) {
                reader.onOpened(player);
            }
        }

        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);

        return tile instanceof TileReader && side == ((TileReader) tile).getDirection().getOpposite();
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileReader();
    }

    @Override
    public boolean hasConnectivityState() {
        return true;
    }

    @Override
    @Nullable
    public Direction getDirection() {
        return Direction.ANY_FACE_PLAYER;
    }
}
