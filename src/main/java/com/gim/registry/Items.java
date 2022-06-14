package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.items.ConstellationItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(GenshinImpactMod.ModID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Items {
    public static final ConstellationItem anemo_traveler_star = null;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Item> event) {

        event.getRegistry().registerAll(
                new ConstellationItem(new Item.Properties()
                        .stacksTo(4)
                        .setNoRepair(),
                        Lazy.of(() -> GenshinCharacters.ANEMO_TRAVELER))
                        .setRegistryName(GenshinImpactMod.ModID, "anemo_traveler_star")
        );
    }
}
