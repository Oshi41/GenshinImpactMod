package com.gim.attack;

import com.gim.registry.Effects;
import com.gim.registry.ParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class GenshinMobEffect extends MobEffect {
    private boolean pureElemental;

    private boolean elementalReaction;

    public GenshinMobEffect(MobEffectCategory effectCategory, int color) {
        super(effectCategory, color);
    }

    public boolean isPureElemental() {
        return pureElemental;
    }

    public GenshinMobEffect setPureElemental(boolean pureElemental) {
        this.pureElemental = pureElemental;
        return this;
    }

    public boolean isElementalReaction() {
        return elementalReaction;
    }

    public GenshinMobEffect setElementalReaction(boolean elementalReaction) {
        this.elementalReaction = elementalReaction;
        return this;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int p_19468_) {
        super.applyEffectTick(livingEntity, p_19468_);

        if (this == Effects.ANEMO) {
            spawnElementalEffects(livingEntity, ParticleTypes.ANEMO);
        }

        if (this == Effects.HYDRO) {
            spawnElementalEffects(livingEntity, ParticleTypes.HYDRO);
        }

        if (this == Effects.PYRO) {
            spawnElementalEffects(livingEntity, ParticleTypes.PYRO);
        }

        if (this == Effects.CRYO) {
            spawnElementalEffects(livingEntity, ParticleTypes.CRYO);
        }

        if (this == Effects.DENDRO) {
            spawnElementalEffects(livingEntity, ParticleTypes.DENDRO);
        }

        if (this == Effects.GEO) {
            spawnElementalEffects(livingEntity, ParticleTypes.GEO);
        }

        if (this == Effects.ELECTRO) {
            spawnElementalEffects(livingEntity, ParticleTypes.ELECTRO);
        }

        if (this == Effects.FROZEN) {
            livingEntity.setTicksFrozen(15);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        if (super.isDurationEffectTick(duration, amplifier)) {
            return true;
        }

        if (isPureElemental()) {
            return duration % 10 == 0;
        }

        return false;
    }

    private void spawnElementalEffects(LivingEntity e, ParticleOptions particle) {

        Vec3 movement = e.getDeltaMovement();
        float yPos = (float) Mth.floor(e.getY());


        for (int i = 0; (float) i < 1.0F + e.getBbWidth() * 20.0F; ++i) {
            double d0 = (e.getRandom().nextDouble() * 2.0D - 1.0D) * (double) e.getBbWidth();
            double d1 = (e.getRandom().nextDouble() * 2.0D - 1.0D) * (double) e.getBbWidth();
            e.getLevel().addParticle(particle, e.getX() + d0, yPos + 1.0F, e.getZ() + d1, movement.x, movement.y - e.getRandom().nextDouble() * (double) 0.2F, movement.z);
        }
    }
}
