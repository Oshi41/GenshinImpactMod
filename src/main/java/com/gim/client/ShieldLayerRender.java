package com.gim.client;

import com.gim.capability.shield.IShield;
import com.gim.registry.Capabilities;
import com.gim.registry.Elementals;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class ShieldLayerRender<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    static ResourceLocation resourceLocation = new ResourceLocation("forge:textures/white.png");

    public ShieldLayerRender(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int p_117472_, T entity, float p_117474_, float p_117475_, float p_117476_, float p_117477_, float p_117478_, float p_117479_) {
        entity.getCapability(Capabilities.SHIELDS).ifPresent(shield -> {
            if (shield.isAvailable()) {
                render(shield, poseStack, bufferSource, p_117472_, entity, p_117474_, p_117475_, p_117476_, p_117477_, p_117478_, p_117479_);
            }
        });
    }

    private void render(IShield shield, PoseStack poseStack, MultiBufferSource bufferSource, int p_117472_, T entity, float p_117474_, float p_117475_, float p_117476_, float p_117477_, float p_117478_, float p_117479_) {
        VertexConsumer vertexConsumer = entity.isInvisible()
                ? bufferSource.getBuffer(RenderType.outline(resourceLocation))
                : bufferSource.getBuffer(RenderType.entityTranslucent(resourceLocation));

        float scale = 1.2f;
        poseStack.scale(scale, scale, scale);

        float[] floats = getColor(shield.getElement());

        getParentModel().prepareMobModel(entity, p_117474_, p_117475_, p_117476_);
        getParentModel().setupAnim(entity, p_117474_, p_117475_, p_117477_, p_117478_, p_117479_);
        getParentModel().renderToBuffer(poseStack, vertexConsumer, p_117472_,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                floats[0], floats[1], floats[2], 0.15f);
    }

    private float[] getColor(Elementals e) {
        Color c = switch (e) {
            case PYRO -> Color.RED;
            case HYDRO -> Color.BLUE;
            case CRYO -> Color.CYAN;
            case ELECTRO -> Color.MAGENTA;
            case DENDRO -> Color.GREEN;
            case ANEMO -> Color.WHITE;
            case GEO -> Color.YELLOW;
            default -> Color.GRAY;
        };

        return new float[]{
                c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f
        };
    }
}
