package net.shoreline.client.api.gui.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.shoreline.client.api.gui.Theme;
import net.shoreline.client.api.gui.component.AbstractComponent;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.render.ColorUtil;

import java.awt.*;

public interface GuiComponent
{
    String getLabel();

    void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks);

    float getX();

    float getAlignedX();

    float getY();

    float getWidth();

    float getHeight();

    void setX(float x);

    void setAlignedX(float x);

    void setY(float y);

    void setWidth(float width);

    void setHeight(float height);

    void setScroll(double scroll);

    boolean isVisible();

    default Theme getTheme()
    {
        return Theme.getInstance();
    }

    /**
     * Component constants.
     * Could just make all of these settings instead,
     * but I don't like the guis people make.
     */
    default float getFeatureHeight()
    {
        return 15f;
    }

    default float getBorder()
    {
        return 2f;
    }

    default float getPadding()
    {
        return 1f;
    }

    default float getTextPadding()
    {
        return 2f;
    }

    /* ---------- End of component constants ---------- */

    default boolean isHovered(double mouseX, double mouseY)
    {
        return mouseWithinBounds(mouseX,
                mouseY,
                getAlignedX(),
                getY(),
                getWidth(),
                getHeight());
    }

    default boolean mouseWithinBounds(double mouseX,
                                      double mouseY,
                                      double x,
                                      double y,
                                      double width,
                                      double height)
    {
        return (mouseX >= x && mouseX <= (x + width)) &&
                (mouseY >= y && mouseY <= (y + height));
    }
}
