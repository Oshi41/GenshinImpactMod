package com.gim.attack;

import com.gim.registry.Effects;
import com.gim.registry.ParticleTypes;
import com.google.common.collect.Iterators;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Stream;

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
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        super.applyEffectTick(livingEntity, amplifier);

        if (livingEntity.getLevel().isClientSide() && livingEntity.tickCount % 10 == 0) {
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
                spawnCircleEffects(livingEntity, ParticleTypes.FROZEN, 0.3);
            }

            if (this == Effects.DEFENCE_DEBUFF) {
                spawnCircleEffects(livingEntity, ParticleTypes.DEFENCE_DEBUFF, Math.PI * 2 / amplifier);
            }
        }

        if (this == Effects.FROZEN) {
            livingEntity.setDeltaMovement(Vec3.ZERO);
            livingEntity.setLastHurtMob(null);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
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

    private void spawnCircleEffects(LivingEntity e, ParticleOptions particle, double step) {
        int index = 0;

        Map<MobEffect, MobEffectInstance> map = e.getActiveEffectsMap();
        if (map.size() > 1) {
            index = Iterators.indexOf(map.keySet().iterator(), x -> x == this);
        }

        Vec3 movement = e.getDeltaMovement();
        double radius = e.getBbWidth();
        float yPos = (float) (e.getY() + (e.getBbHeight() / 2) + index * step);

        for (double t = e.getRandom().nextDouble() * step; t < 2 * Math.PI; t += step) {
            e.getLevel().addParticle(
                    particle,
                    e.getX() + (radius * Math.cos(t)),
                    yPos + 1.0F,
                    e.getZ() + (radius * Math.sin(t)),
                    movement.x,
                    movement.y,
                    movement.z);
        }
    }
}
