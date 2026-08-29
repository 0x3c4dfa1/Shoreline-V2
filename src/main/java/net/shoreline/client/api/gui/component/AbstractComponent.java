package net.shoreline.client.api.gui.component;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.shoreline.client.api.gui.api.GuiComponent;
import net.shoreline.client.api.interfaces.Globals;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Render2DUtil;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.animation.ColorAnimation;
import net.shoreline.client.impl.render.animation.Easing;
import net.shoreline.client.impl.render.animation.Smoother;

import java.awt.*;
import java.util.function.Supplier;

@Getter
@Setter
public abstract class AbstractComponent implements GuiComponent, Globals
{
    protected final String label;
    protected final Supplier<Boolean> visibility;
    protected float x, y, width, height, alignedX;
    protected final Animation scrollAnimation;
    protected final Animation hoverAnimation;
    protected final Animation closeAnimation;
    protected final Smoother textSmoother;
    protected boolean scrollState;

    public AbstractComponent(String label, Supplier<Boolean> visibility)
    {
        this.label = label;
        this.visibility = visibility;
        this.hoverAnimation = new Animation(150, Easing.SMOOTH);
        this.scrollAnimation = new Animation(false, 0, 0, 1000, Easing.LINEAR);
        this.closeAnimation = new Animation(150, Easing.LINEAR);
        this.textSmoother = new Smoother();
        this.closeAnimation.setStateHard(isVisible());
    }

    /**
     * Its important that we do all the actual rendering
     * in this method and not the render method so we
     * don't have to draw components that are off-screen,
     * while still getting their correct dimensions.
     *
     * @param graphics the graphics provided.
     * @param mouseX the mouse x position.
     * @param mouseY the mouse y position.
     * @param partialTicks the current progress between ticks.
     */
    public abstract void drawComponent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks);

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        boolean hovered = mouseWithinBounds(mouseX, mouseY, getAlignedX(), getY(), getWidth(), getFeatureHeight());
        if (hovered)
        {
            if (scrollAnimation.isFinished())
            {
                scrollState = !scrollState;
                scrollAnimation.setState(scrollState);
            }
        }
        else
        {
            scrollAnimation.setState(false);
        }

        hoverAnimation.setState(hovered);
        if (shouldRenderComponent())
        {
            drawComponent(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public float getHeight()
    {
        return (float) (height * closeAnimation.getFactor());
    }

    @Override
    public void setScroll(double scroll)
    {
        scrollAnimation.setTarget(scroll);
    }

    @Override
    public boolean isVisible()
    {
        return visibility == null || visibility.get();
    }

    protected void drawHoverRect(GuiGraphicsExtractor graphics)
    {
        Render2DUtil.drawRect(graphics, getX(), getY() + 1.5f, getX() + getWidth(), getY() + getFeatureHeight(),
                applyCloseEffect(ColorUtil.withTransparency(Color.GRAY, Math.max(50, (int) (75 * hoverAnimation.getFactor()))).getRGB()));
    }

    public void drawValueComponent(GuiGraphicsExtractor graphics, String value, float partialTicks)
    {
        drawHoverRect(graphics);
        scissorText(graphics, value);
        drawSettingText(graphics, getLabel(), false, false);
        graphics.disableScissor();
        drawAnimatedRightText(graphics, value, false, partialTicks);
    }

    public void scissorText(GuiGraphicsExtractor graphics, String value)
    {
        float align = getWidth() - getTextPadding();
        float nameX = getX() + getTextPadding();
        float x = getX() + align;
        float y = getY() + (getFeatureHeight()) / 2 + 1f;

        graphics.enableScissor((int) nameX, (int) (y - Managers.TEXT.getHeight()), (int) (x - Managers.TEXT.getWidth(value) - 2.5f), (int) (y + Managers.TEXT.getHeight()));
        float maxWidth = x - nameX - Managers.TEXT.getWidth(value) - 5.0f;
        float overflow = Managers.TEXT.getWidth(getLabel()) - maxWidth;
        setScroll(Math.max(0, overflow));
    }

    public void drawAnimatedRightText(GuiGraphicsExtractor graphics, String text, boolean primaryColor, float partialTicks)
    {
        drawRightSettingText(graphics, text, primaryColor, (float) textSmoother.smooth(Managers.TEXT.getWidth(text), 0.5f, partialTicks));
    }

    public void drawString(GuiGraphicsExtractor graphics, String text, float x, float y, boolean primaryColor, boolean rightAlign)
    {
        int color = primaryColor
                ? getTheme().getPrimary()
                : 0xFFFFFFFF;
        float align = rightAlign
                ? x - Managers.TEXT.getWidth(text)
                : x;

        Managers.TEXT.drawString(graphics, text, align, y - (Managers.TEXT.getHeight() >> 1), applyCloseEffect(color));
    }

    public void drawRightString(GuiGraphicsExtractor graphics,
                                String text,
                                float x,
                                float y,
                                boolean primaryColor,
                                float width)
    {
        int color = primaryColor
                ? getTheme().getPrimary()
                : 0xFFFFFFFF;
        float align = x - width;

        Managers.TEXT.drawString(graphics, text, align, y - (Managers.TEXT.getHeight() >> 1), applyCloseEffect(color));
    }

    public void drawString(GuiGraphicsExtractor graphics,
                           String text,
                           float x,
                           float y,
                           int color)
    {
        Managers.TEXT.drawString(graphics, text, x, y - (Managers.TEXT.getHeight() >> 1), applyCloseEffect(color));
    }

    public void drawRightSettingText(GuiGraphicsExtractor graphics,
                                     String value,
                                     boolean primaryColor,
                                     float width)
    {
        if (value == null)
        {
            return;
        }

        float align = getWidth() - getTextPadding();
        float x = getAlignedX() + align;
        float y = getY() + (getFeatureHeight()) / 2 + 1f;
        drawRightString(graphics, value, x, y, primaryColor, width);
    }

    public void drawSettingText(GuiGraphicsExtractor graphics, String value, boolean primaryColor, boolean rightAlign)
    {
        float extra;
        extra  = (float) hoverAnimation.getCurrent();
        extra -= (float) scrollAnimation.getCurrent();

        float align = rightAlign ? getWidth() - getTextPadding() : getTextPadding();
        float x = getX() + align + extra;
        float y = getY() + (getFeatureHeight()) / 2 + 1f;
        drawString(graphics, value, x, y, primaryColor, rightAlign);
    }

    public void drawToggleableRect(GuiGraphicsExtractor graphics, ColorAnimation colorAnimation)
    {
        double hFactor = hoverAnimation.getFactor();
        double eFactor = colorAnimation.getFactor();
        Color hoverColor = ColorUtil.withTransparency(
                Color.GRAY,
                Math.max(50, (int) (75 * hFactor))
        );

        Color clr = getTheme().getPrimaryC(0.5f);
        Color enabledColor = ColorUtil.interpolate(clr, getTheme().getHoverC(0.5f), hFactor);
        Color color = ColorUtil.interpolate(
                hoverColor,
                enabledColor,
                eFactor
        );

        Render2DUtil.drawRect(graphics, getX(), getY() + 1.5f, getX() + getWidth(), getY() + getFeatureHeight(), applyCloseEffect(color.getRGB()));
    }

    public int applyCloseEffect(int color)
    {
        return ColorUtil.withTransparencyMultiplier(color, (float) closeAnimation.getFactor()).getRGB();
    }

    public boolean shouldRenderComponent()
    {
        return getY() + getHeight() > 0 && getY() < mc.getWindow().getGuiScaledHeight();
    }
}