package com.gim.client.overlay;

import com.gim.GenshinHeler;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.client.layers.ShieldLayerRender;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Attributes;
import com.gim.registry.Capabilities;
import com.gim.registry.KeyMappings;
import com.google.common.collect.Iterators;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.GuiUtils;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.energy.IEnergyStorage;

import java.awt.*;
import java.text.DecimalFormat;

@OnlyIn(Dist.CLIENT)
public class GenshinRender implements IIngameOverlay {
    private final DecimalFormat SECONDS_FORMAT = new DecimalFormat("0.0s");
    private final ResourceLocation WHITE_COLOR = new ResourceLocation("forge", "textures/white.png");
    private final Color ACTIVE_COLOR_1 = new Color(255, 255, 255, 124);
    private final Color ACTIVE_COLOR_2 = new Color(255, 255, 255, 50);
    private final Color DISABLED_COLOR_1 = new Color(128, 128, 128, 90);
    private final Color DISABLED_COLOR_2 = new Color(128, 128, 128, 50);

    @Override
    public void render(ForgeIngameGui gui, PoseStack mStack, float partialTicks, int width, int height) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        IGenshinInfo info = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
        if (info == null)
            return;

        // at least 4 players in the stack
        int end = info.currentStack().size();

        int xStart = width - 32;
        int y = height / 5;
        int yStep = 32;
        Font font = Minecraft.getInstance().font;

        // longest character name length in pixels
        Integer nameLength = info.currentStack().stream().map(x -> font.width(x.getName().getString())).max(Integer::compare).orElse(24);

        for (int i = 0; i < end; i++) {
            // current genshin character
            IGenshinPlayer genshinPlayer = info.currentStack().size() > i
                    ? Iterators.get(info.currentStack().iterator(), i)
                    : null;

            int xPos = xStart;
            int yPos = y + yStep * i;
            // slot number
            String text = (i + 1) + "";

            // render background
            RenderSystem.setShaderTexture(0, WHITE_COLOR);
            // selected are brighter than unselected characters
            Color color1 = i == info.currentIndex() ? ACTIVE_COLOR_1 : DISABLED_COLOR_1;
            Color color2 = i == info.currentIndex() ? ACTIVE_COLOR_2 : DISABLED_COLOR_2;
            GuiUtils.drawGradientRect(mStack.last().pose(), 0, xPos - nameLength - 18 - 28, yPos, width - 32, yPos + 18,
                    color1.getRGB(), color2.getRGB());

            // render slot
            RenderSystem.setShaderTexture(0, GuiComponent.STATS_ICON_LOCATION);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            GuiComponent.blit(mStack, xPos, yPos, 0, 0.0F, 0.0F, 18, 18, 128, 128);

            // render slot number
            GuiComponent.drawString(mStack, font, text,
                    xPos + (18 - font.width(text)) / 2,
                    yPos + (18 - font.lineHeight) / 2,
                    Color.BLACK.getRGB());

            if (genshinPlayer != null) {
                text = genshinPlayer.getName().getString();
                // character icon size
                xPos -= 24;

                // default color - white
                RenderSystem.setShaderColor(1, 1, 1, 1);

                // if cannot switch to another player
                if (info.ticksTillSwitch(player) > 0) {
                    // seconds format: 0.5 / 1.2 etc
                    text = SECONDS_FORMAT.format(info.ticksTillSwitch(player) / 20d);

                    GuiComponent.drawString(mStack, font, text,
                            xPos,
                            yPos + (18 - font.lineHeight) / 2,
                            Color.WHITE.getRGB());

                    // set color for icon. It should be a bit "bisabled"
                    RenderSystem.setShaderColor(1, 1, 1, 0.5f);
                }

                // render character icon
                RenderSystem.setShaderTexture(0, getSource(genshinPlayer, "icon", false));
                GuiUtils.drawInscribedRect(mStack, xPos, yPos, 18, 18, 18, 18);

                // spacing with longest name lenght
                xPos -= 4 + nameLength;

                // render character name
                GuiComponent.drawString(mStack, font, text,
                        xPos,
                        yPos + (18 - font.lineHeight) / 2,
                        Color.WHITE.getRGB());

                // space between player name and icons
                xPos -= 16;

                // icon size + space
                xPos -= 28;
                if (info.isBurstEnabled(player, genshinPlayer)) {
                    renderAnimation(mStack, xPos, yPos - 4, 24, 24, getSource(genshinPlayer, "burst", true));
                }

                // icon size + space
                xPos -= 28;
                if (info.isSkillEnabled(player, genshinPlayer)) {
                    renderAnimation(mStack, xPos, yPos - 4, 24, 24, getSource(genshinPlayer, "skill", true));
                }
            }
        }

