package com.raoulvdberge.refinedstorage.render;

import com.google.common.base.Function;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.TRSRTransformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ModelDiskDrive implements IModel {
    private static final ResourceLocation MODEL_BASE = new ResourceLocation("refinedstorage:block/disk_drive");
    private static final ResourceLocation MODEL_DISK = new ResourceLocation("refinedstorage:block/disk");
    private static final ResourceLocation MODEL_DISK_NEAR_CAPACITY = new ResourceLocation("refinedstorage:block/disk_near_capacity");
    private static final ResourceLocation MODEL_DISK_FULL = new ResourceLocation("refinedstorage:block/disk_full");
    private static final ResourceLocation MODEL_DISK_DISCONNECTED = new ResourceLocation("refinedstorage:block/disk_disconnected");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        List<ResourceLocation> dependencies = new ArrayList<>();

        dependencies.add(MODEL_BASE);
        dependencies.add(MODEL_DISK);
        dependencies.add(MODEL_DISK_NEAR_CAPACITY);
        dependencies.add(MODEL_DISK_FULL);
        dependencies.add(MODEL_DISK_DISCONNECTED);

        return dependencies;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return Collections.emptyList();
    }

    // DEVIATION: BakedModelDiskDrive implements ISmartBlockModel + IFlexibleBakedModel
    // natively, so no Wrapper is needed (wrapping would hide handleBlockState).
    // (1.8.9 IModel.bake returns IFlexibleBakedModel, which our model is.)
    // Sub-models bake with IDENTITY state, not the incoming variant state: on 1.8.9 the
    // variant y-rotation is baked into quads (proven by vanilla blocks), while the
    // per-facing BakedModelTRSR rotations (read from blockstate direction at render)
    // already carry the full orientation — passing the variant rotation through would
    // rotate the drive twice (backwards). 1.11 defers orientation to render time, which
    // is why the source can pass state through; 1.8.9 cannot.
    @Override
    public IFlexibleBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IModel baseModel;
        IModel diskModel;
        IModel diskModelNearCapacity;
        IModel diskModelFull;
        IModel diskModelDisconnected;

        try {
            baseModel = ModelLoaderRegistry.getModel(MODEL_BASE);
            diskModel = ModelLoaderRegistry.getModel(MODEL_DISK);
            diskModelNearCapacity = ModelLoaderRegistry.getModel(MODEL_DISK_NEAR_CAPACITY);
            diskModelFull = ModelLoaderRegistry.getModel(MODEL_DISK_FULL);
            diskModelDisconnected = ModelLoaderRegistry.getModel(MODEL_DISK_DISCONNECTED);
        } catch (Exception e) {
            throw new Error("Unable to load disk drive models", e);
        }

        IModelState identity = TRSRTransformation.identity();

        return new BakedModelDiskDrive(
            format,
            baseModel.bake(identity, format, bakedTextureGetter),
            diskModel.bake(identity, format, bakedTextureGetter),
            diskModelNearCapacity.bake(identity, format, bakedTextureGetter),
            diskModelFull.bake(identity, format, bakedTextureGetter),
            diskModelDisconnected.bake(identity, format, bakedTextureGetter)
        );
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
}
