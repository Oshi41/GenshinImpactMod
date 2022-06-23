package com.gim.client.screen;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.client.screen.base.GenshinScreenBase;
import com.gim.menu.ConstellationMenu;
import com.gim.menu.LevelStationMenu;
import com.gim.players.base.AscendInfo;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Attributes;
import com.gim.registry.Capabilities;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LevelStationScreen extends GenshinScreenBase<LevelStationMenu> {
    private Button applyBtn;
    private Button left;
    private Button right;

    public LevelStationScreen(LevelStationMenu menu, Inventory inventory, Component text) {
        super(menu, inventory, text, new ResourceLocation(GenshinImpactMod.ModID, "textures/gui/level_station/level_station.png"));

        this.titleLabelX = 70;
        this.titleLabelY = 9;
    }

    @Override
    protected void init() {
        super.init();

        TranslatableComponent component = new TranslatableComponent(GenshinImpactMod.ModID + ".upgrade");
        int buttonWidth = this.minecraft.font.width(component) + 4 * 2;

        int xStart = this.leftPos + this.imageWidth - buttonWidth - 4;

        applyBtn = addRenderableWidget(new Button(xStart, this.topPos + 110, buttonWidth, 18, component,
                p_93751_ -> this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 3)
        ));

        left = addRenderableWidget(new Button(this.leftPos + 56, this.topPos + 4, 18, 18, new TextComponent("<"),
                p_93751_ -> this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 0)));

        right = addRenderableWidget(new Button(this.leftPos + this.imageWidth - 18 - 4, this.topPos + 4, 18, 18, new TextComponent(">"),
                p_93751_ -> this.minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 1)));
    }

    @Override
    protected void renderBg(PoseStack p_97787_, float p_97788_, int p_97789_, int p_97790_) {
        super.renderBg(p_97787_, p_97788_, p_97789_, p_97790_);

        // control activating button
        applyBtn.active = getMenu().canApply();

        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;

        GenshinEntityData entityData = getMenu().current();

        // render model player
        IGenshinPlayer assotiatedPlayer = entityData.getAssotiatedPlayer();

        renderEntityInInventory(i + 51 - 20,
                j + 75 - 5,
                30,
                (float) (i + 51) - this.xMouse,
                (float) (j + 75 - 50 - 10) - this.yMouse,
                this.minecraft.player,
                assotiatedPlayer);

        BaseComponent component = assotiatedPlayer.getName();
        drawPLayerName(component, p_97787_, i + 114, j + minecraft.font.lineHeight * 3);

        AscendInfo forCurrent = getMenu().getForCurrent();
        if (forCurrent != null) {
            for (int k = 0; k < forCurrent.materials.size(); k++) {
                ItemStack itemStack = forCurrent.materials.get(k);
                int xStart = i + 8 + 18 * k;
                int yStart = j + 110 - 18;
                this.itemRenderer.renderAndDecorateItem(itemStack, xStart, yStart);
                this.itemRenderer.renderGuiItemDecorations(minecraft.font, itemStack, xStart, yStart);

                if (xStart <= xMouse && xMouse <= xStart + 16
                        &&
                        yStart <= yMouse && yMouse <= yStart + 16) {
                    renderTooltip(p_97787_, itemStack, (int) xMouse, (int) yMouse);
                }
            }

            for (int k = 0; k < forCurrent.info.size(); k++) {
                Component text = forCurrent.info.get(k);
                getMinecraft().font.draw(p_97787_, text, i + 65, j + 40 + (k * getMinecraft().font.lineHeight), -1);
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        this.font.draw(p_97808_, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
        // this.font.draw(p_97808_, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
    }
}
