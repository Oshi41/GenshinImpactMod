package com.gim.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class CustomTextureRender<T extends Entity> extends EntityRenderer<T> {
    private final Function<T, ResourceLocation> supplier;

    private final Map<ResourceLocation, RenderType> renders = new HashMap<>();

    public CustomTextureRender(EntityRendererProvider.Context context, Function<T, ResourceLocation> supplier) {
        super(context);
        this.supplier = supplier;
    }

    public CustomTextureRender(EntityRendererProvider.Context p_174008_) {
        this(p_174008_, t -> null);
    }

    @Override
    public void render(T entity, float p_114081_, float p_114082_, PoseStack poseStack, MultiBufferSource p_114084_, int p_114085_) {
        poseStack.pushPose();
        renderImage(poseStack, p_114084_.getBuffer(renders.computeIfAbsent(this.getTextureLocation(entity), RenderType::entityCutoutNoCull)), this.entityRenderDispatcher, p_114085_, 1);
        poseStack.popPose();
        super.render(entity, p_114081_, p_114082_, poseStack, p_114084_, p_114085_);
    }

    public static void renderImage(PoseStack poseStack, VertexConsumer vertexconsumer, EntityRenderDispatcher entityRenderDispatcher, int partialTicks, float scale) {
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.translate(0, 0.4, 0);
        PoseStack.Pose posestack$pose = poseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        vertex(vertexconsumer, matrix4f, matrix3f, partialTicks, 0.0F, 0, 0, 1);
        vertex(vertexconsumer, matrix4f, matrix3f, partialTicks, 1.0F, 0, 1, 1);
        vertex(vertexconsumer, matrix4f, matrix3f, partialTicks, 1.0F, 1, 1, 0);
        vertex(vertexconsumer, matrix4f, matrix3f, partialTicks, 0.0F, 1, 0, 0);
    }

    private static void vertex(VertexConsumer p_114090_, Matrix4f p_114091_, Matrix3f p_114092_, int p_114093_, float p_114094_, int p_114095_, int p_114096_, int p_114097_) {
        p_114090_.vertex(p_114091_, p_114094_ - 0.5F, (float) p_114095_ - 0.25F, 0.0F).color(255, 255, 255, 255).uv((float) p_114096_, (float) p_114097_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_114093_).normal(p_114092_, 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(T e) {
        return supplier.apply(e);
    }
}
