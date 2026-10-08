package com.raoulvdberge.refinedstorage.item;

import cofh.api.energy.IEnergyContainerItem;
import com.raoulvdberge.refinedstorage.energy.EnergyStorageAdv;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import java.util.List;

public abstract class ItemEnergyItem extends ItemBase implements IEnergyContainerItem {
    public static final int CAPACITY = 3200;

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_CREATIVE = 1;

    public ItemEnergyItem(String name) {
        super(name);

        setMaxDamage(CAPACITY);
        setMaxStackSize(1);
        setHasSubtypes(true);
    }

    // DEVIATION: 1.8.9 has no energy capability. Items expose RF 1.8 IEnergyContainerItem
    // backed by the same capacity/math and "Energy" NBT key as the 1.11.2 capability path.
    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        return EnergyStorageAdv.forStack(container, CAPACITY, Integer.MAX_VALUE, Integer.MAX_VALUE).receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        return EnergyStorageAdv.forStack(container, CAPACITY, Integer.MAX_VALUE, Integer.MAX_VALUE).extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored(ItemStack container) {
        return EnergyStorageAdv.getStackEnergy(container);
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {
        return CAPACITY;
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - ((double) getEnergyStored(stack) / (double) getMaxEnergyStored(stack));
    }

    // DEVIATION: 1.8.9 Item/RenderItem has no getRGBDurabilityForDisplay (bar color is vanilla
    // default); energy fraction display via getDurabilityForDisplay is preserved.
    @Override
    public boolean isDamaged(ItemStack stack) {
        return stack.getItemDamage() != TYPE_CREATIVE;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        // NO OP
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
        list.add(new ItemStack(item, 1, TYPE_NORMAL));

        ItemStack fullyCharged = new ItemStack(item, 1, TYPE_NORMAL);

        receiveEnergy(fullyCharged, getMaxEnergyStored(fullyCharged), false);

        list.add(fullyCharged);

        list.add(new ItemStack(item, 1, TYPE_CREATIVE));
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);

        if (stack.getItemDamage() != TYPE_CREATIVE) {
            tooltip.add(I18n.format("misc.refinedstorage:energy_stored", getEnergyStored(stack), getMaxEnergyStored(stack)));
        }
    }
}
