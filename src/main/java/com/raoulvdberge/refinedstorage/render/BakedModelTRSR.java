package com.raoulvdberge.refinedstorage.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.TRSRTransformation;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;

/**
 * @link https://github.com/SlimeKnights/Mantle/blob/master/src/main/java/slimeknights/mantle/client/model/TRSRBakedModel.java
 *
 * DEVIATION: 1.8.9 has no ItemOverrideList and BakedQuad carries no format, so the
 * 1.11 VertexTransformer/UnpackedBakedQuad pipeline is replaced by a direct int-level
 * POSITION transform (POSITION is element 0 in every vanilla/Forge format; 1.8.9 BLOCK
 * quads carry no NORMAL element, so there is nothing else to rotate). TRSR math
 * (blockCenterToCorner, culling adjustment) is preserved verbatim.
 */
public class BakedModelTRSR implements IBakedModel {
    protected final IBakedModel original;
    public TRSRTransformation transformation;
    private final int faceOffset;

    public BakedModelTRSR(IBakedModel original, float x, float y, float z, float scale) {
        this(original, x, y, z, 0, 0, 0, scale, scale, scale);
    }

    public BakedModelTRSR(IBakedModel original, float x, float y, float z, float rotX, float rotY, float rotZ, float scale) {
        this(original, x, y, z, rotX, rotY, rotZ, scale, scale, scale);
    }

    public BakedModelTRSR(IBakedModel original, float x, float y, float z, float rotX, float rotY, float rotZ, float scaleX, float scaleY, float scaleZ) {
        this(original, new TRSRTransformation(
            new javax.vecmath.Vector3f(x, y, z),
            null,
            new javax.vecmath.Vector3f(scaleX, scaleY, scaleZ),
            // DEVIATION: 1.8.9 has quatFromYXZ(y,x,z), source quatFromXYZ(x,y,z).
            // Args reordered to preserve semantics (single-axis uses stay on the same axis).
            TRSRTransformation.quatFromYXZ(rotY, rotX, rotZ)));
    }

    public BakedModelTRSR(IBakedModel original, TRSRTransformation transform) {
        this.original = original;
        this.transformation = TRSRTransformation.blockCenterToCorner(transform);
        this.faceOffset = 0;
    }

    /**
     * Rotates around the Y axis and adjusts culling appropriately. South is default.
     */
    public BakedModelTRSR(IBakedModel original, EnumFacing facing) {
        this.original = original;
        this.faceOffset = 4 + EnumFacing.NORTH.getHorizontalIndex() - facing.getHorizontalIndex();

        double r = Math.PI * (360 - facing.getOpposite().getHorizontalIndex() * 90) / 180d;
        // FIX 2026-10-09: quatFromYXZ takes (y,x,z) — the Y angle goes FIRST.
        // The old code passed (0,r,0) = X rotation, which is why north (r=0) worked
        // while S/E/W were permuted (south trans was (0,1,1) not (1,0,1)).
        TRSRTransformation t = new TRSRTransformation(null, null, null, TRSRTransformation.quatFromYXZ((float) r, 0, 0));
        this.transformation = TRSRTransformation.blockCenterToCorner(t);
    }

    protected BakedQuad transformQuad(BakedQuad quad) {
        int[] data = quad.getVertexData().clone();
        int stride = data.length / 4;

        Matrix4f matrix = transformation.getMatrix();

        for (int v = 0; v < 4; ++v) {
            Vector4f vec = new Vector4f(
                Float.intBitsToFloat(data[v * stride]),
                Float.intBitsToFloat(data[v * stride + 1]),
                Float.intBitsToFloat(data[v * stride + 2]),
                1f);
            matrix.transform(vec);
            data[v * stride] = Float.floatToRawIntBits(vec.x);
            data[v * stride + 1] = Float.floatToRawIntBits(vec.y);
            data[v * stride + 2] = Float.floatToRawIntBits(vec.z);
        }

        return new BakedQuad(data, quad.hasTintIndex() ? quad.getTintIndex() : -1, quad.getFace());
    }

    protected List<BakedQuad> transformQuads(List<BakedQuad> quads) {
        List<BakedQuad> result = new ArrayList<>(quads.size());

        for (BakedQuad quad : quads) {
            result.add(transformQuad(quad));
        }

        return result;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing facing) {
        EnumFacing side = facing;

        // adjust side to facing-rotation (source parity)
        if (side != null && side.getHorizontalIndex() > -1) {
            side = EnumFacing.getHorizontal((side.getHorizontalIndex() + faceOffset) % 4);
        }

        return transformQuads(original.getFaceQuads(side));
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        return transformQuads(original.getGeneralQuads());
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
