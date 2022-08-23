package com.gim.client.entity;

import com.gim.entity.Energy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EnergyRenderer extends EntityRenderer<Energy> {

    private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");
    private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

    public EnergyRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    protected int getBlockLightLevel(Energy p_114606_, BlockPos p_114607_) {
        return Mth.clamp(super.getBlockLightLevel(p_114606_, p_114607_) + 7, 7, 15);
    }

    @Override
    public ResourceLocation getTextureLocation(Energy p_114482_) {
        return EXPERIENCE_ORB_LOCATION;
    }

    @Override
    public void render(Energy entity, float p_114600_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        int i = entity.getIcon();
        float f = (float) (i % 4 * 16) / 64.0F;
        float f1 = (float) (i % 4 * 16 + 16) / 64.0F;
        float f2 = (float) (i / 4 * 16) / 64.0F;
        float f3 = (float) (i / 4 * 16 + 16) / 64.0F;
        float f8 = ((float) entity.tickCount + partialTicks) / 2.0F;
        int red = (int) ((Mth.sin(f8 + 0.0F) + 1.0F) * 0.5F * 255.0F);
        int green = 255;
        int blue = (int) ((Mth.sin(f8 + 4.1887903F) + 1.0F) * 0.1F * 255.0F);

        red = 255;
        blue = 255;

        poseStack.translate(0.0D, (double) 0.1F, 0.0D);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.scale(0.3F, 0.3F, 0.3F);
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RENDER_TYPE);
        PoseStack.Pose posestack$pose = poseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        vertex(vertexconsumer, matrix4f, matrix3f, -0.5F, -0.25F, red, green, blue, f, f3, light);
        vertex(vertexconsumer, matrix4f, matrix3f, 0.5F, -0.25F, red, green, blue, f1, f3, light);
        vertex(vertexconsumer, matrix4f, matrix3f, 0.5F, 0.75F, red, green, blue, f1, f2, light);
        vertex(vertexconsumer, matrix4f, matrix3f, -0.5F, 0.75F, red, green, blue, f, f2, light);
        poseStack.popPose();

        super.render(entity, p_114600_, partialTicks, poseStack, bufferSource, light);
    }

    private static void vertex(VertexConsumer p_114609_, Matrix4f p_114610_, Matrix3f p_114611_, float p_114612_, float p_114613_, int red, int green, int blue, float p_114617_, float p_114618_, int p_114619_) {
        p_114609_.vertex(p_114610_, p_114612_, p_114613_, 0.0F)
                .color(red, green, blue, 255)
                .uv(p_114617_, p_114618_)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(p_114619_)
                .normal(p_114611_, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
