package net.shoreline.client.impl.render;

import lombok.experimental.UtilityClass;
import net.minecraft.util.Mth;
import org.joml.Vector4f;

import java.awt.*;

@UtilityClass
public class ColorUtil
{
    public Color interpolate(Color start, Color end, double factor)
    {
        return new Color(
                (int) (start.getRed() + (end.getRed() - start.getRed()) * factor),
                (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * factor),
                (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * factor),
                (int) (start.getAlpha() + (end.getAlpha() - start.getAlpha()) * factor));
    }

    public int interpolate(int start, int end, double factor)
    {
        float t = Mth.clamp((float) factor, 0.0f, 1.0f);
        float[] s = getRGBValues(start);
        float[] e = getRGBValues(end);

        return new Color(
            Mth.lerp(t, e[0], s[0]),
            Mth.lerp(t, e[1], s[1]),
            Mth.lerp(t, e[2], s[2]),
            Mth.lerp(t, e[3], s[3])
        ).getRGB();
    }

    public Color withTransparency(Color color, int alpha)
    {
        return new Color(color.getRed(),
                color.getGreen(),
                color.getBlue(),
                alpha);
    }

    public Color withTransparency(int color, int alpha)
    {
        return withTransparency(new Color(color, true), alpha);
    }

    public Color withTransparency(int color, float alpha)
    {
        return withTransparency(color, (int) Math.clamp(alpha * 255, 0, 255));
    }

    public Color withTransparencyMultiplier(Color color, float multiplier)
    {
        multiplier = Math.clamp(multiplier, 0.0f, 1.0f);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * multiplier));
    }

    public Color withTransparencyMultiplier(int color, float multiplier)
    {
        return withTransparencyMultiplier(new Color(color, true), multiplier);
    }

    public int getSimpleVariation(float offset, Color color)
    {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.HSBtoRGB(hsb[0], hsb[1], ColorUtil.getVariation(1.0f, 1.5f, hsb[2], (int) (-offset)));
    }

    public float getVariation(float speedFactor, float rangeFactor, float value, int offset)
    {
        float time = (float) Math.sin(getRainbowHue(speedFactor / 36, (int) (offset / speedFactor)) * 360);
        float variation = time * (rangeFactor / 8);
        return Math.clamp(value - (rangeFactor / 8) + variation, 0f, 1f);
    }

    public float getRainbowHue(float speedFactor, int offset)
    {
        float speed = 2500 / speedFactor;
        return ((System.currentTimeMillis() + offset) % (int) speed) / speed;
    }

    public float[] getRGBValues(int color)
    {
        Color c = new Color(color, (color >>> 24) != 0);
        float r = c.getRed() / 255.0f;
        float g = c.getGreen() / 255.0f;
        float b = c.getBlue() / 255.0f;
        float a = c.getAlpha() / 255.0f;
        return new float[] { r, g, b, a };
    }

    public Vector4f injectColor(Vector4f vector, int color)
    {
        vector.x = ((color >> 16) & 0xFF) / 255f;
        vector.y = ((color >>  8) & 0xFF) / 255f;
        vector.z = (color & 0xFF) / 255f;
        return vector;
    }

    public static Color hslToColor(float f, float f2, float f3, float f4)
    {
        f %= 360.0f;
        float f5;
        f5 = (double) f3 < 0.5 ? f3 * (1.0f + f2) : (f3 /= 100.0f) + (f2 /= 100.0f) - f2 * f3;
        f2 = 2.0f * f3 - f5;
        f3 = Math.max(0.0f, colorCalc(f2, f5, (f /= 360.0f) + 0.33333334f));
        float f6 = Math.max(0.0f, colorCalc(f2, f5, f));
        f2 = Math.max(0.0f, colorCalc(f2, f5, f - 0.33333334f));
        f3 = Math.min(f3, 1.0f);
        f6 = Math.min(f6, 1.0f);
        f2 = Math.min(f2, 1.0f);
        return new Color(f3, f6, f2, f4);
    }

    private static float colorCalc(float f, float f2, float f3)
    {
        if (f3 < 0.0f)
        {
            f3 += 1.0f;
        }

        if (f3 > 1.0f)
        {
            f3 -= 1.0f;
        }

        if (6.0f * f3 < 1.0f)
        {
            float f4 = f;
            return f4 + (f2 - f4) * 6.0f * f3;
        }

        if (2.0f * f3 < 1.0f)
        {
            return f2;
        }

        if (3.0f * f3 < 2.0f)
        {
            float f5 = f;
            return f5 + (f2 - f5) * 6.0f * (0.6666667f - f3);
        }

        return f;
    }
}
