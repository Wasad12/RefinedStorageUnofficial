package com.raoulvdberge.refinedstorage;

import com.google.common.collect.ImmutableMap;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.storage.AccessType;
import com.raoulvdberge.refinedstorage.api.storage.IStorage;
import com.raoulvdberge.refinedstorage.api.storage.IStorageDisk;
import com.raoulvdberge.refinedstorage.api.storage.IStorageDiskProvider;
import com.raoulvdberge.refinedstorage.api.util.IStackList;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Function;

public final class RSUtils {
    public static final ItemStack EMPTY_BUCKET = new ItemStack(Items.bucket);

    public static final Comparator<IStorage> STORAGE_COMPARATOR = (left, right) -> {
        int compare = Integer.compare(right.getPriority(), left.getPriority());

        return compare != 0 ? compare : Integer.compare(right.getStored(), left.getStored());
    };

    public static final DecimalFormat QUANTITY_FORMATTER = new DecimalFormat("####0.#", DecimalFormatSymbols.getInstance(Locale.US));

    private static final String NBT_INVENTORY = "Inventory_%d";
    private static final String NBT_SLOT = "Slot";
    private static final String NBT_ACCESS_TYPE = "AccessType";

    private static final Map<Integer, List<ItemStack>> OREDICT_CACHE = new HashMap<>();
    private static final Map<Integer, Boolean> OREDICT_EQUIVALENCY_CACHE = new HashMap<>();

    // DEVIATION: 1.8.9 has no NonNullList; plain lists are used (nulls filtered at boundaries).
    private static final List<Object> EMPTY_LIST = Collections.emptyList();

    public static List<ItemStack> singleItem(ItemStack stack) {
        List<ItemStack> list = new ArrayList<>();
        list.add(stack);
        return list;
    }

    static {
        QUANTITY_FORMATTER.setRoundingMode(RoundingMode.DOWN);
    }

    public static List<ItemStack> getEquivalentStacks(ItemStack stack) {
        int hash = API.instance().getItemStackHashCode(stack, false);

        if (OREDICT_CACHE.containsKey(hash)) {
            return OREDICT_CACHE.get(hash);
        }

        List<ItemStack> ores = new ArrayList<>();

        for (int id : OreDictionary.getOreIDs(stack)) {
            ores.addAll(OreDictionary.getOres(OreDictionary.getOreName(id)));
        }

        OREDICT_CACHE.put(hash, ores);

        return ores;
    }

    public static boolean areStacksEquivalent(ItemStack left, ItemStack right) {
        int code = API.instance().getItemStackHashCode(left, false);
        code = 31 * code + API.instance().getItemStackHashCode(right, false);

        if (OREDICT_EQUIVALENCY_CACHE.containsKey(code)) {
            return OREDICT_EQUIVALENCY_CACHE.get(code);
        }

        int[] leftIds = OreDictionary.getOreIDs(left);
        int[] rightIds = OreDictionary.getOreIDs(right);

        for (int i : rightIds) {
            if (ArrayUtils.contains(leftIds, i)) {
                OREDICT_EQUIVALENCY_CACHE.put(code, true);

                return true;
            }
        }

        OREDICT_EQUIVALENCY_CACHE.put(code, false);

        return false;
    }

    public static void writeItemStack(ByteBuf buf, ItemStack stack) {
        buf.writeInt(Item.getIdFromItem(stack.getItem()));
        buf.writeInt(stack.stackSize);
        buf.writeInt(stack.getItemDamage());
        // DEVIATION: 1.8.9 Item has no getNBTShareTag; the full tag is written (same wire effect here).
        ByteBufUtils.writeTag(buf, stack.getTagCompound());
    }

    public static ItemStack readItemStack(ByteBuf buf) {
        ItemStack stack = new ItemStack(Item.getItemById(buf.readInt()), buf.readInt(), buf.readInt());
        stack.setTagCompound(ByteBufUtils.readTag(buf));
        return stack;
    }

