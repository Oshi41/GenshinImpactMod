package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.client.entity.CustomTextureRender;
import com.gim.client.entity.EnergyRenderer;
import com.gim.client.layers.IceRender;
import com.gim.client.layers.ShieldLayerRender;
import com.gim.client.entity.players.anemo_traveler.TornadoRenderer;
import com.gim.entity.Shield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Collectors;

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
        e.registerEntityRenderer(Entities.energy_type, EnergyRenderer::new);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBakeModel(final EntityRenderersEvent.AddLayers event) {

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        List<LivingEntityRenderer> list = dispatcher.getSkinMap().values().stream().map(x -> (LivingEntityRenderer) x).collect(Collectors.toList());
        dispatcher.renderers.values().stream()
                .filter(x -> x instanceof LivingEntityRenderer)
                .map(x -> (LivingEntityRenderer) x)
                .forEach(list::add);

        for (LivingEntityRenderer renderer : list) {
            injectLayers(renderer);
        }
    }

    /**
     * Injecting layers for custom renders
     *
     * @param renderer - current living entity render
     */
    public static void injectLayers(LivingEntityRenderer renderer) {
        try {
            renderer.addLayer(new ShieldLayerRender(renderer));
            renderer.addLayer(new IceRender(renderer));
            // renderer.addLayer(new GenshinInfoRender(renderer));
        } catch (Exception e) {
            GenshinImpactMod.LOGGER.debug("Error during injecting custom layers to LivingEntityRenderer");
            GenshinImpactMod.LOGGER.debug(e);
        }
    }

    /**
     * Customize blocks to use neede render type
     */
    public static void setCustomBlockRender() {
        List.of(Blocks.wind_astra).forEach(x -> ItemBlockRenderTypes.setRenderLayer(x, RenderType.cutout()));
    }
}
