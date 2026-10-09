package com.raoulvdberge.refinedstorage.render;

import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.apiimpl.autocrafting.CraftingPattern;
import com.raoulvdberge.refinedstorage.gui.GuiBase;
import com.raoulvdberge.refinedstorage.item.ItemPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.client.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.List;

// DEVIATION: 1.8.9 has no ItemOverrideList. The shift-shows-output behavior moves to
// ISmartItemModel.handleItemState (same trigger: shift + valid single-output pattern),
// and perspective handling uses 1.8.9 IPerspectiveAwareModel (same interface name,
// IFlexibleBakedModel-based here instead of vanilla IBakedModel).
public class BakedModelPattern implements IPerspectiveAwareModel, ISmartItemModel {
    private IBakedModel base;

    public BakedModelPattern(IBakedModel base) {
        this.base = base;
    }

    @Override
    public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        TRSRTransformation transform = RSUtils.getDefaultItemTransforms().get(cameraTransformType);

        return Pair.of(this, transform == null ? RSUtils.EMPTY_MATRIX_TRANSFORM : transform.getMatrix());
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack) {
        if (stack != null && Minecraft.getMinecraft().theWorld != null) {
            CraftingPattern pattern = ItemPattern.getPatternFromCache(Minecraft.getMinecraft().theWorld, stack);

            if (canDisplayPatternOutput(pattern) && !pattern.getOutputs().isEmpty() && pattern.getOutputs().get(0) != null) {
                IBakedModel output = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(pattern.getOutputs().get(0));

                if (output != null) {
                    return output;
                }
            }
        }

        return this;
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

    public static boolean canDisplayPatternOutput(CraftingPattern pattern) {
        return pattern != null && GuiBase.isShiftKeyDown() && pattern.isValid() && pattern.getOutputs().size() == 1;
    }
}
