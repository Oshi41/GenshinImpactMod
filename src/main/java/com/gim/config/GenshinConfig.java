package com.gim.config;

import com.gim.GenshinImpactMod;
import net.minecraftforge.common.ForgeConfigSpec;

public class GenshinConfig {
    public final ForgeConfigSpec.BooleanValue indicateDamage;
    public final ForgeConfigSpec.BooleanValue indicateReactions;

    public GenshinConfig(ForgeConfigSpec.Builder builder) {

        builder.comment("Genshin Impact Mod settings")
                .push("damage indication");

        indicateDamage = builder
                .comment("Should Genshin Impact Mod shows colored damage indication for different elementals?")
                .translation(String.format("%s.configgui.indicateDamage", GenshinImpactMod.ModID))
                .define("indicateDamage", true);

        indicateReactions = builder
                .comment("Should Genshin Impact Mod shows info about elemental reactions for entities?")
                .translation(String.format("%s.configgui.indicateReactions", GenshinImpactMod.ModID))
                .define("indicateReactions", true);
    }
}
