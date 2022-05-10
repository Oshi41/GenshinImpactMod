package com.gim.capability.shield;

import com.gim.registry.Elementals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;

public class ShieldCapability implements IShield {
    private double hp;
    private Elementals elemental;
    private int ticks;

    @Override
    public double getHp() {
        return hp;
    }

    @Override
    public Elementals getElement() {
        return elemental;
    }

    @Override
    public boolean isAvailable() {
        return ticks > 0 && elemental != null && getHp() > 0;
    }

    @Override
    public void setShield(double hp, Elementals elemental, int ticks) {
        this.hp = hp;
        this.elemental = elemental;
        this.ticks = ticks;
    }

    @Override
    public void damageShield(double amount, DamageSource source) {
        this.hp -= amount;
    }

    @Override
    public void tick() {
        ticks = Math.max(0, ticks - 1);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Ticks", ticks);
        tag.putDouble("HP", getHp());
        if (getElement() != null) {
            tag.putString("Elemental", getElement().name());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        Elementals e = null;
        try {
            e = Enum.valueOf(Elementals.class, nbt.getString("Elemental"));
        } catch (Exception ex) {

        }
        setShield(
                nbt.getInt("HP"),
                e,
                nbt.getInt("Ticks"));
    }
}

