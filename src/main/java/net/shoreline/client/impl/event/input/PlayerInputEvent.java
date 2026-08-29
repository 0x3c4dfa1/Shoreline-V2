package net.shoreline.client.impl.event.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.shoreline.eventbus.Event;

@Getter
@Setter
@AllArgsConstructor
public class PlayerInputEvent extends Event
{
    private ClientInput input;

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Correction extends Event
    {
        private Input input;
    }
}