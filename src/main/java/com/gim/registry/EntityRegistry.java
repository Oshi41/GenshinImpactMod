package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.entity.ShieldEntity;
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
public class EntityRegistry {

    public static final EntityType<ShieldEntity> shield_entity_type = null;

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(
                new EntityType<>(ShieldEntity::new,
                        MobCategory.AMBIENT,
                        true,
                        true,
                        true,
                        false,
                        ImmutableSet.<Block>builder().build(),
                        new EntityDimensions(0.5f, 0.5f, true),
                        16,
                        5)
                        .setRegistryName(GenshinImpactMod.ModID, "shield_entity_type")
        );
    }
}
