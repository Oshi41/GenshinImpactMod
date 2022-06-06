package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.entity.Energy;
import com.gim.entity.TextParticle;
import com.gim.entity.Shield;
import com.gim.entity.Tornado;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class Entities {

    public static final EntityType<Shield> shield_entity_type = null;
    public static final EntityType<Tornado> tornado_entity_type = null;

    public static final EntityType<TextParticle> text_particle_entity_type = null;

    public static final EntityType<Energy> energy_type = null;

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(
                new EntityType<>(Shield::new,
                        MobCategory.AMBIENT,
                        true,
                        true,
                        true,
                        false,
                        ImmutableSet.<Block>builder().build(),
                        new EntityDimensions(0.5f, 0.5f, true),
                        16,
                        5)
                        .setRegistryName(GenshinImpactMod.ModID, "shield_entity_type"),

                new EntityType<Tornado>(Tornado::new,
                        MobCategory.AMBIENT,
                        true,
                        true,
                        true,
                        false,
                        ImmutableSet.<Block>builder().build(),
                        new EntityDimensions(2, 5, true),
                        16,
                        5)
                        .setRegistryName(GenshinImpactMod.ModID, "tornado_entity_type"),

                new EntityType<>(TextParticle::new,
                        MobCategory.AMBIENT,
                        true,
                        true,
                        true,
                        false,
                        ImmutableSet.<Block>builder().build(),
                        new EntityDimensions(2.5f, 0.1f, true),
                        16,
                        5)
                        .setRegistryName(GenshinImpactMod.ModID, "text_particle_entity_type"),

                new EntityType<>(Energy::new,
                        MobCategory.AMBIENT,
                        true,
                        true,
                        true,
                        false,
                        ImmutableSet.<Block>builder().build(),
                        new EntityDimensions(0.5f, 0.5f, true),
                        16 * 2,
                        5)
                        .setRegistryName(GenshinImpactMod.ModID, "energy_type")
        );
    }
}
