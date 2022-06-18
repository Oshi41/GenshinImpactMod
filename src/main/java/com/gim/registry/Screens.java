package com.gim.registry;

import com.gim.client.screen.ArtifactsStationScreen;
import com.gim.client.screen.ConstellationScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Screens {
    public static void register() {
        MenuScreens.register(Menus.constellation, ConstellationScreen::new);
        MenuScreens.register(Menus.artifacts_station, ArtifactsStationScreen::new);
    }
}
