package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinProvider;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.capability.shield.IShield;
import com.gim.capability.shield.ShieldProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

    public static final Capability<IGenshinInfo> GENSHIN_INFO = CapabilityManager.get(new CapabilityToken<>() {
    });


    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    static class Register {
        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            // For new capabilities add them as well known to CapabilityUpdatePackage

            event.register(IShield.class);
            event.register(IGenshinInfo.class);
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    static class Attach {
        @SubscribeEvent
        public static void attach(AttachCapabilitiesEvent<Entity> e) {
            if (e.getObject() instanceof LivingEntity) {
                e.addCapability(new ResourceLocation(GenshinImpactMod.ModID, "shield"), new ShieldProvider(e.getObject()));
            }

            if (e.getObject() instanceof Player) {
                e.addCapability(new ResourceLocation(GenshinImpactMod.ModID, "genshin"), new GenshinProvider(((Player) e.getObject())));
            }
        }
    }
}
