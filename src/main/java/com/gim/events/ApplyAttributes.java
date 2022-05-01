package com.gim.events;

import com.gim.GenshinImpactMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ApplyAttributes {

    public static <T> T safeCast(Object o, Class<T> clazz) {
        return clazz != null && clazz.isInstance(o) ? clazz.cast(o) : null;
    }

    @SubscribeEvent
    public static void onApply(EntityAttributeModificationEvent event) {

        List<Attribute> attributes = ForgeRegistries.ATTRIBUTES.getValues().stream()
                .filter(x -> x.getRegistryName().getNamespace().equals(GenshinImpactMod.ModID))
                .toList();

        ForgeRegistries.ENTITIES.getValues().stream()
                .filter(x -> x.getBaseClass().isAssignableFrom(LivingEntity.class))
                .map(x -> ((EntityType) x))
                .forEach(x -> attributes.forEach(attribute -> event.add(x, attribute)));
    }
}
