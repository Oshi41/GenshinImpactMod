package com.gim.attack;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;

import java.util.Objects;

public class GenshinDamageSource extends EntityDamageSource {
    private final DamageSource source;
    private boolean ignoreResistance;
    private boolean ignoreBonus;
    private boolean skill;
    private boolean burst;

    public GenshinDamageSource(DamageSource source, Entity entity) {
        super(source.getMsgId(), entity);
        this.source = source;
    }

    public DamageSource getInnerSource() {
        return source;
    }

    /**
     * Damage source must ignore resistance
     *
     * @return this
     */
    public GenshinDamageSource ignoreResistance() {
        ignoreResistance = true;
        return this;
    }

    /**
     * Damage source must ignore attacker's elemental bonus
     *
     * @return this
     */
    public GenshinDamageSource ignoreElementalBonus() {
        ignoreBonus = true;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (Objects.equals(o, getInnerSource()) || this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        GenshinDamageSource that = (GenshinDamageSource) o;
        return Objects.equals(getInnerSource(), that.getInnerSource());
    }

    @Override
    public int hashCode() {
        return getInnerSource().hashCode();
    }

    /**
     * Should current damage source totally ignore victim resistance?
     */
    public boolean shouldIgnoreResistance() {
        return ignoreResistance;
    }

    /**
     * Should current damage source totally ignore attacker's elemetnal bonus?
     */
    public boolean shouldIgnoreBonus() {
        return ignoreBonus;
    }

    /**
     * Current damage source came from skill attack
     */
    public boolean isSkill() {
        return skill;
    }

    /**
     * Damage source is coming from skill attack
     */
    public GenshinDamageSource bySkill() {
        this.skill = true;
        return this;
    }

    /**
     * Current damage source came from burst attack
     */
    public boolean isBurst() {
        return burst;
    }

    /**
     * damage source is coming from burst attack
     */
    public GenshinDamageSource byBurst() {
        this.burst = true;
        return this;
    }
}
