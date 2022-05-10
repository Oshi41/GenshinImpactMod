package com.gim.client;

import com.gim.entity.ShieldEntity;
import com.gim.registry.Elementals;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ShieldEntityRender extends EntityRenderer<ShieldEntity> {
    private final ItemRenderer itemRenderer;
    private final Map<Elementals, Tuple<ItemStack, BakedModel>> models = new HashMap<>();

    public ShieldEntityRender(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
    }

    private Tuple<ItemStack, BakedModel> create(Item item) {
        return new Tuple<>(item.getDefaultInstance(), this.itemRenderer.getModel(item.getDefaultInstance(), null, null, 0));
    }

    @Override
    public void render(ShieldEntity shieldEntity, float x, float yyy, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLightIn) {

        Tuple<ItemStack, BakedModel> tuple = models.computeIfAbsent(shieldEntity.getElemental(), elementals -> {
            ItemStack item = get(elementals).getDefaultInstance();
            return new Tuple<>(item, this.itemRenderer.getModel(item, shieldEntity.getLevel(), null, shieldEntity.getId()));
        });

        float bobOffs = Mth.sin(shieldEntity.getId());

        poseStack.pushPose();

        float f1 = Mth.sin((shieldEntity.tickCount + yyy) / 10.0F + bobOffs) * 0.1F + 0.1F;
        float f2 = tuple.getB().getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
        poseStack.translate(0, f1 + 0.25F * f2, 0);

        float f3 = (shieldEntity.tickCount + yyy) / 20 + bobOffs;
        poseStack.mulPose(Vector3f.YP.rotation(f3));

        for (int i = 0; i < 2; i++) {
            this.itemRenderer.render(
                    tuple.getA(),
                    ItemTransforms.TransformType.GROUND,
                    false,
                    poseStack,
                    bufferSource,
                    combinedLightIn,
                    OverlayTexture.NO_OVERLAY,
                    tuple.getB()
            );

            poseStack.mulPose(Vector3f.YP.rotation(90));
            poseStack.translate(0, 0.05, 0);
        }

        poseStack.popPose();

        super.render(shieldEntity, x, yyy, poseStack, bufferSource, combinedLightIn);
    }

    @Override
    public boolean shouldRender(ShieldEntity shieldEntity, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
        return super.shouldRender(shieldEntity, p_114492_, p_114493_, p_114494_, p_114495_) && shieldEntity.getElemental() != null;
    }

    @Override
    public ResourceLocation getTextureLocation(ShieldEntity p_114482_) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    private static Item get(Elementals e) {
        return switch (e) {
            case GEO -> Items.YELLOW_STAINED_GLASS;
            case PYRO -> Items.RED_STAINED_GLASS;
            case CRYO -> Items.LIGHT_BLUE_STAINED_GLASS;
            case ELECTRO -> Items.MAGENTA_STAINED_GLASS;
            case HYDRO -> Items.BLUE_STAINED_GLASS;
            case ANEMO -> Items.WHITE_STAINED_GLASS;
            case DENDRO -> Items.GRAY_STAINED_GLASS;
            default -> null;
        };
    }

    public static float getY(Entity p_114159_, float p_114160_) {
        float f = (float) p_114159_.tickCount + p_114160_;
        float f1 = Mth.sin(f * 0.2F) / 2.0F + 0.5F;
        f1 = (f1 * f1 + f1) * 0.4F;
        return f1 - 1.4F;
    }
}
