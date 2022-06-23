package com.gim.client.screen;

import com.gim.GenshinImpactMod;
import com.gim.client.GenshinClientHooks;
import com.gim.client.screen.base.GenshinScreenBase;
import com.gim.menu.ArtifactsStationMenu;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;

public class ArtifactsStationScreen extends GenshinScreenBase<ArtifactsStationMenu> {
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
    protected void renderBg(PoseStack p_97787_, float p_97788_, int p_97789_, int p_97790_) {
        super.renderBg(p_97787_, p_97788_, p_97789_, p_97790_);

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
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        this.font.draw(p_97808_, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
        // this.font.draw(p_97808_, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
    }
}
