package com.gim.client.screen;

import com.gim.GenshinImpactMod;
import com.gim.client.screen.base.GenshinScreenBase;
import com.gim.menu.ArtifactsStationMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ArtifactsStationScreen extends GenshinScreenBase<ArtifactsStationMenu> {
    private final static ResourceLocation QUESTIONMARK_LOCATION = new ResourceLocation("realms", "textures/gui/realms/questionmark.png");
    private Button left;
    private Button right;

    public ArtifactsStationScreen(ArtifactsStationMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, new ResourceLocation(GenshinImpactMod.ModID, "textures/gui/artifacts_station/atrifacts_station.png"));

        this.titleLabelX = 70;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        left = addRenderableWidget(new Button(this.leftPos + 7, this.topPos + 110, 18, 18, new TextComponent("<"), p_93751_ -> navigate(false)));
        right = addRenderableWidget(new Button(this.leftPos + 151, this.topPos + 110, 18, 18, new TextComponent(">"), p_93751_ -> navigate(true)));
    }

    private void navigate(boolean forward) {
        getMinecraft().gameMode.handleInventoryButtonClick(getMenu().containerId, forward ? 1 : 0);
    }

    @Override
    protected void renderBg(PoseStack p_97787_, float p_97788_, int xMouse, int yMouse) {
        super.renderBg(p_97787_, p_97788_, xMouse, yMouse);

        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;

        renderEntityInInventory(i + 51 - 20,
                j + 75 - 5,
                30,
                (float) (i + 51) - this.xMouse,
                (float) (j + 75 - 50 - 10) - this.yMouse,
                this.minecraft.player,
                getMenu().current().getAssotiatedPlayer());

        BaseComponent component = getMenu().current().getAssotiatedPlayer().getName();

        drawPLayerName(component, p_97787_, this.leftPos + 114, this.topPos + minecraft.font.lineHeight * 2);

        RenderSystem.setShaderTexture(0, QUESTIONMARK_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int size = 16;
        int xPos = i + this.imageWidth - 4 - size;
        int yPos = j + 3;

        blit(p_97787_, xPos, yPos, 0, 0, size, size, size * 2, size);

        if (xPos <= xMouse && xMouse <= xPos + size
                &&
                yPos <= yMouse && yMouse <= yPos + size) {
            List<Component> components = LevelStationScreen.from(getMenu().current().getAttributes());
            renderTooltip(p_97787_, components, Optional.empty(), xMouse, yMouse);
        }
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        this.font.draw(p_97808_, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
        // this.font.draw(p_97808_, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
    }
}
