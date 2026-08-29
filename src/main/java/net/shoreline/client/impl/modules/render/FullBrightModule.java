package net.shoreline.client.impl.modules.render;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.shoreline.client.api.module.Category;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.api.setting.Setting;
import net.shoreline.client.api.setting.impl.EnumSetting;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.api.Subscribe;

public class FullBrightModule extends Toggleable
{
    public static FullBrightModule INSTANCE;

    Setting<Brightness> mode = new EnumSetting.Builder<Brightness>("Mode")
            .setDescription("The client world brightness mode")
            .setObserver(v -> removeEffect())
            .setDefaultValue(Brightness.GAMMA).build();

    public FullBrightModule()
    {
        super("FullBright", "Brightens the world", Category.RENDER);
        INSTANCE = this;
    }

    @Subscribe
    public void onTick(TickEvent event)
    {
        if (checkNull() || mode.getValue() != Brightness.POTION)
        {
            return;
        }

        mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1215));
    }

    public void removeEffect()
    {
        if (checkNull() || !mc.player.hasEffect(MobEffects.NIGHT_VISION))
        {
            return;
        }

        mc.player.removeEffect(MobEffects.NIGHT_VISION);
    }

    public enum Brightness
    {
        GAMMA,
        POTION
    }
}
