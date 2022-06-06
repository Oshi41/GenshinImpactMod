package com.gim.capability.shield;

import com.gim.GenshinHeler;
import com.gim.registry.Elementals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class ShieldCapability implements IShield {
    private double hp;
    private Elementals elemental;
    private int ticks;
    private double effectivity;

    @Override
    public double getHp() {
        return hp;
    }

    @Override
    public double getEffectivity() {
        return effectivity;
    }

    @Override
    public Elementals getElement() {
        return elemental;
    }

    @Override
    public boolean isAvailable() {
        return ticks > 0 && getElement() != null && getHp() > 0 && getEffectivity() > 0;
    }

    @Override
    public void setShield(double hp, double effectivity, Elementals elemental, int ticks) {
        this.hp = hp;
        this.effectivity = effectivity;
        this.elemental = elemental;
        this.ticks = ticks;
    }


    @Override
    public void damageShield(double amount, DamageSource source) {
        this.hp -= amount;
    }

    @Override
    public void tick() {
        if (isAvailable()) {
            ticks--;
        }
    }

    @Override
    public float acceptDamage(LivingEntity victim, DamageSource source, float amount) {
        return IShield.super.acceptDamage(victim, source, amount);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Ticks", ticks);
        tag.putDouble("HP", getHp());
        tag.putDouble("Effectivity", getEffectivity());
        if (getElement() != null) {
            tag.putString("Elemental", getElement().name());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.hp = nbt.getDouble("HP");
        this.ticks = nbt.getInt("Ticks");
        this.effectivity = nbt.getDouble("Effectivity");
        this.elemental = GenshinHeler.safeGet(Elementals.class, nbt.getString("Elemental"));
    }

}

