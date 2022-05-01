package com.gim.events;

import com.gim.GenshinImpactMod;
import com.gim.registry.DamageSources;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Debug {

    @SubscribeEvent
    public static void onSwordHit(AttackEntityEvent e) {
        if (e.getPlayer() == null || e.getEntityLiving() == null)
            return;

        Player player = e.getPlayer();
        Item item = player.getMainHandItem().getItem();

        if (item == Items.IRON_SWORD) {
            e.getEntityLiving().hurt(new DamageSource(DamageSources.PYRO_MESSAGE_ID), 3);
        }

        if (item == Items.GOLDEN_SWORD) {
            e.getEntityLiving().hurt(new DamageSource(DamageSources.ELECTRO_MESSAGE_ID), 3);
        }
    }
}
