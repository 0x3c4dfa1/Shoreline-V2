package net.shoreline.client.impl.modules.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import net.shoreline.client.api.module.Category;
import net.shoreline.client.api.setting.Setting;
import net.shoreline.client.api.setting.impl.BooleanSetting;
import net.shoreline.client.api.setting.impl.SettingGroup;
import net.shoreline.client.api.setting.impl.ToggleableSettingGroup;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.modules.impl.RenderModule;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Interpolation;
import net.shoreline.eventbus.api.Subscribe;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NametagsModule extends RenderModule
{
    public static NametagsModule INSTANCE;

    Setting<Boolean> health = new BooleanSetting.Builder("Health")
            .setDescription("Shows info about the players health")
            .setDefaultValue(true).build();
    Setting<Boolean> latency = new BooleanSetting.Builder("Latency")
            .setDescription("Shows info about the players latency")
            .setDefaultValue(true).build();
    Setting<Boolean> gamemode = new BooleanSetting.Builder("Gamemode")
            .setDescription("Shows info about the players gamemode")
            .setDefaultValue(false).build();
    Setting<Boolean> entityId = new BooleanSetting.Builder("EntityID")
            .setDescription("Shows the players entity id")
            .setDefaultValue(false).build();
    Setting<Void> infoGroup = new SettingGroup.Builder("Info")
            .addAll(health, latency, gamemode, entityId).build();

    Setting<Boolean> enchantments = new BooleanSetting.Builder("Enchantments")
            .setDescription("Shows the armors enchantments")
            .setDefaultValue(true).build();
    Setting<Boolean> armor = new ToggleableSettingGroup.Builder("Armor")
            .addAll(enchantments)
            .setDescription("Shows the opponents armor")
            .setDefaultValue(true).build();

    private final List<PlayerEntry> entries = new ArrayList<>();
    private final SubmitNodeStorage storage = new SubmitNodeStorage();
    private final ItemFeatureRenderer renderer = new ItemFeatureRenderer();

    public NametagsModule()
    {
        super("Nametags", "Renders a nametag over players", Category.RENDER);
        INSTANCE = this;
    }

    @Subscribe
    public void onRender(RenderWorldEvent event)
    {
        if (checkNull())
        {
            return;
        }

        Frustum frustum = event.getFrustum();
        PoseStack matrices = event.getPoseStack();
        Camera camera = mc.getEntityRenderDispatcher().camera;
        for (PlayerEntry entry : entries)
        {
            Player player = entry.getPlayer();
            if (player == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON || !frustum.isVisible(player.getBoundingBox()))
            {
                continue;
            }

            String info = entry.getInfo();
            Vec3 interp = Interpolation.getRenderPosition(player, event.getPartialTicks());
            double offset = player.isVisuallySwimming() ? 1.0f : player.isShiftKeyDown() ? 2.0f : 2.3f;
            double x = interp.x - camera.position().x;
            double y = interp.y + offset - camera.position().y;
            double z = interp.z - camera.position().z;

            float distance = (float) Math.sqrt(camera.position().distanceToSqr(interp.x, interp.y, interp.z));
            float scaling = 0.0018f + 0.003f * distance;
            if (distance <= 8.0)
            {
                scaling = 0.0245f;
            }

            matrices.pushPose();
            matrices.translate(x, y ,z);
            matrices.mulPose(camera.rotation());
            matrices.scale(scaling, -scaling, scaling);

            renderItems(matrices, storage, player);

            float width = getTextWidth(entry.getInfo()) / 2f;
            drawText(matrices, info, -width, 0, 0xFFFFFFFF);
            matrices.popPose();
        }

        flushItems();
        flush();

        storage.endFrame();
        storage.clear();
    }

    @Subscribe
    public void onTick(TickEvent event)
    {
        entries.clear();
        if (checkNull())
        {
            return;
        }

        for (Player player : mc.level.players())
        {
            entries.add(new PlayerEntry(player));
        }
    }

    private void renderItems(PoseStack matrices, SubmitNodeStorage storage, Player player)
    {
        List<ItemStack> displayItems = new ArrayList<>();
        if (!player.getOffhandItem().isEmpty())
        {
            displayItems.add(player.getOffhandItem());
        }

        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR)
        {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty())
            {
                displayItems.add(stack);
            }
        }

        if (!player.getMainHandItem().isEmpty())
        {
            displayItems.add(player.getMainHandItem());
        }

        Collections.reverse(displayItems);
        float xOffset = 0;
        int yOffset = 0;
        for (ItemStack stack : displayItems)
        {
            xOffset -= 9;
            if (stack.getEnchantments().keySet().size() > yOffset)
            {
                yOffset = stack.getEnchantments().keySet().size();
            }
        }

        float enchY = enchantOffset(yOffset);
        for (ItemStack stack : displayItems)
        {
            matrices.pushPose();
            matrices.translate(xOffset + 8.0f, enchY + 8.0f, 0.0f);
            matrices.scale(16.0f, -16.0f, 0.1f); // >= 0.001 renders under the glint -_-
            renderItem(stack, storage, matrices);
            matrices.popPose();

            renderItemOverlay(matrices, stack, xOffset, enchY);
            if (!stack.isEnchanted() && !stack.isDamageableItem())
            {
                xOffset += 18f;
                continue;
            }

            matrices.pushPose();
            matrices.scale(0.5f, 0.5f, 0.5f);
            renderDurability(matrices, stack, xOffset + 2.0f, enchY - 4.5f);
            renderEnchants(matrices, stack, xOffset + 2.0f, enchY);
            matrices.popPose();

            xOffset += 18f;
        }
    }

    private void renderItem(ItemStack stack, SubmitNodeStorage storage, PoseStack matrices)
    {
        ItemStackRenderState state = new ItemStackRenderState();
        mc.getItemModelResolver().updateForTopItem(state, stack, ItemDisplayContext.GUI, mc.level, mc.player, 0);
        state.submit(matrices, storage, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
    }

    private void renderItemOverlay(PoseStack matrices, ItemStack stack, float x, float y)
    {
        if (stack.getCount() != 1)
        {
            String count = String.valueOf(stack.getCount());
            drawText(matrices, count, x + 17 - getTextWidth(count), y + 9, -1);
        }
    }

    private void renderEnchants(PoseStack matrices, ItemStack itemStack, float x, float y)
    {
        if (!itemStack.isEnchanted())
        {
            return;
        }

        ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        Object2IntMap<Holder<Enchantment>> enchantments = new Object2IntOpenHashMap<>();
        for (Holder<Enchantment> enchantment : enchants.keySet())
        {
            enchantments.put(enchantment, enchants.getLevel(enchantment));
        }

        float height = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : Object2IntMaps.fastIterable(enchantments))
        {
            String enchantString = getEnchantmentName(entry.getKey().getRegisteredName(), entry.getIntValue());
            drawText(matrices, enchantString, x * 2, (y + height) * 2, -1);
            height += 4.5f;
        }
    }

    private void renderDurability(PoseStack matrices, ItemStack itemStack, float x, float y)
    {
        if (!itemStack.isDamageableItem())
        {
            return;
        }

        int maxDamage = itemStack.getMaxDamage();
        int damage = itemStack.getDamageValue();
        int durability = (int) ((maxDamage - damage) / ((float) maxDamage) * 100.0f);
        int color = ColorUtil.hslToColor((float) (maxDamage - damage) / (float) maxDamage * 120.0f, 100.0f, 50.0f, 1.0f).getRGB();
        drawText(matrices, durability + "%", (int) (x * 2), (int) (y * 2), color);
    }

    private String getEnchantmentName(String id, int level)
    {
        id = id.replace("minecraft:", "");
        id = level > 1 ? id.substring(0, 2) : id.substring(0, 3);
        return id.substring(0, 1).toUpperCase() + id.substring(1) + (level > 1 ? level : "");
    }

    private float enchantOffset(int yOffset)
    {
        if (!enchantments.getValue() || yOffset <= 3)
        {
            return -18.0f;
        }

        float offset = -14.0f;
        offset -= (yOffset - 3) * 4.5f;
        return offset;
    }

    private void flushItems()
    {
        GL11C.glEnable(GL11C.GL_POLYGON_OFFSET_FILL);
        GL11C.glPolygonOffset(1.0f, -32500000);

        MultiBufferSource.BufferSource source = mc.renderBuffers().bufferSource();
        OutlineBufferSource outline = mc.renderBuffers().outlineBufferSource();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);

        try
        {
            for (SubmitNodeCollection collection : storage.getSubmitsPerOrder().values())
            {
                renderer.renderSolid(collection, source, outline);
            }

            for (SubmitNodeCollection collection : storage.getSubmitsPerOrder().values())
            {
                renderer.renderTranslucent(collection, source, outline);
            }

            source.endBatch();
        }
        finally
        {
            mc.gameRenderer.getLighting().setupFor(Lighting.Entry.LEVEL);
        }

        GL11C.glPolygonOffset(1.0f, 32500000);
        GL11C.glDisable(GL11C.GL_POLYGON_OFFSET_FILL);
    }

    @Getter
    public class PlayerEntry
    {
        private final Player player;
        private final String info;

        public PlayerEntry(Player player)
        {
            this.player = player;
            StringBuilder builder = new StringBuilder(player.getName().getString());
            builder.append(" ");
            if (entityId.getValue())
            {
                builder.append("ID: ").append(player.getId()).append(" ");
            }

            if (gamemode.getValue())
            {
                if (player.isCreative())
                {
                    builder.append("[C] ");
                }
                else if (player.isSpectator())
                {
                    builder.append("[I] ");
                }
                else
                {
                    builder.append("[S] ");
                }
            }

            if (latency.getValue() && mc.getConnection() != null)
            {
                PlayerInfo playerEntry = mc.getConnection().getPlayerInfo(player.getGameProfile().id());
                if (playerEntry != null)
                {
                    builder.append(playerEntry.getLatency());
                    builder.append("ms ");
                }
            }

            if (health.getValue())
            {
                int health = Math.round(player.getHealth() + player.getAbsorptionAmount());

                ChatFormatting hcolor;
                if (health > 18)
                {
                    hcolor = ChatFormatting.GREEN;
                }
                else if (health > 16)
                {
                    hcolor = ChatFormatting.DARK_GREEN;
                }
                else if (health > 12)
                {
                    hcolor = ChatFormatting.YELLOW;
                }
                else if (health > 8)
                {
                    hcolor = ChatFormatting.GOLD;
                }
                else if (health > 4)
                {
                    hcolor = ChatFormatting.RED;
                }
                else
                {
                    hcolor = ChatFormatting.DARK_RED;
                }

                builder.append(hcolor);
                builder.append(health);
                builder.append(" ");
            }

            /* if (totemsConfig.getValue() && player != mc.player)
            {
                int totems = Managers.TOTEM.getTotems(player);
                if (totems > 0)
                {
                    builder.append(Formatting.WHITE);
                    builder.append(-totems);
                    builder.append(" ");
                }
            } */

            info = builder.toString().trim();
        }
    }
}
