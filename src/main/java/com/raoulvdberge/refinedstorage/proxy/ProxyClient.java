package com.raoulvdberge.refinedstorage.proxy;

import com.raoulvdberge.refinedstorage.RSUtils;

import com.raoulvdberge.refinedstorage.*;
import com.raoulvdberge.refinedstorage.apiimpl.autocrafting.CraftingPattern;
import com.raoulvdberge.refinedstorage.block.*;
import com.raoulvdberge.refinedstorage.gui.GuiCraftingPreview;
import com.raoulvdberge.refinedstorage.gui.grid.GuiCraftingStart;

import com.raoulvdberge.refinedstorage.item.*;
import com.raoulvdberge.refinedstorage.network.MessageGridCraftingPreviewResponse;
import com.raoulvdberge.refinedstorage.render.*;
import com.raoulvdberge.refinedstorage.tile.TileController;
import com.raoulvdberge.refinedstorage.tile.TileStorageMonitor;
import com.raoulvdberge.refinedstorage.tile.grid.portable.PortableGrid;
import com.raoulvdberge.refinedstorage.tile.grid.portable.TilePortableGrid;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ProxyClient extends ProxyCommon {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(this);

        // TESRs
        ClientRegistry.bindTileEntitySpecialRenderer(TileStorageMonitor.class, new TileEntitySpecialRendererStorageMonitor());

        // Item Variants
        ModelBakery.registerItemVariants(RSItems.STORAGE_DISK,
            new ResourceLocation("refinedstorage:1k_storage_disk"),
            new ResourceLocation("refinedstorage:4k_storage_disk"),
            new ResourceLocation("refinedstorage:16k_storage_disk"),
            new ResourceLocation("refinedstorage:64k_storage_disk"),
            new ResourceLocation("refinedstorage:creative_storage_disk")
        );

        ModelBakery.registerItemVariants(RSItems.STORAGE_PART,
            new ResourceLocation("refinedstorage:1k_storage_part"),
            new ResourceLocation("refinedstorage:4k_storage_part"),
            new ResourceLocation("refinedstorage:16k_storage_part"),
            new ResourceLocation("refinedstorage:64k_storage_part")
        );

        ModelBakery.registerItemVariants(RSItems.FLUID_STORAGE_DISK,
            new ResourceLocation("refinedstorage:64k_fluid_storage_disk"),
            new ResourceLocation("refinedstorage:128k_fluid_storage_disk"),
            new ResourceLocation("refinedstorage:256k_fluid_storage_disk"),
            new ResourceLocation("refinedstorage:512k_fluid_storage_disk"),
            new ResourceLocation("refinedstorage:creative_fluid_storage_disk")
        );

        ModelBakery.registerItemVariants(RSItems.FLUID_STORAGE_PART,
            new ResourceLocation("refinedstorage:64k_fluid_storage_part"),
            new ResourceLocation("refinedstorage:128k_fluid_storage_part"),
            new ResourceLocation("refinedstorage:256k_fluid_storage_part"),
            new ResourceLocation("refinedstorage:512k_fluid_storage_part")
        );

        ModelBakery.registerItemVariants(RSItems.PROCESSOR,
            new ResourceLocation("refinedstorage:basic_printed_processor"),
            new ResourceLocation("refinedstorage:improved_printed_processor"),
            new ResourceLocation("refinedstorage:advanced_printed_processor"),
            new ResourceLocation("refinedstorage:basic_processor"),
            new ResourceLocation("refinedstorage:improved_processor"),
            new ResourceLocation("refinedstorage:advanced_processor"),
            new ResourceLocation("refinedstorage:printed_silicon")
        );

        ModelBakery.registerItemVariants(RSItems.CORE,
            new ResourceLocation("refinedstorage:construction_core"),
            new ResourceLocation("refinedstorage:destruction_core")
        );

        ModelBakery.registerItemVariants(RSItems.UPGRADE,
            new ResourceLocation("refinedstorage:upgrade"),
            new ResourceLocation("refinedstorage:range_upgrade"),
            new ResourceLocation("refinedstorage:speed_upgrade"),
            new ResourceLocation("refinedstorage:crafting_upgrade"),
            new ResourceLocation("refinedstorage:stack_upgrade"),
            new ResourceLocation("refinedstorage:interdimensional_upgrade"),
            new ResourceLocation("refinedstorage:silk_touch_upgrade"),
            new ResourceLocation("refinedstorage:fortune_upgrade")
        );

        // Items
        ModelLoader.setCustomModelResourceLocation(RSItems.STORAGE_DISK, ItemStorageDisk.TYPE_1K, new ModelResourceLocation("refinedstorage:1k_storage_disk", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.STORAGE_DISK, ItemStorageDisk.TYPE_4K, new ModelResourceLocation("refinedstorage:4k_storage_disk", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.STORAGE_DISK, ItemStorageDisk.TYPE_16K, new ModelResourceLocation("refinedstorage:16k_storage_disk", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.STORAGE_DISK, ItemStorageDisk.TYPE_64K, new ModelResourceLocation("refinedstorage:64k_storage_disk", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.STORAGE_DISK, ItemStorageDisk.TYPE_CREATIVE, new ModelResourceLocation("refinedstorage:creative_storage_disk", "inventory"));

        ModelLoader.setCustomModelResourceLocation(RSItems.STORAGE_PART, ItemStoragePart.TYPE_1K, new ModelResourceLocation("refinedstorage:1k_storage_part", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.STORAGE_PART, ItemStoragePart.TYPE_4K, new ModelResourceLocation("refinedstorage:4k_storage_part", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.STORAGE_PART, ItemStoragePart.TYPE_16K, new ModelResourceLocation("refinedstorage:16k_storage_part", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.STORAGE_PART, ItemStoragePart.TYPE_64K, new ModelResourceLocation("refinedstorage:64k_storage_part", "inventory"));

        ModelLoader.setCustomModelResourceLocation(RSItems.FLUID_STORAGE_DISK, ItemFluidStorageDisk.TYPE_64K, new ModelResourceLocation("refinedstorage:64k_fluid_storage_disk", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.FLUID_STORAGE_DISK, ItemFluidStorageDisk.TYPE_128K, new ModelResourceLocation("refinedstorage:128k_fluid_storage_disk", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.FLUID_STORAGE_DISK, ItemFluidStorageDisk.TYPE_256K, new ModelResourceLocation("refinedstorage:256k_fluid_storage_disk", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.FLUID_STORAGE_DISK, ItemFluidStorageDisk.TYPE_512K, new ModelResourceLocation("refinedstorage:512k_fluid_storage_disk", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.FLUID_STORAGE_DISK, ItemFluidStorageDisk.TYPE_CREATIVE, new ModelResourceLocation("refinedstorage:creative_fluid_storage_disk", "inventory"));

        ModelLoader.setCustomModelResourceLocation(RSItems.FLUID_STORAGE_PART, ItemFluidStoragePart.TYPE_64K, new ModelResourceLocation("refinedstorage:64k_fluid_storage_part", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.FLUID_STORAGE_PART, ItemFluidStoragePart.TYPE_128K, new ModelResourceLocation("refinedstorage:128k_fluid_storage_part", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.FLUID_STORAGE_PART, ItemFluidStoragePart.TYPE_256K, new ModelResourceLocation("refinedstorage:256k_fluid_storage_part", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.FLUID_STORAGE_PART, ItemFluidStoragePart.TYPE_512K, new ModelResourceLocation("refinedstorage:512k_fluid_storage_part", "inventory"));

        ModelLoader.setCustomModelResourceLocation(RSItems.PROCESSOR, ItemProcessor.TYPE_PRINTED_BASIC, new ModelResourceLocation("refinedstorage:basic_printed_processor", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.PROCESSOR, ItemProcessor.TYPE_PRINTED_IMPROVED, new ModelResourceLocation("refinedstorage:improved_printed_processor", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.PROCESSOR, ItemProcessor.TYPE_PRINTED_ADVANCED, new ModelResourceLocation("refinedstorage:advanced_printed_processor", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.PROCESSOR, ItemProcessor.TYPE_BASIC, new ModelResourceLocation("refinedstorage:basic_processor", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.PROCESSOR, ItemProcessor.TYPE_IMPROVED, new ModelResourceLocation("refinedstorage:improved_processor", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.PROCESSOR, ItemProcessor.TYPE_ADVANCED, new ModelResourceLocation("refinedstorage:advanced_processor", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.PROCESSOR, ItemProcessor.TYPE_PRINTED_SILICON, new ModelResourceLocation("refinedstorage:printed_silicon", "inventory"));

        ModelLoader.setCustomModelResourceLocation(RSItems.SILICON, 0, new ModelResourceLocation("refinedstorage:silicon", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.QUARTZ_ENRICHED_IRON, 0, new ModelResourceLocation("refinedstorage:quartz_enriched_iron", "inventory"));

        ModelLoader.setCustomModelResourceLocation(RSItems.CORE, ItemCore.TYPE_CONSTRUCTION, new ModelResourceLocation("refinedstorage:construction_core", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.CORE, ItemCore.TYPE_DESTRUCTION, new ModelResourceLocation("refinedstorage:destruction_core", "inventory"));

        ModelLoader.setCustomModelResourceLocation(RSItems.WIRELESS_GRID, 0, new ModelResourceLocation("refinedstorage:wireless_grid", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.WIRELESS_FLUID_GRID, 0, new ModelResourceLocation("refinedstorage:wireless_fluid_grid", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.WIRELESS_CRAFTING_MONITOR, 0, new ModelResourceLocation("refinedstorage:wireless_crafting_monitor", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.PATTERN, 0, new ModelResourceLocation("refinedstorage:pattern", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.STORAGE_HOUSING, 0, new ModelResourceLocation("refinedstorage:storage_housing", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.FILTER, 0, new ModelResourceLocation("refinedstorage:filter", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.NETWORK_CARD, 0, new ModelResourceLocation("refinedstorage:network_card", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.WRENCH, 0, new ModelResourceLocation("refinedstorage:wrench", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.SECURITY_CARD, 0, new ModelResourceLocation("refinedstorage:security_card", "inventory"));

        ModelLoader.setCustomModelResourceLocation(RSItems.UPGRADE, 0, new ModelResourceLocation("refinedstorage:upgrade", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.UPGRADE, ItemUpgrade.TYPE_RANGE, new ModelResourceLocation("refinedstorage:range_upgrade", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.UPGRADE, ItemUpgrade.TYPE_SPEED, new ModelResourceLocation("refinedstorage:speed_upgrade", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.UPGRADE, ItemUpgrade.TYPE_CRAFTING, new ModelResourceLocation("refinedstorage:crafting_upgrade", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.UPGRADE, ItemUpgrade.TYPE_STACK, new ModelResourceLocation("refinedstorage:stack_upgrade", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.UPGRADE, ItemUpgrade.TYPE_INTERDIMENSIONAL, new ModelResourceLocation("refinedstorage:interdimensional_upgrade", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.UPGRADE, ItemUpgrade.TYPE_SILK_TOUCH, new ModelResourceLocation("refinedstorage:silk_touch_upgrade", "inventory"));
        ModelLoader.setCustomModelResourceLocation(RSItems.UPGRADE, ItemUpgrade.TYPE_FORTUNE, new ModelResourceLocation("refinedstorage:fortune_upgrade", "inventory"));

        // Blocks
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.CABLE), 0, new ModelResourceLocation("refinedstorage:cable", "inventory"));
        // DEVIATION: 1.11 maps grid items to full-state variants (per-type fronts in hand);
        // the Slice-2 static `#inventory` mapping shared one front. Restored per source.
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.GRID), GridType.NORMAL.getId(), new ModelResourceLocation("refinedstorage:grid", "connected=false,direction=north,type=normal"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.GRID), GridType.CRAFTING.getId(), new ModelResourceLocation("refinedstorage:grid", "connected=false,direction=north,type=crafting"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.GRID), GridType.PATTERN.getId(), new ModelResourceLocation("refinedstorage:grid", "connected=false,direction=north,type=pattern"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.GRID), GridType.FLUID.getId(), new ModelResourceLocation("refinedstorage:grid", "connected=false,direction=north,type=fluid"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.MACHINE_CASING), 0, new ModelResourceLocation("refinedstorage:machine_casing", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.DISK_DRIVE), 0, new ModelResourceLocation("refinedstorage:disk_drive", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.EXPORTER), 0, new ModelResourceLocation("refinedstorage:exporter", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.IMPORTER), 0, new ModelResourceLocation("refinedstorage:importer", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.EXTERNAL_STORAGE), 0, new ModelResourceLocation("refinedstorage:external_storage", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.CONSTRUCTOR), 0, new ModelResourceLocation("refinedstorage:constructor", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.DESTRUCTOR), 0, new ModelResourceLocation("refinedstorage:destructor", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.READER), 0, new ModelResourceLocation("refinedstorage:reader", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.WRITER), 0, new ModelResourceLocation("refinedstorage:writer", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.SOLDERER), 0, new ModelResourceLocation("refinedstorage:solderer", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.DETECTOR), 0, new ModelResourceLocation("refinedstorage:detector", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.RELAY), 0, new ModelResourceLocation("refinedstorage:relay", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.INTERFACE), 0, new ModelResourceLocation("refinedstorage:interface", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.FLUID_INTERFACE), 0, new ModelResourceLocation("refinedstorage:fluid_interface", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.WIRELESS_TRANSMITTER), 0, new ModelResourceLocation("refinedstorage:wireless_transmitter", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.CRAFTING_MONITOR), 0, new ModelResourceLocation("refinedstorage:crafting_monitor", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.CRAFTER), 0, new ModelResourceLocation("refinedstorage:crafter", "connected=false,direction=north"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.PROCESSING_PATTERN_ENCODER), 0, new ModelResourceLocation("refinedstorage:processing_pattern_encoder", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.NETWORK_TRANSMITTER), 0, new ModelResourceLocation("refinedstorage:network_transmitter", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.NETWORK_RECEIVER), 0, new ModelResourceLocation("refinedstorage:network_receiver", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.STORAGE), ItemStorageType.TYPE_1K.getId(), new ModelResourceLocation("refinedstorage:storage", "type=1k"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.STORAGE), ItemStorageType.TYPE_4K.getId(), new ModelResourceLocation("refinedstorage:storage", "type=4k"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.STORAGE), ItemStorageType.TYPE_16K.getId(), new ModelResourceLocation("refinedstorage:storage", "type=16k"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.STORAGE), ItemStorageType.TYPE_64K.getId(), new ModelResourceLocation("refinedstorage:storage", "type=64k"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.STORAGE), ItemStorageType.TYPE_CREATIVE.getId(), new ModelResourceLocation("refinedstorage:storage", "type=creative"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.FLUID_STORAGE), FluidStorageType.TYPE_64K.getId(), new ModelResourceLocation("refinedstorage:fluid_storage", "type=64k"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.FLUID_STORAGE), FluidStorageType.TYPE_128K.getId(), new ModelResourceLocation("refinedstorage:fluid_storage", "type=128k"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.FLUID_STORAGE), FluidStorageType.TYPE_256K.getId(), new ModelResourceLocation("refinedstorage:fluid_storage", "type=256k"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.FLUID_STORAGE), FluidStorageType.TYPE_512K.getId(), new ModelResourceLocation("refinedstorage:fluid_storage", "type=512k"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.FLUID_STORAGE), FluidStorageType.TYPE_CREATIVE.getId(), new ModelResourceLocation("refinedstorage:fluid_storage", "type=creative"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.DISK_MANIPULATOR), 0, new ModelResourceLocation("refinedstorage:disk_manipulator", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.SECURITY_MANAGER), 0, new ModelResourceLocation("refinedstorage:security_manager", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.QUARTZ_ENRICHED_IRON), 0, new ModelResourceLocation("refinedstorage:quartz_enriched_iron_block", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RSBlocks.STORAGE_MONITOR), 0, new ModelResourceLocation("refinedstorage:storage_monitor", "connected=false,direction=north"));

        // DEVIATION (1.8.9): the 1.11 controller/portable-grid ItemMeshDefinitions are
        // restored here (ledger L6.1; per-type/energy in-hand dynamics). Unlike 1.11's
        // StateMap-ignored `type`, the 1.8.9 JSONs declare `type` directly (a custom
        // StateMap would suppress the blockstate `inventory` variant), so the mesh
        // locations carry type explicitly. Variant strings are alphabetical
        // (direction,energy,type): Forge 1.8.9 bakes/looks-up forge-blockstate variants
        // under alphabetically-sorted keys, and ModelResourceLocation matching is exact.
        // Per ModelLoader docs, every mesh-returnable location must ALSO be passed to
        // ModelBakery.registerItemVariants or it never bakes (renders missingno).
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(RSBlocks.CONTROLLER), new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                String type = stack.getItemDamage() == ControllerType.CREATIVE.getId() ? "creative" : "normal";

                int energy = stack.getItemDamage() == ControllerType.CREATIVE.getId() ? 7 : TileController.getEnergyScaled(ItemBlockController.getEnergyStored(stack), ItemBlockController.getEnergyCapacity(stack), 7);

                return controllerLocation(type, energy);
            }
        });

        for (int energy = 0; energy <= 7; ++energy) {
            ModelBakery.registerItemVariants(Item.getItemFromBlock(RSBlocks.CONTROLLER),
                controllerLocation("normal", energy),
                controllerLocation("creative", energy));
        }

        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(RSBlocks.PORTABLE_GRID), new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                PortableGrid portableGrid = new PortableGrid(null, stack);

                String type = stack.getItemDamage() == ItemBlockPortableGrid.TYPE_CREATIVE ? "creative" : "normal";

                return portableGridLocation(type, portableGrid.getEnergy() != 0, TilePortableGrid.getDiskState(portableGrid));
            }
        });

        ModelBakery.registerItemVariants(Item.getItemFromBlock(RSBlocks.PORTABLE_GRID), portableGridLocations());

        // DEVIATION: 1.8.9 model lookup never sees the bare JSON path. ForgeBlockStateV1
        // prepends "block/" when deserializing and ModelLoaderRegistry.getActualLocation
        // prepends "models/" before consulting loaders (verified in Forge sources) — so the
        // location arriving here is "refinedstorage:models/block/disk_drive_smart".
        // Matching only the bare path (as 1.11 does) silently never fires: vanilla falls
        // back to the plain casing JSON with no error, rendering a diskless drive forever.
        // (This was the invisible-drive-disks bug: no crash, no log, base renders fine.)
        // The "_smart" indirection is deliberate: the blockstates point at it while
        // ModelDiskDrive's own base/disk dependencies stay on the plain JSON paths.
        // Without the split, the loader would also catch its own base model and recurse
        // infinitely (StackOverflowError at bake — observed).
        ModelLoaderRegistry.registerLoader(new ICustomModelLoader() {
            @Override
            public boolean accepts(ResourceLocation modelLocation) {
                return modelLocation.getResourceDomain().equals(RS.ID) && (modelLocation.getResourcePath().equals("block/disk_drive_smart") || modelLocation.getResourcePath().equals("models/block/disk_drive_smart"));
            }

            @Override
            public IModel loadModel(ResourceLocation modelLocation) throws java.io.IOException {
                return new ModelDiskDrive();
            }

            @Override
            public void onResourceManagerReload(IResourceManager resourceManager) {
            }
        });

        ModelLoaderRegistry.registerLoader(new ICustomModelLoader() {
            @Override
            public boolean accepts(ResourceLocation modelLocation) {
                return modelLocation.getResourceDomain().equals(RS.ID) && (modelLocation.getResourcePath().equals("block/disk_manipulator_smart") || modelLocation.getResourcePath().equals("models/block/disk_manipulator_smart"));
            }

            @Override
            public IModel loadModel(ResourceLocation modelLocation) throws java.io.IOException {
                return new ModelDiskManipulator();
            }

            @Override
            public void onResourceManagerReload(IResourceManager resourceManager) {
            }
        });
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);

        RSKeyBindings.init();

        // PORT-SLICE-5: pattern output tinting moves to Item.getColorFromItemStack (no ItemColors on 1.8.9).
    }

    public static void onReceiveCraftingPreviewResponse(MessageGridCraftingPreviewResponse message) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;

            if (screen instanceof GuiCraftingStart) {
                screen = ((GuiCraftingStart) screen).getParent();
            }

            FMLCommonHandler.instance().showGuiScreen(new GuiCraftingPreview(screen, message.stacks, message.stack, message.quantity));
        });
    }

    public static void onReceiveCraftingStartResponse() {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;

            if (screen instanceof GuiCraftingStart) {
                ((GuiCraftingStart) screen).close();
            }
        });
    }

    private static ModelResourceLocation controllerLocation(String type, int energy) {
        return new ModelResourceLocation("refinedstorage:controller", "direction=north,energy=" + energy + ",type=" + type);
    }

    private static ModelResourceLocation portableGridLocation(String type, boolean connected, PortableGridDiskState diskState) {
        return new ModelResourceLocation("refinedstorage:portable_grid", "connected=" + connected + ",direction=north,disk_state=" + diskState + ",type=" + type);
    }

    private static ModelResourceLocation[] portableGridLocations() {
        List<ModelResourceLocation> locations = new ArrayList<>();

        for (String type : new String[] {"normal", "creative"}) {
            for (boolean connected : new boolean[] {true, false}) {
                for (PortableGridDiskState diskState : PortableGridDiskState.values()) {
                    locations.add(portableGridLocation(type, connected, diskState));
                }
            }
        }

        return locations.toArray(new ModelResourceLocation[locations.size()]);
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent e) {
        // DEVIATION: 1.8.9 ModelBakeEvent exposes modelRegistry as a field (no getKeys()),
        // so known locations are wrapped directly. Pattern uses a static inventory mapping;
        // portable-grid item models arrive via the ItemMeshDefinition as blockstate variants,
        // which are all wrapped for perspective (same alphabetical keys as registration).
        wrapPatternModel(e, new ModelResourceLocation("refinedstorage:pattern", "inventory"));

        for (ModelResourceLocation location : portableGridLocations()) {
            wrapPortableGridModel(e, location);
        }

        wrapPortableGridModel(e, new ModelResourceLocation("refinedstorage:portable_grid", "inventory"));
    }

    private void wrapPatternModel(ModelBakeEvent e, ModelResourceLocation location) {
        net.minecraft.client.resources.model.IBakedModel model = e.modelRegistry.getObject(location);

        if (model != null) {
            e.modelRegistry.putObject(location, new BakedModelPattern(model));
        }
    }

    private void wrapPortableGridModel(ModelBakeEvent e, ModelResourceLocation location) {
        net.minecraft.client.resources.model.IBakedModel model = e.modelRegistry.getObject(location);

        if (model != null) {
            e.modelRegistry.putObject(location, new BakedModelPortableGrid(model));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockDrawHighlight(DrawBlockHighlightEvent e) {
        // DEVIATION: 1.8.9 event exposes player/target/partialTicks as fields.
        if (e.target == null || e.target.getBlockPos() == null) {
            return;
        }

        EntityPlayer player = e.player;

        BlockPos pos = e.target.getBlockPos();

        Block block = player.getEntityWorld().getBlockState(pos).getBlock();

        if (!(block instanceof BlockCable)) {
            return;
        }

        BlockCable cable = (BlockCable) block;

        IBlockState state = cable.getActualStateForRendering(player.getEntityWorld(), pos);

        if (cable.collisionRayTrace(player.getEntityWorld(), pos, RSUtils.getStart(player), RSUtils.getEnd(player)) == null) {
            return;
        }

        List<AxisAlignedBB> unionized = cable.getUnionizedCollisionBoxes(state);
        List<AxisAlignedBB> nonUnionized = cable.getNonUnionizedCollisionBoxes(state);

        e.setCanceled(true);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
        // DEVIATION: 1.8.9 GlStateManager has no glLineWidth; raw GL call used.
        org.lwjgl.opengl.GL11.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) e.partialTicks;
        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) e.partialTicks;
        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) e.partialTicks;

        AxisAlignedBB unionizedAabb = unionized.get(0);

        for (int i = 1; i < unionized.size(); ++i) {
            unionizedAabb = unionizedAabb.union(unionized.get(i));
        }

        drawSelectionBoundingBox(unionizedAabb.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2).offset(pos.getX(), pos.getY(), pos.getZ()));

        for (AxisAlignedBB aabb : nonUnionized) {
            drawSelectionBoundingBox(aabb.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2).offset(pos.getX(), pos.getY(), pos.getZ()));
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawSelectionBoundingBox(AxisAlignedBB aabb) {
        Tessellator tessellator = Tessellator.getInstance();

        WorldRenderer buffer = tessellator.getWorldRenderer();

        buffer.begin(3, DefaultVertexFormats.POSITION);
        buffer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        buffer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();

        tessellator.draw();

        buffer.begin(3, DefaultVertexFormats.POSITION);
        buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();

        tessellator.draw();

        buffer.begin(1, DefaultVertexFormats.POSITION);
        buffer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();

        tessellator.draw();
    }
}
