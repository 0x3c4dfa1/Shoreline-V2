package net.shoreline.client.impl.modules.render;

import net.shoreline.client.api.module.Category;
import net.shoreline.client.api.module.Toggleable;

public class NoWeatherModule extends Toggleable
{

    public NoWeatherModule()
    {
        super("NoWeather", "Changes the weather in your world", Category.RENDER);
    }

    public enum WeatherMode
    {
        CLEAR,
        SNOW,
        RAIN
    }
}
