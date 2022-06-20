package com.gim.client.screen;

import com.gim.GenshinImpactMod;
import com.gim.client.GenshinClientHooks;
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

public class ArtifactsStationScreen extends AbstractContainerScreen<ArtifactsStationMenu> {
    private static final ResourceLocation GUI = new ResourceLocation(GenshinImpactMod.ModID, "textures/gui/artifacts_station/atrifacts_station.png");
    private Button left;
    private Button right;
    private float xMouse;
    private float yMouse;

    public ArtifactsStationScreen(ArtifactsStationMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);

        this.imageWidth = 176;
        this.imageHeight = 215;

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
        getMenu().changeIndex(getMenu().getIndex() + (forward ? 1 : -1));
    }

    @Override
    public void render(PoseStack p_97795_, int p_97796_, int p_97797_, float p_97798_) {
        super.render(p_97795_, p_97796_, p_97797_, p_97798_);
        renderTooltip(p_97795_, p_97796_, p_97797_);

        this.xMouse = (float) p_97796_;
        this.yMouse = (float) p_97797_;
    }

    @Override
    protected void renderBg(PoseStack p_97787_, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(p_97787_, i, j, 0, 0, this.imageWidth, this.imageHeight);

        renderEntityInInventory(i + 51 - 20,
                j + 75 - 5,
                30,
                (float) (i + 51) - this.xMouse,
                (float) (j + 75 - 50 - 10) - this.yMouse,
                this.minecraft.player);

        BaseComponent component = getMenu().current().getAssotiatedPlayer().getName();
        int textHeight = minecraft.font.lineHeight;

        drawCenteredString(p_97787_, minecraft.font, component, this.leftPos + 114, this.topPos + textHeight * 2, getMenu().current().getAssotiatedPlayer().getElemental().getChatColor().getColor());
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        this.font.draw(p_97808_, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
        // this.font.draw(p_97808_, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
    }

    public void renderEntityInInventory(int p_98851_, int p_98852_, int p_98853_, float p_98854_, float p_98855_, LivingEntity p_98856_) {
        float f = (float) Math.atan(p_98854_ / 40.0F);
        float f1 = (float) Math.atan(p_98855_ / 40.0F);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(p_98851_, p_98852_, 1050.0D);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        posestack1.translate(0.0D, 0.0D, 1000.0D);
        posestack1.scale((float) p_98853_, (float) p_98853_, (float) p_98853_);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
        quaternion.mul(quaternion1);
        posestack1.mulPose(quaternion);
        float f2 = p_98856_.yBodyRot;
        float f3 = p_98856_.getYRot();
        float f4 = p_98856_.getXRot();
        float f5 = p_98856_.yHeadRotO;
        float f6 = p_98856_.yHeadRot;
        p_98856_.yBodyRot = 180.0F + f * 20.0F;
        p_98856_.setYRot(180.0F + f * 40.0F);
        p_98856_.setXRot(-f1 * 20.0F);
        p_98856_.yHeadRot = p_98856_.getYRot();
        p_98856_.yHeadRotO = p_98856_.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderdispatcher.overrideCameraOrientation(quaternion1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            this.render(p_98856_, GenshinClientHooks.getRenderer(getMenu().current().getAssotiatedPlayer()), 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880);
        });
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        p_98856_.yBodyRot = f2;
        p_98856_.setYRot(f3);
        p_98856_.setXRot(f4);
        p_98856_.yHeadRotO = f5;
        p_98856_.yHeadRot = f6;
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    public <E extends Entity> void render(E p_114385_, EntityRenderer entityrenderer, double p_114386_, double p_114387_, double p_114388_, float p_114389_, float p_114390_, PoseStack p_114391_, MultiBufferSource p_114392_, int p_114393_) {
        try {
            Vec3 vec3 = entityrenderer.getRenderOffset(p_114385_, p_114390_);
            double d2 = p_114386_ + vec3.x();
            double d3 = p_114387_ + vec3.y();
            double d0 = p_114388_ + vec3.z();
            p_114391_.pushPose();
            p_114391_.translate(d2, d3, d0);
            entityrenderer.render(p_114385_, p_114389_, p_114390_, p_114391_, p_114392_, p_114393_);

            p_114391_.translate(-vec3.x(), -vec3.y(), -vec3.z());
            p_114391_.popPose();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
            p_114385_.fillCrashReportCategory(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
            crashreportcategory1.setDetail("Assigned renderer", entityrenderer);
            crashreportcategory1.setDetail("Location", CrashReportCategory.formatLocation(this.minecraft.level, p_114386_, p_114387_, p_114388_));
            crashreportcategory1.setDetail("Rotation", p_114389_);
            crashreportcategory1.setDetail("Delta", p_114390_);
            throw new ReportedException(crashreport);
        }
    }
}
