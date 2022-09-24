package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.artifacts.base.ArtifactSlotType;
import com.gim.items.ArtefactItem;
import com.gim.items.ConstellationItem;
import com.gim.items.GenshinMaterialItem;
import com.gim.items.ParametricTransformerItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
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

    public static final Item freedom_scroll = null;
    public static final Item freedom_scroll_2 = null;

    public static final Item resistance_scroll = null;
    public static final Item resistance_scroll_2 = null;

    public static final Item ballad_scroll = null;
    public static final Item ballad_scroll_2 = null;

    public static final Item brilliant = null;
    public static final Item brilliant_large = null;
    public static final Item mask = null;
    public static final Item hard_mask = null;
    public static final Item dragon_claw = null;
    public static final Item crown = null;

    public static final ArtefactItem adventure_clock = null;
    public static final ArtefactItem adventure_cup = null;
    public static final ArtefactItem adventure_crown = null;
    public static final ArtefactItem adventure_feather = null;
    public static final ArtefactItem adventure_flower = null;

    public static final ParametricTransformerItem parametric_transformer = null;
    public static final Item wish = null;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Item> event) {

        event.getRegistry().registerAll(
                new ConstellationItem(new Item.Properties()
                        .stacksTo(4)
                        .setNoRepair(),
                        Lazy.of(() -> GenshinCharacters.ANEMO_TRAVELER))
                        .setRegistryName(GenshinImpactMod.ModID, "anemo_traveler_star"),

                new GenshinMaterialItem(new GenshinMaterialItem.GenshinProperties().setNoRepair().rarity(Rarity.UNCOMMON))
                        .setRegistryName(GenshinImpactMod.ModID, "freedom_scroll"),
                new GenshinMaterialItem(new GenshinMaterialItem.GenshinProperties().foil().setNoRepair().rarity(Rarity.RARE))
                        .setRegistryName(GenshinImpactMod.ModID, "freedom_scroll_2"),

                new GenshinMaterialItem(new GenshinMaterialItem.GenshinProperties().setNoRepair().rarity(Rarity.UNCOMMON))
                        .setRegistryName(GenshinImpactMod.ModID, "resistance_scroll"),
                new GenshinMaterialItem(new GenshinMaterialItem.GenshinProperties().foil().setNoRepair().rarity(Rarity.RARE))
                        .setRegistryName(GenshinImpactMod.ModID, "resistance_scroll_2"),

                new GenshinMaterialItem(new GenshinMaterialItem.GenshinProperties().setNoRepair().rarity(Rarity.UNCOMMON))
                        .setRegistryName(GenshinImpactMod.ModID, "ballad_scroll"),
                new GenshinMaterialItem(new GenshinMaterialItem.GenshinProperties().foil().setNoRepair().rarity(Rarity.RARE))
                        .setRegistryName(GenshinImpactMod.ModID, "ballad_scroll_2"),

                new GenshinMaterialItem(new GenshinMaterialItem.GenshinProperties().setNoRepair().rarity(Rarity.UNCOMMON))
                        .setRegistryName(GenshinImpactMod.ModID, "brilliant"),
                new GenshinMaterialItem(new GenshinMaterialItem.GenshinProperties().setNoRepair().rarity(Rarity.RARE))
                        .setRegistryName(GenshinImpactMod.ModID, "brilliant_large"),
                new GenshinMaterialItem(new GenshinMaterialItem.GenshinProperties().setNoRepair().rarity(Rarity.UNCOMMON))
                        .setRegistryName(GenshinImpactMod.ModID, "mask"),
                new GenshinMaterialItem(new GenshinMaterialItem.GenshinProperties().setNoRepair().rarity(Rarity.RARE))
                        .setRegistryName(GenshinImpactMod.ModID, "hard_mask"),

                new GenshinMaterialItem(new Item.Properties().setNoRepair().rarity(Rarity.EPIC))
                        .setRegistryName(GenshinImpactMod.ModID, "crown"),
                new GenshinMaterialItem(new Item.Properties().setNoRepair().rarity(Rarity.EPIC))
                        .setRegistryName(GenshinImpactMod.ModID, "dragon_claw"),

                new ParametricTransformerItem().setRegistryName(GenshinImpactMod.ModID, "parametric_transformer"),
                new ForgeSpawnEggItem(() -> Entities.hilichurl, 15582019, 5843472,
                        new Item.Properties().tab(CreativeModeTab.TAB_MISC))
                        .setRegistryName(GenshinImpactMod.ModID, "hilichurl_egg"),

                new GenshinMaterialItem(new Item.Properties().setNoRepair().rarity(Rarity.EPIC))
                        .setRegistryName(GenshinImpactMod.ModID, "wish")
        );

        event.getRegistry().registerAll(createSet("adventure"));
    }

    private static Item[] createSet(String baseName) {
        return Arrays.stream(ArtifactSlotType.values()).map(x ->
                        new ArtefactItem(x).setRegistryName(GenshinImpactMod.ModID, String.format("%s_%s", baseName, x.name().toLowerCase())))
                .toArray(ArtefactItem[]::new);
    }

}
