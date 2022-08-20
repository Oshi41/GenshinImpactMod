package com.gim.config;

import com.gim.GenshinImpactMod;
import net.minecraftforge.common.ForgeConfigSpec;

public class GenshinConfig {
    public final ForgeConfigSpec.BooleanValue indicateDamage;
    public final ForgeConfigSpec.BooleanValue indicateReactions;
    public final ForgeConfigSpec.IntValue levelUpTime;
    public final ForgeConfigSpec.DoubleValue levelScaling;
    public final ForgeConfigSpec.DoubleValue maxLevelAttackMultiplier;

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
                .comment("Time in minutes when world should spawn stronger mobs")
                .translation(String.format("%s.configgui.levelUpTime", GenshinImpactMod.ModID))
                .defineInRange("levelUpTime", 60 * 24 * 2, 10, Integer.MAX_VALUE);

        levelScaling = builder
                .comment("Multiplier for common attributes: attack/defence/hp/special attribute for character. Means how much more entity on max level if bigger than entity on 1 level")
                .translation(String.format("%s.configgui.levelScaling", GenshinImpactMod.ModID))
                .defineInRange("levelScaling", 5., 1, Integer.MAX_VALUE);

        maxLevelAttackMultiplier = builder
                .comment("How much damage entity can make on max level. By default it's 3x")
                .translation(String.format("%s.configgui.maxLevelAttackMultiplier", GenshinImpactMod.ModID))
                .defineInRange("maxLevelAttackMultiplier",  3,1., Integer.MAX_VALUE);

    }
}
