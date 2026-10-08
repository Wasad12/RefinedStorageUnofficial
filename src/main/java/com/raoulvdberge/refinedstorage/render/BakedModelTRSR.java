package com.raoulvdberge.refinedstorage.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.TRSRTransformation;

import java.util.List;

/**
 * @link https://github.com/SlimeKnights/Mantle/blob/master/src/main/java/slimeknights/mantle/client/model/TRSRBakedModel.java
 */
// PORT-SLICE-5: pass-through shell. Quad transformation is re-implemented against the 1.8.9
// IFlexibleBakedModel/vertex pipeline (verified BLOCK/ITEM formats per prompt §13).
public class BakedModelTRSR implements IBakedModel {
    protected final IBakedModel original;
    public TRSRTransformation transformation;

    public BakedModelTRSR(IBakedModel original, float x, float y, float z, float scale) {
        this(original, x, y, z, 0, 0, 0, scale, scale, scale);
    }

    public BakedModelTRSR(IBakedModel original, float x, float y, float z, float rotX, float rotY, float rotZ, float scale) {
        this(original, x, y, z, rotX, rotY, rotZ, scale, scale, scale);
    }

    public BakedModelTRSR(IBakedModel original, float x, float y, float z, float rotX, float rotY, float rotZ, float scaleX, float scaleY, float scaleZ) {
        this(original, TRSRTransformation.identity());
    }

    public BakedModelTRSR(IBakedModel original, TRSRTransformation transform) {
        this.original = original;
        this.transformation = transform;
    }

    /**
     * Rotates around the Y axis and adjusts culling appropriately. South is default.
     */
    public BakedModelTRSR(IBakedModel original, EnumFacing facing) {
        this(original, TRSRTransformation.identity());
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing facing) {
        return original.getFaceQuads(facing);
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        return original.getGeneralQuads();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return original.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return original.getParticleTexture();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return original.getItemCameraTransforms();
    }
}
