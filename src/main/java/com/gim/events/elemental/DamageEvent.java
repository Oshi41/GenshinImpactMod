package com.gim.events.elemental;

import com.gim.GenshinImpactMod;
import com.gim.registry.Attributes;
import com.gim.registry.DamageSources;
import com.gim.registry.Effects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.w3c.dom.Attr;

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

    // endregion

    // region APPLYING ELEMENTAL REACTIONS

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyElementalReactions(LivingDamageEvent e) {
        if (e.getSource() == null || e.getEntityLiving() == null)
            return;

        if (handleBonusAttack(e)) {
            GenshinImpactMod.LOGGER.debug("attack bonus applied");
        }


        if (handleOverload(e.getEntityLiving(), e.getSource())) {
            e.setAmount(getActualDamage(e.getEntityLiving(), e.getSource(), e.getAmount() * 2));
            GenshinImpactMod.LOGGER.debug("overload applied");
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

    // region Helping methods

    /**
     * Returns elemental majesty bonus
     * https://genshin-impact.fandom.com/wiki/Damage#Amplifying_Reaction_Damage
     *
     * @param entity - current entity
     * @return
     */
    private static float majestyBonus(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.elemental_majesty);
        if (instance != null) {
            double value = instance.getValue();
            if (value > 0) {
                return (float) (16 * value / (value + 2000) / 100);
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

            majesty += majestyBonus(attacker);
            elementalBonus = elementalBonus(attacker, source);

            AttributeInstance instance = attacker.getAttribute(Attributes.level);
            if (instance != null) {
                level = (int) instance.getValue();
            }

            instance = entity.getAttribute(Attributes.defence);
            if (instance != null) {
                double defenceRaw = instance.getValue();
                defence = (float) (defenceRaw / (defenceRaw + 5 * level));
            }

            elementalResistance = elementalResistance(entity, source);
        }

        return damage * level * (1 + majesty + elementalBonus) * (1 - defence) * (1 - elementalResistance - majesty);
    }

    // endregion

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

        entity.removeEffect(Effects.ELECTRO);
        entity.removeEffect(Effects.PYRO);

        return canHandle;
    }
}
