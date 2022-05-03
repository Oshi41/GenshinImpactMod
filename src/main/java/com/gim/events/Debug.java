package com.gim.events;

import com.gim.registry.Elementals;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Debug {

    @SubscribeEvent
    public static void onSwordHit(LivingAttackEvent e) {
        if (e.getSource() == null || e.getEntityLiving() == null || e.isCanceled())
            return;

        if (e.getSource().getEntity() instanceof LivingEntity) {
            Elementals source = null;
            Item item = ((LivingEntity) e.getSource().getEntity()).getMainHandItem().getItem();

            if (item == Items.WOODEN_SWORD) {
                source = Elementals.ANEMO;
            }

            if (item == Items.STONE_SWORD) {
                source = Elementals.GEO;
            }

            if (item == Items.IRON_SWORD) {
                source = Elementals.PYRO;
            }

            if (item == Items.GOLDEN_SWORD) {
                source = Elementals.ELECTRO;
            }

            if (item == Items.DIAMOND_SWORD) {
                source = Elementals.CRYO;
            }

            if (item == Items.NETHERITE_SWORD) {
                source = Elementals.HYDRO;
            }

            if (source != null) {
                // not elemental attack
                if (!Arrays.stream(Elementals.values()).anyMatch(x -> x.is(e.getSource()))) {
                    e.getEntityLiving().hurt(source.create(e.getSource().getEntity()), 5);
                }
            }
        }


    }
}
