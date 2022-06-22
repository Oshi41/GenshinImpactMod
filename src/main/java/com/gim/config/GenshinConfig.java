package com.gim.config;

import com.gim.GenshinImpactMod;
import net.minecraftforge.common.ForgeConfigSpec;

public class GenshinConfig {
    public final ForgeConfigSpec.BooleanValue indicateDamage;
    public final ForgeConfigSpec.BooleanValue indicateReactions;
    public final ForgeConfigSpec.IntValue levelUpTime;
    public final ForgeConfigSpec.DoubleValue levelScaling;

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

        levelUpTime = builder
                .comment("Time in ticks when world should spawn stronger mobs (in minutes)")
                .translation(String.format("%s.configgui.levelUpTime", GenshinImpactMod.ModID))
                .defineInRange("levelUpTime", 60 * 24 * 2, 10, Integer.MAX_VALUE);

        levelScaling = builder
                .comment("Value for level attributes scaling. Currently scales 3 attributes: health, attack and armor")
                .translation(String.format("%s.configgui.levelScaling", GenshinImpactMod.ModID))
                .defineInRange("levelUpTime", 1.2, 1 + Double.MIN_NORMAL, Integer.MAX_VALUE);

    }
}
