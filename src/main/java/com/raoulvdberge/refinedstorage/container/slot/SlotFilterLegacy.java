package com.raoulvdberge.refinedstorage.container.slot;

import com.raoulvdberge.refinedstorage.RSUtils;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class SlotFilterLegacy extends Slot {
    public SlotFilterLegacy(IInventory inventory, int id, int x, int y) {
        super(inventory, id, x, y);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return true;
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        if (!RSUtils.isEmpty(stack)) {
            stack.stackSize = 1;
        }

        super.putStack(stack);
    }
}
