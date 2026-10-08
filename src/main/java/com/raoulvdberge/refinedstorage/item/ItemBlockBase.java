package com.raoulvdberge.refinedstorage.item;

import com.raoulvdberge.refinedstorage.block.Direction;
import com.raoulvdberge.refinedstorage.tile.TileBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class ItemBlockBase extends ItemBlock {
    private Direction direction;
    private final String name;
    private final com.raoulvdberge.refinedstorage.block.BlockBase base;

    public ItemBlockBase(Block block, Direction direction, boolean subtypes) {
        super(block);

        // DEVIATION: 1.8.9 has no getRegistryName; all RS ItemBlocks wrap a BlockBase.
        this.base = (com.raoulvdberge.refinedstorage.block.BlockBase) block;
        this.name = this.base.getName();

        this.direction = direction;

        if (subtypes) {
            setMaxDamage(0);
            setHasSubtypes(true);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, java.util.List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);

        base.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (getHasSubtypes()) {
            return getUnlocalizedName() + "." + stack.getItemDamage();
        }

        return getUnlocalizedName();
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        boolean result = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);

        if (result && direction != null) {
            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof TileBase) {
                ((TileBase) tile).setDirection(direction.getFrom(side, pos, player));
            }
        }

        return result;
    }
}
