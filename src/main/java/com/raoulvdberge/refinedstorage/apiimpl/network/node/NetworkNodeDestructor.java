package com.raoulvdberge.refinedstorage.apiimpl.network.node;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerBase;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerFluid;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerListenerNetworkNode;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerUpgrade;
import com.raoulvdberge.refinedstorage.item.ItemUpgrade;
import com.raoulvdberge.refinedstorage.tile.TileDestructor;
import com.raoulvdberge.refinedstorage.tile.config.IComparable;
import com.raoulvdberge.refinedstorage.tile.config.IFilterable;
import com.raoulvdberge.refinedstorage.tile.config.IType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkNodeDestructor extends NetworkNode implements IComparable, IFilterable, IType {
    public static final String ID = "destructor";

    private static final String NBT_COMPARE = "Compare";
    private static final String NBT_MODE = "Mode";
    private static final String NBT_TYPE = "Type";
    private static final String NBT_PICKUP = "Pickup";

    private static final int BASE_SPEED = 20;

    private ItemHandlerBase itemFilters = new ItemHandlerBase(9, new ItemHandlerListenerNetworkNode(this));
    private ItemHandlerFluid fluidFilters = new ItemHandlerFluid(9, new ItemHandlerListenerNetworkNode(this));

    private ItemHandlerUpgrade upgrades = new ItemHandlerUpgrade(4, new ItemHandlerListenerNetworkNode(this), ItemUpgrade.TYPE_SPEED, ItemUpgrade.TYPE_SILK_TOUCH, ItemUpgrade.TYPE_FORTUNE);

    private int compare = IComparer.COMPARE_NBT | IComparer.COMPARE_DAMAGE;
    private int mode = IFilterable.WHITELIST;
    private int type = IType.ITEMS;
    private boolean pickupItem = false;

    public NetworkNodeDestructor(World world, BlockPos pos) {
        super(world, pos);
    }

    @Override
    public int getEnergyUsage() {
        return RS.INSTANCE.config.destructorUsage + upgrades.getEnergyUsage();
    }

    @Override
    public void update() {
        super.update();

        if (network != null && canUpdate() && ticks % upgrades.getSpeed(BASE_SPEED, 4) == 0) {
            BlockPos front = pos.offset(getDirection());

            if (pickupItem && type == IType.ITEMS) {
                List<Entity> droppedItems = new ArrayList<>();

                Chunk chunk = world.getChunkFromBlockCoords(front);
                chunk.getEntitiesWithinAABBForEntity(null, new AxisAlignedBB(front.getX(), front.getY(), front.getZ(), front.getX() + 1, front.getY() + 1, front.getZ() + 1), droppedItems, null);

                for (Entity entity : droppedItems) {
                    if (entity instanceof EntityItem) {
                        // DEVIATION: 1.8.9 EntityItem exposes getEntityItem, not getItem.
                        ItemStack droppedItem = ((EntityItem) entity).getEntityItem();

                        if (IFilterable.canTake(itemFilters, mode, compare, droppedItem) && network.insertItem(droppedItem, droppedItem.stackSize, true) == null) {
                            network.insertItemTracked(droppedItem.copy(), droppedItem.stackSize);

                            world.removeEntity(entity);

                            break;
                        }
                    }
                }
            } else if (type == IType.ITEMS) {
                IBlockState frontBlockState = world.getBlockState(front);
                Block frontBlock = frontBlockState.getBlock();

                // DEVIATION: 1.8.9 getPickBlock takes (target, world, pos[, player]); playAuxSFX replaces playEvent.
                ItemStack frontStack = frontBlock.getPickBlock(new MovingObjectPosition(new Vec3(front.getX() + 0.5D, front.getY() + 0.5D, front.getZ() + 0.5D), EnumFacing.UP, front), world, front);

                if (!RSUtils.isEmpty(frontStack)) {
                    if (IFilterable.canTake(itemFilters, mode, compare, frontStack) && frontBlockState.getBlock().getBlockHardness(world, front) != -1.0) {
                        List<ItemStack> drops;
                        if (upgrades.hasUpgrade(ItemUpgrade.TYPE_SILK_TOUCH) && frontBlock.canSilkHarvest(world, front, frontBlockState, null)) {
                            drops = Collections.singletonList(frontStack);
                        } else {
                            drops = frontBlock.getDrops(world, front, frontBlockState, upgrades.getFortuneLevel());
                        }

                        for (ItemStack drop : drops) {
                            if (network.insertItem(drop, drop.stackSize, true) != null) {
                                return;
                            }
                        }

                        BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, front, frontBlockState, FakePlayerFactory.getMinecraft((WorldServer) world));

                        if (!MinecraftForge.EVENT_BUS.post(e)) {
                            world.playAuxSFX(2001, front, Block.getStateId(frontBlockState));
                            world.setBlockToAir(front);

                            for (ItemStack drop : drops) {
                                // We check if the controller isn't null here because when a destructor faces a node and removes it
                                // it will essentially remove this block itself from the network without knowing
                                if (network == null) {
                                    world.spawnEntityInWorld(new EntityItem(world, front.getX(), front.getY(), front.getZ(), drop));
                                } else {
                                    network.insertItemTracked(drop, drop.stackSize);
                                }
                            }
                        }
                    }
                }
            } else if (type == IType.FLUIDS) {
                FluidStack stack = drainFluidBlock(front, false);

                if (stack != null && IFilterable.canTakeFluids(fluidFilters, mode, compare, stack) && network.insertFluid(stack, stack.amount, true) == null) {
                    FluidStack drained = drainFluidBlock(front, true);

                    network.insertFluid(drained, drained.amount, false);
                }
            }
        }
    }

    // DEVIATION: 1.11 fluid wrappers (BlockLiquidWrapper/FluidBlockWrapper) don't exist on 1.8.9.
    // Inline equivalent: drain still vanilla liquids and IFluidBlock mod fluids for one bucket.
    @javax.annotation.Nullable
    private FluidStack drainFluidBlock(BlockPos front, boolean doDrain) {
        IBlockState state = world.getBlockState(front);
        Block block = state.getBlock();

        boolean stillWater = (block == Blocks.water || block == Blocks.flowing_water)
            && state.getValue(BlockLiquid.LEVEL) == 0;
        boolean stillLava = (block == Blocks.lava || block == Blocks.flowing_lava)
            && state.getValue(BlockLiquid.LEVEL) == 0;

        if (stillWater || stillLava) {
            if (doDrain) {
                world.setBlockToAir(front);
            }

            return new FluidStack(stillWater ? FluidRegistry.WATER : FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME);
        }

        if (block instanceof IFluidBlock) {
            FluidStack simulated = ((IFluidBlock) block).drain(world, front, false);

            if (simulated != null && simulated.amount >= FluidContainerRegistry.BUCKET_VOLUME) {
                FluidStack result = new FluidStack(simulated.getFluid(), FluidContainerRegistry.BUCKET_VOLUME);

                if (doDrain) {
                    ((IFluidBlock) block).drain(world, front, true);
                }

                return result;
            }
        }

        return null;
    }

    @Override
    public int getCompare() {
        return compare;
    }

    @Override
    public void setCompare(int compare) {
        this.compare = compare;

        markDirty();
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public void setMode(int mode) {
        this.mode = mode;

        markDirty();
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);

        RSUtils.readItems(upgrades, 1, tag);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        RSUtils.writeItems(upgrades, 1, tag);

        return tag;
    }

    @Override
    public NBTTagCompound writeConfiguration(NBTTagCompound tag) {
        super.writeConfiguration(tag);

        tag.setInteger(NBT_COMPARE, compare);
        tag.setInteger(NBT_MODE, mode);
        tag.setInteger(NBT_TYPE, type);
        tag.setBoolean(NBT_PICKUP, pickupItem);

        RSUtils.writeItems(itemFilters, 0, tag);
        RSUtils.writeItems(fluidFilters, 2, tag);

        return tag;
    }

    @Override
    public void readConfiguration(NBTTagCompound tag) {
        super.readConfiguration(tag);

        if (tag.hasKey(NBT_COMPARE)) {
            compare = tag.getInteger(NBT_COMPARE);
        }

        if (tag.hasKey(NBT_MODE)) {
            mode = tag.getInteger(NBT_MODE);
        }

        if (tag.hasKey(NBT_TYPE)) {
            type = tag.getInteger(NBT_TYPE);
        }

        if (tag.hasKey(NBT_PICKUP)) {
            pickupItem = tag.getBoolean(NBT_PICKUP);
        }

        RSUtils.readItems(itemFilters, 0, tag);
        RSUtils.readItems(fluidFilters, 2, tag);
    }

    public IItemHandler getUpgrades() {
        return upgrades;
    }

    public IItemHandler getInventory() {
        return itemFilters;
    }

    @Override
    public boolean hasConnectivityState() {
        return true;
    }

    @Override
    public IItemHandler getDrops() {
        return upgrades;
    }

    @Override
    public int getType() {
        return world.isRemote ? TileDestructor.TYPE.getValue() : type;
    }

    @Override
    public void setType(int type) {
        this.type = type;

        markDirty();
    }

    @Override
    public IItemHandler getFilterInventory() {
        return getType() == IType.ITEMS ? itemFilters : fluidFilters;
    }

    public boolean isPickupItem() {
        return pickupItem;
    }

    public void setPickupItem(boolean pickupItem) {
        this.pickupItem = pickupItem;
    }
}
