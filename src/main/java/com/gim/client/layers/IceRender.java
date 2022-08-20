package com.gim.client.layers;

import com.gim.registry.Effects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IceRender<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private M model;
    static ResourceLocation resourceLocation = new ResourceLocation("textures/block/ice.png");

    public IceRender(RenderLayerParent<T, M> layerParent) {
        super(layerParent);

        // special render for slimes
        if (layerParent instanceof SlimeRenderer) {
            this.model = (M) new SlimeModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.SLIME_OUTER));
        }
    }

    @Override
    public M getParentModel() {
        return model != null
                ? model
                : super.getParentModel();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int p_117472_, T entity, float p_117474_, float p_117475_, float p_117476_, float p_117477_, float p_117478_, float p_117479_) {
        if (entity.hasEffect(Effects.FROZEN)) {
            VertexConsumer vertexConsumer = entity.isInvisible()
                    ? bufferSource.getBuffer(RenderType.outline(resourceLocation))
                    : bufferSource.getBuffer(RenderType.entityTranslucent(resourceLocation));

            float scale = 1.1f;
            poseStack.scale(scale, scale, scale);

            // needed for slime (for example)
            if (model != null) {
                super.getParentModel().copyPropertiesTo(getParentModel());
            }

            getParentModel().prepareMobModel(entity, p_117474_, p_117475_, p_117476_);
            getParentModel().setupAnim(entity, p_117474_, p_117475_, p_117477_, p_117478_, p_117479_);
            getParentModel().renderToBuffer(poseStack, vertexConsumer, p_117472_,
                    LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                    1, 1, 1, 1);
        }
    }
}
