package com.gim.client.entity.players.anemo_traveler;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.AnemoTraveler;
import com.gim.registry.Capabilities;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
class AnemoTravelerModel extends PlayerModel<AbstractClientPlayer> {
    public int skillTicks;
    public int burstTicks;

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
