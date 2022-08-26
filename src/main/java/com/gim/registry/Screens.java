package com.gim.registry;

import com.gim.client.screen.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Screens {
    public static void register() {
        MenuScreens.register(Menus.constellation, ConstellationScreen::new);
        MenuScreens.register(Menus.artifacts_station, ArtifactsStationScreen::new);
        MenuScreens.register(Menus.artifacts_forge, ArtifactsForgeScreen::new);
        MenuScreens.register(Menus.level_station, LevelStationScreen::new);
        MenuScreens.register(Menus.skill_station, SkillStationScreen::new);
        MenuScreens.register(Menus.parametric_transformer, ParametricTransformerScreen::new);
    }
}
