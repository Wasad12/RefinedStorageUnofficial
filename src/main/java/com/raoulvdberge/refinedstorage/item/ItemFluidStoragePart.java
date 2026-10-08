package com.raoulvdberge.refinedstorage.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemFluidStoragePart extends ItemBase {
    public static final int TYPE_64K = 0;
    public static final int TYPE_128K = 1;
    public static final int TYPE_256K = 2;
    public static final int TYPE_512K = 3;

    public ItemFluidStoragePart() {
        super("fluid_storage_part");

        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems) {
        for (int i = 0; i <= 3; ++i) {
            subItems.add(new ItemStack(item, 1, i));
        }
    }
}
