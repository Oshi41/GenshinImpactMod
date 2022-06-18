package com.gim.client.entity;

import com.gim.entity.TextParticle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;

import java.awt.*;

public class TextureParticleRenderer<T extends TextParticle> extends EntityRenderer<T> {
    public TextureParticleRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public void render(T entity, float p_114486_, float p_114487_, PoseStack poseStack, MultiBufferSource p_114489_, int p_114490_) {
        this.renderNameTag(entity, entity.getName(), poseStack, p_114489_, p_114490_);
    }

    @Override
    protected void renderNameTag(T p_114498_, Component p_114499_, PoseStack p_114500_, MultiBufferSource p_114501_, int light) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(p_114498_);
        if (ForgeHooksClient.isNameplateInRenderDistance(p_114498_, d0)) {
            boolean flag = !p_114498_.isDiscrete();
            float f = p_114498_.getBbHeight() + 0.5F;

            light = LightTexture.pack(15, 15);

            float scale = getScale(p_114498_);

            int i = "deadmau5".equals(p_114499_.getString()) ? -10 : 0;
            p_114500_.pushPose();
            p_114500_.translate(0.0, (double) f, 0.0);
            p_114500_.mulPose(this.entityRenderDispatcher.cameraOrientation());
            p_114500_.scale(scale, scale, -scale);
            Matrix4f matrix4f = p_114500_.last().pose();
            float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int j = (int) (f1 * 255.0F) << 24;
            Font font = this.getFont();
            float f2 = (float) (-font.width(p_114499_) / 2);
            font.drawInBatch(p_114499_, f2, (float) i, Color.white.getRGB(), true, matrix4f, p_114501_, flag, 0, light);

            p_114500_.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    /**
     * Scaling entity by its tick count
     * 1 --> 3 --> 1
     */
    private float getScale(T entity) {
        // base size
        float scale = 0.025f;
        // maxExp scale size during entity living
        float maxScale = 3;
        // half life of particle
        float halfEdge = TextParticle.MAX_AGE / 2f;
        // current size in percentages
        float percentage = entity.tickCount > halfEdge
                ? (TextParticle.MAX_AGE - entity.tickCount) / halfEdge
                : entity.tickCount / halfEdge;

        // scaling by percentage
        scale *= percentage * maxScale;

        float minDistance = 5;
        float maxDistance = 16;

        Vec3 gamePos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float maxRenderDistance = Minecraft.getInstance().gameRenderer.getRenderDistance();
        double distance = gamePos.distanceTo(entity.position());

        if (minDistance > distance) {
            scale *= distance / minDistance;
        } else if (maxDistance < distance) {
            scale *= (maxRenderDistance - distance) / maxRenderDistance * (maxDistance / minDistance);
        }

        // returning negative value ???
        return -scale;
    }
}
