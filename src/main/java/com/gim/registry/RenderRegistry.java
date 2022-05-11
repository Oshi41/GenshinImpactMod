package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.client.CustomTextureRender;
import com.gim.entity.ShieldEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RenderRegistry {

    private static ModelLayerLocation create(String name) {
        return new ModelLayerLocation(new ResourceLocation(GenshinImpactMod.ModID, name), name);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e) {
    }

    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers e) {

        //////////////////////
        // ENTITIES
        //////////////////////
        e.registerEntityRenderer(EntityRegistry.shield_entity_type, c -> new CustomTextureRender<>(c) {
            @Override
            public ResourceLocation getTextureLocation(ShieldEntity e) {
                return switch (e.getElemental()) {
                    case GEO -> new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/yellow_crystal.png");
                    case CRYO ->
                            new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/light_blue_crystal.png");
                    case HYDRO -> new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/blue_crystal.png");
                    case ELECTRO ->
                            new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/magenta_crystal.png");
                    case DENDRO -> new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/green_crystal.png");
                    case ANEMO -> new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/white_crystal.png");
                    case PYRO -> new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/red_crystal.png");
                    default -> null;
                };
            }
        });
    }
}
