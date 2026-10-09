package com.raoulvdberge.refinedstorage.integration.jei;

import com.raoulvdberge.refinedstorage.gui.grid.GuiGrid;
import mezz.jei.api.gui.IAdvancedGuiHandler;

import java.awt.Rectangle;
import java.util.List;

public class GuiHandlerGrid implements IAdvancedGuiHandler<GuiGrid> {
    @Override
    public Class<GuiGrid> getGuiContainerClass() {
        return GuiGrid.class;
    }

    @Override
    public List<Rectangle> getGuiExtraAreas(GuiGrid gui) {
        return null;
    }

    // DEVIATION: JEI 2.x IAdvancedGuiHandler has no getIngredientUnderMouse, so the
    // 1.11.2 grid-hover lookup (JEI shows the hovered grid stack) has no 2.x hook
    // and is not ported. Extra areas stay null per source.
}
