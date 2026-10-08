package com.raoulvdberge.refinedstorage.inventory;

import java.util.Arrays;
import java.util.List;

import com.raoulvdberge.refinedstorage.RSUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

public class ItemHandlerFilterItems extends ItemStackHandler {
    private ItemStack stack;

    public ItemHandlerFilterItems(ItemStack stack) {
        super(27);

        this.stack = stack;

        if (stack.hasTagCompound()) {
            RSUtils.readItems(this, 0, stack.getTagCompound());
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        RSUtils.writeItems(this, 0, stack.getTagCompound());
    }

    // DEVIATION: 1.8.9 ItemStackHandler stores ItemStack[] (nulls allowed); callers null-guard.
    public List<ItemStack> getFilteredItems() {
        return Arrays.asList(stacks);
    }
}
