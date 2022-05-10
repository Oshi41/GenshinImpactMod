package com.gim.capability.attack;

import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IAttackTracker {

    /**
     * Should be a weak link!
     * @return current mob combat tracker
     */
    @Nullable CombatTracker getCombatTracker();

    /**
     * List of attacks performing by current mob
     */
    List<CombatEntry> attacks();

    default void recordAttack(DamageSource source, float health, float damage) {
        if (source == null || getCombatTracker() == null){
            return;
        }

        CombatTracker tracker = getCombatTracker();


    }
}
