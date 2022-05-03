package com.gim.events;

import com.gim.GenshinImpactMod;
import com.gim.registry.DamageSources;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Debug {

    @SubscribeEvent
    public static void onSwordHit(LivingAttackEvent e) {
        if (e.getSource() == null || e.getEntityLiving() == null || e.isCanceled())
            return;

        if (e.getSource().getEntity() instanceof LivingEntity) {
            DamageSource source = null;
            Item item = ((LivingEntity) e.getSource().getEntity()).getMainHandItem().getItem();

            if (item == Items.WOODEN_SWORD) {
                source = DamageSources.AnemoSource;
            }

            if (item == Items.STONE_SWORD) {
                source = DamageSources.GeoSource;
            }

            if (item == Items.IRON_SWORD) {
                source = DamageSource.ON_FIRE;
            }

            if (item == Items.GOLDEN_SWORD) {
                source = DamageSource.LIGHTNING_BOLT;
            }

            if (item == Items.DIAMOND_SWORD) {
                source = DamageSource.FREEZE;
            }

            if (item == Items.NETHERITE_SWORD) {
                source = DamageSources.HydroSource;
            }

            if (source != null) {
                e.getEntityLiving().hurt(source, 5);
            }
        }


    }
}
