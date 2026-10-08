package com.raoulvdberge.refinedstorage.container.slot;

import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNode;
import com.raoulvdberge.refinedstorage.tile.config.IType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class SlotFilterType extends SlotFilter {
    private IType type;
    private int filterId;

    public SlotFilterType(IType type, int id, int x, int y, int flags) {
        // DEVIATION: 1.8.9 SlotItemHandler reads its itemHandler FIELD directly (public final,
        // no virtual getter like 1.11), so passing null + overriding getItemHandler() leaves every
        // slot operation hitting a null field (NPE on open: server detectAndSendChanges + client
        // drawSlot both call getStack()). Pass the current inventory so the field is never null,
        // and override every field-touching method below to use the DYNAMIC getItemHandler()
        // (the filter inventory switches between item/fluid handlers at runtime).
        super(type.getFilterInventory(), id, x, y, flags);

        this.type = type;
        this.filterId = id;
    }

    public SlotFilterType(IType type, int id, int x, int y) {
        this(type, id, x, y, 0);
    }

    @Override
    public IItemHandler getItemHandler() {
        return type.getFilterInventory();
    }

    @Override
    public boolean allowsSize() {
        return super.allowsSize() && type.getType() != IType.FLUIDS;
    }

    @Override
    public boolean allowsBlocks() {
        return super.allowsBlocks() && type.getType() == IType.ITEMS;
    }

    @Override
    public ItemStack getStack() {
        if (type.getType() == IType.ITEMS || !((NetworkNode) type).getWorld().isRemote) {
            return getDynamicStack();
        }

        return null;
    }

    public ItemStack getRealStack() {
        return getDynamicStack();
    }

    private ItemStack getDynamicStack() {
        IItemHandler handler = getItemHandler();

        return handler == null ? null : handler.getStackInSlot(filterId);
    }

    // DEVIATION: replicates 1.8.9 SlotItemHandler.isItemValid against the dynamic handler,
    // then applies SlotFilter's allowsBlocks rule (super.isItemValid would simulate against
    // the stale construction-time field).
    @Override
    public boolean isItemValid(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        IItemHandler handler = getItemHandler();

        if (handler == null) {
            return false;
        }

        ItemStack remainder = handler.insertItem(filterId, stack, true);

        if (remainder == null || remainder.stackSize >= stack.stackSize) {
            return false;
        }

        if (allowsBlocks()) {
            return stack.getItem() instanceof ItemBlock || stack.getItem() instanceof IPlantable || stack.getItem() instanceof ItemSkull;
        }

        return true;
    }

    // DEVIATION: replicates SlotFilter's size clamp + 1.8.9 SlotItemHandler.putStack against
    // the dynamic handler.
    @Override
    public void putStack(ItemStack stack) {
        if (!RSUtils.isEmpty(stack) && !allowsSize()) {
            stack.stackSize = 1;
        }

        IItemHandler handler = getItemHandler();

        if (handler instanceof IItemHandlerModifiable) {
            ((IItemHandlerModifiable) handler).setStackInSlot(filterId, stack);
        }

        onSlotChanged();
    }

    // DEVIATION: replicates 1.8.9 SlotItemHandler.decrStackSize against the dynamic handler.
    @Override
    public ItemStack decrStackSize(int amount) {
        IItemHandler handler = getItemHandler();

        return handler == null ? null : handler.extractItem(filterId, amount, false);
    }

    // DEVIATION: replicates 1.8.9 SlotItemHandler.getItemStackLimit against the dynamic handler.
    @Override
    public int getItemStackLimit(ItemStack stack) {
        ItemStack maxAdd = stack.copy();
        maxAdd.stackSize = stack.getMaxStackSize();

        IItemHandler handler = getItemHandler();

        ItemStack currentStack = handler == null ? null : handler.getStackInSlot(filterId);
        ItemStack remainder = handler == null ? maxAdd : handler.insertItem(filterId, maxAdd, true);

        int current = currentStack != null ? currentStack.stackSize : 0;
        int added = maxAdd.stackSize - (remainder != null ? remainder.stackSize : 0);

        return current + added;
    }
}
