package com.raoulvdberge.refinedstorage.render;

import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.tile.TileStorageMonitor;
import com.raoulvdberge.refinedstorage.tile.config.IType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

public class TileEntitySpecialRendererStorageMonitor extends TileEntitySpecialRenderer<TileStorageMonitor> {
    // DEVIATION: 1.8.9 has no setLightmapDisabled (used by the 1.11 original for
    // fullbright monitor content). Understood equivalent: force max lightmap coords
    // while drawing, then restore the world's combined light for this position.
    @Override
    public void renderTileEntityAt(TileStorageMonitor tile, double x, double y, double z, float partialTicks, int destroyStage) {
        int combinedLight = tile.getWorld().getCombinedLight(tile.getPos(), 0);

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        float disX = 0, disXText = 0;
        float disY = 0.5F, disYText = 0.23F;
        float disZ = 0, disZText = 0;
        float spacing = 0.01F;

        float rotX = 0;
        float rotY = 0;
        float rotZ = 0;

        String amount = tile.getType() == IType.ITEMS ? RSUtils.formatQuantity(tile.getAmount()) : RSUtils.QUANTITY_FORMATTER.format((float) tile.getAmount() / 1000F);

        // Very bad, but I don't know how to translate a 2D font width to a 3D font width...
        float textWidth = 0;
        for (int i = 0; i < amount.length(); ++i) {
            char c = amount.charAt(i);
            if (c == '.') {
                textWidth += 0.005F;
            } else {
                textWidth += 0.026F;
            }
        }

        if (tile.getDirection() == EnumFacing.NORTH) {
            disX = 0.5F;
            disXText = disX + textWidth;

            disZ = -spacing;
            disZText = disZ - spacing;

            rotZ = 1F;
        } else if (tile.getDirection() == EnumFacing.WEST) {
            disX = -spacing;
            disXText = disX - spacing;

            disZ = 0.5F;
            disZText = disZ - textWidth;

            rotZ = 1F;
            rotX = 1F;
        } else if (tile.getDirection() == EnumFacing.SOUTH) {
            disX = 0.5F;
            disXText = disX - textWidth;

            disZ = 1F + spacing;
            disZText = disZ + spacing;

            rotX = 1F;
        } else if (tile.getDirection() == EnumFacing.EAST) {
            disX = 1F + spacing;
            disXText = disX + spacing;

            disZ = 0.5F;
            disZText = disZ + textWidth;

            rotZ = 1F;
            rotX = -1F;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + disX, y + disY, z + disZ);
        GlStateManager.rotate(180F, rotX, rotY, rotZ);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        // DEVIATION: 1.8.9 GlStateManager has no SourceFactor/DestFactor enums; raw GL factors used.
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.depthMask(true);
        GlStateManager.scale(0.4F, -0.4F, -0.015F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1F, 1F, 1F, 1F);

        if (tile.getItemStack() != null) {
            IBakedModel bakedModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(tile.getItemStack());
            // DEVIATION: 1.8.9 handleCameraTransforms takes (model, type) only.
            bakedModel = ForgeHooksClient.handleCameraTransforms(bakedModel, ItemCameraTransforms.TransformType.GUI);
            Minecraft.getMinecraft().getRenderItem().renderItem(tile.getItemStack(), bakedModel);
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        GlStateManager.disableBlend();
        GlStateManager.color(1F, 1F, 1F, 1F);

        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + disXText, y + disYText, z + disZText);
        GlStateManager.rotate(180F, rotX, rotY, rotZ);
        float size = 0.00450F;
        float factor = 2.0f;
        GlStateManager.scale(size * factor, size * factor, size);

        if (tile.getItemStack() != null) {
            Minecraft.getMinecraft().fontRendererObj.drawString(amount, 0, 0, 0xFFFFFF);
        }

        GlStateManager.popMatrix();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, combinedLight % 65536, combinedLight / 65536);
    }
}
