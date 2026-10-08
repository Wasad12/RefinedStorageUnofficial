package com.raoulvdberge.refinedstorage.apiimpl.util;

import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.block.BlockNode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;

public class Comparer implements IComparer {
    // DEVIATION: local tri-state replacing vanilla EnumActionResult (absent on 1.8.9).
    // Semantics preserved: EQUAL/UNEQUAL short-circuit, CONTINUE falls through to flag checks.
    private enum Validity {
        EQUAL,
        UNEQUAL,
        CONTINUE
    }

    @Override
    public boolean isEqual(@Nullable ItemStack left, @Nullable ItemStack right, int flags) {
        Validity validity = validityCheck(left, right);

        if (validity != Validity.CONTINUE) {
            return validity == Validity.EQUAL;
        }

        if ((flags & COMPARE_OREDICT) == COMPARE_OREDICT) {
            if (isEqualOredict(left, right)) {
                return true;
            }
        }

        if (left.getItem() != right.getItem()) {
            return false;
        }

        if ((flags & COMPARE_DAMAGE) == COMPARE_DAMAGE && left.getItemDamage() != OreDictionary.WILDCARD_VALUE && right.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
            if (left.getItemDamage() != right.getItemDamage()) {
                return false;
            }
        }

        if ((flags & COMPARE_NBT) == COMPARE_NBT) {
            if ((flags & COMPARE_STRIP_NBT) == COMPARE_STRIP_NBT) {
                left = stripTags(left.copy());
                right = stripTags(right.copy());
            }
            if (!isEqualNBT(left, right)) {
                return false;
            }
        }

        if ((flags & COMPARE_QUANTITY) == COMPARE_QUANTITY) {
            if (left.stackSize != right.stackSize) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEqual(@Nullable FluidStack left, @Nullable FluidStack right, int flags) {
        if (left == null && right == null) {
            return true;
        }

        if ((left == null && right != null) || (left != null && right == null)) {
            return false;
        }

        if (left.getFluid() != right.getFluid()) {
            return false;
        }

        if ((flags & COMPARE_QUANTITY) == COMPARE_QUANTITY) {
            if (left.amount != right.amount) {
                return false;
            }
        }

        if ((flags & COMPARE_NBT) == COMPARE_NBT) {
            if (left.tag != null && !left.tag.equals(right.tag)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEqualNBT(@Nullable ItemStack left, @Nullable ItemStack right) {
        Validity validity = validityCheck(left, right);

        if (validity != Validity.CONTINUE) {
            return validity == Validity.EQUAL;
        }

        if (!ItemStack.areItemStackTagsEqual(left, right)) {
            if (left.hasTagCompound() && !right.hasTagCompound() && left.getTagCompound().hasNoTags()) {
                return true;
            } else if (!left.hasTagCompound() && right.hasTagCompound() && right.getTagCompound().hasNoTags()) {
                return true;
            }

            return false;
        }

        return true;
    }

    @Override
    public boolean isEqualOredict(@Nullable ItemStack left, @Nullable ItemStack right) {
        Validity validity = validityCheck(left, right);

        if (validity != Validity.CONTINUE) {
            return validity == Validity.EQUAL;
        }

        return RSUtils.areStacksEquivalent(left, right);
    }

    private Validity validityCheck(@Nullable ItemStack left, @Nullable ItemStack right) {
        if (left == null && right == null) {
            return Validity.EQUAL;
        }

        if ((left == null && right != null) || (left != null && right == null)) {
            return Validity.UNEQUAL;
        }

        boolean leftEmpty = RSUtils.isEmpty(left);
        boolean rightEmpty = RSUtils.isEmpty(right);

        if (leftEmpty && rightEmpty) {
            return Validity.EQUAL;
        }

        if ((leftEmpty && !rightEmpty) || (!leftEmpty && rightEmpty)) {
            return Validity.UNEQUAL;
        }

        return Validity.CONTINUE;
    }

    public static ItemStack stripTags(ItemStack stack) {
        if (stack != null && stack.hasTagCompound()) {
            switch (RSUtils.getModId(stack)) {
                case "mekanism":
                case "mekanismgenerators":
                case "mekanismtools":
                    stack.getTagCompound().removeTag("mekData");
                    break;
                case "enderio":
                    // Soul vials
                    stack.getTagCompound().removeTag("entity");
                    stack.getTagCompound().removeTag("isStub");
                    // Capacitors
                    stack.getTagCompound().removeTag("Energy");
                    // Painted
                    stack.getTagCompound().removeTag("paintSource__null");
                    stack.getTagCompound().removeTag("paintSource");
                    // Sided config
                    stack.getTagCompound().removeTag("faceModes__null");
                    stack.getTagCompound().removeTag("faceModes");
                    // Tank
                    stack.getTagCompound().removeTag("tank");
                    stack.getTagCompound().removeTag("voidMode");
                    stack.getTagCompound().removeTag("inventory");
                    // Name
                    stack.getTagCompound().removeTag("display");
                    stack.getTagCompound().removeTag("eio.abstractMachine");
                    break;
                case "simplyjetpacks":
                    stack.getTagCompound().removeTag("sjData");
                    stack.getTagCompound().removeTag("PackOn");
                    break;
                case "refinedstorage":
                    stack.getTagCompound().removeTag(BlockNode.NBT_REFINED_STORAGE_DATA);
                    break;
                case "immersiveengineering":
                    stack.getTagCompound().removeTag("hammerDmg");
                    stack.getTagCompound().removeTag("cutterDmg");
                    break;
                case "storagedrawers":
                    stack.getTagCompound().removeTag("material");
                    break;
                case "fluxnetworks":
                    stack.getTagCompound().removeTag("dropped");
                    stack.getTagCompound().removeTag("energy");
                    break;
                case "draconicevolution":
                    stack.getTagCompound().removeTag("Energy");
                    stack.getTagCompound().removeTag("DEUpgrades");
                    Set<String> profiles = stack.getTagCompound().getKeySet().stream().filter(key -> key.startsWith("Profile")).collect(Collectors.toSet());
                    for (String profile : profiles) {
                        stack.getTagCompound().removeTag(profile);
                    }
                    break;
                case "minecraft":
                    stack.getTagCompound().removeTag("RepairCost");
                    break;
            }
        }

        return stack;
    }
}
