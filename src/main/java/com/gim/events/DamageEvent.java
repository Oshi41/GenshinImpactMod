package com.gim.events;

import com.gim.GenshinHeler;
import com.gim.GenshinImpactMod;
import com.gim.registry.Attributes;
import com.gim.registry.ElementalReactions;
import com.gim.registry.Elementals;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageEvent {

    // physical damage
    private final static Set<String> attackNames = Set.of("mob", "player");

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
                    GenshinHeler.addEffect(event.getEntityLiving(), new MobEffectInstance(elemental.getEffect(), 20 * 10));
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

        // applying wet effect on entity
        if (!e.getEntityLiving().getLevel().isClientSide() && !e.getEntityLiving().hasEffect(Elementals.HYDRO.getEffect()) && e.getEntityLiving().isInWaterOrRain()) {
            GenshinHeler.addEffect(e.getEntityLiving(), new MobEffectInstance(Elementals.HYDRO.getEffect(), 10 * 20));
            applyElementalReactions(new LivingDamageEvent(e.getEntityLiving(), Elementals.HYDRO.create(null), Float.MIN_NORMAL));
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

        for (ElementalReactions reaction : ElementalReactions.values()) {
            reaction.handle(e);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
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

    // region HANDLING SIMPLE REACTIONS

    /**
     * Handle attack bonus
     *
     * @return
     */
    private static boolean handleBonusAttack(LivingDamageEvent e) {
        // only physical damage (by mob and player attack)
        if (attackNames.contains(e.getSource().getMsgId())) {
            double bonus = Math.max(0, GenshinHeler.safeGetAttribute(e.getSource().getEntity(), Attributes.physical_bonus)) + 1;
            e.setAmount((float) (e.getAmount() * bonus));
        }

        float actualDamage = GenshinHeler.getActualDamage(e.getEntityLiving(), e.getSource(), e.getAmount());

        // same value as was
        if (actualDamage == e.getAmount()) {
            return false;
        }

        e.setAmount(actualDamage);

        // reduce all damage
        // todo think about no pushing if reducing all damage
        if (actualDamage <= 0) {
            e.setCanceled(true);
        }

        return true;
    }

    // endregion

}
