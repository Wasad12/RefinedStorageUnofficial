package com.raoulvdberge.refinedstorage.container.slot;

import com.raoulvdberge.refinedstorage.RSUtils;

import com.raoulvdberge.refinedstorage.RSItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotFilter extends SlotItemHandler {
    public static final int FILTER_ALLOW_SIZE = 1;
    public static final int FILTER_ALLOW_BLOCKS = 2;

    private int flags = 0;

    public SlotFilter(IItemHandler handler, int id, int x, int y, int flags) {
        super(handler, id, x, y);

        this.flags = flags;
    }

    // DEVIATION: 1.8.9 SlotItemHandler exposes itemHandler as a field (no getter); re-exposed here.
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public SlotFilter(IItemHandler handler, int id, int x, int y) {
        this(handler, id, x, y, 0);
    }

    // DEVIATION: null guard (1.11 ItemStack.EMPTY.getItem() is null-safe; 1.8.9 null is not).
    @Override
    public boolean canTakeStack(EntityPlayer player) {
        ItemStack stack = getStack();

        return stack != null && stack.getItem() == RSItems.FILTER;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        if (super.isItemValid(stack)) {
            if (allowsBlocks()) {
                // DEVIATION: 1.8.9 has no ItemBlockSpecial (1.11 sign/skull placement helper).
                return stack.getItem() instanceof ItemBlock || stack.getItem() instanceof IPlantable || stack.getItem() instanceof ItemSkull;
            }

            return true;
        }

        return false;
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        if (!RSUtils.isEmpty(stack) && !allowsSize()) {
            stack.stackSize = 1;
        }

        super.putStack(stack);
    }

    public boolean allowsSize() {
        return (flags & FILTER_ALLOW_SIZE) == FILTER_ALLOW_SIZE;
    }

    public boolean allowsBlocks() {
        return (flags & FILTER_ALLOW_BLOCKS) == FILTER_ALLOW_BLOCKS;
    }

    public static IBlockState getBlockState(IBlockAccess world, BlockPos pos, ItemStack stack) {
        if (stack != null) {
            Item item = stack.getItem();

            if (item instanceof ItemBlock) {
                return (((ItemBlock) item).getBlock()).getDefaultState();
            } else if (item instanceof IPlantable) {
                return ((IPlantable) item).getPlant(world, pos);
            } else if (item instanceof ItemSkull) {
                return Blocks.skull.getDefaultState();
            }
        }

        return null;
    }
}
