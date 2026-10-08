package com.raoulvdberge.refinedstorage.item;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSBlocks;
import com.raoulvdberge.refinedstorage.RSGui;
import com.raoulvdberge.refinedstorage.tile.grid.portable.PortableGrid;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemBlockPortableGrid extends ItemBlockEnergyItem {
    public ItemBlockPortableGrid() {
        super(RSBlocks.PORTABLE_GRID, RSBlocks.PORTABLE_GRID.getDirection());
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        
        if (!world.isRemote) {
            player.openGui(RS.INSTANCE, RSGui.WIRELESS_GRID, player.getEntityWorld(), 0, 0, PortableGrid.GRID_TYPE);
        }

        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);

        tooltip.add(I18n.format("block.refinedstorage:portable_grid.tooltip"));
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            return false;
        }

        return super.onItemUse(stack, player, worldIn, pos, facing, hitX, hitY, hitZ);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.getItem() == newStack.getItem()) {
            if (ItemWirelessGrid.getSortingDirection(oldStack) == ItemWirelessGrid.getSortingDirection(newStack) &&
                ItemWirelessGrid.getSortingType(oldStack) == ItemWirelessGrid.getSortingType(newStack) &&
                ItemWirelessGrid.getSearchBoxMode(oldStack) == ItemWirelessGrid.getSearchBoxMode(newStack) &&
                ItemWirelessGrid.getTabSelected(oldStack) == ItemWirelessGrid.getTabSelected(newStack) &&
                ItemWirelessGrid.getSize(oldStack) == ItemWirelessGrid.getSize(newStack)) {
                return false;
            }
        }

        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}
