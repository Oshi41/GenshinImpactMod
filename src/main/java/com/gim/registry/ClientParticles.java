package com.gim.registry;

import com.gim.client.particle.CircleParticle;
import com.gim.client.particle.ElementalParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
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

        Minecraft.getInstance().particleEngine.register(
                ParticleTypes.LIGHTNING,
                x -> new PortalParticle.Provider(x) {
                    @Override
                    public Particle createParticle(SimpleParticleType p_107581_, ClientLevel p_107582_, double p_107583_, double p_107584_, double p_107585_, double p_107586_, double p_107587_, double p_107588_) {
                        Particle particle = super.createParticle(p_107581_, p_107582_, p_107583_, p_107584_, p_107585_, p_107586_, p_107587_, p_107588_);

                        if (particle != null) {
                            particle.scale(3);
                        }

                        return particle;
                    }
                }
        );
    }
}
