package net.shoreline.client.asm.mixins.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.shoreline.client.impl.modules.render.NametagsModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer<T extends Entity, S extends EntityRenderState>
{
    @Inject(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V", at = @At(value = "HEAD"), cancellable = true)
    private void shouldShowNameHook(S state,
                                    PoseStack poseStack,
                                    SubmitNodeCollector submitNodeCollector,
                                    CameraRenderState camera,
                                    int offset,
                                    CallbackInfo info)
    {
        if (state.entityType != EntityType.PLAYER)
        {
            return;
        }

        NametagsModule nametags = NametagsModule.INSTANCE;
        if (nametags.isEnabled())
        {
            info.cancel();
        }
    }
}
