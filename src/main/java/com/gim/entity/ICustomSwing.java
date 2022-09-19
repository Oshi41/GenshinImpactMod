package com.gim.entity;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeEntity;

public interface ICustomSwing extends IForgeEntity {
    private LivingEntity self() {
        return (LivingEntity) this;
    }

    int getMaxSwingTime();

    /**
     * Public fixed version of
     *
     * @see LivingEntity
     * getCurrentSwingDuration()
     */
    default int getCurrentSwingDuration() {
        LivingEntity self = self();

        if (MobEffectUtil.hasDigSpeed(self)) {
            return getMaxSwingTime() - (1 + MobEffectUtil.getDigSpeedAmplification(self));
        } else {
            return self.hasEffect(MobEffects.DIG_SLOWDOWN)
                    ? getMaxSwingTime() + (1 + self.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2
                    : getMaxSwingTime();
        }
    }

    /**
     * Fixed version of
     *
     * @see LivingEntity
     * updateSwingTime()
     */
    default void updateSwingTimeFixed() {
        LivingEntity self = self();

        int i = getCurrentSwingDuration();
        if (self.swinging) {
            ++self.swingTime;
            if (self.swingTime >= i) {
                self.swingTime = 0;
                self.swinging = false;
            }
        } else {
            self.swingTime = 0;
        }

        self.attackAnim = (float) self.swingTime / (float) i;
    }

    /**
     * Fixed version of
     *
     * @see LivingEntity#swing(InteractionHand, boolean)
     */
    default void swingFixed(InteractionHand p_21012_, boolean p_21013_) {
        LivingEntity self = self();

        ItemStack stack = self.getItemInHand(p_21012_);
        if (!stack.isEmpty() && stack.onEntitySwing(self)) return;

        if (!self.swinging || self.swingTime >= getCurrentSwingDuration() / 2 || self.swingTime < 0) {
            self.swingTime = -1;
            self.swinging = true;
            self.swingingArm = p_21012_;
            if (self.level instanceof ServerLevel) {
                ClientboundAnimatePacket clientboundanimatepacket = new ClientboundAnimatePacket(self, p_21012_ == InteractionHand.MAIN_HAND ? 0 : 3);
                ServerChunkCache serverchunkcache = ((ServerLevel) self.level).getChunkSource();
                if (p_21013_) {
                    serverchunkcache.broadcastAndSend(self, clientboundanimatepacket);
                } else {
                    serverchunkcache.broadcast(self, clientboundanimatepacket);
                }
            }
        }
    }


    /////////////////////////////////////
    // Methods below must be implemented
    //    @Override
    //    public void updateSwingTime() {
    //        this.updateSwingTimeFixed();
    //    }
    //
    //    @Override
    //    public void swing(InteractionHand hand, boolean p_21013_) {
    //        this.swingFixed(hand, p_21013_);
    //    }
    /////////////////////////////////////


    void updateSwingTime();
    void swing(InteractionHand hand, boolean flag);
}
