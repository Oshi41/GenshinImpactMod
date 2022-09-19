package com.gim.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraftforge.common.extensions.IForgeEntity;

import java.util.BitSet;
import java.util.function.Consumer;

public interface IBothAttacker extends RangedAttackMob, IForgeEntity {
    private LivingEntity self() {
        return (LivingEntity) this;
    }

    void onFlagsChanged(Consumer<BitSet> change);

    BitSet getFlags();


    /**
     * Is performing melee attack
     */
    default boolean isMeleeAttacking() {
        return getFlags().get(0) && self().attackAnim > 0;
    }

    /**
     * Is performing range attack
     */
    default boolean isRangeAttacking() {
        return getFlags().get(1) && self().attackAnim > 0;
    }

    /**
     * Setting melee flag
     */
    default void setIsMeleeAttacking(boolean attack) {
        onFlagsChanged(bitSet -> bitSet.set(0, attack));
    }

    /**
     * Setting range flag
     */
    default void setIsRangeAttacking(boolean attack) {
        onFlagsChanged(bitSet -> bitSet.set(1, attack));
    }
}
