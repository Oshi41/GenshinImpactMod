package com.gim.attack;

import com.gim.GenshinHeler;
import com.gim.registry.Effects;
import com.gim.registry.Elementals;
import com.gim.registry.ParticleTypes;
import com.google.common.collect.Iterators;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.Random;

public class GenshinMobEffect extends MobEffect {
    private boolean pureElemental;

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

    // region Tick

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        super.applyEffectTick(livingEntity, amplifier);

        if (livingEntity.getLevel().isClientSide()) {
            if (livingEntity.tickCount % 5 == 0) {
                fifthClientTick(livingEntity, amplifier);
            }
        } else {
            serverTick(livingEntity, amplifier);
        }
    }

    // Server side
    public void endEffect(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.getLevel().isClientSide()) {
            ((ServerLevel) livingEntity.getLevel()).getServer().getPlayerList().broadcast(
                    null,
                    livingEntity.getX(),
                    livingEntity.getY(),
                    livingEntity.getZ(),
                    32,
                    livingEntity.getLevel().dimension(),
                    new ClientboundRemoveMobEffectPacket(livingEntity.getId(), this)
            );
        }
    }

    /**
     * Perform particle spawn here
     * Calls every 5-th client tick
     */
    @OnlyIn(Dist.CLIENT)
    private void fifthClientTick(LivingEntity livingEntity, int amplifier) {
        if (this.equals(Elementals.ANEMO.getEffect())) {
            spawnElementalEffects(livingEntity, ParticleTypes.ANEMO);
        }

        if (this.equals(Elementals.HYDRO.getEffect())) {
            spawnElementalEffects(livingEntity, ParticleTypes.HYDRO);
        }

        if (this.equals(Elementals.PYRO.getEffect())) {
            spawnElementalEffects(livingEntity, ParticleTypes.PYRO);
        }

        if (this.equals(Elementals.CRYO.getEffect())) {
            spawnElementalEffects(livingEntity, ParticleTypes.CRYO);
        }

        if (this.equals(Elementals.DENDRO.getEffect())) {
            spawnElementalEffects(livingEntity, ParticleTypes.DENDRO);
        }

        if (this.equals(Elementals.GEO.getEffect())) {
            spawnElementalEffects(livingEntity, ParticleTypes.GEO);
        }

        if (this.equals(Elementals.ELECTRO.getEffect())) {
            spawnElementalEffects(livingEntity, ParticleTypes.ELECTRO);
        }

        if (this.equals(Elementals.ELECTROCHARGED.getEffect())) {
            spawnElementalEffects(livingEntity, ParticleTypes.LIGHTNING);
        }

        if (this.equals(Elementals.BURNING.getEffect())) {
            // hidden effect means dendro damage (i know not good but it's working!)
            ParticleOptions particleType = livingEntity.getEffect(Elementals.BURNING.getEffect()).save(new CompoundTag()).contains("HiddenEffect")
                    ? ParticleTypes.DENDRO
                    : ParticleTypes.PYRO;

            spawnElementalEffects(livingEntity, particleType);
        }

        // handle electro changed particles
        if (this.equals(Elementals.HYDRO.getEffect()) && Elementals.ELECTRO.is(livingEntity)
                ||
                (this.equals(Elementals.ELECTRO.getEffect()) && Elementals.HYDRO.is(livingEntity))) {
            Random random = livingEntity.getRandom();

            for (int j = 0; j < 16; ++j) {
                double d0 = (double) j / 127.0D;
                float f = (random.nextFloat() - 0.5F) * 0.2F;
                float f1 = (random.nextFloat() - 0.5F) * 0.2F;
                float f2 = (random.nextFloat() - 0.5F) * 0.2F;
                double d1 = Mth.lerp(d0, livingEntity.xo, livingEntity.getX()) + (random.nextDouble() - 0.5D) * (double) livingEntity.getBbWidth() * 2.0D;
                double d2 = Mth.lerp(d0, livingEntity.yo, livingEntity.getY()) + random.nextDouble() * (double) livingEntity.getBbHeight();
                double d3 = Mth.lerp(d0, livingEntity.zo, livingEntity.getZ()) + (random.nextDouble() - 0.5D) * (double) livingEntity.getBbWidth() * 2.0D;
                livingEntity.getLevel().addParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, d1, d2, d3, f, f1, f2);
            }
        }

        if (this.equals(Effects.DEFENCE_DEBUFF)) {
            spawnCircleEffects(livingEntity, ParticleTypes.DEFENCE_DEBUFF, Math.PI * 2 / amplifier);
        }
    }

    /**
     * Perform server logic here
     */
    private void serverTick(LivingEntity livingEntity, int amplifier) {
        if (this.equals(Elementals.FROZEN.getEffect())) {
            GenshinHeler.addEffect(livingEntity, new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 10, 5));
            GenshinHeler.addEffect(livingEntity, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 5));
            GenshinHeler.addEffect(livingEntity, new MobEffectInstance(MobEffects.WEAKNESS, 10, 5));
        }

        if (this.equals(Elementals.ELECTROCHARGED.getEffect())) {
            // damage every second
            if (livingEntity.tickCount % 20 == 0) {
                AABB findArea = livingEntity.getBoundingBox()
                        .expandTowards(-3, -1, -3)
                        .expandTowards(3, 1, 3);

                for (LivingEntity entity : livingEntity.getLevel().getEntities(EntityTypeTest.forClass(LivingEntity.class),
                        findArea, Elementals.HYDRO::is)) {
                    entity.hurt(new GenshinDamageSource(Elementals.SUPERCONDUCT.create(null), livingEntity), amplifier + 1);
                }
            }
        }

        if (this.equals(Elementals.BURNING.getEffect())) {
            // damage every second
            if (livingEntity.tickCount % 20 == 0) {
                MobEffectInstance instance = livingEntity.getEffect(Elementals.BURNING.getEffect());
                if (instance != null) {
                    // hidden effect means dendro damage (i know not good but it's working!)
                    Elementals e = instance.save(new CompoundTag()).contains("HiddenEffect")
                            ? Elementals.DENDRO
                            : Elementals.PYRO;

                    livingEntity.hurt(e.create(null), amplifier);
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    private void spawnElementalEffects(LivingEntity e, ParticleOptions particle) {
        Vec3 movement = e.getDeltaMovement();
        float yPos = (float) Mth.floor(e.getY());

        for (int i = 0; (float) i < 1.0F + e.getBbWidth() * 10.0F; ++i) {
            double d0 = (e.getRandom().nextDouble() * 2.0D - 1.0D) * (double) e.getBbWidth();
            double d1 = (e.getRandom().nextDouble() * 2.0D - 1.0D) * (double) e.getBbWidth();
            e.getLevel().addParticle(particle, e.getX() + d0, yPos + 1.0F, e.getZ() + d1, movement.x, movement.y - e.getRandom().nextDouble() * (double) 0.2F, movement.z);
        }
    }

    private void spawnCircleEffects(LivingEntity e, ParticleOptions particle, double step) {
        int index = 0;

        Map<MobEffect, MobEffectInstance> map = e.getActiveEffectsMap();
        if (map.size() > 1) {
            index = Iterators.indexOf(map.keySet().iterator(), x -> x.equals(this));
        }

        Vec3 movement = e.getDeltaMovement();
        double radius = e.getBbWidth();
        float yPos = (float) (e.getY() + (e.getBbHeight() / 2) + index * 0.3);

        for (double t = 0.1 * (e.tickCount % step); t < 2 * Math.PI; t += step) {
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

    // endregion
}
