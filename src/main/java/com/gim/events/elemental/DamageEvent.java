package com.gim.events.elemental;

import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinMobEffect;
import com.gim.registry.Attributes;
import com.gim.registry.DamageSources;
import com.gim.registry.Effects;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageEvent {

    // region APPLYING ELEMENTAL EFFECTS

    /**
     * Applying potion effect BEFORE actual damage
     * !!!ONLY SERVER SIDE!!!
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void applyElementalStatus(LivingDamageEvent event) {
        if (event.getEntityLiving() == null || event.getSource() == null || event.isCanceled()) {
            return;
        }

        // getting effect from damage source
        MobEffectInstance mobEffect = effectFromSource(event.getSource(), true);
        if (mobEffect == null)
            return;

        // already have such effect
        if (event.getEntityLiving().hasEffect(mobEffect.getEffect()))
            return;

        addEffect(event.getEntity(), mobEffect);
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

        if (!e.getEntityLiving().getLevel().isClientSide() && !e.getEntityLiving().hasEffect(Effects.HYDRO) && e.getEntityLiving().isInWaterOrRain()) {
            // addEffect(e.getEntityLiving(), new MobEffectInstance(Effects.HYDRO, 20 * 10));
            applyElementalReactions(new LivingDamageEvent(e.getEntityLiving(), DamageSources.HydroSource, 0));
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
            // adding base attack damage bonus
            e.setAmount(e.getAmount() + getLevel(e.getEntityLiving()));
            e.setAmount(getActualDamage(e.getEntityLiving(), e.getSource(), e.getAmount() * 2));
            GenshinImpactMod.LOGGER.debug("overload applied");
        }

        if (handleFrozen(e.getEntityLiving(), e.getSource())) {
            e.setAmount(getActualDamage(e.getEntityLiving(), e.getSource(), e.getAmount()));
            GenshinImpactMod.LOGGER.debug("frozen applied");
        }

        if (handleVaporize(e.getEntityLiving(), e.getSource())) {
            // adding base attack damage bonus
            e.setAmount(e.getAmount() + getLevel(e.getEntityLiving()));
            float multiplier = e.getSource().isFire() ? 2f : 1.5f;
            e.setAmount(getActualDamage(e.getEntityLiving(), e.getSource(), e.getAmount() * multiplier));
            GenshinImpactMod.LOGGER.debug(e.getSource().isFire() ? "" : "reverse " + "vaporize applied");
        }

        if (handleSuperconduct(e.getEntityLiving(), e.getSource())) {
            // adding base attack damage bonus
            e.setAmount(e.getAmount() + getLevel(e.getEntityLiving()));
            e.setAmount(getActualDamage(e.getEntityLiving(), e.getSource(), e.getAmount() * 0.5f));
            GenshinImpactMod.LOGGER.debug("superconduct applied");
        }

        if (handleSwirl(e.getEntityLiving(), e.getSource())) {
            // adding base attack damage bonus
            e.setAmount(e.getAmount() + getLevel(e.getEntityLiving()));
            e.setAmount(getActualDamage(e.getEntityLiving(), e.getSource(), e.getAmount() * 0.6f));
            GenshinImpactMod.LOGGER.debug("swirl applied");
        }

        if (handleElectroCharged(e.getEntityLiving(), e.getSource())) {
            // adding base attack damage bonus
            e.setAmount(e.getAmount() + getLevel(e.getEntityLiving()));
            e.setAmount(getActualDamage(e.getEntityLiving(), e.getSource(), e.getAmount() * 1.2f));
            GenshinImpactMod.LOGGER.debug("electro-charged applied");
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

        e.setDamageModifier((float) (e.getOldDamageModifier() * instance.getValue()));
        GenshinImpactMod.LOGGER.debug("applied crit hit by Genshin Impact Mod");
    }

    @SubscribeEvent
    public static void onPotionAdded(PotionColorCalculationEvent e) {
        if (e.getEntityLiving() == null || e.getEffects().isEmpty() || e.areParticlesHidden())
            return;

        boolean shouldHide = e.getEffects().stream()
                .map(MobEffectInstance::getEffect)
                .anyMatch(x -> x instanceof GenshinMobEffect && ((GenshinMobEffect) x).isPureElemental());

        if (shouldHide) {
            // e.setColor(0);
        }
    }

    // endregion

    // region HANDLING REACTIONS

    private static boolean handleOverload(LivingEntity entity, DamageSource source) {
        boolean canHandle = (entity.hasEffect(Effects.ELECTRO) && source.isFire()) ||
                (entity.hasEffect(Effects.PYRO) && source == DamageSource.LIGHTNING_BOLT);

        if (canHandle) {
            explode(entity, source.getEntity(), DamageSource.ON_FIRE, Effects.ELECTRO);
        }

        return canHandle;
    }

    private static boolean handleFrozen(LivingEntity entity, DamageSource source) {
        boolean canHandle = (entity.hasEffect(Effects.CRYO) && source == DamageSources.HydroSource) ||
                (entity.hasEffect(Effects.HYDRO) && source == DamageSource.FREEZE);

        if (canHandle) {
            int ticks = (int) (20 * 10 * (1 + majestyBonus(source.getEntity())));

            // removing both effects
            removeEffect(entity, Effects.HYDRO);
            removeEffect(entity, Effects.CRYO);
            // adding frozen effect
            addEffect(entity, new MobEffectInstance(Effects.FROZEN, ticks));
        }

        return canHandle;
    }

    private static boolean handleVaporize(LivingEntity entity, DamageSource source) {
        boolean canHandle = (entity.hasEffect(Effects.HYDRO) && source.isFire()) ||
                (entity.hasEffect(Effects.PYRO) && source == DamageSources.HydroSource);

        if (canHandle) {
            DamageSource currentSource = source.isFire() ? DamageSource.ON_FIRE : DamageSources.HydroSource;
            explode(entity, source.getEntity(), currentSource, currentSource.isFire() ? Effects.HYDRO : Effects.PYRO);
        }

        return canHandle;
    }

    private static boolean handleSuperconduct(LivingEntity entity, DamageSource source) {
        boolean canHandle = (entity.hasEffect(Effects.ELECTRO) && source == DamageSource.FREEZE) ||
                (entity.hasEffect(Effects.CRYO) && source == DamageSource.LIGHTNING_BOLT);

        if (canHandle) {
            explode(entity, source.getEntity(), DamageSources.SuperconductSource, Effects.ELECTRO);
        }

        return canHandle;
    }

    private static boolean handleSwirl(LivingEntity entity, DamageSource source) {
        if (!entity.hasEffect(Effects.ANEMO) &&
                !entity.hasEffect(Effects.PYRO) &&
                !entity.hasEffect(Effects.CRYO) &&
                !entity.hasEffect(Effects.HYDRO) &&
                !entity.hasEffect(Effects.FROZEN) &&
                !entity.hasEffect(Effects.ELECTRO)) {
            return false;
        }

        DamageSource attack = null;

        if (entity.hasEffect(Effects.ANEMO)) {
            if (source.isFire()
                    || source == DamageSource.LIGHTNING_BOLT
                    || source == DamageSource.FREEZE
                    || source == DamageSources.Frozen
                    || source == DamageSources.HydroSource) {
                attack = source;
            }
        } else if (source == DamageSources.AnemoSource) {
            if (entity.hasEffect(Effects.PYRO)) {
                attack = DamageSource.ON_FIRE;
            }

            if (entity.hasEffect(Effects.ELECTRO)) {
                attack = DamageSource.LIGHTNING_BOLT;
            }

            if (entity.hasEffect(Effects.CRYO)) {
                attack = DamageSource.FREEZE;
            }

            if (entity.hasEffect(Effects.HYDRO)) {
                attack = DamageSources.HydroSource;
            }

            if (entity.hasEffect(Effects.HYDRO)) {
                attack = DamageSources.HydroSource;
            }

            if (entity.hasEffect(Effects.FROZEN)) {
                attack = DamageSources.Frozen;
            }
        }

        if (attack != null) {
            explode(entity, source.getEntity(), attack, Effects.ANEMO);
        }

        return attack != null;
    }

    private static boolean handleElectroCharged(LivingEntity entity, DamageSource source) {
        if (!entity.hasEffect(Effects.HYDRO) && !entity.hasEffect(Effects.ELECTRO))
            return false;

        boolean canHandle = (entity.hasEffect(Effects.HYDRO) && source == DamageSource.LIGHTNING_BOLT)
                ||
                (entity.hasEffect(Effects.ELECTRO) && source == DamageSources.HydroSource);

        if (canHandle) {
            return false;
        }

        return canHandle;
    }

    private static boolean handleBurning(LivingEntity entity, DamageSource source) {
        if (!entity.hasEffect(Effects.DENDRO) && !entity.hasEffect(Effects.PYRO))
            return false;

        boolean canHandle = (entity.hasEffect(Effects.DENDRO) && source.isFire())
                ||
                (entity.hasEffect(Effects.PYRO) && source == DamageSources.DendroSource);

        if (canHandle) {
            removeEffect(entity, Effects.DENDRO);
            int seconds = (int) (10 * (1 + majestyBonus(source.getEntity())));
            entity.setSecondsOnFire(seconds);
        }

        return canHandle;
    }

    private static boolean handleCrystalize(LivingEntity entity, DamageSource source) {
        if (!entity.hasEffect(Effects.PYRO) &&
                !entity.hasEffect(Effects.HYDRO) &&
                !entity.hasEffect(Effects.CRYO) &&
                !entity.hasEffect(Effects.ELECTRO) &&
                !entity.hasEffect(Effects.GEO)
        ) {
            return false;
        }
        MobEffect effect = null;

        if (source == DamageSources.GeoSource) {
            effect = Arrays.asList(Effects.PYRO, Effects.HYDRO, Effects.CRYO, Effects.ELECTRO)
                    .stream().filter(entity::hasEffect).findFirst().orElse(null);
        } else if (entity.hasEffect(Effects.GEO)) {
            MobEffectInstance effectInstance = effectFromSource(source, false);
            if (effectInstance != null
                    && effectInstance.getEffect() != Effects.ANEMO
                    && effectInstance.getEffect() != Effects.DENDRO
                    && effectInstance.getEffect() != Effects.GEO) {
                effect = effectInstance.getEffect();
            }
        }

        if (effect != null) {
            // TODO spawn shield entity
        }

        return effect != null;
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
            return true;
        }

        return false;
    }

    // endregion

    // region Helping methods

    /**
     * Returns applying effect from current damage source
     *
     * @param source           damage source
     * @param includeReactions - shoud we include effects from elemental reactions.
     *                         If false use only base 7 elementals instead
     * @return
     */
    private static MobEffectInstance effectFromSource(DamageSource source, boolean includeReactions) {
        int baseTicks = 20 * 10;

        if (source.isFire()) {
            return new MobEffectInstance(Effects.PYRO, baseTicks);
        }

        if (source == DamageSource.LIGHTNING_BOLT) {
            return new MobEffectInstance(Effects.ELECTRO, baseTicks);
        }

        if (source == DamageSource.FREEZE) {
            return new MobEffectInstance(Effects.CRYO, baseTicks);
        }

        if (source == DamageSources.AnemoSource) {
            return new MobEffectInstance(Effects.ANEMO, baseTicks);
        }

        if (source == DamageSources.HydroSource) {
            return new MobEffectInstance(Effects.HYDRO, baseTicks);
        }

        if (source == DamageSources.GeoSource) {
            return new MobEffectInstance(Effects.GEO, baseTicks);
        }

        if (source == DamageSources.DendroSource) {
            return new MobEffectInstance(Effects.DENDRO, baseTicks);
        }

        if (includeReactions) {
            if (source == DamageSources.SuperconductSource) {
                return new MobEffectInstance(Effects.DEFENCE_DEBUFF, 20 * 12, 4);
            }

            if (source == DamageSources.Frozen) {
                return new MobEffectInstance(Effects.FROZEN, (int) (baseTicks * (1 + majestyBonus(source.getEntity()))));
            }
        }

        return null;
    }

    private static int getLevel(Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity e = (LivingEntity) entity;
            AttributeInstance instance = e.getAttribute(Attributes.level);
            if (instance != null) {
                return (int) instance.getValue();
            }
        }

        return 0;
    }

    /**
     * Returns elemental majesty bonus
     * https://genshin-impact.fandom.com/wiki/Damage#Amplifying_Reaction_Damage
     *
     * @param e - current entity
     * @return
     */
    private static float majestyBonus(Entity e) {
        if (e instanceof LivingEntity) {

            LivingEntity entity = (LivingEntity) e;

            AttributeInstance instance = entity.getAttribute(Attributes.elemental_majesty);
            if (instance != null) {
                double value = instance.getValue();
                if (value > 0) {
                    return (float) (16 * value / (value + 2000) / 100);
                }
            }
        }

        return 0;
    }

    /**
     * Returns bonus for each elemental attack
     *
     * @param entity - attacking entity
     * @param source - damage source
     * @return
     */
    private static float elementalBonus(LivingEntity entity, DamageSource source) {
        Attribute bonus = null;

        if (source.isFire()) {
            bonus = Attributes.pyro_bonus;
        }

        if (source == DamageSource.LIGHTNING_BOLT) {
            bonus = Attributes.electro_bonus;
        }

        if (source == DamageSource.FREEZE || source == DamageSources.SuperconductSource) {
            bonus = Attributes.cryo_bonus;
        }

        if (source == DamageSources.AnemoSource) {
            bonus = Attributes.anemo_bonus;
        }

        if (source == DamageSources.DendroSource) {
            bonus = Attributes.dendro_bonus;
        }

        if (source == DamageSources.HydroSource) {
            bonus = Attributes.hydro_bonus;
        }

        if (source == DamageSources.GeoSource) {
            bonus = Attributes.geo_bonus;
        }

        if (bonus != null) {
            AttributeInstance instance = entity.getAttribute(bonus);
            if (instance != null) {
                return (float) instance.getValue();
            }
        }

        return 0;
    }

    /**
     * Returns actual elementary resistance
     *
     * @param entity
     * @param source
     * @return
     */
    private static float elementalResistance(LivingEntity entity, DamageSource source) {
        Attribute bonus = null;

        if (source.isFire()) {
            bonus = Attributes.pyro_resistance;
        }

        if (source == DamageSource.LIGHTNING_BOLT) {
            bonus = Attributes.electro_resistance;
        }

        if (source == DamageSource.FREEZE || source == DamageSources.SuperconductSource) {
            bonus = Attributes.cryo_resistance;
        }

        if (source == DamageSources.AnemoSource) {
            bonus = Attributes.anemo_resistance;
        }

        if (source == DamageSources.DendroSource) {
            bonus = Attributes.dendro_resistance;
        }

        if (source == DamageSources.HydroSource) {
            bonus = Attributes.hydro_resistance;
        }

        if (source == DamageSources.GeoSource) {
            bonus = Attributes.geo_resistance;
        }

        if (bonus != null) {
            AttributeInstance instance = entity.getAttribute(bonus);
            if (instance != null) {
                return (float) instance.getValue();
            }
        }

        return 0;
    }

    /**
     * Calcuates main damage
     *
     * @param entity - victim
     * @param source - damage source
     * @param damage - damage amount
     * @return
     */
    private static float getActualDamage(LivingEntity entity, DamageSource source, float damage) {
        int level = 1;
        float majesty = 0;
        float elementalBonus = 0;
        float elementalResistance = 0;
        float defence = 0;

        if (source.getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) source.getEntity();

            majesty = majestyBonus(attacker);
            elementalBonus = elementalBonus(attacker, source);
            elementalResistance = elementalResistance(entity, source);

            AttributeInstance instance = attacker.getAttribute(Attributes.level);
            if (instance != null) {
                level = (int) instance.getValue();
            }

            instance = entity.getAttribute(Attributes.defence);
            if (instance != null) {
                double defenceRaw = instance.getValue();
                defence = (float) (defenceRaw / (defenceRaw + 5 * level));
            }
        }

        return damage * level * (1 + majesty + elementalBonus) * (1 - defence) * (1 - elementalResistance - majesty);
    }


    /**
     * Explode with current damage source according to attacker level and majesty bonus
     */
    private static void explode(Entity victim, Entity attacker, DamageSource source, MobEffect... toRemove) {
        if (victim instanceof LivingEntity && !victim.getLevel().isClientSide()) {
            LivingEntity victim1 = (LivingEntity) victim;

            // removing effect before explosion
            if (toRemove != null && toRemove.length > 0) {
                Arrays.stream(toRemove).forEach(x -> removeEffect(victim, x));
            }

            victim1.getLevel().explode(
                    attacker,
                    source,
                    null,
                    victim.getX(),
                    victim.getY(),
                    victim.getZ(),
                    2,
                    // getLevel(attacker) + (3 * majestyBonus(attacker)),
                    source.isFire(),
                    Explosion.BlockInteraction.NONE
            );
        }
    }

    private static boolean addEffect(Entity e, MobEffectInstance effect) {

        if (e instanceof LivingEntity
                && effect != null
                && !e.getLevel().isClientSide()
                && ((LivingEntity) e).addEffect(effect)) {
            ((ServerLevel) e.getLevel()).getServer().getPlayerList().broadcast(
                    null,
                    e.getX(),
                    e.getY(),
                    e.getZ(),
                    16,
                    e.getLevel().dimension(),
                    new ClientboundUpdateMobEffectPacket(e.getId(), effect)
            );

            return true;
        }

        return false;
    }

    private static boolean removeEffect(Entity e, MobEffect effect) {
        if (e instanceof LivingEntity
                && effect != null
                && !e.getLevel().isClientSide()
                && ((LivingEntity) e).removeEffect(effect)) {
            ((ServerLevel) e.getLevel()).getServer().getPlayerList().broadcast(
                    null,
                    e.getX(),
                    e.getY(),
                    e.getZ(),
                    16,
                    e.getLevel().dimension(),
                    new ClientboundRemoveMobEffectPacket(e.getId(), effect)
            );

            return true;
        }

        return false;
    }

    // endregion
}
