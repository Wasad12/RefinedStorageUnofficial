package com.raoulvdberge.refinedstorage.item;

import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPatternContainer;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPatternProvider;
import com.raoulvdberge.refinedstorage.api.util.IStackList;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.apiimpl.autocrafting.CraftingPattern;
import com.raoulvdberge.refinedstorage.render.BakedModelPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemPattern extends ItemBase implements ICraftingPatternProvider {
    /**
     * A cache that maps a stack to a crafting pattern.
     * Only used client side for rendering and tooltips, to avoid crafting pattern allocations and crafting pattern output calculation (which is expensive).
     */
    private static Map<ItemStack, CraftingPattern> PATTERN_CACHE = new HashMap<>();

    private static final String NBT_SLOT = "Slot_%d";
    private static final String NBT_OUTPUTS = "Outputs";
    private static final String NBT_OREDICT = "Oredict";
    private static final String NBT_BLOCKING = "Blocking";

    public ItemPattern() {
        super("pattern");
    }

    // DEVIATION: 1.8.9 has no ItemColors registry; per-item tint via override (same logic
    // as the 1.11 color handler: output's color when shift-showing a valid single output).
    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        if (Minecraft.getMinecraft().theWorld != null) {
            CraftingPattern pattern = getPatternFromCache(Minecraft.getMinecraft().theWorld, stack);

            if (BakedModelPattern.canDisplayPatternOutput(pattern) && !pattern.getOutputs().isEmpty() && pattern.getOutputs().get(0) != null) {
                ItemStack output = pattern.getOutputs().get(0);

                int color = output.getItem().getColorFromItemStack(output, renderPass);

                if (color != -1) {
                    return color;
                }
            }
        }

        return 0xFFFFFF;
    }

    public static CraftingPattern getPatternFromCache(World world, ItemStack stack) {
        if (!PATTERN_CACHE.containsKey(stack)) {
            PATTERN_CACHE.put(stack, new CraftingPattern(world, null, stack));
        }

        return PATTERN_CACHE.get(stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        if (!stack.hasTagCompound()) {
            return;
        }

        ICraftingPattern pattern = getPatternFromCache(player.getEntityWorld(), stack);

        if (pattern.isValid()) {
            if (GuiScreen.isShiftKeyDown() || isProcessing(stack)) {
                tooltip.add(EnumChatFormatting.YELLOW + I18n.format("misc.refinedstorage:pattern.inputs") + EnumChatFormatting.RESET);

                combineItems(tooltip, true, RSUtils.toNonNullList(pattern.getInputs()));

                tooltip.add(EnumChatFormatting.YELLOW + I18n.format("misc.refinedstorage:pattern.outputs") + EnumChatFormatting.RESET);
            }

            combineItems(tooltip, true, RSUtils.toNonNullList(pattern.getOutputs()));

            if (isOredict(stack)) {
                tooltip.add(EnumChatFormatting.BLUE + I18n.format("misc.refinedstorage:pattern.oredict") + EnumChatFormatting.RESET);
            }

            if (isBlocking(stack)) {
                tooltip.add(EnumChatFormatting.BLUE + I18n.format("misc.refinedstorage:blocking") + EnumChatFormatting.RESET);
            }
        } else {
            tooltip.add(EnumChatFormatting.RED + I18n.format("misc.refinedstorage:pattern.invalid") + EnumChatFormatting.RESET);

            // Display a helpful message stating the outputs if this is a legacy pattern
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Inputs") && stack.getTagCompound().hasKey("Outputs")) {
                tooltip.add(EnumChatFormatting.WHITE + "This pattern is a legacy pattern made before RS 1.0, please re-make!" + EnumChatFormatting.RESET);

                tooltip.add("This pattern used to output:");

                NBTTagList outputsTag = stack.getTagCompound().getTagList("Outputs", Constants.NBT.TAG_COMPOUND);

                List<ItemStack> outputs = new ArrayList<>();

                for (int i = 0; i < outputsTag.tagCount(); ++i) {
                    outputs.add(ItemStack.loadItemStackFromNBT(outputsTag.getCompoundTagAt(i)));
                }

                combineItems(tooltip, true, outputs);

                if (stack.getTagCompound().hasKey("Processing") && stack.getTagCompound().getBoolean("Processing")) {
                    tooltip.add(EnumChatFormatting.GREEN + "This pattern was a processing pattern!" + EnumChatFormatting.RESET);
                }
            }
        }
    }

    public static void setSlot(ItemStack pattern, int slot, ItemStack stack) {
        if (!pattern.hasTagCompound()) {
            pattern.setTagCompound(new NBTTagCompound());
        }

        pattern.getTagCompound().setTag(String.format(NBT_SLOT, slot), stack.serializeNBT());
    }

    public static ItemStack getSlot(ItemStack pattern, int slot) {
        String id = String.format(NBT_SLOT, slot);

        if (!pattern.hasTagCompound() || !pattern.getTagCompound().hasKey(id)) {
            return null;
        }

        return ItemStack.loadItemStackFromNBT(pattern.getTagCompound().getCompoundTag(id));
    }

    public static void addOutput(ItemStack pattern, ItemStack output) {
        if (!pattern.hasTagCompound()) {
            pattern.setTagCompound(new NBTTagCompound());
        }

        NBTTagList outputs;
        if (!pattern.getTagCompound().hasKey(NBT_OUTPUTS)) {
            outputs = new NBTTagList();
        } else {
            outputs = pattern.getTagCompound().getTagList(NBT_OUTPUTS, Constants.NBT.TAG_COMPOUND);
        }

        outputs.appendTag(output.serializeNBT());

        pattern.getTagCompound().setTag(NBT_OUTPUTS, outputs);
    }

    public static List<ItemStack> getOutputs(ItemStack pattern) {
        if (!isProcessing(pattern)) {
            return null;
        }

        IStackList<ItemStack> outputs = API.instance().createItemStackList();

        NBTTagList outputsTag = pattern.getTagCompound().getTagList(NBT_OUTPUTS, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < outputsTag.tagCount(); ++i) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(outputsTag.getCompoundTagAt(i));

            if (!RSUtils.isEmpty(stack)) {
                outputs.add(stack);
            }
        }

        return new ArrayList<>(outputs.getStacks());
    }

    public static boolean isProcessing(ItemStack pattern) {
        return pattern.hasTagCompound() && pattern.getTagCompound().hasKey(NBT_OUTPUTS);
    }

    public static boolean isOredict(ItemStack pattern) {
        return pattern.hasTagCompound() && pattern.getTagCompound().hasKey(NBT_OREDICT) && pattern.getTagCompound().getBoolean(NBT_OREDICT);
    }

    public static boolean isBlocking(ItemStack pattern) {
        return pattern.hasTagCompound() && pattern.getTagCompound().hasKey(NBT_BLOCKING) && pattern.getTagCompound().getBoolean(NBT_BLOCKING);
    }

    public static void setOredict(ItemStack pattern, boolean oredict) {
        if (!pattern.hasTagCompound()) {
            pattern.setTagCompound(new NBTTagCompound());
        }

        pattern.getTagCompound().setBoolean(NBT_OREDICT, oredict);
    }

    public static void setBlocking(ItemStack pattern, boolean blockingTask) {
        if (!pattern.hasTagCompound()) {
            pattern.setTagCompound(new NBTTagCompound());
        }

        pattern.getTagCompound().setBoolean(NBT_BLOCKING, blockingTask);
    }

    public static void combineItems(List<String> tooltip, boolean displayAmount, List<ItemStack> stacks) {
        Set<Integer> combinedIndices = new HashSet<>();

        for (int i = 0; i < stacks.size(); ++i) {
            if (!RSUtils.isEmpty(stacks.get(i)) && !combinedIndices.contains(i)) {
                ItemStack stack = stacks.get(i);

                String data = stack.getDisplayName();

                int amount = stack.stackSize;

                for (int j = i + 1; j < stacks.size(); ++j) {
                    if (API.instance().getComparer().isEqual(stack, stacks.get(j))) {
                        amount += stacks.get(j).stackSize;

                        combinedIndices.add(j);
                    }
                }

                data = (displayAmount ? (EnumChatFormatting.WHITE + String.valueOf(amount) + " ") : "") + EnumChatFormatting.GRAY + data;

                tooltip.add(data);
            }
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && player.isSneaking()) {
            return new ItemStack(RSItems.PATTERN, stack.stackSize);
        }

        return stack;
    }

    @Override
    @Nonnull
    public ICraftingPattern create(World world, ItemStack stack, ICraftingPatternContainer container) {
        return new CraftingPattern(world, container, stack);
    }
}
