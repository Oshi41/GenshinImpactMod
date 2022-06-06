package com.gim.events;

import com.gim.capability.shield.IShield;
import com.gim.registry.Capabilities;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GenshinCapability {

    @SubscribeEvent
    public static void onUpdate(LivingEvent.LivingUpdateEvent e) {
        e.getEntityLiving().getCapability(Capabilities.GENSHIN_INFO).ifPresent(x -> x.tick(e.getEntityLiving()));
        e.getEntityLiving().getCapability(Capabilities.SHIELDS).ifPresent(IShield::tick);
    }
}
