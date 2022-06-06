package com.gim.attack;

import com.google.common.collect.Lists;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Combat tracker that records attacks too
 */
public class GenshinCombatTracker extends CombatTracker {
    /**
     * Max duration to store attacks history
     */
    public static final int MAX_ATTACK_HISTORY_DURATION = 20 * 60;
    private final List<CombatEntry> attacks = Lists.newArrayList();

    // called from coremod modification
    public GenshinCombatTracker(LivingEntity entity) {
        super(entity);
    }

    @Override
    public void recordDamage(DamageSource source, float damage, float health) {
        super.recordDamage(source, damage, health);

        if (source.getEntity() instanceof LivingEntity) {
            CombatTracker combatTracker = ((LivingEntity) source.getEntity()).getCombatTracker();
            if (combatTracker instanceof GenshinCombatTracker) {
                ((GenshinCombatTracker) combatTracker).recordAttack(source, damage, health);
            }
        }
    }

    public void recordAttack(DamageSource source, float damage, float health) {
        CombatEntry combatentry = new CombatEntry(source, getMob().tickCount, damage, health, null, getMob().fallDistance);
        attacks.add(combatentry);
    }

    public void removeAttacks(Predicate<GenshinDamageSource> condition) {
        attacks.removeIf(x -> x.getSource() instanceof GenshinDamageSource && condition.test((GenshinDamageSource) x.getSource()));
    }


    /**
     * Find first attack record by curernt condition
     *
     * @param condition - condition for all GenshinDamageSource sources
     */
    @Nullable
    public CombatEntry findFirstAttack(Predicate<GenshinDamageSource> condition) {
        if (condition != null && !attacks.isEmpty()) {
            CombatEntry entry = attacks.stream().filter(x -> x.getSource() instanceof GenshinDamageSource && condition.test((GenshinDamageSource) x.getSource()))
                    .min(Comparator.comparingInt(CombatEntry::getTime))
                    .orElse(null);

            return entry;
        }

        return null;
    }

    @Override
    public void recheckStatus() {
        super.recheckStatus();

        // max time for storing attacks is minute
        attacks.removeIf(x -> getMob().tickCount - x.getTime() > MAX_ATTACK_HISTORY_DURATION);
    }
}
