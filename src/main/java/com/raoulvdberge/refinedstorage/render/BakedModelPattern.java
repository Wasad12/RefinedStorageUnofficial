package com.raoulvdberge.refinedstorage.render;

import com.raoulvdberge.refinedstorage.apiimpl.autocrafting.CraftingPattern;
import com.raoulvdberge.refinedstorage.gui.GuiBase;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import java.util.List;

// PORT-SLICE-5: pass-through shell. Pattern-output override and perspective handling are
// re-implemented against 1.8.9 (ItemMeshDefinition/ISmartItemModel, §10 transforms).
public class BakedModelPattern implements IBakedModel {
    private IBakedModel base;

    public BakedModelPattern(IBakedModel base) {
        this.base = base;
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
        return GuiBase.isShiftKeyDown() && pattern.isValid() && pattern.getOutputs().size() == 1;
    }
}
