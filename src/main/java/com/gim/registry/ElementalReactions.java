package com.gim.registry;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinAreaSpreading;
import com.gim.attack.GenshinDamageSource;
import com.gim.entity.Shield;
import com.gim.entity.TextParticle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum ElementalReactions {
    OVERLOAD("overload", ElementalReactions::handleOverload, ChatFormatting.RED),
    FROZEN("frozen", ElementalReactions::handleFrozen, ChatFormatting.AQUA),
    VAPORIZE("vaporize", ElementalReactions::handleVaporize, ChatFormatting.GOLD),
    REVERSE_VAPORIZE("reverse_vaporize", ElementalReactions::handleReverseVaporize, ChatFormatting.GOLD),
    SUPERCONDUCT("superconduct", ElementalReactions::handleSuperconduct, ChatFormatting.LIGHT_PURPLE),
    SWIRL("swirl", ElementalReactions::handleSwirl, ChatFormatting.AQUA),
    ELECTROCHARGED("electrocharged", ElementalReactions::handleElectrocharged, ChatFormatting.LIGHT_PURPLE),
    BURNING("burning", ElementalReactions::handleBurning, ChatFormatting.GOLD),
    CRYSTALIZE("crystalize", ElementalReactions::handleCrystalize, ChatFormatting.GOLD),
    MELT("melt", ElementalReactions::handleMelt, ChatFormatting.GOLD),
    REVERSE_MELT("reverse_melt", ElementalReactions::handleReverseMelt, ChatFormatting.GOLD),
    ;

    private final Predicate<LivingDamageEvent> predicate;
    public final TranslatableComponent text;

    ElementalReactions(String langKey, Predicate<LivingDamageEvent> predicate, ChatFormatting formatting) {
        this.predicate = predicate;
        this.text = new TranslatableComponent(GenshinImpactMod.ModID + ".reactions." + langKey);
        text.setStyle(text.getStyle().withColor(formatting));
    }

    /**
     * Handling elemental reaction
     *
     * @param e - damage event
     */
    public boolean handle(LivingDamageEvent e) {
        if (predicate != null && predicate.test(e)) {
            // setting current reaction
            if (e.getSource() instanceof GenshinDamageSource) {
                ((GenshinDamageSource) e.getSource()).byElementalReaction(this);
            }


            return true;
        }

        return false;
    }

    /**
     * Is current damage source from current reaction
     *
     * @param source
     * @return
     */
    public boolean is(DamageSource source) {
        return source instanceof GenshinDamageSource && this.equals(((GenshinDamageSource) source).possibleReaction());
    }

    // region REACTIONS

    /**
     * Handles overload reaction (causing explosion)
     *
     * @param e - damage event
     * @return - was overload successfull
     */
    private static boolean handleOverload(LivingDamageEvent e) {
        if (GenshinHeler.canApply(e.getEntityLiving(), e.getSource(), Elementals.ELECTRO, Elementals.PYRO)) {
            // removes effects from victim (to prevent others reactions)
            GenshinHeler.removeEffects(e.getEntityLiving(), Effects.ELECTRO);

            // reaction causes explosion with fire damage
            explode(e.getEntityLiving(), e.getSource().getEntity(), Elementals.PYRO.create(e.getSource().getEntity()));

            // overload causes 2x damage
            setAdditiveDamage(e, 2);

            // logging here
            GenshinImpactMod.LOGGER.debug("Overloaded applied");

            return true;
        }

        return false;
    }

    /**
     * Handles frozen reaction (cover entity with ice with and freeze them)
     *
     * @param e - daage event
     * @return - was frozen applied
     */
    private static boolean handleFrozen(LivingDamageEvent e) {
        if (GenshinHeler.canApply(e.getEntityLiving(), e.getSource(), Elementals.HYDRO, Elementals.CRYO)) {
            // ticks for beeing frozen
            int ticks = (int) (20 * 25 * (1 + GenshinHeler.majestyBonus(e.getSource().getEntity())));

            // adding potion effect
            GenshinHeler.addEffect(e.getEntityLiving(), new MobEffectInstance(Effects.FROZEN, ticks));

            // logging here
            GenshinImpactMod.LOGGER.debug("Frozen applied");

            // no extra damage
            return true;
        }

        return false;
    }

    /**
     * Handles vapoize (hydro+pyro), causes pyro explosion
     *
     * @param e - damage event
     * @return - was vaporize successfull
     */
    private static boolean handleVaporize(LivingDamageEvent e) {
        // wet entity meets flame
        if (Elementals.HYDRO.is(e.getEntityLiving()) && Elementals.PYRO.is(e.getSource())) {

            // remove hydro effect
            GenshinHeler.removeEffects(e.getEntityLiving(), Elementals.HYDRO.getEffect());

            // perform pyro explosion
            explode(e.getEntityLiving(), e.getSource().getEntity(), Elementals.PYRO.create(e.getSource().getEntity()));

            // vaporize deals 2x damage
            setAdditiveDamage(e, 2);

            // logging here
            GenshinImpactMod.LOGGER.debug("Vaporize applied");

            return true;
        }

        return false;
    }

    /**
     * Handles reverse vaprize (pyro+hydro) causes steam explosion
     *
     * @param e - damage event
     * @return - was reverse vaporize successfull
     */
    private static boolean handleReverseVaporize(LivingDamageEvent e) {
        // burning entity meets hydro
        if (Elementals.PYRO.is(e.getEntityLiving()) && Elementals.HYDRO.is(e.getSource())) {

            // remove pyro effect
            GenshinHeler.removeEffects(e.getEntityLiving(), Elementals.PYRO.getEffect());

            // perform water explosion
            explode(e.getEntityLiving(), e.getSource().getEntity(), Elementals.HYDRO.create(e.getSource().getEntity()));

            // reverse vaporize deals 1.5x damage
            setAdditiveDamage(e, 1.5f);

            // logging here
            GenshinImpactMod.LOGGER.debug("Reverse vaporize applied");

            return true;
        }

        return false;
    }

    /**
     * Applying superconduct (electro + cryo/frozen) causes explosion
     *
     * @param e - damage event
     * @return - was superconduct applied
     */
    private static boolean handleSuperconduct(LivingDamageEvent e) {
        // applied both on cryo and frozen entities
        if (GenshinHeler.canApply(e.getEntityLiving(), e.getSource(), Elementals.ELECTRO, Elementals.CRYO, Elementals.FROZEN)) {

            // removing cryo and electro effects to prevent multiple reactions
            GenshinHeler.removeEffects(e.getEntityLiving(), Elementals.ELECTRO.getEffect(), Elementals.CRYO.getEffect());

            // creating explosion with superconduct damage
            explode(e.getEntityLiving(), e.getSource().getEntity(), Elementals.SUPERCONDUCT.create(e.getSource().getEntity()));

            // superconduct deals 0.5x damage
            setAdditiveDamage(e, .5f);

            GenshinImpactMod.LOGGER.debug("superconduct applied");

            return true;
        }

        return false;
    }

    /**
     * Elementals can spread by anemo
     */
    private static final Lazy<Elementals[]> swirlElements = Lazy.of(() -> new Elementals[]{Elementals.PYRO, Elementals.HYDRO, Elementals.ELECTRO, Elementals.CRYO, Elementals.FROZEN});

    /**
     * Performing swirl (anemo + element) causes explosion with current element
     *
     * @param e - damage event
     * @return - was swirl applied
     */
    private static boolean handleSwirl(LivingDamageEvent e) {
        if (GenshinHeler.canApply(e.getEntityLiving(), e.getSource(), Elementals.ANEMO, swirlElements.get())) {

            // choosing first element to spread
            Elementals toSpread = Arrays.stream(swirlElements.get()).filter(x -> x.is(e.getSource()) || x.is(e.getEntityLiving())).findFirst().orElse(null);
            if (toSpread != null) {
                // removes anemo effect on entity to prevent multiple reactions
                GenshinHeler.removeEffects(e.getEntity(), Elementals.ANEMO.getEffect());

                // creating explosion
                explode(e.getEntity(), e.getSource().getEntity(), toSpread.create(e.getSource().getEntity()));

                // swirl deals 0.6x damage
                setAdditiveDamage(e, .6f);

                // logging purpose
                GenshinImpactMod.LOGGER.debug("Swirl applied");

                return true;
            }
        }

        return false;
    }

    /**
     * Handling electro charged (hydro+electro) causing pereodically damaging
     *
     * @param e - damage event
     * @return - was superconduct successfull
     */
    private static boolean handleElectrocharged(LivingDamageEvent e) {
        if (!Elementals.ELECTROCHARGED.is(e.getEntityLiving()) &&
                GenshinHeler.canApply(e.getEntityLiving(), e.getSource(), Elementals.ELECTRO, Elementals.HYDRO)) {

            // calculating amplifier
            int level = (int) (GenshinHeler.safeGetAttribute(e.getSource().getEntity(), Attributes.level)
                    * (1 + GenshinHeler.majestyBonus(e.getSource().getEntity())));

            // adding effect on
            GenshinHeler.addEffect(e.getEntityLiving(), new MobEffectInstance(Elementals.ELECTROCHARGED.getEffect(), 20 * 8, level));

            // logging
            GenshinImpactMod.LOGGER.debug("Electrocharged applied");

            return true;
        }

        return false;
    }

    /**
     * Handling burning reaction (dendro+pyro), deals long fire
     *
     * @param e - damage event
     * @return - was burrning successfull
     */
    private static boolean handleBurning(LivingDamageEvent e) {
        if (!Elementals.BURNING.is(e.getEntityLiving()) &&
                GenshinHeler.canApply(e.getEntityLiving(), e.getSource(), Elementals.DENDRO, Elementals.PYRO)) {

            // weakest reaction, deals 0.25x damage
            setAdditiveDamage(e, .25f);

            int seconds = (int) (3 * (1 + GenshinHeler.majestyBonus(e.getSource().getEntity())));

            MobEffectInstance effectInstance = new MobEffectInstance(
                    Elementals.BURNING.getEffect(),
                    seconds * 20,
                    (int) GenshinHeler.safeGetAttribute(e.getEntityLiving(), Attributes.level),
                    false,
                    true,
                    true,
                    // indicator for prerodical damage
                    Elementals.DENDRO.is(e.getSource()) ? new MobEffectInstance(Elementals.BURNING.getEffect()) : null
            );

            // adding prerodical damage
            GenshinHeler.addEffect(e.getEntity(), effectInstance);

            // logging
            GenshinImpactMod.LOGGER.debug("Burning applied");

            return true;
        }

        return false;
    }

    private static final Lazy<Elementals[]> crystalElements = Lazy.of(() -> new Elementals[]{Elementals.PYRO, Elementals.HYDRO, Elementals.ELECTRO, Elementals.CRYO});

    /**
     * Handling crystal reaction (geo+element) spawning a shield crystal around
     *
     * @param e - damage event
     * @return - was crystalize successfull
     */
    private static boolean handleCrystalize(LivingDamageEvent e) {
        if (GenshinHeler.canApply(e.getEntityLiving(), e.getSource(), Elementals.GEO, crystalElements.get())) {
            Elementals shieldElemental = Elementals.GEO;

            // if geo triggers reaction
            if (shieldElemental.is(e.getSource())) {
                // choose other elemental on entity
                shieldElemental = Arrays.stream(crystalElements.get()).filter(x -> x.is(e.getEntityLiving())).findFirst().orElse(null);
            }

            if (shieldElemental != null) {
                // remove effect from entity
                GenshinHeler.removeEffects(e.getEntity(), shieldElemental.getEffect());

                // calculating health for shield
                int health = (int) (5 * (GenshinHeler.safeGetAttribute(e.getSource().getEntity(), Attributes.level) + 1) * (1 + GenshinHeler.majestyBonus(e.getSource().getEntity())));

                if (!e.getEntityLiving().getLevel().isClientSide()) {
                    // geo shield gives more defence
                    double effectivity = shieldElemental.equals(Elementals.GEO) ? 1d : 0.4;
                    // spawn siheld entity
                    e.getEntityLiving().getLevel().addFreshEntity(new Shield(e.getSource().getEntity(), shieldElemental, health, effectivity, 17 * 20));
                }

                // logging
                GenshinImpactMod.LOGGER.debug("Crystalize applied");

                return true;
            }
        }

        return false;
    }

    private static final Lazy<Elementals[]> meltElements = Lazy.of(() -> new Elementals[]{Elementals.FROZEN, Elementals.CRYO});

    /**
     * Handling melt (cryo+pyro) just deals more damage
     *
     * @param e - living damage event
     * @return - was melt successfull
     */
    private static boolean handleMelt(LivingDamageEvent e) {
        // cold or frozen entity meets flame
        if (Elementals.PYRO.is(e.getSource()) && Arrays.stream(meltElements.get()).anyMatch(x -> x.is(e.getEntityLiving()))) {
            // remove all effects
            GenshinHeler.removeEffects(e.getEntityLiving(), Stream.concat(Arrays.stream(meltElements.get()), Stream.of(Elementals.PYRO)).map(Elementals::getEffect).toArray(MobEffect[]::new));

            // melt dealing 2x damage
            setAdditiveDamage(e, 2);

            // logging
            GenshinImpactMod.LOGGER.debug("Melt applied");

            return true;
        }

        return false;
    }

    private static boolean handleReverseMelt(LivingDamageEvent e) {
        // burning entity meets
        if (Elementals.PYRO.is(e.getEntityLiving()) && Arrays.stream(meltElements.get()).anyMatch(x -> x.is(e.getSource()))) {
            // remove all effects
            GenshinHeler.removeEffects(e.getEntityLiving(), Stream.concat(Arrays.stream(meltElements.get()), Stream.of(Elementals.PYRO)).map(Elementals::getEffect).toArray(MobEffect[]::new));

            // reverse melt deals 1.5x damage
            setAdditiveDamage(e, 1.5f);

            // logging
            GenshinImpactMod.LOGGER.debug("Reverse melt applied");

            return true;
        }

        return false;
    }

    // endregion

    // region Helping methods

    /**
     * Explode with current damage source according to attacker level and majesty bonus
     *
     * @param victim   - victim which reaction causes explosion
     * @param attacker - current attacker
     * @param source   - damage source applied to entities inside explosion
     */
    private static void explode(Entity victim, Entity attacker, DamageSource source) {
        if (victim != null && !victim.getLevel().isClientSide()) {
            GenshinAreaSpreading areaSpreading = new GenshinAreaSpreading(attacker, victim.position(), source,
                    (float) (GenshinHeler.safeGetAttribute(attacker, Attributes.level) + (3 * GenshinHeler.majestyBonus(attacker))));
            areaSpreading.explode();
        }
    }

    /**
     * Multiplies damage
     *
     * @param e          - event
     * @param multiplier - damage multiplier
     */
    private static void setAdditiveDamage(LivingDamageEvent e, float multiplier) {
        e.setAmount(GenshinHeler.getActualDamage(e.getEntityLiving(), e.getSource(), (float) ((e.getAmount() + GenshinHeler.safeGetAttribute(e.getSource().getEntity(), Attributes.level)) * multiplier)));
    }

    // endregion
}
