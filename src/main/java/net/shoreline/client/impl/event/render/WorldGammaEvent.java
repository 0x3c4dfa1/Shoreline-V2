package net.shoreline.client.impl.event.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;

@AllArgsConstructor
@Getter
@Setter
public class WorldGammaEvent extends Event
{
    private float gamma;
}
