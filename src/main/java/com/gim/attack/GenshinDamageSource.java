package com.gim.attack;

import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.ElementalReactions;
import com.gim.registry.Elementals;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GenshinDamageSource extends EntityDamageSource {
    private final DamageSource source;
    private boolean ignoreResistance;
    private boolean ignoreBonus;
    private IGenshinPlayer skill;
    private IGenshinPlayer burst;
    private ElementalReactions reaction;
    private Elementals element;

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
        if (Objects.equals(o, getInnerSource()) || this == o) {
            return true;
        }

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
    @Nullable
    public IGenshinPlayer skillOf() {
        return skill;
    }

    /**
     * Damage source is coming from skill attack
     */
    public GenshinDamageSource bySkill(IGenshinPlayer player) {
        this.skill = player;
        return this;
    }

    /**
     * Current damage source came from burst attack
     */
    @Nullable
    public IGenshinPlayer burstOf() {
        return burst;
    }

    /**
     * damage source is coming from burst attack
     */
    public GenshinDamageSource byBurst(IGenshinPlayer player) {
        this.burst = player;
        return this;
    }

    /**
     * Elemental reaction for this
     *
     * @return
     */
    @Nullable
    public ElementalReactions possibleReaction() {
        return reaction;
    }

    /**
     * Damage source was caused by current elemental reaction
     *
     * @param reaction - elemental reaction
     */
    public GenshinDamageSource byElementalReaction(ElementalReactions reaction) {
        this.reaction = reaction;
        return this;
    }

    /**
     * Returns possible damaging element
     */
    public Elementals getElement() {
        return element;
    }

    /**
     * Set current damagin element
     *
     * @param element - current element
     */
    public GenshinDamageSource withElement(Elementals element) {
        this.element = element;
        return this;
    }
}