        // draw skill/icon helper below the screen
        if (info.current() != null) {
            int burstHeightAndWidth = 48 + font.lineHeight;
            int barWidth = 6;


            xStart = width - 12;
            y = height - burstHeightAndWidth - 12 + font.lineHeight;

            // render energy bar
            Color color = ShieldLayerRender.getColor(info.current().getElemental());
            IEnergyStorage energyStorage = info.getPersonInfo(info.current()).burstInfo();
            int energyHeight = (int) ((double)energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored() * burstHeightAndWidth);

            int blackColor = ShieldLayerRender.getColor(info.current().getElemental()).getRGB();

            GuiUtils.drawGradientRect(mStack.last().pose(), 0, xStart, y, xStart + barWidth, y + 1, blackColor, blackColor);
            GuiUtils.drawGradientRect(mStack.last().pose(), 0, xStart, y, xStart + 1, y + burstHeightAndWidth, blackColor, blackColor);
            GuiUtils.drawGradientRect(mStack.last().pose(), 0, xStart + barWidth, y, xStart + barWidth + 1, y + burstHeightAndWidth, blackColor, blackColor);
            GuiUtils.drawGradientRect(mStack.last().pose(), 0, xStart, y + burstHeightAndWidth, xStart + barWidth, y + burstHeightAndWidth + 1, blackColor, blackColor);

            RenderSystem.setShaderTexture(0, new ResourceLocation("forge:textures/white.png"));

            GuiUtils.drawGradientRect(mStack.last().pose(),
                    0,
                    xStart + 1,
                    // top height is stopping by energy maximum of current player
                    y + 1 + (burstHeightAndWidth - energyHeight),
                    // lowest Y is always the same
                    xStart + barWidth,
                    y + burstHeightAndWidth,
                    GenshinHeler.withAlpha(color, 255).getRGB(),
                    GenshinHeler.withAlpha(color, 128).getRGB());


            burstHeightAndWidth = 48;
            xStart = width - burstHeightAndWidth - 12; // 12 - margin from screen edges
            y = height - burstHeightAndWidth - 12; // 12 - margin from screen edges

            if (info.canUseBurst(player)) {
                renderAnimation(mStack, xStart, y, burstHeightAndWidth, burstHeightAndWidth, getSource(info.current(), "burst", true));
            } else {
                renderStill(mStack, xStart, y, burstHeightAndWidth, burstHeightAndWidth, info.current(), "burst");

                // render ticks delay
                int ticks = info.ticksTillBurst(player, info.current());
                if (ticks > 0) {
                    // render seconds till
                    String text = SECONDS_FORMAT.format(ticks / 20d);

                    // render seconds
                    GuiComponent.drawString(mStack, font, text,
                            xStart + (burstHeightAndWidth - font.width(text)) / 2,
                            y - font.lineHeight,
                            Color.GRAY.getRGB());
                }
            }

            // render keyboard helping info
            String text = fromMapping(KeyMappings.BURST);
            GuiComponent.drawString(mStack, font, text,
                    xStart + (burstHeightAndWidth - font.width(text)) / 2,
                    y + burstHeightAndWidth, // 4 for spacing
                    (info.canUseBurst(player) ? Color.WHITE : Color.GRAY).getRGB());


            // spacing
            xStart -= 4;
            // burst icon size
            xStart -= burstHeightAndWidth;
            burstHeightAndWidth = 28;
            y += (48 - burstHeightAndWidth);

            // if can use skill
            if (info.canUseSkill(player)) {
                // render animation
                renderAnimation(mStack, xStart, y, burstHeightAndWidth, burstHeightAndWidth, getSource(info.current(), "skill", true));
            } else {
                // otherwise render still icon
                renderStill(mStack, xStart, y, burstHeightAndWidth, burstHeightAndWidth, info.current(), "skill");

                // render ticks delay
                int ticks = info.ticksTillSkill(player, info.current());
                if (ticks > 0) {
                    // render seconds till
                    text = SECONDS_FORMAT.format(ticks / 20d);

                    // render seconds
                    GuiComponent.drawString(mStack, font, text,
                            xStart + (burstHeightAndWidth - font.width(text)) / 2,
                            y - font.lineHeight,
                            Color.GRAY.getRGB());
                }
            }

            // render keyboard helping info
            text = fromMapping(KeyMappings.SKILL);
            GuiComponent.drawString(mStack, font, text,
                    xStart + (burstHeightAndWidth - font.width(text)) / 2,
                    y + font.lineHeight + burstHeightAndWidth / 2 + 4, // 4 for spacing
                    (info.canUseSkill(player) ? Color.WHITE : Color.GRAY).getRGB());
        }
    }

    private String fromMapping(KeyMapping mapping) {
        if (mapping == null) {
            return "";
        }

        InputConstants.Key key = mapping.getKey();
        return mapping.getKeyModifier().getCombinedName(key, key::getDisplayName).getString();
    }

    /**
     * Rendering animation
     *
     * @param stack    - pose stack
     * @param x        - x start
     * @param y        - y start
     * @param width    - rectangle width
     * @param height   - rectangle height
     * @param location - sprite location in InventoryMenu.BLOCK_ATLAS atlas
     */
    public static void renderAnimation(PoseStack stack, int x, int y, int width, int height, ResourceLocation location) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(location);
        if (sprite == null)
            return;

        RenderSystem.setShaderTexture(0, sprite.atlas().location());
        innerBlit(stack.last().pose(),
                x, x + width,
                y, y + height, 0,
                sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(),
                1, 1, 1, 1);
    }

    private void renderStill(PoseStack stack, int x, int y, int width, int height, IGenshinPlayer current, String resource) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(getSource(current, resource, true));
        if (sprite == null)
            return;

        RenderSystem.setShaderTexture(0, getSource(current, resource, false));
        RenderSystem.setShaderColor(1, 1, 1, 0.5f);
        GuiComponent.blit(stack, x, y, 0, 0, 0, width, height, width, height * sprite.getFrameCount());
    }


    public static void innerBlit(Matrix4f p_93113_, float x1, float x2, float x3, float x4, float x5, float x6, float x7, float x8, float x9, float red, float green, float blue, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(p_93113_, x1, x4, x5).uv(x6, x9).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(p_93113_, x2, x4, x5).uv(x7, x9).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(p_93113_, x2, x3, x5).uv(x7, x8).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(p_93113_, x1, x3, x5).uv(x6, x8).color(red, green, blue, alpha).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }

    /**
     * Find specific icons location for player
     *
     * @param player   - current character
     * @param picture  - String of resource. Could be 'icon', 'burst', 'skill' or 'skin'
     * @param animated - should icon be animated.
     * @return - location to image. 'burst' and 'skill' must be animated
     */
    public static ResourceLocation getSource(IGenshinPlayer player, String picture, boolean animated) {
        ResourceLocation source = player.getRegistryName();
        String formattedString = "players/%s/%s";
        if (!animated) {
            formattedString = "textures/" + formattedString + ".png";
        }

        return new ResourceLocation(source.getNamespace(), String.format(formattedString, source.getPath(), picture));
    }
}
