package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.players.base.IGenshinPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registries {
    public static final Lazy<IForgeRegistry<IGenshinPlayer>> CHARACTERS = Lazy.of(() -> RegistryManager.ACTIVE.getRegistry(IGenshinPlayer.class));

    @SubscribeEvent
    public static void onNewRegistry(RegistryEvent.NewRegistry event) {
        new RegistryBuilder<IGenshinPlayer>()
                .setType(IGenshinPlayer.class)
                .setName(new ResourceLocation(GenshinImpactMod.ModID, "genshin_characters"))
                .setDefaultKey(new ResourceLocation(GenshinImpactMod.ModID, "anemo_traveler"))
                .create();
    }
}
