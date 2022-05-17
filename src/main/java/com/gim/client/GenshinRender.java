package com.gim.client;

import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;

import java.awt.*;
import java.text.DecimalFormat;

@OnlyIn(Dist.CLIENT)
public class GenshinRender implements IIngameOverlay {
    private final DecimalFormat switchSecondsFormat = new DecimalFormat("#.#");

    @Override
    public void render(ForgeIngameGui gui, PoseStack mStack, float partialTicks, int width, int height) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        IGenshinInfo info = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
        if (info == null)
            return;

        int index = -1;

        for (IGenshinPlayer genshinPlayer : info.currentStack()) {
            index++;

            int x = width - 64;
            int y = (height / 2) - 64 + (index * 64);

            Color color = Color.WHITE;

            if (info.ticksTillSwitch(player) < 1 || info.getPersonInfo(genshinPlayer).getHealth() <= 0) {
                color = Color.GRAY;
            }

            RenderSystem.setShaderColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            RenderSystem.setShaderTexture(0, GuiComponent.STATS_ICON_LOCATION);
            GuiComponent.blit(mStack, x, y, 0, 0.0F, 0.0F, 18, 18, 128, 128);


            RenderSystem.setShaderColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            RenderSystem.setShaderTexture(0, genshinPlayer.getIcon());
            GuiComponent.blit(mStack, x - 64 - 16, y, 0, 0.0F, 0.0F, 64, 64, 128, 128);

            if (info.ticksTillSwitch(player) > 0) {
                String text = switchSecondsFormat.format(info.ticksTillSwitch(player) / 20d);
                GuiComponent.drawString(mStack, Minecraft.getInstance().font, text, x - 64 - 16, y, Color.WHITE.getRGB());
            }
        }
    }
}
