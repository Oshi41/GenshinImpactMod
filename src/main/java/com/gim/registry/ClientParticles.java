package com.gim.registry;

import com.gim.particle.CircleParticle;
import com.gim.particle.ElementalParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.DripParticle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
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

        Minecraft.getInstance().particleEngine.register(
                ParticleTypes.FROZEN,
                CircleParticle.Provider::new
        );

        Minecraft.getInstance().particleEngine.register(
                ParticleTypes.DEFENCE_DEBUFF,
                CircleParticle.Provider::new
        );
    }
}
