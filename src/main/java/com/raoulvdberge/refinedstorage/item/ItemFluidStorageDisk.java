package com.raoulvdberge.refinedstorage.item;

import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.api.storage.IStorageDisk;
import com.raoulvdberge.refinedstorage.api.storage.IStorageDiskProvider;
import com.raoulvdberge.refinedstorage.api.storage.StorageDiskType;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.apiimpl.storage.StorageDiskFluid;
import com.raoulvdberge.refinedstorage.block.FluidStorageType;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidContainerRegistry;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemFluidStorageDisk extends ItemBase implements IStorageDiskProvider<FluidStack> {
    public static final int TYPE_64K = 0;
    public static final int TYPE_128K = 1;
    public static final int TYPE_256K = 2;
    public static final int TYPE_512K = 3;
    public static final int TYPE_CREATIVE = 4;
    public static final int TYPE_DEBUG = 5;

    private NBTTagCompound debugDiskTag;

    public ItemFluidStorageDisk() {
        super("fluid_storage_disk");

        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems) {
        for (int i = 0; i < 5; ++i) {
            subItems.add(API.instance().getDefaultStorageDiskBehavior().initDisk(StorageDiskType.FLUIDS, new ItemStack(item, 1, i)));
        }
    }

    private void applyDebugDiskData(ItemStack stack) {
        if (debugDiskTag == null) {
            debugDiskTag = API.instance().getDefaultStorageDiskBehavior().getTag(StorageDiskType.FLUIDS);

            StorageDiskFluid storage = new StorageDiskFluid(debugDiskTag, -1);

            for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
                storage.insert(new FluidStack(fluid, 0), FluidContainerRegistry.BUCKET_VOLUME * 1000, false);
            }

            storage.writeToNBT();
        }

        // DEVIATION: 1.8.9 NBTBase.copy returns NBTBase; cast is safe (always a compound here).
        stack.setTagCompound((NBTTagCompound) debugDiskTag.copy());
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.onUpdate(stack, world, entity, slot, selected);

        if (!stack.hasTagCompound()) {
            if (stack.getMetadata() == TYPE_DEBUG) {
                applyDebugDiskData(stack);
            } else {
                API.instance().getDefaultStorageDiskBehavior().initDisk(StorageDiskType.FLUIDS, stack);
            }
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        ItemStack disk = stack;

        IStorageDisk storage = create(disk);

        if (!world.isRemote && player.isSneaking() && storage.isValid(disk) && storage.getStored() <= 0 && disk.getMetadata() != TYPE_CREATIVE) {
            ItemStack storagePart = new ItemStack(RSItems.FLUID_STORAGE_PART, 1, disk.getMetadata());

            if (!player.inventory.addItemStackToInventory(storagePart.copy())) {
                world.spawnEntityInWorld(new EntityItem(world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), storagePart));
            }

            return new ItemStack(RSItems.STORAGE_HOUSING);
        }

        return disk;
    }

    @Override
    public void addInformation(ItemStack disk, EntityPlayer player, List<String> tooltip, boolean advanced) {
        IStorageDisk storage = create(disk);

        if (storage.isValid(disk)) {
            if (storage.getCapacity() == -1) {
                tooltip.add(I18n.format("misc.refinedstorage:storage.stored", storage.getStored()));
            } else {
                tooltip.add(I18n.format("misc.refinedstorage:storage.stored_capacity", storage.getStored(), storage.getCapacity()));
            }
        }
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        super.onCreated(stack, world, player);

        API.instance().getDefaultStorageDiskBehavior().initDisk(StorageDiskType.FLUIDS, stack);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, World world) {
        return Integer.MAX_VALUE;
    }

    // DEVIATION: 1.8.9 Item has no getNBTShareTag hook; the full tag syncs instead (see ItemStorageDisk).
    @Nonnull
    @Override
    public IStorageDisk<FluidStack> create(ItemStack disk) {
        return API.instance().getDefaultStorageDiskBehavior().createFluidStorage(disk.getTagCompound(), FluidStorageType.getById(disk.getItemDamage()).getCapacity());
    }
}
