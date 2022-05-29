package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.client.CustomTextureRender;
import com.gim.client.IceRender;
import com.gim.client.ShieldLayerRender;
import com.gim.client.players.anemo_traveler.TornadoRenderer;
import com.gim.entity.Shield;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Renders {


    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers e) {

        //////////////////////
        // ENTITIES
        //////////////////////
        e.registerEntityRenderer(Entities.shield_entity_type, c -> new CustomTextureRender<>(c) {
            @Override
            public ResourceLocation getTextureLocation(Shield e) {
                return switch (e.getElemental()) {
                    case GEO ->
                            new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/yellow_crystal.png");
                    case CRYO ->
                            new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/light_blue_crystal.png");
                    case HYDRO ->
                            new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/blue_crystal.png");
                    case ELECTRO ->
                            new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/magenta_crystal.png");
                    case DENDRO ->
                            new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/green_crystal.png");
                    case ANEMO ->
                            new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/white_crystal.png");
                    case PYRO -> new ResourceLocation(GenshinImpactMod.ModID, "textures/entity/shield/red_crystal.png");
                    default -> null;
                };
            }
        });

        e.registerEntityRenderer(Entities.tornado_entity_type, TornadoRenderer::new);
    }

    @SubscribeEvent
    public static void onBakeModel(final EntityRenderersEvent.AddLayers event) {

        List<LivingEntityRenderer> list = event.getSkins().stream().map(x -> (LivingEntityRenderer) event.getSkin(x)).toList();
        for (EntityType type : ForgeRegistries.ENTITIES.getValues()) {
            try {
                LivingEntityRenderer renderer = event.getRenderer(type);
                if (renderer != null) {
                    list.add(renderer);
                }
            } catch (Exception e) {
                GenshinImpactMod.LOGGER.debug(e);
            }
        }

        for (LivingEntityRenderer renderer : list) {
            injectLayers(renderer);
        }
    }

    /**
     * Injecting layers for custom renders
     *
     * @param renderer - current living entity render
     */
    private static void injectLayers(LivingEntityRenderer renderer) {
        try {
            renderer.addLayer(new ShieldLayerRender(renderer));
            renderer.addLayer(new IceRender(renderer));
        } catch (Exception e) {
            GenshinImpactMod.LOGGER.debug(e);
        }
    }
}
