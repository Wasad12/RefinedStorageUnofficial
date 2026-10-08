package com.raoulvdberge.refinedstorage.apiimpl.network.item;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSGui;
import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.item.INetworkItem;
import com.raoulvdberge.refinedstorage.api.network.item.INetworkItemHandler;
import com.raoulvdberge.refinedstorage.api.network.security.Permission;
import com.raoulvdberge.refinedstorage.item.ItemWirelessCraftingMonitor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cofh.api.energy.IEnergyContainerItem;

public class NetworkItemWirelessCraftingMonitor implements INetworkItem {
    private INetworkItemHandler handler;
    private EntityPlayer player;
    private ItemStack stack;

    public NetworkItemWirelessCraftingMonitor(INetworkItemHandler handler, EntityPlayer player, ItemStack stack) {
        this.handler = handler;
        this.player = player;
        this.stack = stack;
    }

    @Override
    public EntityPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean onOpen(INetwork network, EntityPlayer player, World controllerWorld) {
        if (RS.INSTANCE.config.wirelessCraftingMonitorUsesEnergy && stack.getItemDamage() != ItemWirelessCraftingMonitor.TYPE_CREATIVE && ((IEnergyContainerItem) stack.getItem()).getEnergyStored(stack) <= RS.INSTANCE.config.wirelessCraftingMonitorOpenUsage) {
            return false;
        }

        if (!network.getSecurityManager().hasPermission(Permission.MODIFY, player) || !network.getSecurityManager().hasPermission(Permission.AUTOCRAFTING, player)) {
            RSUtils.sendNoPermissionMessage(player);

            return false;
        }

        player.openGui(RS.INSTANCE, RSGui.WIRELESS_CRAFTING_MONITOR, player.getEntityWorld(), 0, controllerWorld.provider.getDimensionId(), 0);

        network.sendCraftingMonitorUpdate((EntityPlayerMP) player);

        drainEnergy(RS.INSTANCE.config.wirelessCraftingMonitorOpenUsage);

        return true;
    }

    public void drainEnergy(int energy) {
        if (RS.INSTANCE.config.wirelessCraftingMonitorUsesEnergy && stack.getItemDamage() != ItemWirelessCraftingMonitor.TYPE_CREATIVE) {
            IEnergyContainerItem energyItem = (IEnergyContainerItem) stack.getItem();

            energyItem.extractEnergy(stack, energy, false);

            if (energyItem.getEnergyStored(stack) <= 0) {
                handler.onClose(player);

                player.closeScreen();
            }
        }
    }
}