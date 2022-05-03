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
    @ObjectHolder("frozen")
    public static final SimpleParticleType FROZEN = null;
    @ObjectHolder("defence_debuff")
    public static final SimpleParticleType DEFENCE_DEBUFF = null;

    @SubscribeEvent
    public static void onRegisterParticleTypes(RegistryEvent.Register<ParticleType<?>> event) {
        IForgeRegistry<ParticleType<?>> registry = event.getRegistry();

        registry.registerAll(
                new SimpleParticleType(false).setRegistryName(GenshinImpactMod.ModID, "electro"),
                new SimpleParticleType(false).setRegistryName(GenshinImpactMod.ModID, "cryo"),
                new SimpleParticleType(false).setRegistryName(GenshinImpactMod.ModID, "pyro"),
                new SimpleParticleType(false).setRegistryName(GenshinImpactMod.ModID, "geo"),
                new SimpleParticleType(false).setRegistryName(GenshinImpactMod.ModID, "dendro"),
                new SimpleParticleType(false).setRegistryName(GenshinImpactMod.ModID, "anemo"),
                new SimpleParticleType(false).setRegistryName(GenshinImpactMod.ModID, "hydro"),
                new SimpleParticleType(false).setRegistryName(GenshinImpactMod.ModID, "frozen"),
                new SimpleParticleType(false).setRegistryName(GenshinImpactMod.ModID, "defence_debuff")
        );
    }
}
