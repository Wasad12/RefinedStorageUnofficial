package com.raoulvdberge.refinedstorage.render;

import com.raoulvdberge.refinedstorage.RSUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.List;

// DEVIATION: perspective handling uses 1.8.9 IPerspectiveAwareModel (same interface
// name, IFlexibleBakedModel-based here). Disk-state/connected item variants arrive via
// the ItemMeshDefinition in ProxyClient (no ItemOverrideList on 1.8.9).
public class BakedModelPortableGrid implements IPerspectiveAwareModel {
    private IBakedModel base;

    public BakedModelPortableGrid(IBakedModel base) {
        this.base = base;
    }

    @Override
    public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        TRSRTransformation transform = RSUtils.getDefaultBlockTransforms().get(cameraTransformType);

        return Pair.of(this, transform == null ? RSUtils.EMPTY_MATRIX_TRANSFORM : transform.getMatrix());
    }

    @Override
    public VertexFormat getFormat() {
        if (base instanceof IFlexibleBakedModel) {
            return ((IFlexibleBakedModel) base).getFormat();
        }

        return DefaultVertexFormats.ITEM;
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
