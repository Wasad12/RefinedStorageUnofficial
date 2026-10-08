package com.raoulvdberge.refinedstorage.block;

import com.raoulvdberge.refinedstorage.RSUtils;

import com.raoulvdberge.refinedstorage.proxy.CapabilityNetworkNodeProxy;
import com.raoulvdberge.refinedstorage.tile.TileCable;
import com.raoulvdberge.refinedstorage.tile.TileNode;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockCable extends BlockNode {
    public static final AxisAlignedBB CORE_AABB = RSUtils.getAABB(6, 6, 6, 10, 10, 10);
    private static final AxisAlignedBB NORTH_AABB = RSUtils.getAABB(6, 6, 0, 10, 10, 6);
    private static final AxisAlignedBB EAST_AABB = RSUtils.getAABB(10, 6, 6, 16, 10, 10);
    private static final AxisAlignedBB SOUTH_AABB = RSUtils.getAABB(6, 6, 10, 10, 10, 16);
    private static final AxisAlignedBB WEST_AABB = RSUtils.getAABB(0, 6, 6, 6, 10, 10);
    private static final AxisAlignedBB UP_AABB = RSUtils.getAABB(6, 10, 6, 10, 16, 10);
    private static final AxisAlignedBB DOWN_AABB = RSUtils.getAABB(6, 0, 6, 10, 6, 10);

    protected static final PropertyBool NORTH = PropertyBool.create("north");
    protected static final PropertyBool EAST = PropertyBool.create("east");
    protected static final PropertyBool SOUTH = PropertyBool.create("south");
    protected static final PropertyBool WEST = PropertyBool.create("west");
    protected static final PropertyBool UP = PropertyBool.create("up");
    protected static final PropertyBool DOWN = PropertyBool.create("down");

    public BlockCable(String name) {
        super(name);
    }

    public BlockCable() {
        this("cable");
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileCable();
    }

    public boolean hasConnectivityState() {
        return false;
    }

    @Override
    protected List<IProperty> getStateProperties() {
        List<IProperty> properties = super.getStateProperties();

        properties.add(NORTH);
        properties.add(EAST);
        properties.add(SOUTH);
        properties.add(WEST);
        properties.add(UP);
        properties.add(DOWN);

        return properties;
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);

        state = super.getActualState(state, world, pos)
            .withProperty(NORTH, hasConnectionWith(world, pos, tile, EnumFacing.NORTH))
            .withProperty(EAST, hasConnectionWith(world, pos, tile, EnumFacing.EAST))
            .withProperty(SOUTH, hasConnectionWith(world, pos, tile, EnumFacing.SOUTH))
            .withProperty(WEST, hasConnectionWith(world, pos, tile, EnumFacing.WEST))
            .withProperty(UP, hasConnectionWith(world, pos, tile, EnumFacing.UP))
            .withProperty(DOWN, hasConnectionWith(world, pos, tile, EnumFacing.DOWN));

        return state;
    }

    // This is used for rendering the box outlines in the client proxy.
    // We use this because MCMP wraps the block in a MCMP wrapper block, creating issues where
    // it cannot assign properties to the MCMP blockstate. Here, we make sure that it uses our block state.
    private IBlockState stateForRendering;

    public IBlockState getActualStateForRendering(IBlockAccess world, BlockPos pos) {
        if (stateForRendering == null) {
            stateForRendering = createBlockState().getBaseState();
        }

        return getActualState(stateForRendering, world, pos);
    }

    private boolean hasConnectionWith(IBlockAccess world, BlockPos pos, TileEntity tile, EnumFacing direction) {
        if (!(tile instanceof TileNode)) {
            return false;
        }

        TileEntity otherTile = world.getTileEntity(pos.offset(direction));
        EnumFacing otherTileSide = direction.getOpposite();

        if (otherTile != null && otherTile.hasCapability(CapabilityNetworkNodeProxy.NETWORK_NODE_PROXY_CAPABILITY, otherTileSide)) {
            if (getDirection() != null && ((TileNode) tile).getNode().getFacingTile() == otherTile) {
                return false;
            }

            return true;
        }

        return false;
    }

    protected boolean hitCablePart(IBlockState state, World world, BlockPos pos, float hitX, float hitY, float hitZ) {
        state = getActualState(state, world, pos);

        return RSUtils.isInAABB(CORE_AABB, hitX, hitY, hitZ) ||
            (state.getValue(NORTH) && RSUtils.isInAABB(NORTH_AABB, hitX, hitY, hitZ)) ||
            (state.getValue(EAST) && RSUtils.isInAABB(EAST_AABB, hitX, hitY, hitZ)) ||
            (state.getValue(SOUTH) && RSUtils.isInAABB(SOUTH_AABB, hitX, hitY, hitZ)) ||
            (state.getValue(WEST) && RSUtils.isInAABB(WEST_AABB, hitX, hitY, hitZ)) ||
            (state.getValue(UP) && RSUtils.isInAABB(UP_AABB, hitX, hitY, hitZ)) ||
            (state.getValue(DOWN) && RSUtils.isInAABB(DOWN_AABB, hitX, hitY, hitZ));
    }

    public List<AxisAlignedBB> getUnionizedCollisionBoxes(IBlockState state) {
        List<AxisAlignedBB> boxes = new ArrayList<>();

        boxes.add(CORE_AABB);

        if (state.getValue(NORTH)) {
            boxes.add(NORTH_AABB);
        }

        if (state.getValue(EAST)) {
            boxes.add(EAST_AABB);
        }

        if (state.getValue(SOUTH)) {
            boxes.add(SOUTH_AABB);
        }

        if (state.getValue(WEST)) {
            boxes.add(WEST_AABB);
        }

        if (state.getValue(UP)) {
            boxes.add(UP_AABB);
        }

        if (state.getValue(DOWN)) {
            boxes.add(DOWN_AABB);
        }

        return boxes;
    }

    public List<AxisAlignedBB> getNonUnionizedCollisionBoxes(IBlockState state) {
        return Collections.emptyList();
    }

    public List<AxisAlignedBB> getCollisionBoxes(IBlockState state) {
        List<AxisAlignedBB> boxes = new ArrayList<>();

        boxes.addAll(getUnionizedCollisionBoxes(state));
        boxes.addAll(getNonUnionizedCollisionBoxes(state));

        return boxes;
    }

    // DEVIATION: 1.8.9 hook is addCollisionBoxesToList(World, BlockPos, IBlockState, ...);
    // box-vs-mask test inlined (same logic as the later-Forge static helper).
    @Override
    @SuppressWarnings("deprecation")
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
        for (AxisAlignedBB aabb : getCollisionBoxes(this.getActualState(state, world, pos))) {
            AxisAlignedBB offset = aabb.offset(pos.getX(), pos.getY(), pos.getZ());

            if (entityBox.intersectsWith(offset)) {
                collidingBoxes.add(offset);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 start, Vec3 end) {
        IBlockState state = world.getBlockState(pos);

        RSUtils.AdvancedRayTraceResult result = RSUtils.collisionRayTrace(pos, start, end, getCollisionBoxes(this.getActualState(state, world, pos)));

        return result != null ? result.hit : null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube() {
        return false;
    }

    // DEVIATION: 1.8.9 Block has no getStateForPlacement; onBlockPlaced is the equivalent.
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = super.onBlockPlaced(world, pos, facing, hitX, hitY, hitZ, meta, entity);

        if (getDirection() != null) {
            return state.withProperty(getDirection().getProperty(), getDirection().getFrom(facing, pos, entity));
        }

        return state;
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    @Nullable
    public Direction getDirection() {
        return null;
    }

    public static AxisAlignedBB getCableExtensionAABB(EnumFacing facing) {
        if (facing == EnumFacing.NORTH) {
            return NORTH_AABB;
        } else if (facing == EnumFacing.EAST) {
            return EAST_AABB;
        } else if (facing == EnumFacing.SOUTH) {
            return SOUTH_AABB;
        } else if (facing == EnumFacing.WEST) {
            return WEST_AABB;
        } else if (facing == EnumFacing.UP) {
            return UP_AABB;
        } else if (facing == EnumFacing.DOWN) {
            return DOWN_AABB;
        }

        return NORTH_AABB;
    }
}
