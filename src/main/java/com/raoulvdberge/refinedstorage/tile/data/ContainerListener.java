package com.raoulvdberge.refinedstorage.tile.data;

import com.raoulvdberge.refinedstorage.container.ContainerBase;
import com.raoulvdberge.refinedstorage.tile.TileBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

public class ContainerListener {
    // DEVIATION: 1.8.9 has no PlayerContainerEvent.Open/Close. GUI open is hooked from
    // GuiHandler.getServerGuiElement; close from ContainerBase.onContainerClosed. Bodies unchanged.
    public static void onContainerOpen(EntityPlayer player, Container container) {
        if (container instanceof ContainerBase) {
            TileBase tile = ((ContainerBase) container).getTile();

            if (tile != null && !tile.getWorld().isRemote) {
                TileDataManager manager = tile.getDataManager();

                manager.sendParametersTo((EntityPlayerMP) player);

                int watchers = manager.getWatchers().size();

                manager.getWatchers().add(player);

                if (watchers == 0) {
                    Thread listenerThread = new Thread(() -> {
                        while (manager.getWatchers().size() > 0) {
                            manager.detectAndSendChanges();
                        }
                    }, "RS tile listener " + tile.getPos().getX() + ", " + tile.getPos().getY() + ", " + tile.getPos().getZ());

                    listenerThread.start();
                }
            }
        }
    }

    public static void onContainerClose(EntityPlayer player, Container container) {
        if (container instanceof ContainerBase) {
            TileBase tile = ((ContainerBase) container).getTile();

            if (tile != null && !tile.getWorld().isRemote) {
                tile.getDataManager().getWatchers().remove(player);
            }
        }
    }
}
