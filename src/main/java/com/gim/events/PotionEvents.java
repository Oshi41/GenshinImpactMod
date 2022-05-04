package com.gim.events;

import com.gim.attack.GenshinMobEffect;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PotionEvents {

    // Server side
    @SubscribeEvent
    public static void onExpired(PotionEvent.PotionExpiryEvent e) {
        if (e.getPotionEffect() != null && e.getPotionEffect().getEffect() instanceof GenshinMobEffect) {
            ((GenshinMobEffect) e.getPotionEffect().getEffect()).endEffect(e.getEntityLiving(), e.getPotionEffect().getAmplifier());
        }
    }
}
