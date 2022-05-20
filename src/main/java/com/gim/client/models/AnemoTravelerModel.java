package com.gim.client.models;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.registry.Capabilities;
import com.gim.registry.GenshinModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnemoTravelerModel<T extends LivingEntity> extends PlayerModel<T> {
    private final ModelPart skillVortex;
    public int isSkill;
    public int isBurst;

    public TextureAtlasSprite sprite;

    public AnemoTravelerModel() {
        super(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
        ModelPart modelPart = Minecraft.getInstance().getEntityModels().bakeLayer(GenshinModelLayers.ANEMO_TRAVELER_LAYER);
        this.skillVortex = modelPart.getChild("vortex");
    }

    public static MeshDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("vortex",
                CubeListBuilder.create().addBox(1, 1, 1, 1, 1, 1),
                PartPose.ZERO);

        return meshdefinition;
    }

    @Override
    public void prepareMobModel(T entity, float p_102862_, float p_102863_, float p_102864_) {
        super.prepareMobModel(entity, p_102862_, p_102863_, p_102864_);

        entity.getCapability(Capabilities.GENSHIN_INFO).ifPresent(info -> {
            GenshinEntityData data = info.getPersonInfo(info.current());
            if (data != null) {
                isSkill = data.getSkillTicksAnim();
                isBurst = data.getBurstTicksAnim();

                sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(info.current().getSkillIcon());
            }
        });
    }

    @Override
    public void setupAnim(T p_103395_, float p_103396_, float p_103397_, float p_103398_, float p_103399_, float p_103400_) {
        super.setupAnim(p_103395_, p_103396_, p_103397_, p_103398_, p_103399_, p_103400_);

        if (isSkill > 0) {
            leftArm.xRot = rightArm.xRot = 5.2f;
            this.rightArm.zRot = -1;
            this.leftArm.zRot = 1;
        }

        if (isBurst > 0) {
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer p_102035_, int p_102036_, int p_102037_, float p_102038_, float p_102039_, float p_102040_, float p_102041_) {
        super.renderToBuffer(poseStack, p_102035_, p_102036_, p_102037_, p_102038_, p_102039_, p_102040_, p_102041_);

        if (isSkill > 0) {
            // GuiComponent.blit(poseStack, x, y, 0, 0, 0, width, height, width, height * sprite.getFrameCount());
            //skillVortex.render(poseStack, p_102035_, p_102036_, p_102037_, p_102038_, p_102039_, p_102040_, p_102041_);
        }
    }
}
