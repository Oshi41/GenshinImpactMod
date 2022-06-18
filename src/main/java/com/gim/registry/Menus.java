package com.gim.registry;

import com.gim.GenshinImpactMod;
import com.gim.menu.ArtifactsStationMenu;
import com.gim.menu.ConstellationMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(GenshinImpactMod.ModID)
public class Menus {
    public static final MenuType<ConstellationMenu> constellation = null;
    public static final MenuType<ArtifactsStationMenu> artifacts_station = null;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MenuType<?>> event) {
        event.getRegistry().registerAll(
                IForgeMenuType.create(ConstellationMenu::new)
                        .setRegistryName(GenshinImpactMod.ModID, "constellation"),

                IForgeMenuType.create(ArtifactsStationMenu::new)
                        .setRegistryName(GenshinImpactMod.ModID, "artifacts_station")
        );
    }
}
