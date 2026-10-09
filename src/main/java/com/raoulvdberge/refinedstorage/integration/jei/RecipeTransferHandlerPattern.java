package com.raoulvdberge.refinedstorage.integration.jei;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.container.ContainerProcessingPatternEncoder;
import com.raoulvdberge.refinedstorage.network.MessageProcessingPatternEncoderTransfer;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.ingredients.IGuiIngredient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class RecipeTransferHandlerPattern implements IRecipeTransferHandler {
    @Override
    public Class<? extends Container> getContainerClass() {
        return ContainerProcessingPatternEncoder.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        // DEVIATION: JEI 2.x has no universal transfer handler (4.x
        // addUniversalRecipeTransferHandler). Crafting covers the use case.
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(Container container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            List<ItemStack> inputs = new LinkedList<>();
            List<ItemStack> outputs = new LinkedList<>();

            for (IGuiIngredient<ItemStack> guiIngredient : recipeLayout.getItemStacks().getGuiIngredients().values()) {
                // DEVIATION: JEI 2.x IGuiIngredient has no getDisplayedIngredient;
                // use the first variant (ore-dict lists collapse to their first entry).
                if (guiIngredient != null && !guiIngredient.getAllIngredients().isEmpty()) {
                    ItemStack ingredient = guiIngredient.getAllIngredients().get(0).copy();
                    if (guiIngredient.isInput()) {
                        inputs.add(ingredient);
                    } else {
                        outputs.add(ingredient);
                    }
                }
            }

            RS.INSTANCE.network.sendToServer(new MessageProcessingPatternEncoderTransfer(inputs, outputs));
        }

        return null;
    }
}
