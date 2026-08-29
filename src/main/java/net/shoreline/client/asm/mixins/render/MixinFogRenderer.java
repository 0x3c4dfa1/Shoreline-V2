package net.shoreline.client.asm.mixins.render;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.shoreline.client.impl.modules.render.SkyboxModule;
import net.shoreline.client.impl.render.ColorUtil;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class MixinFogRenderer
{
    @Inject(method = "setupFog", at = @At(value = "TAIL"))
    private void setupFogHook(Camera camera,
                              int renderDistanceInChunks,
                              DeltaTracker deltaTracker,
                              float darkenWorldAmount,
                              ClientLevel level,
                              CallbackInfoReturnable<FogData> cir)
    {
        SkyboxModule skybox = SkyboxModule.INSTANCE;
        if (skybox.isEnabled() && skybox.getCancelFog().getValue() == SkyboxModule.FogMode.COLOR)
        {
            switch (skybox.getCancelFog().getValue())
            {
                case COLOR ->
                {
                    FogData data = cir.getReturnValue();
                    Vector4f vector = data.color;
                    data.color = ColorUtil.injectColor(vector, skybox.getSkyColor().getValue().getRGB());
                }
                case CLEAR ->
                {

                }
            }
        }
    }
}
