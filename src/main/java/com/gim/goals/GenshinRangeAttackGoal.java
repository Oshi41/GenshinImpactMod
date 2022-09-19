package com.gim.goals;

import com.gim.entity.IBothAttacker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraftforge.common.util.LazyOptional;

public class GenshinRangeAttackGoal extends RangedAttackGoal {
    private final LazyOptional<IBothAttacker> _attacker;
    private final Mob mob;
    private final float minRange;

    /**
     * Range attack goal
     *
     * @param mob               - current mob
     * @param speedModifier     - 1 for regular speed
     * @param attackIntervalMin - min attack interval
     * @param attackIntervalMax - max attack interval
     * @param minRange          - min attack radius
     * @param attackRadius      - max attack radius
     */
    public GenshinRangeAttackGoal(RangedAttackMob mob, double speedModifier, int attackIntervalMin,
                                  int attackIntervalMax, float minRange, float attackRadius) {
        super(mob, speedModifier, attackIntervalMin, attackIntervalMax, attackRadius);

        this.mob = (Mob) mob;

        _attacker = mob instanceof IBothAttacker
                ? LazyOptional.of(() -> (IBothAttacker) mob)
                : LazyOptional.empty();

        this.minRange = minRange;
    }

    @Override
    public void start() {
        super.start();
        _attacker.ifPresent(iBothAttacker -> iBothAttacker.setIsRangeAttacking(true));
    }

    @Override
    public void stop() {
        super.stop();
        _attacker.ifPresent(iBothAttacker -> iBothAttacker.setIsRangeAttacking(false));
    }



    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target != null && target.isAlive() && mob.distanceTo(target) >= minRange) {
            return super.canUse();
        }

        return false;
    }
}
