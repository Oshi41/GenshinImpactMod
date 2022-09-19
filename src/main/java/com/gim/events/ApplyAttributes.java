package com.gim.events;

import com.gim.GenshinImpactMod;
import com.gim.registry.Entities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ApplyAttributes {
    private static final Lazy<Set<EntityType>> _forbidden = Lazy.of(() ->
            Set.of(Entities.parametric_transformer));

    @SubscribeEvent
    public static void onApply(EntityAttributeModificationEvent event) {

        List<Attribute> genshinAttributes = ForgeRegistries.ATTRIBUTES.getValues().stream().filter(x -> x.getRegistryName().getNamespace().equals(GenshinImpactMod.ModID)).toList();

        for (EntityType<?> entityType : ForgeRegistries.ENTITIES.getValues()) {
            if (!_forbidden.get().contains(entityType) && DefaultAttributes.hasSupplier(entityType)) {
                EntityType<? extends LivingEntity> livingEntityType = (EntityType<? extends LivingEntity>) entityType;
                for (Attribute attribute : genshinAttributes) {
                    event.add(livingEntityType, attribute);
                }
            }
        }
    }
}
