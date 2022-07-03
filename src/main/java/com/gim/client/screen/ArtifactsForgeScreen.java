package com.gim.client.screen;

import com.gim.GenshinImpactMod;
import com.gim.artifacts.base.ArtifactProperties;
import com.gim.artifacts.base.ArtifactSlotType;
import com.gim.client.screen.base.GenshinScreenBase;
import com.gim.items.ArtefactItem;
import com.gim.menu.ArtifactsForgeMenu;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ArtifactsForgeScreen extends GenshinScreenBase<ArtifactsForgeMenu> {
    private static final ResourceLocation GUI = new ResourceLocation(GenshinImpactMod.ModID, "textures/gui/artifacts_forge/artifacts_forge.png");
    private Button applyBtn;

    public ArtifactsForgeScreen(ArtifactsForgeMenu forgeMenu, Inventory inventory, Component text) {
        super(forgeMenu, inventory, text, new ResourceLocation(GenshinImpactMod.ModID, "textures/gui/artifacts_forge/artifacts_forge.png"));
    }

    @Override
    protected void init() {
        super.init();

        applyBtn = addRenderableWidget(new Button(this.leftPos + 118, this.topPos + 111, 51, 18, new TranslatableComponent(GenshinImpactMod.ModID + ".upgrade"),
                p_93751_ -> this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 0)
        ));
    }

    @Override
    public void render(PoseStack p_97795_, int p_97796_, int p_97797_, float p_97798_) {
        super.render(p_97795_, p_97796_, p_97797_, p_97798_);
        applyBtn.active = getMenu().isCanApply();
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        this.font.draw(p_97808_, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int p_97789_, int p_97790_) {
        super.renderBg(poseStack, p_97788_, p_97789_, p_97790_);

        int x = this.leftPos;
        int y = (this.height - this.imageHeight) / 2;

        ItemStack artifact = getMenu().getSlot(0).getItem();

        // render artifact exp
        renderXpBar(poseStack, x, y, artifact);

        // artifact type was choosen
        if (getMenu().getSlotType() != null) {
            int index = Arrays.binarySearch(ArtifactSlotType.values(), getMenu().getSlotType());
            if (index >= 0) {
                int size = 22;
                int xImage = 176;
                int yImage = index * 22;


                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, GUI);
                blit(poseStack, x + 5, y + 66, xImage, yImage, size, size, 256, 256);
            }
        }

        // is applying some levels
        if (!artifact.isEmpty() && getMenu().isCanApply() && getMenu().getApplyingLevels() > 0) {
            ArtifactProperties properties = ((ArtefactItem) artifact.getItem()).from(artifact);

            int totalModifiers = properties.getSubModifiers().size();
            int subStatAdded = 0;
            int subStatUpgraded = 0;

            // obtaining final artifact
            double finalAttrValue = properties.getPrimal().getForLevel(properties.getRarity(),
                    properties.getRarity().getLevel(properties.getExp()) + getMenu().getApplyingLevels()) -
                    properties.getPrimal().getForLevel(properties.getRarity(),
                            properties.getRarity().getLevel(properties.getExp()));

            for (int i = properties.getRarity().getLevel(properties.getExp()) + 1, end = i + getMenu().getApplyingLevels(); i < end; i++) {
                if (i % 4 == 0) {
                    if (totalModifiers < 4) {
                        totalModifiers++;
                        subStatAdded++;
                    } else {
                        subStatUpgraded++;
                    }
                }
            }

            ArrayList<Component> list = new ArrayList<>();
            list.add(new TextComponent("+").withStyle(ChatFormatting.YELLOW).append(new TranslatableComponent("attribute.modifier.equals." + properties.getPrimal().getOperation().toValue(),
                    ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(finalAttrValue),
                    new TranslatableComponent(properties.getPrimal().getAttribute().getDescriptionId()))
                    .withStyle(ChatFormatting.YELLOW)));

            if (subStatAdded > 0)
                list.add(new TranslatableComponent(GenshinImpactMod.ModID + ".sub_stat_added", subStatAdded).withStyle(ChatFormatting.WHITE));

            if (subStatUpgraded > 0)
                list.add(new TranslatableComponent(GenshinImpactMod.ModID + ".sub_stat_upgraded", subStatUpgraded).withStyle(ChatFormatting.WHITE));

            int xStart = this.leftPos + this.imageWidth - 102 - 4 - minecraft.font.width("exp");
            int yStart = y + 40 + minecraft.font.lineHeight;

            for (int i = 0; i < list.size(); i++) {
                Component component = list.get(i);

                minecraft.font.draw(poseStack, component, xStart, yStart + i * minecraft.font.lineHeight, 4210752);
            }
        }
    }

    private void renderXpBar(PoseStack poseStack, int x, int y, ItemStack artifact) {
        if (artifact.isEmpty())
            return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI);

        int barLength = 102;

        int xStart = this.leftPos + this.imageWidth - barLength - 4;
        int yStart = y + 30;

        // progressBarBase
        blit(poseStack, xStart, yStart, this.getBlitOffset(), 0, 220, barLength, 5, 256, 256);

        // EXP helping text
        Component component = new TextComponent("EXP");
        minecraft.font.draw(poseStack, component, xStart - minecraft.font.width(component) - 2, yStart, 4210752);

        // draw applying exp amount above bar
        if (getMenu().getApplyingExp() > 0) {
            component = new TextComponent("+" + getMenu().getApplyingExp()).withStyle(ChatFormatting.GREEN);
            drawCenteredString(poseStack, minecraft.font, component,
                    xStart + 50, yStart - minecraft.font.lineHeight, 4210752);
        }


        // writing artifact value for item
        int artifactLevel = getMenu().getArtifactLevel();
        component = new TextComponent("+" + artifactLevel);
        minecraft.font.draw(poseStack, component, xStart - minecraft.font.width(component) - 2, yStart - minecraft.font.lineHeight, ChatFormatting.WHITE.getColor());

        // if reached max experience
        if (getMenu().getArtifactLevel() == ((ArtefactItem) artifact.getItem()).from(artifact).getRarity().getMaxLevel()) {
            component = new TextComponent("max").withStyle(ChatFormatting.YELLOW);
            drawCenteredString(poseStack, minecraft.font, component,
                    xStart + 50, yStart - minecraft.font.lineHeight, 4210752);
        }

        // drawing how much levels will upgrade
        if (getMenu().getApplyingLevels() > 0) {
            component = new TextComponent("+" + getMenu().getApplyingLevels()).withStyle(ChatFormatting.YELLOW);
            drawCenteredString(poseStack, minecraft.font, component,
                    xStart + minecraft.font.width("+" + artifactLevel) + 2,
                    yStart - minecraft.font.lineHeight, ChatFormatting.YELLOW.getColor());

        }

        // own artifact exp (white one)
        if (getMenu().getArtifactExp() >= 0 && getMenu().getArtifactExpToNextLevel() > 0) {
            int length = (int) ((barLength - 2.) * getMenu().getArtifactExp() / getMenu().getArtifactExpToNextLevel());

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI);
            blit(poseStack, xStart + 1, yStart + 1, this.getBlitOffset(), 2, 216, length, 3, 256, 256);

            xStart += length;
            barLength -= length;
        }

        // applying artifact exp (green one)
        if (getMenu().getApplyingExp() > 0) {
            int length = (int) Math.min(barLength - 2, barLength * (getMenu().getArtifactExp() + getMenu().getApplyingExp()) / (double) getMenu().getArtifactExpToNextLevel());

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI);
            blit(poseStack, xStart + 1, yStart + 1, this.getBlitOffset(), 2, 226, length, 3, 256, 256);
        }

        // drawing exp for current level
        if (getMenu().getArtifactExpToMaxLevel() > 0) {
            String text = String.format("%s/%s", getMenu().getArtifactExp(), getMenu().getArtifactExpToNextLevel());
            minecraft.font.draw(poseStack, text, this.leftPos + this.imageWidth - minecraft.font.width(text) - 4,
                    yStart + 8, 4210752);
        }
    }
}
