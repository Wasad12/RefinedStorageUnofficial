package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.tile.data.TileDataManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public abstract class TileBase extends TileEntity {
    protected static final String NBT_DIRECTION = "Direction";

    private EnumFacing direction = EnumFacing.NORTH;

    protected TileDataManager dataManager = new TileDataManager(this);

    public void setDirection(EnumFacing direction) {
        this.direction = direction;

        // DEVIATION: 1.8.9 TileEntity field is worldObj; notifyNeighborsOfStateChange takes (pos, block).
        worldObj.notifyNeighborsOfStateChange(pos, worldObj.getBlockState(pos).getBlock());

        markDirty();
    }

    public EnumFacing getDirection() {
        return direction;
    }

    public TileDataManager getDataManager() {
        return dataManager;
    }

    public NBTTagCompound write(NBTTagCompound tag) {
        tag.setInteger(NBT_DIRECTION, direction.ordinal());

        return tag;
    }

    public NBTTagCompound writeUpdate(NBTTagCompound tag) {
        tag.setInteger(NBT_DIRECTION, direction.ordinal());

        return tag;
    }

    public void read(NBTTagCompound tag) {
        direction = EnumFacing.getFront(tag.getInteger(NBT_DIRECTION));
    }

    public void readUpdate(NBTTagCompound tag) {
        boolean doRender = canCauseRenderUpdate(tag);

        direction = EnumFacing.getFront(tag.getInteger(NBT_DIRECTION));

        if (doRender) {
            RSUtils.updateBlock(worldObj, pos);
        }
    }

    protected boolean canCauseRenderUpdate(NBTTagCompound tag) {
        return true;
    }

    // DEVIATION: 1.8.9 has no getUpdateTag/handleUpdateTag/getUpdatePacket; sync via getDescriptionPacket.
    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();

        writeUpdate(tag);

        return new S35PacketUpdateTileEntity(pos, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readUpdate(packet.getNbtCompound());

        // DEVIATION: 1.8.9's S35 handler applies data but never re-renders (verified in
        // NetHandlerPlayClient sources). Without this, tile-state visuals (drive disks,
        // controller energy level) stay stale until an unrelated chunk rebuild. This only
        // ever runs client-side (packet handler), so the server is unaffected.
        if (worldObj != null && pos != null) {
            worldObj.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    public void handleUpdateTag(NBTTagCompound tag) {
        super.readFromNBT(tag);

        readUpdate(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        read(tag);
    }

    // DEVIATION: 1.8.9 TileEntity.writeToNBT returns void.
    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        write(tag);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Nullable
    public IItemHandler getDrops() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TileBase && ((TileBase) o).getPos().equals(pos) && ((TileBase) o).worldObj.provider.getDimensionId() == worldObj.provider.getDimensionId();
    }

    @Override
    public int hashCode() {
        int result = pos.hashCode();
        result = 31 * result + worldObj.provider.getDimensionId();
        return result;
    }
}
