package com.gim.client.players.anemo_traveler;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Capabilities;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

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
    public void setupAnim(AbstractClientPlayer player, float p_103396_, float p_103397_, float p_103398_, float p_103399_, float p_103400_) {
        super.setupAnim(player, p_103396_, p_103397_, p_103398_, p_103399_, p_103400_);

        if (skillTicks > 0) {
            this.leftArm.xRot = this.rightArm.xRot = 5.2f;
            this.rightArm.zRot = -1;
            this.leftArm.zRot = 1;
        }

        if (burstTicks > 0) {

        }
    }

    private GenshinEntityData getData(Player player) {
        IGenshinInfo info = player.getCapability(Capabilities.GENSHIN_INFO).orElse(null);
        return info != null
                ? info.getPersonInfo(info.current())
                : null;
    }
}
