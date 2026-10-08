package com.raoulvdberge.refinedstorage.item;

import com.raoulvdberge.refinedstorage.api.util.IWrenchable;
import com.raoulvdberge.refinedstorage.block.BlockNode;
import com.raoulvdberge.refinedstorage.tile.TileNode;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemWrench extends ItemBase {
    private enum WrenchMode {
        ROTATION(0),
        CONFIGURATION(1),
        DISMANTLING(2);

        private final int id;

        WrenchMode(int id) {
            this.id = id;
        }

        public WrenchMode cycle() {
            return this == ROTATION ? CONFIGURATION : (this == CONFIGURATION ? DISMANTLING : ROTATION);
        }

        public NBTTagCompound writeToNBT(NBTTagCompound tag) {
            tag.setInteger(NBT_WRENCH_MODE, id);

            return tag;
        }

        public static WrenchMode readFromNBT(NBTTagCompound tag) {
            if (tag != null && tag.hasKey(NBT_WRENCH_MODE)) {
                int id = tag.getInteger(NBT_WRENCH_MODE);

                for (WrenchMode mode : values()) {
                    if (mode.id == id) {
                        return mode;
                    }
                }
            }

            return ROTATION;
        }
    }

    private static final String NBT_WRENCH_MODE = "WrenchMode";
    private static final String NBT_WRENCHED_DATA = "WrenchedData";
    private static final String NBT_WRENCHED_TILE = "WrenchedTile";

    public ItemWrench() {
        super("wrench");

        setMaxStackSize(1);
    }

    // DEVIATION: 1.8.9 has no off-hand or EnumActionResult. Main-hand only; SUCCESS/PASS map to true/false.
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            return false;
        }

        if (world.isRemote) {
            return true;
        }

        WrenchMode mode = WrenchMode.readFromNBT(stack.getTagCompound());

        if (mode == WrenchMode.ROTATION) {
            Block block = world.getBlockState(pos).getBlock();

            block.rotateBlock(world, pos, player.getHorizontalFacing().getOpposite());

            return true;
        } else if (mode == WrenchMode.CONFIGURATION) {
            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof IWrenchable) {
                IWrenchable wrenchable = (IWrenchable) tile;

                boolean canWrite = false;

                if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_WRENCHED_DATA) && stack.getTagCompound().hasKey(NBT_WRENCHED_TILE)) {
                    NBTTagCompound wrenchedData = stack.getTagCompound().getCompoundTag(NBT_WRENCHED_DATA);
                    String wrenchedTile = stack.getTagCompound().getString(NBT_WRENCHED_TILE);

                    if (wrenchable.getClass().getName().equals(wrenchedTile)) {
                        wrenchable.readConfiguration(wrenchedData);

                        tile.markDirty();

                        player.addChatMessage(new ChatComponentTranslation("item.refinedstorage:wrench.read"));
                    } else {
                        canWrite = true;
                    }
                } else {
                    canWrite = true;
                }

                if (canWrite) {
                    stack.getTagCompound().setString(NBT_WRENCHED_TILE, wrenchable.getClass().getName());
                    stack.getTagCompound().setTag(NBT_WRENCHED_DATA, wrenchable.writeConfiguration(new NBTTagCompound()));

                    player.addChatMessage(new ChatComponentTranslation("item.refinedstorage:wrench.saved"));
                }

                return true;
            }
        } else if (mode == WrenchMode.DISMANTLING) {
            TileEntity tile = world.getTileEntity(pos);
            IBlockState state = world.getBlockState(pos);

            if (tile instanceof TileNode) {
                NBTTagCompound data = new NBTTagCompound();

                ((TileNode) tile).writeConfiguration(data);

                ItemStack tileStack = state.getBlock().getDrops(world, pos, state, 0).get(0);

                if (!tileStack.hasTagCompound()) {
                    tileStack.setTagCompound(new NBTTagCompound());
                }

                tileStack.getTagCompound().setTag(BlockNode.NBT_REFINED_STORAGE_DATA, data);

                world.setBlockToAir(pos);

                world.spawnEntityInWorld(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), tileStack));
            }
        }

        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && !player.isSneaking()) {
            WrenchMode mode = WrenchMode.readFromNBT(stack.getTagCompound());

            if (stack.hasTagCompound()) {
                stack.getTagCompound().removeTag(NBT_WRENCHED_TILE);
                stack.getTagCompound().removeTag(NBT_WRENCHED_DATA);
            } else {
                stack.setTagCompound(new NBTTagCompound());
            }

            WrenchMode next = mode.cycle();

            next.writeToNBT(stack.getTagCompound());

            player.addChatMessage(new ChatComponentTranslation(
                "item.refinedstorage:wrench.mode",
                new ChatComponentTranslation("item.refinedstorage:wrench.mode." + next.id).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW))
            ));
        }

        return super.onItemRightClick(stack, world, player);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);

        WrenchMode mode = WrenchMode.readFromNBT(stack.getTagCompound());

        tooltip.add(I18n.format("item.refinedstorage:wrench.mode", EnumChatFormatting.YELLOW + I18n.format("item.refinedstorage:wrench.mode." + mode.id) + EnumChatFormatting.RESET));
    }
}
