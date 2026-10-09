package com.raoulvdberge.refinedstorage.integration.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class RecipeWrapperSolderer extends BlankRecipeWrapper {
    private IDrawableAnimated progress;

    private List<List<ItemStack>> inputs;
    private ItemStack output;

    public RecipeWrapperSolderer(IGuiHelper guiHelper, int duration, List<List<ItemStack>> inputs, ItemStack output) {
        IDrawableStatic arrow = guiHelper.createDrawable(
            new ResourceLocation("refinedstorage", "textures/gui/solderer.png"),
            212,
            0,
            22,
            15
        );

        this.progress = guiHelper.createAnimatedDrawable(
            arrow,
            duration,
            IDrawableAnimated.StartDirection.LEFT,
            false
        );

        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public List<List<ItemStack>> getInputs() {
        return inputs;
    }

    @Override
    public List<ItemStack> getOutputs() {
        return Collections.singletonList(output);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight) {
        progress.draw(minecraft, 40, 18);
    }
}
