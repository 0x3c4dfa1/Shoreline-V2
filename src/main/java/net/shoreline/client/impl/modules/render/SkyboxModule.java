package net.shoreline.client.impl.modules.render;

import lombok.Getter;
import net.shoreline.client.api.module.Category;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.api.setting.Setting;
import net.shoreline.client.api.setting.impl.BooleanSetting;
import net.shoreline.client.api.setting.impl.ColorSetting;
import net.shoreline.client.api.setting.impl.EnumSetting;
import net.shoreline.client.api.setting.impl.NumberSetting;

import java.awt.*;

@Getter
public class SkyboxModule extends Toggleable
{
    public static SkyboxModule INSTANCE;

    Setting<FogMode> cancelFog = new EnumSetting.Builder<FogMode>("Fog")
            .setDescription("Prevents fog from rendering in the world")
            .setDefaultValue(FogMode.CLEAR).build();
    Setting<Integer> fogDistance = new NumberSetting.Builder<Integer>("FogDistance")
            .setMin(1).setMax(256).setDefaultValue(120)
            .setVisible(() -> cancelFog.getValue() == FogMode.COLOR)
            .setDescription("The distance from the player that the fog will start").build();
    Setting<Boolean> cancelSky = new BooleanSetting.Builder("Sky")
            .setDescription("Change how the sky is rendered in the world")
            .setDefaultValue(false).build();
    Setting<Color> skyColor = new ColorSetting.Builder("SkyColor")
            .setDescription("The color of the sky")
            .setDefaultValue(Color.WHITE).build();
    Setting<Color> cloudColor = new ColorSetting.Builder("CloudColor")
            .setDescription("The color of the clouds")
            .setDefaultValue(Color.WHITE).build();

    public SkyboxModule()
    {
        super("Skybox", "Changes the world skybox", Category.RENDER);
        INSTANCE = this;
    }

    public enum FogMode
    {
        CLEAR, COLOR, OFF
    }
}
