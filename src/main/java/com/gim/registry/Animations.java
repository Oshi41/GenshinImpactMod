package com.gim.registry;

import com.gim.players.base.IGenshinPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Animations {

    @SubscribeEvent
    public static void onAtlasLoading(TextureStitchEvent.Pre event) {
        TextureAtlas atlas = event.getAtlas();
        if (atlas.location().equals(InventoryMenu.BLOCK_ATLAS)) {
            IForgeRegistry<IGenshinPlayer> registry = Registries.characters();

            if (registry != null) {
                registry.getValues().forEach(player -> {
                    ResourceLocation location = player.getRegistryName();

                    event.addSprite(new ResourceLocation(location.getNamespace(), String.format("players/%s/burst", location.getPath())));
                    event.addSprite(new ResourceLocation(location.getNamespace(), String.format("players/%s/skill", location.getPath())));
                });
            }
        }
    }
}
