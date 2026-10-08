package com.raoulvdberge.refinedstorage.item;

import com.raoulvdberge.refinedstorage.RSBlocks;
import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeStorage;
import com.raoulvdberge.refinedstorage.apiimpl.storage.StorageDiskItem;
import com.raoulvdberge.refinedstorage.block.ItemStorageType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

public class ItemBlockStorage extends ItemBlockBase {
    public ItemBlockStorage() {
        super(RSBlocks.STORAGE, RSBlocks.STORAGE.getDirection(), true);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        ItemStorageType type = ItemStorageType.getById(stack.getMetadata());

        if (type != null && isValid(stack)) {
            NBTTagCompound tag = stack.getTagCompound().getCompoundTag(NetworkNodeStorage.NBT_STORAGE);

            if (type == ItemStorageType.TYPE_CREATIVE) {
                tooltip.add(I18n.format("misc.refinedstorage:storage.stored", StorageDiskItem.getStored(tag)));
            } else {
                tooltip.add(I18n.format("misc.refinedstorage:storage.stored_capacity", StorageDiskItem.getStored(tag), type.getCapacity()));
            }
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        
        ItemStorageType type = ItemStorageType.getById(stack.getMetadata());

        if (type != null && stack.stackSize == 1 && isValid(stack) && StorageDiskItem.getStored(stack.getTagCompound().getCompoundTag(NetworkNodeStorage.NBT_STORAGE)) <= 0 && stack.getMetadata() != ItemStorageDisk.TYPE_CREATIVE && !world.isRemote && player.isSneaking()) {
            ItemStack storagePart = new ItemStack(RSItems.STORAGE_PART, 1, stack.getMetadata());

            if (!player.inventory.addItemStackToInventory(storagePart.copy())) {
                world.spawnEntityInWorld(new EntityItem(world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), storagePart));
            }

            ItemStack processor = new ItemStack(RSItems.PROCESSOR, 1, ItemProcessor.TYPE_BASIC);

            if (!player.inventory.addItemStackToInventory(processor.copy())) {
                world.spawnEntityInWorld(new EntityItem(world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), processor));
            }

            return new ItemStack(RSBlocks.MACHINE_CASING);
        }

        return stack;
    }

    private static boolean isValid(ItemStack stack) {
        return stack.getTagCompound() != null && stack.getTagCompound().hasKey(NetworkNodeStorage.NBT_STORAGE);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.onUpdate(stack, world, entity, slot, selected);

        if (!stack.hasTagCompound()) {
            initNBT(stack);
        }
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        super.onCreated(stack, world, player);

        initNBT(stack);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, World world) {
        return Integer.MAX_VALUE;
    }

    // DEVIATION: 1.8.9 Item has no getNBTShareTag hook; the full tag syncs instead (see ItemStorageDisk).
    public static ItemStack initNBT(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(NetworkNodeStorage.NBT_STORAGE, StorageDiskItem.getTag());
        stack.setTagCompound(tag);
        return stack;
    }
}
