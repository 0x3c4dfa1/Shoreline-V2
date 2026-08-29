package net.shoreline.client.impl.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.phys.Vec3;
import net.shoreline.client.api.module.Category;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.api.setting.Setting;
import net.shoreline.client.api.setting.impl.*;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.ClientEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.render.ShaderEvent;
import net.shoreline.client.impl.render.shader.AbstractShaderChain;
import net.shoreline.client.impl.render.shader.ShaderPass;
import net.shoreline.client.impl.render.shader.ShaderPasses;
import net.shoreline.client.impl.render.shader.shaders.OutlineShader;
import net.shoreline.client.impl.render.shader.util.ShaderNodeCollector;
import net.shoreline.eventbus.api.Subscribe;

import java.awt.*;
import java.util.Objects;

@Getter
public class ShaderModule extends Toggleable
{
    public static ShaderModule INSTANCE;

    Setting<Float> rangeConfig = new NumberSetting.Builder<Float>("Range")
            .setMin(0.0f).setDefaultValue(30.0f).setMax(250.0f).setFormat("m")
            .setDescription("If entity is within this range we apply shaders").build();
    Setting<EnumShader> mode = new EnumSetting.Builder<EnumShader>("Shader")
            .setDefaultValue(EnumShader.OUTLINE)
            .setDescription("The shader mode to use").build();
    Setting<Float> width = new NumberSetting.Builder<Float>("Width")
            .setMin(0f).setMax(5f).setDefaultValue(1f)
            .setDescription("The width of the shader outline").build();
    Setting<Float> fillOpacity = new NumberSetting.Builder<Float>("FillOpacity")
            .setMin(0.0f).setMax(1.0f).setDefaultValue(0.2f)
            .setDescription("The fill opacity").build();
    Setting<Float> outlineOpacity = new NumberSetting.Builder<Float>("OutlineOpacity")
            .setMin(0.0f).setMax(1.0f).setDefaultValue(0.8f)
            .setDescription("The outline opacity").build();
    Setting<Color> color = new ColorSetting.Builder("Color")
            .setDescription("The shader color")
            .setDefaultValue(Color.PINK).build();

    Setting<Boolean> hands = new BooleanSetting.Builder("Hands")
            .setDescription("Render shaders over hands")
            .setDefaultValue(true).build();
    Setting<Boolean> players = new BooleanSetting.Builder("Players")
            .setDescription("Render shaders over other players")
            .setDefaultValue(true).build();
    Setting<Boolean> self = new BooleanSetting.Builder("Self")
            .setDescription("Render shaders over the player")
            .setDefaultValue(true).build();
    Setting<Boolean> crystals = new BooleanSetting.Builder("Crystals")
            .setDescription("Render shaders over crystals")
            .setDefaultValue(true).build();
    Setting<Boolean> items = new BooleanSetting.Builder("Items")
            .setDescription("Render shaders over items")
            .setDefaultValue(true).build();
    Setting<Boolean> xp = new BooleanSetting.Builder("XP")
            .setDescription("Render shaders over xp bottles")
            .setDefaultValue(true).build();
    Setting<Boolean> pearls = new BooleanSetting.Builder("Pearls")
            .setDescription("Render shaders over pearls")
            .setDefaultValue(true).build();
    Setting<Boolean> passive = new BooleanSetting.Builder("Passive")
            .setDescription("Render shaders over hands")
            .setDefaultValue(true).build();
    Setting<Boolean> hostiles = new BooleanSetting.Builder("Hostiles")
            .setDescription("Render shaders over hands")
            .setDefaultValue(true).build();
    public Setting<Void> renderTargets = new SettingGroup.Builder("Target")
            .addAll(hands, players, self, crystals, items, xp, pearls, passive, hostiles).build();

    public ShaderModule()
    {
        super("Shader", "Renders a shader over entities", Category.RENDER);
        INSTANCE = this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Subscribe
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (checkNull())
        {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Frustum frustum = event.getFrustum();
        Vec3 camPos = event.getCamera().pos;

        ShaderPass shader = ShaderPasses.ENTITIES;
        shader.begin();

        OutlineBufferSource bufferSource = new OutlineBufferSource();
        ShaderNodeCollector collector = new ShaderNodeCollector(bufferSource);

        shader.bind();
        try
        {
            for (Entity entity : mc.level.entitiesForRendering())
            {
                if (!shouldRenderShader(entity) || !frustum.isVisible(entity.getBoundingBox()))
                {
                    continue;
                }

                EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer(entity);
                EntityRenderState state = renderer.createRenderState(entity, event.getPartialTicks());

                collector.setColor(color.getValue().getRGB());

                poseStack.pushPose();
                poseStack.translate(state.x - camPos.x, state.y - camPos.y, state.z - camPos.z);
                renderer.submit(state, event.getPoseStack(), collector, event.getCamera());
                poseStack.popPose();
            }

            collector.flush();
        }
        finally
        {
            shader.unbind();
        }
    }

    @Subscribe
    public void onShader(ShaderEvent event)
    {
        AbstractShaderChain<ShaderModule> chain = mode.getValue().getShader();
        ShaderPasses.ENTITIES.draw(chain, this);
        ShaderPasses.HANDS.draw(chain, this);

        //ShaderPasses.ENTITIES.clearTarget();
        //ShaderPasses.HANDS.clearTarget();
    }

    private boolean shouldRenderShader(Entity entity)
    {
        if (Mth.square(rangeConfig.getValue()) < entity.distanceToSqr(mc.gameRenderer.getMainCamera().position()))
        {
            return false;
        }

        return switch (entity)
        {
            case Player player when player != mc.player ? players.getValue() : self.getValue() -> true;
            case Monster monster when hostiles.getValue() -> true;
            case Animal animalEntity when passive.getValue() -> true;
            case ItemEntity itemEntity when items.getValue() -> true;
            case ThrownExperienceBottle xpEntity when xp.getValue() -> true;
            case ThrownEnderpearl pearlEntity when pearls.getValue() -> true;
            default -> entity instanceof EndCrystal && crystals.getValue();
        };
    }

    @RequiredArgsConstructor
    @Getter
    public enum EnumShader
    {
        OUTLINE
        {
            AbstractShaderChain<ShaderModule> cached;

            @Override
            public AbstractShaderChain<ShaderModule> getShader()
            {
                return Objects.requireNonNullElseGet(cached, OutlineShader::new);
            }
        };

        public abstract AbstractShaderChain<ShaderModule> getShader();
    }
}
