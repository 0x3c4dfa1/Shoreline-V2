package net.shoreline.client.impl.modules.impl;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.shoreline.client.api.module.Category;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;

public class RenderModule extends Toggleable
{
    private final MultiBufferSource.BufferSource source = MultiBufferSource.immediate(new ByteBufferBuilder(1024));

    public RenderModule(String name, String description, Category category)
    {
        super(name, description, category);
    }

    public RenderModule(final String name,
                        final String[] nameAliases,
                        final String description,
                        final Category category)
    {
        super(name, nameAliases, description, category);
    }

    protected Vec3 getCameraPos()
    {
        return mc.player.position();
    }

    public void drawText(PoseStack matrices, String text, float x, float y)
    {
        drawText(matrices, text, x, y, -1);
    }

    public void drawText(PoseStack matrices, String text, float x, float y, int color)
    {
        Managers.TEXT.drawString(matrices, source, text, x, y, color);
    }

    public int getTextWidth(String text)
    {
        return (int) Managers.TEXT.getWidth(text);
    }

    protected void reload(boolean soft)
    {
        if (mc.levelRenderer == null)
        {
            return;
        }

        if (soft && mc.player != null)
        {
            int x = (int) mc.player.getX();
            int y = (int) mc.player.getY();
            int z = (int) mc.player.getZ();
            int d = mc.options.renderDistance().get() * 16;
            mc.levelRenderer.setBlocksDirty(x - d, y - d, z - d, x + d, y + d, z + d);
        }
        else
        {
            mc.levelRenderer.allChanged();
        }
    }

    public void flush()
    {
        source.endBatch();
    }
}