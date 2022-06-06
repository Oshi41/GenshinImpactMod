package com.gim.capability.shield;

import com.gim.registry.Attributes;
import com.gim.registry.Elementals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public interface IShield extends INBTSerializable<CompoundTag> {

    /**
     * Amount of shield
     *
     * @return
     */
    double getHp();

    /**
     * How effectively shield absorbs damage
     */
    double getEffectivity();

    /**
     * shield element
     *
     * @return
     */
    @Nullable Elementals getElement();

    /**
     * Is current shield active and can handle damage
     *
     * @return
     */
    boolean isAvailable();

    /**
     * Applying new shield to entity
     *
     * @param hp        - HP of shield
     * @param elemental - current shield elemental
     * @param ticks     - ticks duration
     */
    void setShield(double hp, double effectivity, Elementals elemental, int ticks);

    /**
     * damage shield
     *
     * @param amount - amount of damage
     * @param source - damage source
     */
    void damageShield(double amount, DamageSource source);

    /**
     * Called every LivingEntity.tick
     */
    void tick();

    /**
     * Accept damage be shield
     *
     * @return - amount of damage applied to entity
     */
    default float acceptDamage(LivingEntity victim, DamageSource source, float amount) {
        // can't apply shield
        if (!isAvailable())
            return amount;

        double strength = 0;
        double bonus = getEffectivity();

        // actual shield strength of entity
        AttributeInstance instance = victim.getAttribute(Attributes.shield_strength);
        if (instance != null) {
            strength *= instance.getValue();
        }

        // bonus for current element
        if (getElement() != null && getElement().is(source)) {
            bonus += 2.5;
        }

        amount = (float) (amount / bonus / (1 + strength));
        damageShield(amount, source);

        // attack was to strong / shield was too weak
        if (getHp() < 0) {
            return (float) (-getHp() * strength * bonus);
        } else {
            // handle all damage
            return 0;
        }
    }
}
