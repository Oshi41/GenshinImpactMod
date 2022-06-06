package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.players.base.IGenshinPlayer;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registries {
    private static final ResourceLocation registryName = new ResourceLocation(GenshinImpactMod.ModID, "genshin_characters");
    private static Supplier<IForgeRegistry<IGenshinPlayer>> CHARACTERS;

    public static IForgeRegistry<IGenshinPlayer> characters() {
        if (CHARACTERS == null) {
            String msg = "Trying to get Genshin character registry earlier than it creates!";
            CrashReport report = CrashReport.forThrowable(new Exception(msg), msg);
            throw new ReportedException(report);
        }

        return CHARACTERS.get();
    }

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        CHARACTERS = event.create(new RegistryBuilder<IGenshinPlayer>()
                .setType(IGenshinPlayer.class)
                .setName(registryName)
                .setDefaultKey(new ResourceLocation(GenshinImpactMod.ModID, "anemo_traveler"))
        );
    }
}
