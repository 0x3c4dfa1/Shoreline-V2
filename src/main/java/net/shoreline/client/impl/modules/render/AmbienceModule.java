package net.shoreline.client.impl.modules.render;

import lombok.Getter;
import net.shoreline.client.api.module.Category;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.api.setting.Setting;
import net.shoreline.client.api.setting.impl.ColorSetting;

import java.awt.*;

@Getter
public class AmbienceModule extends Toggleable
{
    public static AmbienceModule INSTANCE;

    Setting<Color> color = new ColorSetting.Builder("LightColor")
            .setDescription("The light color")
            .setDefaultValue(Color.WHITE).build();

    public AmbienceModule()
    {
        super("Ambience", "Changes your worlds ambience", Category.RENDER);
        INSTANCE = this;
    }
}
