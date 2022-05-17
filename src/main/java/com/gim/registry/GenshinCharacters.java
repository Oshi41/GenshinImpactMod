package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.players.AnemoTraveler;
import com.gim.players.base.IGenshinPlayer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class GenshinCharacters {
    @ObjectHolder("anemo_traveler")
    public static final IGenshinPlayer ANEMO_TRAVELER = null;

    @SubscribeEvent
    public static void onRegister(RegistryEvent.Register<IGenshinPlayer> e) {
        e.getRegistry().registerAll(
                new AnemoTraveler().setRegistryName(GenshinImpactMod.ModID, "anemo_traveler")
        );
    }
}
