package com.raoulvdberge.refinedstorage.apiimpl.solderer;

import com.raoulvdberge.refinedstorage.RSBlocks;
import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.api.solderer.ISoldererRecipe;
import com.raoulvdberge.refinedstorage.block.ItemStorageType;
import com.raoulvdberge.refinedstorage.item.ItemBlockStorage;
import com.raoulvdberge.refinedstorage.item.ItemProcessor;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import javax.annotation.Nonnull;

public class SoldererRecipeStorage implements ISoldererRecipe {
    private ItemStorageType type;
    private List<ItemStack> rows = new ArrayList<>();

    public SoldererRecipeStorage(ItemStorageType type, int storagePart) {
        this.type = type;

        this.rows.add(new ItemStack(RSItems.PROCESSOR, 1, ItemProcessor.TYPE_BASIC));
        this.rows.add(new ItemStack(RSItems.STORAGE_PART, 1, storagePart));
        this.rows.add(new ItemStack(RSBlocks.MACHINE_CASING));
    }

    @Override
    @Nonnull
    public List<ItemStack> getRow(int row) {
        return new ArrayList<>(Collections.singletonList(rows.get(row)));
    }

    @Override
    @Nonnull
    public ItemStack getResult() {
        return ItemBlockStorage.initNBT(new ItemStack(RSBlocks.STORAGE, 1, type.getId()));
    }

    @Override
    public int getDuration() {
        return 200;
    }
}
