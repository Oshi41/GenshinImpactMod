package com.gim.client.screen;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.client.screen.base.GenshinScreenBase;
import com.gim.menu.SkillStationMenu;
import com.gim.players.base.IGenshinPlayer;
import com.gim.players.base.TalentAscendInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class SkillStationScreen extends GenshinScreenBase<SkillStationMenu> {
    public SkillStationScreen(SkillStationMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, new ResourceLocation(GenshinImpactMod.ModID, "textures/gui/skill_station/skill_station.png"));

        this.titleLabelX = 111;
        this.titleLabelY = 9;

        this.imageWidth = 256;
        this.imageHeight = 256;
    }

    @Override
    protected void init() {
        super.init();

        TranslatableComponent component = new TranslatableComponent("gim.upgrade");
        int buttonWidth = 56;

        addRenderableWidget(new Button(this.leftPos + 79, this.topPos + 151, 18, 18, new TextComponent("<"),
                p_93751_ -> this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 0)));

        addRenderableWidget(new Button(this.leftPos + 155, this.topPos + 151, 18, 18, new TextComponent(">"),
                p_93751_ -> this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 1)));

        addRenderableWidget(new Button(this.leftPos + 98, this.topPos + 151, buttonWidth, 18, component,
                p_93751_ -> {
                    this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 2);
                    SkillStationScreen.this.getMenu().refreshByIndex();
                }
        ) {
            @Override
            public void render(PoseStack p_93657_, int p_93658_, int p_93659_, float p_93660_) {
                super.render(p_93657_, p_93658_, p_93659_, p_93660_);
                this.active = SkillStationScreen.this.getMenu().canApply();
            }
        });
    }

    @Override
    protected void renderBg(PoseStack p_97787_, float p_97788_, int xMouse, int yMouse) {
        super.renderBg(p_97787_, p_97788_, xMouse, yMouse);

        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;

        GenshinEntityData entityData = getMenu().current();

        // render model player
        IGenshinPlayer assotiatedPlayer = entityData.getAssotiatedPlayer();

        // render current model
        renderEntityInInventory(i + 51 - 20,
                j + 75 - 5,
                30,
                (float) (i + 51) - this.xMouse,
                (float) (j + 75 - 50 - 10) - this.yMouse,
                this.minecraft.player,
                assotiatedPlayer);

        TalentAscendInfo talentAscendInfo = getMenu().info(true);
        if (talentAscendInfo == null)
            return;

        // render ghost items above the item stack
        for (int k = 0; k < talentAscendInfo.materials().size(); k++) {
            ItemStack itemStack = talentAscendInfo.materials().get(k);
            Slot firstSlot = getMenu().getSlot(0);
            int xStart = i + firstSlot.x + 18 * k;
            int yStart = j + firstSlot.y - 18;

            // draw items and it's count
            this.itemRenderer.renderAndDecorateItem(itemStack, xStart, yStart);
            this.itemRenderer.renderGuiItemDecorations(minecraft.font, itemStack, xStart, yStart);

            if (xStart <= this.xMouse && this.xMouse <= xStart + 16
                    &&
                    yStart <= this.yMouse && this.yMouse <= yStart + 16) {
                // tooltip for curernt stack
                renderTooltip(p_97787_, itemStack, (int) this.xMouse, (int) this.yMouse);
            }
        }

        // draw name
        drawCenteredString(p_97787_, getMinecraft().font, assotiatedPlayer.getName().withStyle(ChatFormatting.UNDERLINE), i + 111, j + 27, -1);

        for (int k = 0; k < talentAscendInfo.info().size(); k++) {
            MutableComponent text = talentAscendInfo.info().get(k);

            if (k == talentAscendInfo.info().size() - 1) {
                text.withStyle(
                        this.minecraft.player.isCreative() || this.minecraft.player.experienceLevel >= talentAscendInfo.expLevel()
                                ? ChatFormatting.GREEN
                                : ChatFormatting.RED
                );
            }

            int x = i + 58;
            int y = j + 27 + ((k + 2) * getMinecraft().font.lineHeight);

            if (j + 27 + (6 * getMinecraft().font.lineHeight) <= y && y < j + 27 + (11 * getMinecraft().font.lineHeight)) {
                x = i + 5;
            }

            getMinecraft().font.draw(p_97787_, text, x, y, -1);
        }

        RenderSystem.setShaderTexture(0, new ResourceLocation("realms", "textures/gui/realms/questionmark.png"));
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(p_97787_, i + this.imageWidth - 16 - 4, j + 4, 0, 0, 16, 16, 32, 16);

        if (i + this.imageWidth - 16 <= xMouse && xMouse <= i + this.imageWidth
                &&
                j + 4 <= yMouse && yMouse <= j + 4 + 16) {
            List<Component> skillInfo = new ArrayList<>(getMenu().info(false).skillsInfo());
            renderTooltip(p_97787_, skillInfo, Optional.empty(), xMouse, yMouse);
        }
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        drawCenteredString(p_97808_, this.font, this.title, this.titleLabelX, this.titleLabelY, -1);
    }
}
