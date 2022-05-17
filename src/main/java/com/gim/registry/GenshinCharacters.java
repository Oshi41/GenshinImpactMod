package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.capability.genshin.GenshinInfo;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.players.AnemoTraveler;
import com.gim.players.base.IGenshinPlayer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
@ObjectHolder(GenshinImpactMod.ModID)
public class GenshinCharacters {
    public static final IGenshinPlayer anemo_traveler = null;

    @SubscribeEvent
    public static void onRegister(RegistryEvent.Register<IGenshinPlayer> e) {
        e.getRegistry().registerAll(
                new AnemoTraveler().setRegistryName(GenshinImpactMod.ModID, "anemo_traveler")
        );
    }
}
