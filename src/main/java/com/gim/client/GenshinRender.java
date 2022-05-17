package com.gim.client;

import com.gim.GenshinHeler;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.google.common.collect.Iterators;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.GuiUtils;
import net.minecraftforge.client.gui.IIngameOverlay;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.text.DecimalFormat;
import java.util.Comparator;

@OnlyIn(Dist.CLIENT)
public class GenshinRender implements IIngameOverlay {
    private final DecimalFormat switchSecondsFormat = new DecimalFormat("#.#");
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

        int end = Math.max(4, info.currentStack().size());

        int xStart = width - 32;
        int y = height / 5;
        int yStep = 32;
        Font font = Minecraft.getInstance().font;

        Integer nameLength = info.currentStack().stream().map(x -> font.width(x.getName().getString())).max(Integer::compare).orElse(24);

        for (int i = 0; i < end; i++) {
            IGenshinPlayer genshinPlayer = info.currentStack().size() > i
                    ? Iterators.get(info.currentStack().iterator(), i)
                    : null;

            int xPos = xStart;
            int yPos = y + yStep * i;
            String text = (i + 1) + "";

            // render background
            RenderSystem.setShaderTexture(0, WHITE_COLOR);
            Color color1 = i == info.currentIndex() ? ACTIVE_COLOR_1 : DISABLED_COLOR_1;
            Color color2 = i == info.currentIndex() ? ACTIVE_COLOR_2 : DISABLED_COLOR_2;
            GuiUtils.drawGradientRect(mStack.last().pose(), 0, xPos - nameLength - 18 - 24, yPos, width - 32, yPos + 18,
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
                Color color = ShieldLayerRender.getColor(genshinPlayer.getElemental());
                Color lighter = GenshinHeler.withAlpha(color, 124);

                xPos -= 24;

                // render character icon
                RenderSystem.setShaderTexture(0, genshinPlayer.getIcon());
                RenderSystem.setShaderColor(1, 1, 1, 1);
                GuiUtils.drawInscribedRect(mStack, xPos, yPos, 18, 18, 18, 18);

                xPos -= 16 + nameLength;

                // render character name
                GuiComponent.drawString(mStack, font, text,
                        xPos,
                        yPos + (18 - font.lineHeight) / 2,
                        Color.WHITE.getRGB());

                xPos -= 24;
                if (info.ticksTillBurst(player, genshinPlayer) <= 0) {
                    GuiUtils.drawGradientRect(mStack.last().pose(), 0, xPos, yPos, xPos + 18, yPos + 18,
                            color.getRGB(), lighter.getRGB());

                    RenderSystem.setShaderTexture(0, genshinPlayer.getBurstIcon());
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    GuiUtils.drawInscribedRect(mStack, xPos, yPos, 18, 18, 18, 18);
                }

                xPos -= 24;
                if (info.ticksTillSkill(player, genshinPlayer) <= 0) {
                    GuiUtils.drawGradientRect(mStack.last().pose(), 0, xPos, yPos, xPos + 18, yPos + 18,
                            color.getRGB(), lighter.getRGB());

                    RenderSystem.setShaderTexture(0, genshinPlayer.getSkillIcon());
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                    GuiUtils.drawInscribedRect(mStack, xPos, yPos, 18, 18, 18, 18);
                }
            } else {
                xPos = xPos - 16 - 24 * 3 - nameLength;
            }


        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
