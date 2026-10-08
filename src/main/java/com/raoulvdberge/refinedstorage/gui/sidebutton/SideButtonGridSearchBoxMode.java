package com.raoulvdberge.refinedstorage.gui.sidebutton;

import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeGrid;
import com.raoulvdberge.refinedstorage.gui.GuiBase;
import com.raoulvdberge.refinedstorage.gui.grid.GuiGrid;
import net.minecraft.util.EnumChatFormatting;

public class SideButtonGridSearchBoxMode extends SideButton {
    public SideButtonGridSearchBoxMode(GuiGrid gui) {
        super(gui);
    }

    @Override
    public String getTooltip() {
        return EnumChatFormatting.YELLOW + GuiBase.t("sidebutton.refinedstorage:grid.search_box_mode") + EnumChatFormatting.RESET + "\n" + GuiBase.t("sidebutton.refinedstorage:grid.search_box_mode." + ((GuiGrid) gui).getGrid().getSearchBoxMode());
    }

    @Override
    protected void drawButtonIcon(int x, int y) {
        int mode = ((GuiGrid) gui).getGrid().getSearchBoxMode();

        gui.drawTexture(x, y, mode == NetworkNodeGrid.SEARCH_BOX_MODE_NORMAL_AUTOSELECTED || mode == NetworkNodeGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED ? 16 : 0, 96, 16, 16);
    }

    @Override
    public void actionPerformed() {
        int mode = ((GuiGrid) gui).getGrid().getSearchBoxMode();

        if (mode == NetworkNodeGrid.SEARCH_BOX_MODE_NORMAL) {
            mode = NetworkNodeGrid.SEARCH_BOX_MODE_NORMAL_AUTOSELECTED;
        } else if (mode == NetworkNodeGrid.SEARCH_BOX_MODE_NORMAL_AUTOSELECTED) {
            // DEVIATION: JEI integration dropped on 1.8.9 — JEI-synchronized modes unreachable, cycle back to NORMAL.
            mode = NetworkNodeGrid.SEARCH_BOX_MODE_NORMAL;
        } else if (mode == NetworkNodeGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED) {
            mode = NetworkNodeGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED;
        } else if (mode == NetworkNodeGrid.SEARCH_BOX_MODE_JEI_SYNCHRONIZED_AUTOSELECTED) {
            mode = NetworkNodeGrid.SEARCH_BOX_MODE_NORMAL;
        }

        ((GuiGrid) gui).getGrid().onSearchBoxModeChanged(mode);

        ((GuiGrid) gui).updateSearchFieldFocus(mode);
    }
}
