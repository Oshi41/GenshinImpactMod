package com.gim.client.entity.players.anemo_traveler;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.client.GenshinClientHooks;
import com.gim.players.AnemoTraveler;
import com.gim.players.base.GenshinPlayerBase;
import com.gim.registry.Capabilities;
import jdk.jfr.StackTrace;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
class AnemoTravelerModel extends PlayerModel<AbstractClientPlayer> {
    public int skillTicks;
    public int burstTicks;
    public int attackStage;

    public AnemoTravelerModel(ModelPart p_170821_) {
        super(p_170821_, false);
    }

    @Override
    public void prepareMobModel(AbstractClientPlayer player, float p_102862_, float p_102863_, float p_102864_) {
        super.prepareMobModel(player, p_102862_, p_102863_, p_102864_);

        skillTicks = 0;
        burstTicks = 0;
        GenshinEntityData data = getData(player);
        if (data != null) {
            skillTicks = data.getSkillTicksAnim();
            burstTicks = data.getBurstTicksAnim();

            if (data.getAdditional().contains(GenshinPlayerBase.ANIMATE_STAGE_ID) && player.swinging) {
                attackStage = data.getAdditional().getInt(GenshinPlayerBase.ANIMATE_STAGE_ID);

                if (attackStage == 4 && !this.crouching) {
                    this.crouching = true;
                }

            } else if (attackStage >= 0) {
                attackStage = -1;
            }
        }
    }

    @Override
    public void setupAnim(AbstractClientPlayer player, float animPosition, float animSpeed, float bobTicks, float yRot, float xRot) {
        super.setupAnim(player, animPosition, animSpeed, bobTicks, yRot, xRot);

        if (skillTicks > 0) {
            float xRotation = bobTicks > 0
                    ? 5.2f
                    : 0;

            float zRot = bobTicks > 0
                    ? 1
                    : 0.4f;

            if (canRotate(leftArmPose)) {
                this.leftArm.setRotation(xRotation, this.leftArm.yRot, zRot);
                this.leftSleeve.copyFrom(this.leftArm);
            }

            if (canRotate(rightArmPose)) {
                this.rightArm.setRotation(xRotation, this.rightArm.yRot, -zRot);
                this.rightSleeve.copyFrom(this.rightArm);
            }
        }

        // 3-rd face
        if (burstTicks > 0 && bobTicks == 0) {

            float rotation = ((float) Math.PI) * 3f / AnemoTraveler.BURST_ANIM_TIME * (AnemoTraveler.BURST_ANIM_TIME - burstTicks);

            if (canRotate(leftArmPose)) {
                this.leftArm.setRotation(0, 0, rotation);
                this.leftSleeve.copyFrom(this.leftArm);
            }

            if (canRotate(rightArmPose)) {
                this.rightArm.setRotation(0, 0, rotation);
                this.rightSleeve.copyFrom(this.rightArm);
            }
        }

        // TODO left-oriented arm
        switch (attackStage) {
            case 0 -> {
                GenshinClientHooks.rotatePart(player, rightArm, -26, 18, 23, -125.4f, -24, 12);
                GenshinClientHooks.rotatePart(player, leftLeg, 0, 0, 0, 45, 0.19f, -15);
                GenshinClientHooks.rotatePart(player, rightLeg, 0, 0, 0, -27, 3.5f, 12);
            }
            case 1 -> {
                GenshinClientHooks.rotatePart(player, rightArm, -115, -5, 90, -17, -5, 90);
                GenshinClientHooks.rotatePart(player, leftLeg, 0, 0, 0, 32.5f, 0, 0);
                GenshinClientHooks.rotatePart(player, rightLeg, 0, 0, 0, -40, 0, 0);
            }
            case 2 -> {
                GenshinClientHooks.rotatePart(player, rightArm, -146, 31, -21, -36, -33, -27);
                GenshinClientHooks.rotatePart(player, leftLeg, 0, 0, 0, -28.5f, 0, 0);
            }
            case 3 -> {
                GenshinClientHooks.rotatePart(player, rightArm, -116, -39, -2, -25, 6, 37);
                GenshinClientHooks.rotatePart(player, rightLeg, 0, 0, 0, -37, 0, 0);
                GenshinClientHooks.rotatePart(player, rightLeg, 0, 0, 0, 40, 0, 0);
            }
            case 4 -> {
                GenshinClientHooks.rotatePart(player, head, 0, 0, 0, 17.5f, 0, 0);
                GenshinClientHooks.rotatePart(player, rightLeg, 0, 0, 0, 27.5f, 0, 0);
                GenshinClientHooks.rotatePart(player, leftLeg, 0, 0, 0, -37.5f, 0, 0);
                GenshinClientHooks.rotatePart(player, rightArm, -115, 5, 90, 40, 7.7f, 72);
            }
        }

        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);

        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
    }

    /**
     * Checks if can rotate with current arm pose
     */
    private boolean canRotate(ArmPose armPose) {
        return armPose != ArmPose.CROSSBOW_CHARGE &&
                armPose != ArmPose.THROW_SPEAR;
    }

    /**
     * Returns data for current player
     */
    public static GenshinEntityData getData(Entity player) {
        if (player != null) {
            IGenshinInfo info = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
            if (info != null && info.current() != null) {
                return info.getPersonInfo(info.current());
            }
        }

        return null;
    }
}
