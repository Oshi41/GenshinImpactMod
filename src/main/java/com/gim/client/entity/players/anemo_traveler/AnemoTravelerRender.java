package com.gim.client.entity.players.anemo_traveler;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.client.overlay.GenshinRender;
import com.gim.players.AnemoTraveler;
import com.gim.players.base.GenshinPlayerBase;
import com.gim.registry.GenshinCharacters;
import com.gim.registry.Renders;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class AnemoTravelerRender extends PlayerRenderer {
    /**
     * Bool flag to indicate when we need to render offset arm
     */
    private boolean needToRenderOffsetArm;
    private static ResourceLocation SKIN = GenshinRender.getSource(GenshinCharacters.ANEMO_TRAVELER, "skin", false);
    private static ResourceLocation SKILL = GenshinRender.getSource(GenshinCharacters.ANEMO_TRAVELER, "skill", true);

    /**
     * Must be a singleton!
     */
    public AnemoTravelerRender(EntityRendererProvider.Context context) {
        super(context, false);
        model = new AnemoTravelerModel(context.bakeLayer(ModelLayers.PLAYER));

        Renders.injectLayers(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void render(AbstractClientPlayer player, float p_117789_, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int light) {
        super.render(player, p_117789_, partialTicks, poseStack, multiBufferSource, light);

        if (model().skillTicks > 0) {
            // render vortex

            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(SKILL);
            if (sprite == null)
                return;

            poseStack.pushPose();

            Vec3 vector = player.getForward().normalize().add(0, 1, 0);
            poseStack.translate(vector.x, vector.y, vector.z);

            float scale = 0.5f + (partialTicks / 5f);
            poseStack.scale(scale, scale, scale);

            poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));

            innerBlit(poseStack.last().pose(),
                    poseStack.last().normal(),
                    multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(sprite.atlas().location())),
                    light,
                    sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(),
                    1, 1, 1, 1);

            poseStack.popPose();
        }
    }

    private void innerBlit(Matrix4f p_93113_, Matrix3f matrix3f, VertexConsumer bufferbuilder, int partialTicks, float x6, float x7, float x8, float x9, float red, float green, float blue, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        bufferbuilder.vertex(p_93113_, -.5f, -.25f, 0)
                .color(red, green, blue, alpha)
                .uv(x6, x9)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(partialTicks)
                .normal(matrix3f, 0, 1, 0)
                .endVertex();

        bufferbuilder.vertex(p_93113_, 0.5f, -.25f, 0)
                .color(red, green, blue, alpha)
                .uv(x7, x9)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(partialTicks)
                .normal(matrix3f, 0, 1, 0)
                .endVertex();

        bufferbuilder.vertex(p_93113_, 0.5f, 0.75f, 0)
                .color(red, green, blue, alpha)
                .uv(x7, x8)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(partialTicks)
                .normal(matrix3f, 0, 1, 0)
                .endVertex();

        bufferbuilder.vertex(p_93113_, -0.5f, 0.75f, 0)
                .color(red, green, blue, alpha)
                .uv(x6, x8)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(partialTicks)
                .normal(matrix3f, 0, 1, 0)
                .endVertex();
    }

    /**
     * Genshin character skin texture
     */
    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer p_117783_) {
        return SKIN;
    }

    /**
     * Casted character model
     */
    private AnemoTravelerModel model() {
        return (AnemoTravelerModel) super.getModel();
    }

    /**
     * Rendering left item
     * Requesting right hand rendering if using skill
     * Render vortex here
     */
    @Override
    public void renderLeftHand(PoseStack p_117814_, MultiBufferSource p_117815_, int p_117816_, AbstractClientPlayer p_117817_) {
        model.prepareMobModel(p_117817_, 0, 0, 0);

        if (model().skillTicks > 0) {
            needToRenderOffsetArm = true;
            renderVortex(p_117814_, p_117815_, p_117816_, p_117817_);
        }

        this.renderHand(p_117814_, p_117815_, p_117816_, p_117817_, (this.model).leftArm, (this.model).leftSleeve);
    }

    @Override
    protected void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float bobTicks, float yRot, float partialTicks) {
        GenshinEntityData data = AnemoTravelerModel.getData(player);
        if (data != null) {
            if (data.getBurstTicksAnim() > 0) {
                double rotation = 360 * 3f / AnemoTraveler.BURST_ANIM_TIME * (AnemoTraveler.BURST_ANIM_TIME - data.getBurstTicksAnim());
                yRot += rotation;
            }

            // need to apply special rotation render
            if (player.swinging && data.getAdditional().contains(GenshinPlayerBase.ANIMATE_STAGE_ID)) {
                // current attack stage
                int attackStage = data.getAdditional().getInt(GenshinPlayerBase.ANIMATE_STAGE_ID);
                int rotationTickTime = 5;
                if (player.swingTime <= rotationTickTime) {
                    double rotation = 360f / (rotationTickTime + 1) * (player.swingTime + 1);

                    switch (attackStage) {
                        case 2 -> {
                            // third attack, clockwise rotation
                            yRot += rotation;
                        }
                        // fourth attack, counterclockwise rotation
                        case 3 -> {
                            yRot -= rotation;
                        }
                    }
                }
            }
        }

        super.setupRotations(player, poseStack, bobTicks, yRot, partialTicks);
    }

    /**
     * Rendering right hand
     * Requesting left hand rendering if using skill
     * Render vortex here
     */
    @Override
    public void renderRightHand(PoseStack poseStack, MultiBufferSource bufferSource, int light, AbstractClientPlayer player) {
        model().prepareMobModel(player, 0, 0, 0);

        if (model().skillTicks > 0) {
            needToRenderOffsetArm = true;
            renderVortex(poseStack, bufferSource, light, player);
        }

        this.renderHand(poseStack, bufferSource, light, player, (this.model).rightArm, (this.model).rightSleeve);
    }

    private void renderVortex(PoseStack poseStack, MultiBufferSource buffer, int light, Player player) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(SKILL);
        if (sprite == null || player == null)
            return;

        poseStack.pushPose();

        Vec3 vec3 = player.getLookAngle().reverse();
        double translation = -0.7;
        poseStack.translate(translation, 0, translation);

        float scale = .5f + (player.getRandom().nextFloat() - 0.5f) / 10f;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Quaternion.fromXYZDegrees(new Vector3f(vec3)));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(70));

        innerBlit(poseStack.last().pose(),
                poseStack.last().normal(),
                buffer.getBuffer(RenderType.entityCutoutNoCull(sprite.atlas().location())),
                light,
                sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(),
                1, 1, 1, 1);

        poseStack.popPose();
    }

    /**
     * Handling event for rendering offset hand
     */
    @SubscribeEvent
    public void onRenderOffsetArm(RenderHandEvent event) {
        if (InteractionHand.OFF_HAND.equals(event.getHand()) && needToRenderOffsetArm) {
            // turn off offset render flag
            needToRenderOffsetArm = false;

            event.getPoseStack().pushPose();
            renderPlayerArm(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(),
                    Minecraft.getInstance().player, event.getSwingProgress(), event.getEquipProgress(),
                    Minecraft.getInstance().player.getMainArm().getOpposite());
            event.getPoseStack().popPose();
        }
    }

    /**
     * Copied from ItemInHandRenderer.renderPlayerArm
     */
    private void renderPlayerArm(PoseStack poseStack, MultiBufferSource bufferSource, int p_109349_, AbstractClientPlayer player, float swingProgress, float equipProgress, HumanoidArm arm) {
        boolean rightArm = arm != HumanoidArm.LEFT;
        float f = rightArm ? 1.0F : -1.0F;
        float f1 = Mth.sqrt(equipProgress);
        float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * Mth.sin(f1 * ((float) Math.PI * 2F));
        float f4 = -0.4F * Mth.sin(equipProgress * (float) Math.PI);
        poseStack.translate(f * (f2 + 0.64000005F), f3 + -0.6F + swingProgress * -0.6F, f4 + -0.71999997F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(f * 45.0F));
        float f5 = Mth.sin(equipProgress * equipProgress * (float) Math.PI);
        float f6 = Mth.sin(f1 * (float) Math.PI);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(f * f6 * 70.0F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(f * f5 * -20.0F));
        poseStack.translate(f * -1.0F, 3.6F, 3.5D);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(f * 120.0F));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(f * -135.0F));
        poseStack.translate(f * 5.6F, 0.0D, 0.0D);
        model().prepareMobModel(player, 0, 0, 0);
        if (rightArm) {
            renderHand(poseStack, bufferSource, p_109349_, player, model().rightArm, model().rightSleeve);
        } else {
            renderHand(poseStack, bufferSource, p_109349_, player, model().leftArm, model().leftSleeve);
        }
    }

    /**
     * Render single hand
     */
    private void renderHand(PoseStack p_117776_, MultiBufferSource p_117777_, int p_117778_, AbstractClientPlayer player, ModelPart p_117780_, ModelPart p_117781_) {
        AnemoTravelerModel playermodel = model();
        setModelProperties(player);
        playermodel.attackTime = 0.0F;
        playermodel.crouching = false;
        playermodel.swimAmount = 0.0F;

        playermodel.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        p_117780_.xRot = 0.0F;
        p_117780_.render(p_117776_, p_117777_.getBuffer(RenderType.entitySolid(getTextureLocation(player))), p_117778_, OverlayTexture.NO_OVERLAY);
        p_117781_.xRot = 0.0F;
        p_117781_.render(p_117776_, p_117777_.getBuffer(RenderType.entityTranslucent(getTextureLocation(player))), p_117778_, OverlayTexture.NO_OVERLAY);
    }

    /**
     * Setting player model properties for first face view
     */
    private void setModelProperties(AbstractClientPlayer p_117819_) {
        PlayerModel<AbstractClientPlayer> playermodel = this.getModel();
        if (p_117819_.isSpectator()) {
            playermodel.setAllVisible(false);
            playermodel.head.visible = true;
            playermodel.hat.visible = true;
        } else {
            playermodel.setAllVisible(true);
            playermodel.hat.visible = p_117819_.isModelPartShown(PlayerModelPart.HAT);
            playermodel.jacket.visible = p_117819_.isModelPartShown(PlayerModelPart.JACKET);
            playermodel.leftPants.visible = p_117819_.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            playermodel.rightPants.visible = p_117819_.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            playermodel.leftSleeve.visible = p_117819_.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            playermodel.rightSleeve.visible = p_117819_.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            playermodel.crouching = p_117819_.isCrouching();
            HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(p_117819_, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(p_117819_, InteractionHand.OFF_HAND);
            if (humanoidmodel$armpose.isTwoHanded()) {
                humanoidmodel$armpose1 = p_117819_.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
            }

            if (p_117819_.getMainArm() == HumanoidArm.RIGHT) {
                playermodel.rightArmPose = humanoidmodel$armpose;
                playermodel.leftArmPose = humanoidmodel$armpose1;
            } else {
                playermodel.rightArmPose = humanoidmodel$armpose1;
                playermodel.leftArmPose = humanoidmodel$armpose;
            }
        }

    }

    private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer p_117795_, InteractionHand p_117796_) {
        ItemStack itemstack = p_117795_.getItemInHand(p_117796_);
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (p_117795_.getUsedItemHand() == p_117796_ && p_117795_.getUseItemRemainingTicks() > 0) {
                UseAnim useanim = itemstack.getUseAnimation();
                if (useanim == UseAnim.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (useanim == UseAnim.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (useanim == UseAnim.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }

                if (useanim == UseAnim.CROSSBOW && p_117796_ == p_117795_.getUsedItemHand()) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (useanim == UseAnim.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }
            } else if (!p_117795_.swinging && itemstack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemstack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            return HumanoidModel.ArmPose.ITEM;
        }
    }
}
