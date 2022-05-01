package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.particles.ElementalParticle;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class ClientParticles {

    @SubscribeEvent
    public static void onParticlesRegistry(final ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(
                ParticleTypes.ANEMO,
                ElementalParticle.Provider::new
        );

        Minecraft.getInstance().particleEngine.register(
                ParticleTypes.ELECTRO,
                ElementalParticle.Provider::new
        );

        Minecraft.getInstance().particleEngine.register(
                ParticleTypes.GEO,
                ElementalParticle.Provider::new
        );

        Minecraft.getInstance().particleEngine.register(
                ParticleTypes.HYDRO,
                ElementalParticle.Provider::new
        );

        Minecraft.getInstance().particleEngine.register(
                ParticleTypes.PYRO,
                ElementalParticle.Provider::new
        );

        Minecraft.getInstance().particleEngine.register(
                ParticleTypes.DENDRO,
                ElementalParticle.Provider::new
        );

        Minecraft.getInstance().particleEngine.register(
                ParticleTypes.CRYO,
                ElementalParticle.Provider::new
        );
    }
}