    public static void writeItemStack(ByteBuf buf, ItemStack stack, @Nullable INetwork network, boolean displayCraftText) {
        writeItemStack(buf, stack);

        buf.writeInt(API.instance().getItemStackHashCode(stack));

        if (network != null) {
            buf.writeBoolean(network.getCraftingManager().hasPattern(stack));
            buf.writeBoolean(displayCraftText);
        } else {
            buf.writeBoolean(false);
            buf.writeBoolean(false);
        }
    }

    public static void writeFluidStack(ByteBuf buf, FluidStack stack) {
        buf.writeInt(API.instance().getFluidStackHashCode(stack));
        ByteBufUtils.writeUTF8String(buf, FluidRegistry.getFluidName(stack.getFluid()));
        buf.writeInt(stack.amount);
        ByteBufUtils.writeTag(buf, stack.tag);
    }

    public static Pair<Integer, FluidStack> readFluidStack(ByteBuf buf) {
        return Pair.of(buf.readInt(), new FluidStack(FluidRegistry.getFluid(ByteBufUtils.readUTF8String(buf)), buf.readInt(), ByteBufUtils.readTag(buf)));
    }

    // DEVIATION: 1.8.9 has no null; null clears slots natively. Pass-through.
    public static ItemStack transformNullToEmpty(@Nullable ItemStack stack) {
        return stack;
    }

    // DEVIATION: 1.8.9 ItemStack has no isEmpty()/getCount(). Null-safe emptiness check
    // replacing 1.11 isEmpty() at ItemStack-typed call sites (Collection.isEmpty is untouched).
    public static boolean isEmpty(@Nullable ItemStack stack) {
        return stack == null || stack.stackSize <= 0;
    }

    // DEVIATION: 1.8.9 Item has no getRegistryName. The owning mod id is resolved
    // through GameRegistry.findUniqueIdentifierFor ("" when unknown/unregistered).
    public static String getModId(@Nullable ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return "";
        }

        GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());

        return id == null ? "" : id.modId;
    }

    // DEVIATION: 1.8.9 has no null; null is the empty representation and
    // setInventorySlotContents(i, null) natively clears slots. Null-safe pass-through
    // preserving call-site null-check semantics.
    @Nullable
    public static ItemStack transformEmptyToNull(@Nullable ItemStack stack) {
        return isEmpty(stack) ? null : stack;
    }

    @SuppressWarnings("unchecked")
    public static void createStorages(ItemStack disk, int slot, IStorageDisk<ItemStack>[] itemStorages, IStorageDisk<FluidStack>[] fluidStorages, Function<IStorageDisk<ItemStack>, IStorageDisk> itemStorageWrapper, Function<IStorageDisk<FluidStack>, IStorageDisk> fluidStorageWrapper) {
        if (RSUtils.isEmpty(disk)) {
            itemStorages[slot] = null;
            fluidStorages[slot] = null;
        } else {
            IStorageDiskProvider provider = (IStorageDiskProvider) disk.getItem();
            IStorageDisk storage = provider.create(disk);

            storage.readFromNBT();

            switch (storage.getType()) {
                case ITEMS:
                    itemStorages[slot] = itemStorageWrapper.apply(storage);
                    break;
                case FLUIDS:
                    fluidStorages[slot] = fluidStorageWrapper.apply(storage);
                    break;
            }
        }
    }

    public static List<ItemStack> toNonNullList(List<ItemStack> list) {
        List<ItemStack> other = new ArrayList<>();

        for (ItemStack item : list) {
            if (item != null) {
                other.add(item);
            }
        }

        return other;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> emptyNonNullList() {
        return (List<T>) EMPTY_LIST;
    }

    public static void writeItems(IItemHandler handler, int id, NBTTagCompound tag) {
        NBTTagList tagList = new NBTTagList();

        for (int i = 0; i < handler.getSlots(); i++) {
            if (!RSUtils.isEmpty(handler.getStackInSlot(i))) {
                NBTTagCompound stackTag = new NBTTagCompound();

                stackTag.setInteger(NBT_SLOT, i);

                handler.getStackInSlot(i).writeToNBT(stackTag);

                tagList.appendTag(stackTag);
            }
        }

        tag.setTag(String.format(NBT_INVENTORY, id), tagList);
    }

    public static void readItems(IItemHandlerModifiable handler, int id, NBTTagCompound tag) {
        String name = String.format(NBT_INVENTORY, id);

        if (tag.hasKey(name)) {
            NBTTagList tagList = tag.getTagList(name, Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                int slot = tagList.getCompoundTagAt(i).getInteger(NBT_SLOT);

                if (slot >= 0 && slot < handler.getSlots()) {
                    handler.setStackInSlot(slot, ItemStack.loadItemStackFromNBT(tagList.getCompoundTagAt(i)));
                }
            }
        }
    }

    public static void writeItemsLegacy(IInventory inventory, int id, NBTTagCompound tag) {
        NBTTagList tagList = new NBTTagList();

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (!RSUtils.isEmpty(inventory.getStackInSlot(i))) {
                NBTTagCompound stackTag = new NBTTagCompound();

                stackTag.setInteger(NBT_SLOT, i);

                inventory.getStackInSlot(i).writeToNBT(stackTag);

                tagList.appendTag(stackTag);
            }
        }

        tag.setTag(String.format(NBT_INVENTORY, id), tagList);
    }

    public static void readItemsLegacy(IInventory inventory, int id, NBTTagCompound tag) {
        String name = String.format(NBT_INVENTORY, id);

        if (tag.hasKey(name)) {
            NBTTagList tagList = tag.getTagList(name, Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                int slot = tagList.getCompoundTagAt(i).getInteger(NBT_SLOT);

                ItemStack stack = ItemStack.loadItemStackFromNBT(tagList.getCompoundTagAt(i));

                if (!RSUtils.isEmpty(stack)) {
                    inventory.setInventorySlotContents(slot, stack);
                }
            }
        }
    }

    public static NBTTagList serializeFluidStackList(IStackList<FluidStack> list) {
        NBTTagList tagList = new NBTTagList();

        for (FluidStack stack : list.getStacks()) {
            tagList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }

        return tagList;
    }

    public static IStackList<FluidStack> readFluidStackList(NBTTagList tagList) {
        IStackList<FluidStack> list = API.instance().createFluidStackList();

        for (int i = 0; i < tagList.tagCount(); ++i) {
            FluidStack stack = FluidStack.loadFluidStackFromNBT(tagList.getCompoundTagAt(i));

            if (stack != null) {
                list.add(stack, stack.amount);
            }
        }

        return list;
    }

    public static void writeAccessType(NBTTagCompound tag, AccessType type) {
        tag.setInteger(NBT_ACCESS_TYPE, type.getId());
    }

    public static AccessType readAccessType(NBTTagCompound tag) {
        return tag.hasKey(NBT_ACCESS_TYPE) ? getAccessType(tag.getInteger(NBT_ACCESS_TYPE)) : AccessType.INSERT_EXTRACT;
    }

    public static AccessType getAccessType(int id) {
        for (AccessType type : AccessType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return AccessType.INSERT_EXTRACT;
    }

    public static void updateBlock(World world, BlockPos pos) {
        if (world != null) {
            // DEVIATION: 1.8.9 has no notifyBlockUpdate; markBlockForUpdate is the equivalent.
            world.markBlockForUpdate(pos);
        }
    }

    public static IItemHandler getItemHandler(@Nullable TileEntity tile, EnumFacing side) {
        if (tile == null) {
            return null;
        }

        IItemHandler handler = tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) ? tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) : null;

        if (handler == null) {
            if (side != null && tile instanceof ISidedInventory) {
                handler = new SidedInvWrapper((ISidedInventory) tile, side);
            } else if (tile instanceof IInventory) {
                handler = new InvWrapper((IInventory) tile);
            }
        }

        return handler;
    }

    // DEVIATION: 1.8.9 exposes fluids via instanceof IFluidHandler, not capabilities.
    public static IFluidHandler getFluidHandler(TileEntity tile, EnumFacing side) {
        return tile instanceof IFluidHandler ? (IFluidHandler) tile : null;
    }

    // DEVIATION: bucket/container draining via FluidContainerRegistry (no item capabilities).
    // Amounts resolve to the registered container amount (buckets: 1000).
    public static Pair<ItemStack, FluidStack> getFluidFromStack(ItemStack stack, boolean simulate) {
        if (stack == null) {
            return Pair.of(null, null);
        }

        FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);

        if (fluid == null) {
            return Pair.of(null, null);
        }

        if (simulate) {
            return Pair.of(stack, fluid.copy());
        }

        return Pair.of(FluidContainerRegistry.drainFluidContainer(stack), fluid);
    }

    public static boolean hasFluidBucket(FluidStack stack) {
        return stack.getFluid() == FluidRegistry.WATER || stack.getFluid() == FluidRegistry.LAVA || stack.getFluid().getName().equals("milk") || FluidRegistry.getBucketFluids().contains(stack.getFluid());
    }

    public static FluidStack copyStackWithSize(FluidStack stack, int size) {
        FluidStack copy = stack.copy();
        copy.amount = size;
        return copy;
    }

    public static FluidStack copyStack(@Nullable FluidStack stack) {
        return stack == null ? null : stack.copy();
    }

    public static void sendNoPermissionMessage(EntityPlayer player) {
        player.addChatMessage(new ChatComponentTranslation("misc.refinedstorage:security.no_permission").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
    }

    public static String formatQuantity(int qty) {
        if (qty >= 1000000) {
            return QUANTITY_FORMATTER.format((float) qty / 1000000F) + "M";
        } else if (qty >= 1000) {
            return QUANTITY_FORMATTER.format((float) qty / 1000F) + "K";
        }

        return String.valueOf(qty);
    }

    public static AxisAlignedBB getAABB(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return new AxisAlignedBB((float) fromX / 16F, (float) fromY / 16F, (float) fromZ / 16F, (float) toX / 16F, (float) toY / 16F, (float) toZ / 16F);
    }

    public static boolean isInAABB(AxisAlignedBB aabb, float hitX, float hitY, float hitZ) {
        return hitX >= aabb.minX && hitX <= aabb.maxX && hitY >= aabb.minY && hitY <= aabb.maxY && hitZ >= aabb.minZ && hitZ <= aabb.maxZ;
    }

    private static class AdvancedRayTraceResultBase<T extends MovingObjectPosition> {
        public final AxisAlignedBB bounds;
        public final T hit;

        public AdvancedRayTraceResultBase(T mop, AxisAlignedBB bounds) {

            this.hit = mop;
            this.bounds = bounds;
        }

        public boolean valid() {
            return hit != null && bounds != null;
        }

        public double squareDistanceTo(Vec3 vec) {
            return hit.hitVec.squareDistanceTo(vec);
        }
    }

    public static class AdvancedRayTraceResult extends AdvancedRayTraceResultBase<MovingObjectPosition> {
        public AdvancedRayTraceResult(MovingObjectPosition mop, AxisAlignedBB bounds) {
            super(mop, bounds);
        }
    }

    public static Vec3 getStart(EntityPlayer player) {
        return new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ);
    }

    public static Vec3 getEnd(EntityPlayer player) {
        // DEVIATION: 1.8.9 field names (theItemInWorldManager, xCoord/yCoord/zCoord).
        double reachDistance = player instanceof EntityPlayerMP ? ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance() : (player.capabilities.isCreativeMode ? 5.0D : 4.5D);

        Vec3 lookVec = player.getLookVec();
        Vec3 start = getStart(player);

        return start.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);
    }

    public static AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vec3 start, Vec3 end, Collection<AxisAlignedBB> boxes) {
        double minDistance = Double.POSITIVE_INFINITY;
        AdvancedRayTraceResult hit = null;
        int i = -1;

        for (AxisAlignedBB aabb : boxes) {
            AdvancedRayTraceResult result = aabb == null ? null : collisionRayTrace(pos, start, end, aabb, i, null);

            if (result != null) {
                double d = result.squareDistanceTo(start);
                if (d < minDistance) {
                    minDistance = d;
                    hit = result;
                }
            }

            i++;
        }

        return hit;
    }

    public static AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vec3 start, Vec3 end, AxisAlignedBB bounds, int subHit, Object hitInfo) {
        MovingObjectPosition result = bounds.offset(pos.getX(), pos.getY(), pos.getZ()).calculateIntercept(start, end);

        if (result == null) {
            return null;
        }

        result = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.BLOCK, result.hitVec, result.sideHit, pos);
        result.subHit = subHit;
        result.hitInfo = hitInfo;

        return new AdvancedRayTraceResult(result, bounds);
    }

    public static class FluidRenderer {
        private static final int TEX_WIDTH = 16;
        private static final int TEX_HEIGHT = 16;
        private static final int MIN_FLUID_HEIGHT = 1;

        private final int capacityMb;
        private final int width;
        private final int height;

        public FluidRenderer(int capacityMb, int width, int height) {
            this.capacityMb = capacityMb;
            this.width = width;
            this.height = height;
        }

        public void draw(Minecraft minecraft, int xPosition, int yPosition, FluidStack fluidStack) {
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();

            drawFluid(minecraft, xPosition, yPosition, fluidStack);

            GlStateManager.color(1, 1, 1, 1);

            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
        }

        private void drawFluid(Minecraft minecraft, int xPosition, int yPosition, FluidStack fluidStack) {
            if (fluidStack == null) {
                return;
            }

            Fluid fluid = fluidStack.getFluid();

            if (fluid == null) {
                return;
            }

            TextureMap textureMapBlocks = minecraft.getTextureMapBlocks();
            ResourceLocation fluidStill = fluid.getStill();
            TextureAtlasSprite fluidStillSprite = null;

            if (fluidStill != null) {
                fluidStillSprite = textureMapBlocks.getTextureExtry(fluidStill.toString());
            }

            if (fluidStillSprite == null) {
                fluidStillSprite = textureMapBlocks.getMissingSprite();
            }

            int fluidColor = fluid.getColor();

            int scaledAmount = height;

            if (capacityMb != -1) {
                scaledAmount = (fluidStack.amount * height) / capacityMb;

                if (fluidStack.amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
                    scaledAmount = MIN_FLUID_HEIGHT;
                }

                if (scaledAmount > height) {
                    scaledAmount = height;
                }
            }

            minecraft.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            setGLColorFromInt(fluidColor);

            int xTileCount = width / TEX_WIDTH;
            int xRemainder = width - (xTileCount * TEX_WIDTH);
            int yTileCount = scaledAmount / TEX_HEIGHT;
            int yRemainder = scaledAmount - (yTileCount * TEX_HEIGHT);

            int yStart = yPosition + height;

            for (int xTile = 0; xTile <= xTileCount; xTile++) {
                for (int yTile = 0; yTile <= yTileCount; yTile++) {
                    int width = (xTile == xTileCount) ? xRemainder : TEX_WIDTH;
                    int height = (yTile == yTileCount) ? yRemainder : TEX_HEIGHT;
                    int x = xPosition + (xTile * TEX_WIDTH);
                    int y = yStart - ((yTile + 1) * TEX_HEIGHT);

                    if (width > 0 && height > 0) {
                        int maskTop = TEX_HEIGHT - height;
                        int maskRight = TEX_WIDTH - width;

                        drawFluidTexture(x, y, fluidStillSprite, maskTop, maskRight, 100);
                    }
                }
            }
        }
    }

    private static void setGLColorFromInt(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        GlStateManager.color(red, green, blue, 1.0F);
    }

    private static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
        double uMin = (double) textureSprite.getMinU();
        double uMax = (double) textureSprite.getMaxU();
        double vMin = (double) textureSprite.getMinV();
        double vMax = (double) textureSprite.getMaxV();
        uMax = uMax - (maskRight / 16.0 * (uMax - uMin));
        vMax = vMax - (maskTop / 16.0 * (vMax - vMin));

        Tessellator tessellator = Tessellator.getInstance();

        WorldRenderer WorldRenderer = tessellator.getWorldRenderer();
        WorldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        WorldRenderer.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
        WorldRenderer.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
        WorldRenderer.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
        WorldRenderer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    // DEVIATION: full 1.8.9 port of the source's ForgeBlockStateV1-derived defaults.
    // 1.8.9 TransformType has no LEFT/RIGHT_HAND split (single THIRD_PERSON/FIRST_PERSON),
    // and TRSRTransformation has quatFromYXZDegrees, not quatFromXYZDegrees. Single-axis
    // entries are identical under either order; the two dual-axis block entries
    // (thirdperson 75/45, GUI 30/225) are YXZ approximations of the XYZ originals.
    // NOTE: EMPTY_MATRIX_TRANSFORM is built WITHOUT TRSRTransformation on purpose:
    // net.minecraftforge.client.model is client-only, and this static initializer runs
    // on dedicated servers too (source could use it: 1.11 TRSRTransformation is common).
    // The 16-float Matrix4f below is exactly identity, matching getTransform(0..1).
    public static final Matrix4f EMPTY_MATRIX_TRANSFORM = new Matrix4f(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1);

    private static TRSRTransformation getTransform(float tx, float ty, float tz, float ax, float ay, float az, float s) {
        return new TRSRTransformation(
            new javax.vecmath.Vector3f(tx / 16, ty / 16, tz / 16),
            TRSRTransformation.quatFromYXZDegrees(new javax.vecmath.Vector3f(ax, ay, az)),
            new javax.vecmath.Vector3f(s, s, s),
            null
        );
    }

    private static ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> DEFAULT_ITEM_TRANSFORM;
    private static ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> DEFAULT_BLOCK_TRANSFORM;

    public static ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> getDefaultItemTransforms() {
        if (DEFAULT_ITEM_TRANSFORM != null) {
            return DEFAULT_ITEM_TRANSFORM;
        }

        return DEFAULT_ITEM_TRANSFORM = ImmutableMap.<ItemCameraTransforms.TransformType, TRSRTransformation>builder()
            .put(ItemCameraTransforms.TransformType.THIRD_PERSON, getTransform(0, 3, 1, 0, 0, 0, 0.55f))
            .put(ItemCameraTransforms.TransformType.FIRST_PERSON, getTransform(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f))
            .put(ItemCameraTransforms.TransformType.GROUND, getTransform(0, 2, 0, 0, 0, 0, 0.5f))
            .put(ItemCameraTransforms.TransformType.HEAD, getTransform(0, 13, 7, 0, 180, 0, 1))
            .build();
    }

    public static ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> getDefaultBlockTransforms() {
        if (DEFAULT_BLOCK_TRANSFORM != null) {
            return DEFAULT_BLOCK_TRANSFORM;
        }

        return DEFAULT_BLOCK_TRANSFORM = ImmutableMap.<ItemCameraTransforms.TransformType, TRSRTransformation>builder()
            .put(ItemCameraTransforms.TransformType.GUI, getTransform(0, 0, 0, 30, 225, 0, 0.625f))
            .put(ItemCameraTransforms.TransformType.GROUND, getTransform(0, 3, 0, 0, 0, 0, 0.25f))
            .put(ItemCameraTransforms.TransformType.FIXED, getTransform(0, 0, 0, 0, 0, 0, 0.5f))
            .put(ItemCameraTransforms.TransformType.THIRD_PERSON, getTransform(0, 2.5f, 0, 75, 45, 0, 0.375f))
            .put(ItemCameraTransforms.TransformType.FIRST_PERSON, getTransform(0, 0, 0, 0, 45, 0, 0.4f))
            .build();
    }
}
