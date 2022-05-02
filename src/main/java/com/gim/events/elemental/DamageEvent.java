package com.gim.events.elemental;

import com.gim.GenshinImpactMod;
import com.gim.registry.Attributes;
import com.gim.registry.DamageSources;
import com.gim.registry.Effects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
@OnlyIn(Dist.DEDICATED_SERVER)
public class DamageEvent {

    // region APPLYING ELEMENTAL EFFECTS

    /**
     * Applying potion effect BEFORE actual damage
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyElementalStatus(LivingHurtEvent event) {
        if (event.getEntityLiving() == null || event.getSource() != null || event.isCanceled()) {
            return;
        }

        // every fire source will trigger Pyro status
        if (!event.getEntityLiving().hasEffect(Effects.PYRO) && event.getSource().isFire()) {
            event.getEntityLiving().addEffect(new MobEffectInstance(Effects.PYRO, 20 * 10));
        }

        // lightning cause electro status
        if (!event.getEntityLiving().hasEffect(Effects.ELECTRO) && event.getSource() == DamageSource.LIGHTNING_BOLT) {
            event.getEntityLiving().addEffect(new MobEffectInstance(Effects.ELECTRO, 20 * 10));
        }

        // adding hydro status
        if (!event.getEntityLiving().hasEffect(Effects.HYDRO) && event.getSource() == DamageSources.HydroSource) {
            event.getEntityLiving().addEffect(new MobEffectInstance(Effects.HYDRO, 20 * 10));
        }

        // superconduct applies cryo too
        if (!event.getEntityLiving().hasEffect(Effects.CRYO) && event.getSource() == DamageSource.FREEZE
                || event.getSource() == DamageSources.SuperconductSource) {
            event.getEntityLiving().addEffect(new MobEffectInstance(Effects.CRYO, 20 * 10));
        }

        // applying debuff effect
        if (!event.getEntityLiving().hasEffect(Effects.DEFENCE_DEBUFF) && event.getSource() == DamageSources.SuperconductSource) {
            MobEffectInstance effectInstance = new MobEffectInstance(Effects.DEFENCE_DEBUFF, 20 * 12, 4, false, false, false);
            event.getEntityLiving().addEffect(effectInstance);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onUpdate(LivingEvent.LivingUpdateEvent e) {
        if (e.getEntityLiving() == null || e.getEntityLiving().getLevel().isClientSide()) {
            return;
        }

        if (!e.getEntityLiving().hasEffect(Effects.HYDRO) && e.getEntityLiving().isInWaterOrRain()) {
            e.getEntityLiving().hurt(DamageSources.HydroSource, 0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExplode(ExplosionEvent.Detonate e) {
    }

    // endregion

    // region APPLYING ELEMENTAL REACTIONS

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

    // endregion

    // region HANDLING REACTIONS

    private static boolean handleOverload(LivingEntity entity, DamageSource source) {
        if (!entity.hasEffect(Effects.ELECTRO) && !entity.hasEffect(Effects.PYRO))
            return false;

        boolean canHandle = (entity.hasEffect(Effects.ELECTRO) && source.isFire()) ||
                (entity.hasEffect(Effects.PYRO) && source == DamageSource.LIGHTNING_BOLT);

        entity.getLevel().explode(
                source.getDirectEntity(),
                DamageSource.ON_FIRE,
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                5,
                true,
                Explosion.BlockInteraction.NONE
        );

        // removing previous effect
        entity.removeEffect(source.isFire() ? Effects.ELECTRO : Effects.PYRO);

        return canHandle;
    }

    private static boolean handleFrozen(LivingEntity entity, DamageSource source) {
        if (!entity.hasEffect(Effects.CRYO) && !entity.hasEffect(Effects.HYDRO))
            return false;

        boolean canHandle = (entity.hasEffect(Effects.CRYO) && source == DamageSources.HydroSource) ||
                (entity.hasEffect(Effects.HYDRO) && source == DamageSource.FREEZE);

        if (canHandle) {
            int ticks = (int) (20 * 10 * (1 * majestyBonus(source.getEntity())));
            // adding frozen effect
            entity.addEffect(new MobEffectInstance(Effects.FROZEN, ticks, 0, false, false, false));


            // removing previous effect
            entity.removeEffect(source == DamageSources.HydroSource ? Effects.CRYO : Effects.HYDRO);
        }

        return canHandle;
    }

    private static boolean handleVaporize(LivingEntity entity, DamageSource source) {
        if (!entity.hasEffect(Effects.PYRO) && !entity.hasEffect(Effects.HYDRO))
            return false;

        boolean canHandle = (entity.hasEffect(Effects.HYDRO) && source.isFire()) ||
                (entity.hasEffect(Effects.PYRO) && source == DamageSources.HydroSource);

        if (canHandle) {
            DamageSource currentSource = source.isFire() ? DamageSources.HydroSource : DamageSource.ON_FIRE;
            entity.getLevel().explode(
                    source.getEntity(),
                    currentSource,
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    5,
                    currentSource.isFire(),
                    Explosion.BlockInteraction.NONE
            );

            // removing last effect
            entity.removeEffect(currentSource.isFire() ? Effects.HYDRO : Effects.PYRO);
        }

        return canHandle;
    }

    private static boolean handleSuperconduct(LivingEntity entity, DamageSource source) {
        if (!entity.hasEffect(Effects.CRYO) && !entity.hasEffect(Effects.ELECTRO))
            return false;

        boolean canHandle = (entity.hasEffect(Effects.HYDRO) && source == DamageSource.FREEZE) ||
                (entity.hasEffect(Effects.CRYO) && source == DamageSource.LIGHTNING_BOLT);

        if (canHandle) {
            entity.getLevel().explode(
                    source.getEntity(),
                    DamageSources.SuperconductSource,
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    5,
                    false,
                    Explosion.BlockInteraction.NONE);

            entity.removeEffect(Effects.ELECTRO);
        }

        return canHandle;
    }

    private static boolean handleSwirl(LivingEntity entity, DamageSource source) {
        if (!entity.hasEffect(Effects.ANEMO) &&
                !entity.hasEffect(Effects.PYRO) &&
                !entity.hasEffect(Effects.CRYO) &&
                !entity.hasEffect(Effects.HYDRO) &&
                !entity.hasEffect(Effects.ELECTRO)) {
            return false;
        }

        DamageSource attack = null;

        if (entity.hasEffect(Effects.ANEMO)) {
            if (source.isFire()
                    || source == DamageSource.LIGHTNING_BOLT
                    || source == DamageSource.FREEZE
                    || source == DamageSources.HydroSource) {
                attack = source;
            }
        } else {
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
        }

        if (attack != null) {
            entity.getLevel().explode(
                    source.getEntity(),
                    attack,
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    5,
                    false,
                    Explosion.BlockInteraction.NONE);
        }

        return attack != null;
    }

    // endregion

    // region Helping methods

    private static int getLevel(LivingEntity e) {
        if (e != null) {
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

        if (source == DamageSource.FREEZE) {
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

        if (source == DamageSource.FREEZE) {
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
     * Handle attack bonus
     *
     * @return
     */
    private static boolean handleBonusAttack(LivingDamageEvent e) {
        AttributeInstance attackAttr = e.getEntityLiving().getAttribute(Attributes.attack_bonus);
        if (attackAttr != null) {
            double value = attackAttr.getValue();
            if (value > 0) {
                float result = (float) ((value + 1) * e.getAmount());
                e.setAmount(result);
                return true;
            }
        }

        return false;
    }

    // endregion
}
