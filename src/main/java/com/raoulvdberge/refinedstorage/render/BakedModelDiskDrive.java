package com.raoulvdberge.refinedstorage.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.raoulvdberge.refinedstorage.RSBlocks;
import com.raoulvdberge.refinedstorage.block.BlockDiskDrive;
import com.raoulvdberge.refinedstorage.tile.TileDiskDrive;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import java.util.*;

public class BakedModelDiskDrive implements ISmartBlockModel, IFlexibleBakedModel {
    private class CacheKey {
        private IBlockState state;
        private Integer[] diskState;

        CacheKey(IBlockState state, Integer[] diskState) {
            this.state = state;
            // DEVIATION: snapshot — the tile hands out its LIVE array (mutated in place by
            // readUpdate), which would corrupt hash/equals for cached keys.
            this.diskState = diskState.clone();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) o;

            if (!state.equals(cacheKey.state)) {
                return false;
            }

            return Arrays.equals(diskState, cacheKey.diskState);
        }

        @Override
        public int hashCode() {
            int result = state.hashCode();
            result = 31 * result + Arrays.hashCode(diskState);
            return result;
        }
    }

    private static class ComposedModel implements IBakedModel {
        private final IBakedModel base;
        private final Map<EnumFacing, List<BakedQuad>> faces;
        private final List<BakedQuad> general;

        ComposedModel(IBakedModel base, Map<EnumFacing, List<BakedQuad>> faces, List<BakedQuad> general) {
            this.base = base;
            this.faces = faces;
            this.general = general;
        }

        @Override
        public List<BakedQuad> getFaceQuads(EnumFacing facing) {
            List<BakedQuad> quads = faces.get(facing);

            return quads != null ? quads : Collections.<BakedQuad>emptyList();
        }

        @Override
        public List<BakedQuad> getGeneralQuads() {
            return general;
        }

        @Override
        public boolean isAmbientOcclusion() {
            return base.isAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return base.isGui3d();
        }

        @Override
        public boolean isBuiltInRenderer() {
            return base.isBuiltInRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return base.getParticleTexture();
        }

        @Override
        @SuppressWarnings("deprecation")
        public ItemCameraTransforms getItemCameraTransforms() {
            return base.getItemCameraTransforms();
        }
    }

    private IBakedModel base;
    private VertexFormat format;
    private Map<EnumFacing, IBakedModel> models = new HashMap<>();
    private Map<EnumFacing, Map<Integer, List<IBakedModel>>> disks = new HashMap<>();

    private LoadingCache<CacheKey, IBakedModel> cache = CacheBuilder.newBuilder().build(new CacheLoader<CacheKey, IBakedModel>() {
        @Override
        public IBakedModel load(CacheKey key) throws Exception {
            EnumFacing facing = key.state.getValue(RSBlocks.DISK_DRIVE.getDirection().getProperty());

            Map<EnumFacing, List<BakedQuad>> faces = new EnumMap<>(EnumFacing.class);

            for (EnumFacing side : EnumFacing.VALUES) {
                List<BakedQuad> quads = new ArrayList<>(models.get(facing).getFaceQuads(side));

                for (int i = 0; i < 8; ++i) {
                    if (key.diskState[i] != TileDiskDrive.DISK_STATE_NONE) {
                        quads.addAll(disks.get(facing).get(key.diskState[i]).get(i).getFaceQuads(side));
                    }
                }

                faces.put(side, quads);
            }

            List<BakedQuad> general = new ArrayList<>(models.get(facing).getGeneralQuads());

            for (int i = 0; i < 8; ++i) {
                if (key.diskState[i] != TileDiskDrive.DISK_STATE_NONE) {
                    general.addAll(disks.get(facing).get(key.diskState[i]).get(i).getGeneralQuads());
                }
            }

            return new ComposedModel(base, faces, general);
        }
    });

    public BakedModelDiskDrive(VertexFormat format, IBakedModel base, IBakedModel disk, IBakedModel diskNearCapacity, IBakedModel diskFull, IBakedModel diskDisconnected) {
        this.base = base;
        this.format = format;

        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            models.put(facing, new BakedModelTRSR(base, facing));

            disks.put(facing, new HashMap<>());

            disks.get(facing).put(TileDiskDrive.DISK_STATE_NORMAL, new ArrayList<>());
            disks.get(facing).put(TileDiskDrive.DISK_STATE_NEAR_CAPACITY, new ArrayList<>());
            disks.get(facing).put(TileDiskDrive.DISK_STATE_FULL, new ArrayList<>());
            disks.get(facing).put(TileDiskDrive.DISK_STATE_DISCONNECTED, new ArrayList<>());

            initDiskModels(disk, TileDiskDrive.DISK_STATE_NORMAL, facing);
            initDiskModels(diskNearCapacity, TileDiskDrive.DISK_STATE_NEAR_CAPACITY, facing);
            initDiskModels(diskFull, TileDiskDrive.DISK_STATE_FULL, facing);
            initDiskModels(diskDisconnected, TileDiskDrive.DISK_STATE_DISCONNECTED, facing);
        }
    }

    private void initDiskModels(IBakedModel disk, int type, EnumFacing facing) {
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                BakedModelTRSR model = new BakedModelTRSR(disk, facing);

                Vector3f trans = model.transformation.getTranslation();

                if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
                    trans.x += ((2F / 16F) + ((float) x * 7F) / 16F) * (facing == EnumFacing.NORTH ? -1 : 1);
                } else if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
                    trans.z += ((2F / 16F) + ((float) x * 7F) / 16F) * (facing == EnumFacing.EAST ? -1 : 1);
                }

                trans.y -= (2F / 16F) + ((float) y * 3F) / 16F;

                model.transformation = new TRSRTransformation(trans, model.transformation.getLeftRot(), model.transformation.getScale(), model.transformation.getRightRot());

                disks.get(facing).get(type).add(model);
            }
        }
    }

    // DEVIATION: 1.8.9 delivers extended state via ISmartBlockModel.handleBlockState
    // (no per-side getQuads). The composed model is cached per (state, disk-state).
    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        if (!(state instanceof IExtendedBlockState)) {
            return this;
        }

        Integer[] diskState = ((IExtendedBlockState) state).getValue(BlockDiskDrive.DISK_STATE);

        if (diskState == null) {
            return this;
        }

        return cache.getUnchecked(new CacheKey(((IExtendedBlockState) state).getClean(), diskState));
    }

    @Override
    public VertexFormat getFormat() {
        return format;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing facing) {
        return base.getFaceQuads(facing);
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        return base.getGeneralQuads();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return base.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return base.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return base.getParticleTexture();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return base.getItemCameraTransforms();
    }
}
