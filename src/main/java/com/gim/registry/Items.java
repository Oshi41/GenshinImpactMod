package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.artifacts.base.ArtifactSlotType;
import com.gim.items.ArtefactItem;
import com.gim.items.ConstellationItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Arrays;

@ObjectHolder(GenshinImpactMod.ModID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Items {
    public static final ConstellationItem anemo_traveler_star = null;

    public static final ArtefactItem adventure_clock = null;
    public static final ArtefactItem adventure_cup = null;
    public static final ArtefactItem adventure_crown = null;
    public static final ArtefactItem adventure_feather = null;
    public static final ArtefactItem adventure_flower = null;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Item> event) {

        event.getRegistry().registerAll(
                new ConstellationItem(new Item.Properties()
                        .stacksTo(4)
                        .setNoRepair(),
                        Lazy.of(() -> GenshinCharacters.ANEMO_TRAVELER))
                        .setRegistryName(GenshinImpactMod.ModID, "anemo_traveler_star")
                );

        event.getRegistry().registerAll(createSet("adventure"));
    }

    private static Item[] createSet(String baseName) {
        return Arrays.stream(ArtifactSlotType.values()).map(x ->
                        new ArtefactItem(x).setRegistryName(GenshinImpactMod.ModID, String.format("%s_%s", baseName, x.name().toLowerCase())))
                .toArray(ArtefactItem[]::new);
    }

}
