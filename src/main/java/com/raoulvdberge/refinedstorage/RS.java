package com.raoulvdberge.refinedstorage;

import com.raoulvdberge.refinedstorage.proxy.ProxyCommon;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = RS.ID, version = RS.VERSION, dependencies = RS.DEPENDENCIES, guiFactory = RS.GUI_FACTORY, updateJSON = RS.UPDATE_JSON)
public final class RS {
    static {
        FluidRegistry.enableUniversalBucket();
    }

    public static final String ID = "refinedstorage";
    public static final String VERSION = "1.4.21";
    // DEVIATION: retargeted to 1.8.9 Forge (modId "Forge", case-sensitive); JEI soft-dep removed.
    public static final String DEPENDENCIES = "required-after:Forge@[11.15.1.2318,)";
    public static final String GUI_FACTORY = "com.raoulvdberge.refinedstorage.gui.config.ModGuiFactory";
    public static final String UPDATE_JSON = "https://refinedstorage.raoulvdberge.com/update";

    @SidedProxy(clientSide = "com.raoulvdberge.refinedstorage.proxy.ProxyClient", serverSide = "com.raoulvdberge.refinedstorage.proxy.ProxyCommon")
    public static ProxyCommon PROXY;

    @Instance
    public static RS INSTANCE;

    public RSConfig config;
    public final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(ID);
    // DEVIATION: 1.8.9 getTabIconItem returns Item, not ItemStack.
    public final CreativeTabs tab = new CreativeTabs(ID) {
        @Override
        public Item getTabIconItem() {
            return RSItems.STORAGE_HOUSING;
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        config = new RSConfig(e.getSuggestedConfigurationFile());

        PROXY.preInit(e);
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        PROXY.init(e);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        PROXY.postInit(e);
    }

    @EventHandler
    public void onMissingMapping(FMLMissingMappingsEvent e) {
        PROXY.fixMappings(e);
    }
}
