package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.artifacts.base.IArtifactSet;
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
    private static final ResourceLocation charactersRegistryName = new ResourceLocation(GenshinImpactMod.ModID, "genshin_characters");
    private static final ResourceLocation artifactsRegistryName = new ResourceLocation(GenshinImpactMod.ModID, "genshin_artifacts");
    private static Supplier<IForgeRegistry<IGenshinPlayer>> CHARACTERS;
    private static Supplier<IForgeRegistry<IArtifactSet>> ARTIFACTS;

    public static IForgeRegistry<IGenshinPlayer> characters() {
        return get(CHARACTERS, "Genshin characters");
    }

    public static IForgeRegistry<IArtifactSet> artifacts() {
        return get(ARTIFACTS, "Genshin artifact sets");
    }

    private static <V extends IForgeRegistryEntry<V>> IForgeRegistry<V> get(Supplier<IForgeRegistry<V>> supplier, String regisrtyName) {
        if (supplier == null) {
            String msg = String.format("Trying to get %s registry earlier than it creates!", regisrtyName);
            CrashReport report = CrashReport.forThrowable(new Exception(msg), msg);
            throw new ReportedException(report);
        }

        return supplier.get();
    }

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        CHARACTERS = event.create(new RegistryBuilder<IGenshinPlayer>()
                .setType(IGenshinPlayer.class)
                .setName(charactersRegistryName)
                .setDefaultKey(new ResourceLocation(GenshinImpactMod.ModID, "anemo_traveler"))
        );

        ARTIFACTS = event.create(new RegistryBuilder<IArtifactSet>()
                .setType(IArtifactSet.class)
                .setName(artifactsRegistryName)
                .setDefaultKey(new ResourceLocation(GenshinImpactMod.ModID, "empty"))
        );
    }
}
