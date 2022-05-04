package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.capability.IShield;
import com.gim.capability.ShieldProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class Capabilities {
    public static final Capability<IShield> SHIELDS = CapabilityManager.get(new CapabilityToken<>() {
    });


    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    static class Register {
        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IShield.class);
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    static class Attach {
        @SubscribeEvent
        public static void attach(AttachCapabilitiesEvent<Entity> e) {
            if (e.getObject() instanceof LivingEntity) {
                e.addCapability(new ResourceLocation(GenshinImpactMod.ModID, "shield"), new ShieldProvider(e.getObject()));
            }
        }
    }
}
