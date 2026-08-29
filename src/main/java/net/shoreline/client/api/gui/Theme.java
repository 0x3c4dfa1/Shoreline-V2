package net.shoreline.client.api.gui;

import lombok.Getter;
import net.shoreline.client.impl.render.ColorUtil;

import java.awt.*;

@Getter
public class Theme
{
    private static final Theme INSTANCE = new Theme();
    public static Theme getInstance() { return INSTANCE; }

    private int primary = -1;
    private int hover = -1;

    private Theme() {}

    public void update(int primary)
    {
        this.primary = primary;
        this.hover = getHover(primary);
    }

    public int getPrimary(float transparency)
    {
        return getColor(primary, transparency);
    }

    public int getPrimary(int transparency)
    {
        return getColor(primary, transparency);
    }

    public int getHover(float transparency)
    {
        return ColorUtil.withTransparency(hover, transparency).getRGB();
    }

    public Color getHoverC(float transparency)
    {
        return ColorUtil.withTransparency(hover, transparency);
    }

    public Color getPrimaryC(float transparency)
    {
        return ColorUtil.withTransparency(getPrimary(), transparency);
    }

    public int getColor(int color, float transparency)
    {
        return ColorUtil.withTransparency(color, transparency).getRGB();
    }

    public int getColor(int color, int transparency)
    {
        return ColorUtil.withTransparency(color, transparency).getRGB();
    }

    private int getHover(int color)
    {
        Color clr = new Color(color, true);
        int brightness = (clr.getRed() * 299 + clr.getGreen() * 587 + clr.getBlue() * 114) / 1000;
        clr = brightness > 160 ? clr.darker() : clr.brighter();
        return clr.getRGB();
    }
}