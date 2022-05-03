package com.gim.events.elemental;

import com.gim.GenshinImpactMod;
import com.gim.attack.GenshinMobEffect;
import com.gim.registry.Attributes;
import com.gim.registry.Effects;
import com.gim.registry.Elementals;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;
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

        if (!e.getEntityLiving().getLevel().isClientSide() && !e.getEntityLiving().hasEffect(Effects.HYDRO) && e.getEntityLiving().isInWaterOrRain()) {
            applyElementalReactions(new LivingDamageEvent(e.getEntityLiving(), Elementals.HYDRO.create(), 0));
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
            setAdditiveDamage(e, 1.2f);
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
        boolean canHandle = canApply(entity, source, Elementals.ELECTRO, Elementals.PYRO);

        if (canHandle) {
            explode(entity, source.getEntity(), DamageSource.ON_FIRE, Effects.ELECTRO);
        }

        return canHandle;
    }

    private static boolean handleFrozen(LivingEntity entity, DamageSource source) {
        boolean canHandle = canApply(entity, source, Elementals.HYDRO, Elementals.CRYO);

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

    private static final Lazy<Elementals[]> swirlElements = () -> new Elementals[]{Elementals.PYRO, Elementals.HYDRO, Elementals.ELECTRO, Elementals.CRYO, Elementals.FROZEN};

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
        if (canApply(entity, source, Elementals.ELECTRO, Elementals.HYDRO)) {
            // todo think about constant electro damage recive
            return true;
        }

        return false;
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

    private static final Lazy<Elementals[]> crystalElements = () -> new Elementals[]{Elementals.PYRO, Elementals.HYDRO, Elementals.ELECTRO, Elementals.CRYO};

    private static boolean handleCrystalize(LivingEntity entity, DamageSource source) {
        if (canApply(entity, source, Elementals.GEO, crystalElements.get())) {
            Elementals crystal = Elementals.GEO;

            if (!Elementals.GEO.is(entity)) {
                crystal = Arrays.stream(crystalElements.get()).filter(x -> x.is(entity)).findFirst().orElse(null);
            }

            removeEffect(entity, crystal.getEffect());
            // todo spawn crustal shield
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
            return true;
        }

        return false;
    }

    // endregion

    // region Helping methods

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
     * Calcuates main damage
     *
     * @param entity - victim
     * @param source - damage source
     * @param damage - damage amount
     * @return
     */
    private static float getActualDamage(LivingEntity entity, final DamageSource source, float damage) {
        // current attacker level
        int level = 1;
        // elemental majesty
        float majesty = 0;
        // bonus for current elemental attack. Can be zero if non elemental attack happens
        float elementalBonus = 0;
        // elemental resistance. Can be 0 if non elemental attack happens
        float elementalResistance = 0;
        // current defence bonus. Can be negative (kinda debuff, multiplying incoming damage)
        float defence = 0;

        // some null checking
        if (source.getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) source.getEntity();

            // calclulated majesty for attacker
            majesty = majestyBonus(attacker);

            // get attacker level
            AttributeInstance instance = attacker.getAttribute(Attributes.level);
            if (instance != null) {
                level = (int) instance.getValue();
            }

            // find elemental from attack
            Elementals elemental = Arrays.stream(Elementals.values()).filter(x -> x.is(source)).findFirst().orElse(null);
            // if elemental attack hapens
            if (elemental != null) {
                // checking possible bonus for current elemental
                if (elemental.getBonus() != null) {
                    instance = attacker.getAttribute(elemental.getBonus());
                    if (instance != null) {
                        elementalBonus = (float) instance.getValue();
                    }
                }

                // checking possible resistance for current elemental
                if (elemental.getResistance() != null) {
                    instance = attacker.getAttribute(elemental.getResistance());
                    if (instance != null) {
                        elementalResistance = (float) instance.getValue();
                    }
                }
            }

            // applying defence according to victim
            instance = entity.getAttribute(Attributes.defence);
            if (instance != null) {
                double defenceRaw = instance.getValue();
                defence = (float) (defenceRaw / (defenceRaw + 5 * (1 + getLevel(entity))));
            }
        }

        // calcuating raw damage (by level, majesty and elemental bonuses)
        float rawDamage = damage * level * (1 + majesty + elementalBonus);
        // calculating defence by raw damage
        float defenceValue = rawDamage * defence;
        // calculating resistance for raw damage
        float resist = rawDamage * (elementalResistance + majesty);

        // final result is damage without resist
        return rawDamage - defenceValue - resist;
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

    private static void setAdditiveDamage(LivingDamageEvent e, float multiplier) {
        e.setAmount(getActualDamage(e.getEntityLiving(), e.getSource(), (e.getAmount() + getLevel(e.getSource().getEntity())) * multiplier));
    }

    private static boolean canApply(LivingEntity entity, DamageSource source, Elementals first, Elementals... other) {
        if (entity != null && first != null && other != null && other.length > 0 && source != null) {
            if (first.is(entity)) {
                for (Elementals elementals : other) {
                    if (elementals.is(source)) {
                        return true;
                    }
                }
            }

            if (first.is(source)) {
                for (Elementals elementals : other) {
                    if (elementals.is(entity)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // endregion
}
