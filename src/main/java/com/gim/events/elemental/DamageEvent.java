package com.gim.events.elemental;

import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinAreaSpreading;
import com.gim.entity.Shield;
import com.gim.registry.Attributes;
import com.gim.registry.Effects;
import com.gim.registry.Elementals;
import com.google.common.collect.Streams;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.gim.GenshinHeler.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageEvent {

    // region APPLYING ELEMENTAL EFFECTS

    /**
     * Applying potion effect BEFORE actual damage
     * !!!ONLY SERVER SIDE!!!
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void applyElementalStatus(LivingDamageEvent event) {
        // some null checking
        if (event.getEntityLiving() == null
                || event.getSource() == null
                || event.isCanceled()
                // performing only on client side
                || event.getEntityLiving().getLevel().isClientSide()) {
            return;
        }

        // iterating through all possible elementals
        for (Elementals elemental : Elementals.values()) {
            // if damage source belongs current elemental
            if (elemental.is(event.getSource())) {
                // effect should not be applied to entity
                if (!elemental.is(event.getEntityLiving())) {
                    // applying current mob effect
                    addEffect(event.getEntityLiving(), new MobEffectInstance(elemental.getEffect(), 20 * 10));
                }

                return;
            }
        }
    }

    /**
     * Applying wet status with water/rain interract
     *
     * @param e
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onUpdate(LivingEvent.LivingUpdateEvent e) {
        if (e.getEntityLiving() == null) {
            return;
        }

        if (!e.getEntityLiving().getLevel().isClientSide() && !e.getEntityLiving().hasEffect(Elementals.HYDRO.getEffect()) && e.getEntityLiving().isInWaterOrRain()) {
            addEffect(e.getEntityLiving(), new MobEffectInstance(Elementals.HYDRO.getEffect(), 10 * 20));
            applyElementalReactions(new LivingDamageEvent(e.getEntityLiving(), Elementals.HYDRO.create(), Float.MIN_NORMAL));
        }
    }

    // endregion

    // region APPLYING ELEMENTAL REACTIONS

    /**
     * !!!ONLY SERVER SIDE!!!
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyElementalReactions(LivingDamageEvent e) {
        if (e.getSource() == null || e.getEntityLiving() == null || e.isCanceled())
            return;

        if (handleBonusAttack(e)) {
            GenshinImpactMod.LOGGER.debug("attack bonus applied");
        }

        if (handleOverload(e.getEntityLiving(), e.getSource())) {
            setAdditiveDamage(e, 2);
            GenshinImpactMod.LOGGER.debug("overload applied");
        }

        if (handleFrozen(e.getEntityLiving(), e.getSource())) {
            e.setAmount(getActualDamage(e.getEntityLiving(), e.getSource(), e.getAmount()));
            GenshinImpactMod.LOGGER.debug("frozen applied");
        }

        if (handleVaporize(e.getEntityLiving(), e.getSource())) {
            setAdditiveDamage(e, Elementals.HYDRO.is(e.getSource()) ? 2 : 1.5f);
            GenshinImpactMod.LOGGER.debug(Elementals.HYDRO.is(e.getSource()) ? "" : "reverse " + "vaporize applied");
        }

        if (handleSuperconduct(e.getEntityLiving(), e.getSource())) {
            setAdditiveDamage(e, 0.5f);
            GenshinImpactMod.LOGGER.debug("superconduct applied");
        }

        if (handleSwirl(e.getEntityLiving(), e.getSource())) {
            setAdditiveDamage(e, 0.6f);
            GenshinImpactMod.LOGGER.debug("swirl applied");
        }

        if (handleElectroCharged(e.getEntityLiving(), e.getSource())) {
            GenshinImpactMod.LOGGER.debug("electro-charged applied");
        }

        if (handleBurning(e.getEntityLiving(), e.getSource())) {
            setAdditiveDamage(e, 0.25f);
            GenshinImpactMod.LOGGER.debug("burning applied");
        }

        if (handleCrystalize(e.getEntityLiving(), e.getSource())) {
            GenshinImpactMod.LOGGER.debug("crystal applied");
        }

        if (handleMelt(e.getEntityLiving(), e.getSource())) {
            setAdditiveDamage(e, Elementals.PYRO.is(e.getSource()) ? 2 : 1.5f);
            GenshinImpactMod.LOGGER.debug(Elementals.PYRO.is(e.getSource()) ? "" : "reverse " + "melt applied");
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyCritHit(CriticalHitEvent e) {
        LivingEntity entity = e.getEntityLiving();
        if (entity == null || e.isCanceled())
            return;

        AttributeInstance instance = entity.getAttribute(Attributes.crit_rate);
        // if no attr provided or random number is bigger then crit rate
        if (instance == null || entity.getRandom().nextFloat() > instance.getValue())
            return;

        instance = entity.getAttribute(Attributes.crit_bonus);
        if (instance == null)
            return;

        e.setDamageModifier((float) (e.getDamageModifier() * instance.getValue()));
        GenshinImpactMod.LOGGER.debug("applied crit hit by Genshin Impact Mod");
    }

    // endregion

    // region HANDLING REACTIONS

    private static boolean handleOverload(LivingEntity entity, DamageSource source) {
        boolean canHandle = canApply(entity, source, Elementals.ELECTRO, Elementals.PYRO);

        if (canHandle) {
            explode(entity, source.getEntity(), DamageSource.ON_FIRE, Effects.ELECTRO);
        }

        return canHandle;
    }

    private static boolean handleFrozen(LivingEntity entity, DamageSource source) {
        boolean canHandle = canApply(entity, source, Elementals.HYDRO, Elementals.CRYO);

        if (canHandle) {
            int ticks = (int) (20 * 25 * (1 + majestyBonus(source.getEntity())));

            // removing both effects
            removeEffect(entity, Effects.HYDRO);
            removeEffect(entity, Effects.CRYO);
            // adding frozen effect
            addEffect(entity, new MobEffectInstance(Effects.FROZEN, ticks));
        }

        return canHandle;
    }

    private static boolean handleVaporize(LivingEntity entity, DamageSource source) {
        boolean canHandle = canApply(entity, source, Elementals.HYDRO, Elementals.PYRO);

        if (canHandle) {
            Elementals current, other;

            if (Elementals.PYRO.is(source)) {
                current = Elementals.PYRO;
                other = Elementals.HYDRO;
            } else {
                other = Elementals.PYRO;
                current = Elementals.HYDRO;
            }

            explode(entity,
                    source.getEntity(),
                    current.create(source.getEntity()),
                    other.getEffect());
        }

        return canHandle;
    }

    private static boolean handleSuperconduct(LivingEntity entity, DamageSource source) {
        boolean canHandle = canApply(entity, source, Elementals.ELECTRO, Elementals.CRYO, Elementals.FROZEN);

        if (canHandle) {
            explode(entity, source.getEntity(), Elementals.SUPERCONDUCT.create(source.getEntity()), Effects.ELECTRO, Effects.CRYO);
        }

        return canHandle;
    }

    private static final Lazy<Elementals[]> swirlElements = Lazy.of(() -> new Elementals[]{Elementals.PYRO, Elementals.HYDRO, Elementals.ELECTRO, Elementals.CRYO, Elementals.FROZEN});

    private static boolean handleSwirl(LivingEntity entity, DamageSource source) {

        if (canApply(entity, source, Elementals.ANEMO, swirlElements.get())) {
            // anemo uses other elemental statuses
            if (Elementals.ANEMO.is(source)) {
                Elementals playerElemental = Arrays.stream(swirlElements.get()).filter(x -> x.is(entity)).findFirst().orElse(null);
                source = playerElemental.create(source.getEntity());
            }

            explode(entity, source.getEntity(), source, Effects.ANEMO);
            return true;
        }

        return false;
    }

    private static boolean handleElectroCharged(LivingEntity entity, DamageSource source) {
        return canApply(entity, source, Elementals.ELECTRO, Elementals.HYDRO);
    }

    private static boolean handleBurning(LivingEntity entity, DamageSource source) {
        if (canApply(entity, source, Elementals.DENDRO, Elementals.PYRO)) {
            removeEffect(entity, Effects.DENDRO);
            removeEffect(entity, Effects.PYRO);

            int seconds = (int) (10 * (1 + majestyBonus(source.getEntity())));
            entity.setSecondsOnFire(seconds);

            return true;
        }

        return false;
    }

    private static final Lazy<Elementals[]> crystalElements = Lazy.of(() -> new Elementals[]{Elementals.PYRO, Elementals.HYDRO, Elementals.ELECTRO, Elementals.CRYO});

    private static boolean handleCrystalize(LivingEntity entity, DamageSource source) {
        if (canApply(entity, source, Elementals.GEO, crystalElements.get())) {
            Elementals onEntity = Elementals.GEO;
            Elementals onSource = Elementals.GEO;

            // crystals element with GEO elements
            for (Elementals e : Streams.concat(Arrays.stream(crystalElements.get()), Stream.of(Elementals.GEO)).toList()) {

                // Take source elemental at first
                if (e.is(source)) {
                    onSource = e;
                    // than searching for element on entity
                } else if (e.is(entity)) {
                    onEntity = e;
                }
            }

            // removing both elementals
            removeEffect(entity, onEntity.getEffect());
            removeEffect(entity, onSource.getEffect());

            // calculating health for shield
            double health = 5 * (safeGetAttribute(source.getEntity(), Attributes.level) + 1);
            entity.getLevel().addFreshEntity(new Shield(entity, onEntity, (int) health));

            return true;
        }

        return false;
    }

    private static boolean handleMelt(LivingEntity entity, DamageSource source) {
        if (canApply(entity, source, Elementals.PYRO, Elementals.CRYO, Elementals.FROZEN)) {
            removeEffect(entity, Elementals.PYRO.getEffect());
            removeEffect(entity, Elementals.CRYO.getEffect());
            removeEffect(entity, Elementals.FROZEN.getEffect());

            return true;
        }

        return false;
    }

    // endregion

    // region HANDLING SIMPLE REACTIONS

    /**
     * Handle attack bonus
     *
     * @return
     */
    private static boolean handleBonusAttack(LivingDamageEvent e) {
        double bonus = 0;

        AttributeInstance instance = e.getEntityLiving().getAttribute(Attributes.attack_bonus);
        if (instance != null) {
            bonus = instance.getValue();
        }

        float actualDamage = getActualDamage(e.getEntityLiving(), e.getSource(), (float) (e.getAmount() * (1 + bonus)));

        if (actualDamage != e.getAmount()) {
            e.setAmount(actualDamage);

            // reduce all damage
            // todo think about no pushing if reducing all damage
            if (actualDamage == 0) {
                e.setCanceled(true);
            }

            return true;
        }

        return false;
    }

    // endregion

    // region Helping methods

    /**
     * Explode with current damage source according to attacker level and majesty bonus
     */
    private static void explode(Entity victim, Entity attacker, DamageSource source, MobEffect... toRemove) {
        if (victim != null && !victim.getLevel().isClientSide()) {
            // removing effect before explosion
            if (toRemove != null && toRemove.length > 0) {
                Arrays.stream(toRemove).forEach(x -> removeEffect(victim, x));
            }

            GenshinAreaSpreading areaSpreading = new GenshinAreaSpreading(attacker, victim.position(), source,
                    (float) (safeGetAttribute(attacker, Attributes.level) + (3 * majestyBonus(attacker))));
            areaSpreading.explode();
        }
    }

    private static void setAdditiveDamage(LivingDamageEvent e, float multiplier) {
        e.setAmount(getActualDamage(e.getEntityLiving(), e.getSource(),
                (float) ((e.getAmount() + safeGetAttribute(e.getSource().getEntity(), Attributes.level)) * multiplier)));
    }

    // endregion
}
