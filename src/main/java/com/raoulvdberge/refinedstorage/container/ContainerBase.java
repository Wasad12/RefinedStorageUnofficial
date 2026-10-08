package com.raoulvdberge.refinedstorage.container;

import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.container.slot.*;
import com.raoulvdberge.refinedstorage.tile.TileBase;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public abstract class ContainerBase extends Container {
    private TileBase tile;
    private EntityPlayer player;

    public ContainerBase(TileBase tile, EntityPlayer player) {
        this.tile = tile;
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public TileBase getTile() {
        return tile;
    }

    protected void addPlayerInventory(int xInventory, int yInventory) {
        int id = 0;

        for (int i = 0; i < 9; i++) {
            int x = xInventory + i * 18;
            int y = yInventory + 4 + (3 * 18);

            if (i == player.inventory.currentItem && isHeldItemDisabled()) {
                addSlotToContainer(new SlotDisabled(player.inventory, id, x, y));
            } else {
                addSlotToContainer(new Slot(player.inventory, id, x, y));
            }

            id++;
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new Slot(player.inventory, id, xInventory + x * 18, yInventory + y * 18));

                id++;
            }
        }
    }

    // DEVIATION: 1.8.9 slotClick(int, int, int, EntityPlayer); ClickType.QUICK_MOVE == mode 1.
    // Null replaces null throughout.
    @Override
    public ItemStack slotClick(int id, int clickedButton, int mode, EntityPlayer player) {
        Slot slot = id >= 0 ? getSlot(id) : null;

        if (slot instanceof SlotFilter) {
            if (slot.getStack() != null && slot.getStack().getItem() == RSItems.FILTER) {
                return super.slotClick(id, clickedButton, mode, player);
            }

            if (((SlotFilter) slot).allowsSize()) {
                if (mode == 1) {
                    slot.putStack(null);
                } else if (!RSUtils.isEmpty(player.inventory.getItemStack())) {
                    slot.putStack(player.inventory.getItemStack().copy());
                } else if (slot.getHasStack()) {
                    int amount = slot.getStack().stackSize;

                    if (clickedButton == 0) {
                        amount = Math.max(1, amount - 1);
                    } else if (clickedButton == 1) {
                        amount = Math.min(64, amount + 1);
                    }

                    slot.getStack().stackSize = amount;
                }
            } else if (RSUtils.isEmpty(player.inventory.getItemStack())) {
                slot.putStack(null);
            } else if (slot.isItemValid(player.inventory.getItemStack())) {
                if (player.inventory.getItemStack().getItem() == RSItems.FILTER) {
                    return super.slotClick(id, clickedButton, mode, player);
                }

                slot.putStack(player.inventory.getItemStack().copy());
            }

            return player.inventory.getItemStack();
        } else if (slot instanceof SlotFilterLegacy) {
            if (RSUtils.isEmpty(player.inventory.getItemStack())) {
                slot.putStack(null);
            } else if (slot.isItemValid(player.inventory.getItemStack())) {
                slot.putStack(player.inventory.getItemStack().copy());
            }

            return player.inventory.getItemStack();
        } else if (slot instanceof SlotDisabled) {
            return null;
        }

        return super.slotClick(id, clickedButton, mode, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        return null;
    }

    protected ItemStack mergeItemStackToFilters(ItemStack stack, int begin, int end) {
        for (int i = begin; i < end; ++i) {
            if (API.instance().getComparer().isEqualNoQuantity(getStackFromSlot(getSlot(i)), stack)) {
                return null;
            }
        }

        for (int i = begin; i < end; ++i) {
            Slot slot = getSlot(i);

            if (RSUtils.isEmpty(getStackFromSlot(slot)) && slot.isItemValid(stack)) {
                slot.putStack(ItemHandlerHelper.copyStackWithSize(stack, 1));
                slot.onSlotChanged();

                if (stack.getItem() == RSItems.FILTER) {
                    stack.stackSize = 0;
                }

                return null;
            }
        }

        return null;
    }

    @javax.annotation.Nullable
    private ItemStack getStackFromSlot(Slot slot) {
        ItemStack stackInSlot = slot.getStack();

        if (RSUtils.isEmpty(stackInSlot)) {
            if (slot instanceof SlotFilterFluid) {
                stackInSlot = ((SlotFilterFluid) slot).getRealStack();
            } else if (slot instanceof SlotFilterType) {
                stackInSlot = ((SlotFilterType) slot).getRealStack();
            }
        }

        return stackInSlot;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);

        // DEVIATION: replaces PlayerContainerEvent.Close (absent on 1.8.9).
        com.raoulvdberge.refinedstorage.tile.data.ContainerListener.onContainerClose(player, this);
    }

    protected boolean isHeldItemDisabled() {
        return false;
    }
}
