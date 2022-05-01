package com.gim.registry;

import com.gim.GenshinImpactMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class ParticleTypes {

    @ObjectHolder("cryo")
    public static final SimpleParticleType CRYO = null;
    @ObjectHolder("pyro")
    public static final SimpleParticleType PYRO = null;
    @ObjectHolder("dendro")
    public static final SimpleParticleType DENDRO = null;
    @ObjectHolder("anemo")
    public static final SimpleParticleType ANEMO = null;
    @ObjectHolder("geo")
    public static final SimpleParticleType GEO = null;
    @ObjectHolder("electro")
    public static final SimpleParticleType ELECTRO = null;
    @ObjectHolder("hydro")
    public static final SimpleParticleType HYDRO = null;

    @SubscribeEvent
    public static void onRegisterParticleTypes(RegistryEvent.Register<ParticleType<?>> event) {
        IForgeRegistry<ParticleType<?>> registry = event.getRegistry();

        registry.registerAll(
                new SimpleParticleType(true).setRegistryName(GenshinImpactMod.ModID, "electro"),
                new SimpleParticleType(true).setRegistryName(GenshinImpactMod.ModID, "cryo"),
                new SimpleParticleType(true).setRegistryName(GenshinImpactMod.ModID, "pyro"),
                new SimpleParticleType(true).setRegistryName(GenshinImpactMod.ModID, "geo"),
                new SimpleParticleType(true).setRegistryName(GenshinImpactMod.ModID, "dendro"),
                new SimpleParticleType(true).setRegistryName(GenshinImpactMod.ModID, "anemo"),
                new SimpleParticleType(true).setRegistryName(GenshinImpactMod.ModID, "hydro")
        );
    }
}
