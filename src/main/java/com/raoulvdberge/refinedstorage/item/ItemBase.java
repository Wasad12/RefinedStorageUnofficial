package com.raoulvdberge.refinedstorage.item;

import com.raoulvdberge.refinedstorage.RS;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class ItemBase extends Item {
    private final String name;

    public ItemBase(String name) {
        this.name = name;

        setCreativeTab(RS.INSTANCE.tab);
    }

    // DEVIATION: 1.8.9 has no IForgeRegistry/setRegistryName. The plain name is
    // passed explicitly to GameRegistry.registerItem (ProxyCommon).
    public String getName() {
        return name;
    }

    @Override
    public String getUnlocalizedName() {
        return "item." + RS.ID + ":" + name;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (getHasSubtypes()) {
            return getUnlocalizedName() + "." + stack.getItemDamage();
        }

        return getUnlocalizedName();
    }
}
