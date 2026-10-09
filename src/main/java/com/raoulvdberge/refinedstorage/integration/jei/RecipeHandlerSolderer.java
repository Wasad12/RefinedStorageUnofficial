package com.raoulvdberge.refinedstorage.integration.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

// DEVIATION: new class (no 1.11.2 counterpart). JEI 2.x maps recipe objects to
// categories via IRecipeHandler instead of 4.x handleRecipes(Class, Function, String).
public class RecipeHandlerSolderer implements IRecipeHandler<RecipeWrapperSolderer> {
    @Override
    public Class<RecipeWrapperSolderer> getRecipeClass() {
        return RecipeWrapperSolderer.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        return RecipeCategorySolderer.ID;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RecipeWrapperSolderer recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(RecipeWrapperSolderer recipe) {
        return recipe != null;
    }
}
