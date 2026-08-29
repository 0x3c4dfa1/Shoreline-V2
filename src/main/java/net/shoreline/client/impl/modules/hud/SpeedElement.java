package net.shoreline.client.impl.modules.hud;

import net.minecraft.ChatFormatting;
import net.shoreline.client.api.element.dynamic.DynamicElement;
import net.shoreline.client.api.element.dynamic.DynamicEntry;
import net.shoreline.client.api.setting.Setting;
import net.shoreline.client.api.setting.impl.EnumSetting;
import net.shoreline.client.impl.modules.world.TimerModule;

public class SpeedElement extends DynamicElement
{
    Setting<Format> formatMode = new EnumSetting.Builder<Format>("Format")
            .setDescription("The speed value format")
            .setDefaultValue(Format.KMH).build();

    public SpeedElement()
    {
        super("Speedometer", "Displays the player speed", 200, 250);
    }

    @Override
    public void loadEntries()
    {
        getEntries().add(new DynamicEntry(this, this::getSpeedometerText, () -> true));
    }

    private String getSpeedometerText()
    {
        double speed;
        double x = mc.player.getX() - mc.player.xOld;
        double z = mc.player.getZ() - mc.player.zOld;
        float timer = TimerModule.INSTANCE.getTimerTicks();
        if (formatMode.getValue() == Format.KMH)
        {
            double dist = Math.sqrt(x * x + z * z) / 1000.0;
            double div = 0.05 / 3600.0;
            speed = dist / div * timer;
        }
        else
        {
            x *= 20.0;
            z *= 20.0;
            double dist = Math.sqrt(x * x + z * z);
            speed = Math.abs(dist) * timer;
        }

        String format = formatMode.getValue() == Format.KMH ? "km/h" : "b/s";
        return String.format("Speed " + ChatFormatting.WHITE + "%s%s", DECIMAL_TRIMMED.format(speed), format);
    }

    private enum Format
    {
        KMH,
        BPS
    }
}
