package com.gim.events;

import com.gim.GenshinImpactMod;
import com.gim.registry.Attributes;
import com.gim.registry.Entities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ApplyAttributes {

    // workaround
    private static final List<EntityType> special = new ArrayList<>();

    public static <T> T safeCast(Object o, Class<T> clazz) {
        return clazz != null && clazz.isInstance(o) ? clazz.cast(o) : null;
    }

    private static AttributeSupplier.Builder createDefault() {
        AttributeSupplier.Builder builder = AttributeSupplier.builder();

        ForgeRegistries.ATTRIBUTES.getValues().stream().filter(x -> x.getRegistryName().getNamespace().equals(GenshinImpactMod.ModID)).forEach(builder::add);

        return builder;
    }

    private static void put(EntityAttributeCreationEvent event, EntityType<? extends LivingEntity> entity, AttributeSupplier map) {
        event.put(entity, map);
        special.add(entity);
    }

    @SubscribeEvent
    public static void onApply(EntityAttributeModificationEvent event) {

        List<Attribute> attributes = ForgeRegistries.ATTRIBUTES.getValues().stream().filter(x -> x.getRegistryName().getNamespace().equals(GenshinImpactMod.ModID)).toList();

        ForgeRegistries.ENTITIES.getValues().stream()
                .filter(x -> !special.contains(x)).forEach(x -> {
                    EntityType<? extends LivingEntity> type = (EntityType<? extends LivingEntity>) x;
                    for (Attribute attribute : attributes) {
                        event.add(type, attribute);
                    }
                });

        special.clear();
    }
}
