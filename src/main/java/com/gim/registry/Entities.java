package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.entity.*;
import com.gim.entity.hilichurlian.Hilichurl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class Entities {

    public static final EntityType<Shield> shield = null;
    public static final EntityType<Tornado> tornado = null;

    public static final EntityType<Energy> energy_orb = null;
    public static final EntityType<AnemoBlade> anemo_blade = null;
    public static final EntityType<ParametricTransformer> parametric_transformer = null;
    public static final EntityType<Hilichurl> hilichurl = null;
    public static final EntityType<ThrowableItem> throwable_item = null;

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(

                registerType(EntityType.Builder.<Shield>of(Shield::new, MobCategory.MISC)
                                .fireImmune()
                                .sized(.5f, .5f)
                                .clientTrackingRange(4),
                        "shield"),

                registerType(EntityType.Builder.<Tornado>of(Tornado::new, MobCategory.MISC)
                                .sized(2, 5),
                        "tornado"),

                registerType(EntityType.Builder.<Energy>of(Energy::new, MobCategory.MISC)
                                .sized(.5f, .5f)
                                .fireImmune(),
                        "energy_orb"),

                registerType(EntityType.Builder.<AnemoBlade>of(AnemoBlade::new, MobCategory.MISC)
                                .sized(2.5f, 0.5f),
                        "anemo_blade"
                ),

                registerType(EntityType.Builder.<ParametricTransformer>of(ParametricTransformer::new, MobCategory.MISC)
                                .sized(1, 1),
                        "parametric_transformer"
                ),

                registerType(EntityType.Builder.of(Hilichurl::new, MobCategory.MONSTER)
                                .sized(1, 2),
                        "hilichurl"
                ),

                registerType(EntityType.Builder.<ThrowableItem>of(ThrowableItem::new, MobCategory.MISC)
                                .sized(0.3f, 0.3f),
                        "throwable_item"
                )
        );
    }

    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(parametric_transformer, ParametricTransformer.createAttributes().build());
        event.put(hilichurl, Hilichurl.createAttributes().build());
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
