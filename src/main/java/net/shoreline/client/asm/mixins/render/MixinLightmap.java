package net.shoreline.client.asm.mixins.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.shoreline.client.impl.modules.render.AmbienceModule;
import net.shoreline.client.impl.modules.render.FullBrightModule;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(Lightmap.class)
public class MixinLightmap
{
    @Shadow
    @Final
    private GpuTexture texture;

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void renderHook(LightmapRenderState renderState, CallbackInfo info)
    {
        AmbienceModule ambience = AmbienceModule.INSTANCE;
        if (ambience.isEnabled())
        {
            Color color = ambience.getColor().getValue();
            Vector3f vector = new Vector3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            renderState.skyLightColor = vector;
            renderState.ambientColor = vector;
            renderState.blockFactor = 0;
            renderState.brightness = 1;
        }

        FullBrightModule fullbright = FullBrightModule.INSTANCE;
        if (fullbright.isEnabled())
        {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(texture, 0xFFFFFFFF);
        }
    }
}
