package com.gim.registry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class GenshinModelLayers {
//    public static final ModelLayerLocation ANEMO_TRAVELER_LAYER = create("anemo_traveler");
//
//    private static ModelLayerLocation create(String name) {
//        return new ModelLayerLocation(new ResourceLocation(GenshinImpactMod.ModID, name), name);
//    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e) {
    }
}
