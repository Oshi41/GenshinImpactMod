package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.entity.Energy;
import com.gim.entity.Shield;
import com.gim.entity.Tornado;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class Entities {

    public static final EntityType<Shield> shield_entity_type = null;
    public static final EntityType<Tornado> tornado_entity_type = null;

    public static final EntityType<Energy> energy_type = null;

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(

                registerType(EntityType.Builder.<Shield>of(Shield::new, MobCategory.MISC)
                                .fireImmune()
                                .sized(.5f, .5f)
                                .clientTrackingRange(4),
                        "shield_entity_type"),

                registerType(EntityType.Builder.<Tornado>of(Tornado::new, MobCategory.MISC)
                                .sized(2, 5),
                        "tornado_entity_type"),

                registerType(EntityType.Builder.<Energy>of(Energy::new, MobCategory.MISC)
                                .sized(.5f, .5f)
                                .fireImmune(),
                        "energy_type")
        );
    }

    /**
     * Creates type from
     *
     * @param builder - entity type builder
     * @param name    - name of entity
     */
    private static EntityType registerType(EntityType.Builder builder, String name) {
        return (EntityType) builder.build(name)
                .setRegistryName(GenshinImpactMod.ModID, name);
    }
}
