package net.shoreline.client.asm.mixins.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.shoreline.client.asm.ducks.render.IItemFeatureRenderer;
import net.shoreline.client.impl.render.ClientRenderTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemFeatureRenderer.class)
public class MixinItemFeatureRenderer implements IItemFeatureRenderer
{
    @Unique
    private boolean nametagRendering;

    @Override
    public void shoreline$setNametagRendering(boolean bl)
    {
        this.nametagRendering = bl;
    }

    @ModifyExpressionValue(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;" +
                            "itemRenderType()Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType itemRenderTypeHook(RenderType original,
                                          @Local(name = "material") BakedQuad.MaterialInfo material)
    {
        return nametagRendering
                ? ClientRenderTypes.ITEM.apply(material.sprite().atlasLocation(), material.layer().translucent())
                : original;
    }

    @WrapOperation(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/" +
                            "ItemFeatureRenderer;getFoilBuffer(" +
                            "Lnet/minecraft/client/renderer/MultiBufferSource;" +
                            "Lnet/minecraft/client/renderer/rendertype/RenderType;" +
                            "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;)" +
                            "Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer shoreline$nametagFoilBuffer(MultiBufferSource bufferSource,
                                                       RenderType renderType,
                                                       PoseStack.Pose foilDecalPose,
                                                       Operation<VertexConsumer> original)
    {
        if (!nametagRendering)
        {
            return original.call(bufferSource, renderType, foilDecalPose);
        }

        VertexConsumer foil = bufferSource.getBuffer(ClientRenderTypes.GLINT);
        return foilDecalPose != null
                ? new SheetedDecalTextureGenerator(foil, foilDecalPose, 0.0078125f)
                : foil;
    }
}
