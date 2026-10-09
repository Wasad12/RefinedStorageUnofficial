package com.raoulvdberge.refinedstorage.integration.jei;

import com.raoulvdberge.refinedstorage.container.ContainerSolderer;
import com.raoulvdberge.refinedstorage.gui.GuiSolderer;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class RSJEIPlugin extends BlankModPlugin {
    public static RSJEIPlugin INSTANCE;

    private IJeiRuntime runtime;

    @Override
    public void register(IModRegistry registry) {
        INSTANCE = this;

        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        // DEVIATION: JEI 2.x has no IRecipeCategoryRegistration event; categories and
        // recipe handlers are registered on IModRegistry inside register().
        registry.addRecipeCategories(new RecipeCategorySolderer(guiHelper));

        registry.addRecipeHandlers(new RecipeHandlerSolderer());

        registry.addRecipes(RecipeMakerSolderer.getRecipes(guiHelper));

        // DEVIATION: JEI 2.x has no addRecipeCatalyst; the solderer block cannot be
        // registered as a category catalyst (recipes are still browsable by category).

        registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RecipeTransferHandlerPattern());
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RecipeTransferHandlerGrid());
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerSolderer.class, RecipeCategorySolderer.ID, 0, 3, 8, 36);

        registry.addRecipeClickArea(GuiSolderer.class, 80, 36, 22, 15, RecipeCategorySolderer.ID);

        registry.addAdvancedGuiHandlers(new GuiHandlerGrid());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        this.runtime = runtime;
    }

    public IJeiRuntime getRuntime() {
        return runtime;
    }
}
