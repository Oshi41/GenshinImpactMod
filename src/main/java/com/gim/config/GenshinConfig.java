package com.gim.config;

import com.gim.GenshinImpactMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Predicate;

public class GenshinConfig {
    public final ForgeConfigSpec.BooleanValue indicateDamage;
    public final ForgeConfigSpec.BooleanValue indicateReactions;
    public final ForgeConfigSpec.IntValue levelUpTimeMin;
    public final ForgeConfigSpec.DoubleValue levelScaling;
    public final ForgeConfigSpec.DoubleValue maxLevelAttackMultiplier;
    public final ForgeConfigSpec.IntValue parametricTranformerDelayMin;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> talentBooksMaterials;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> ascendingMaterials;

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

        levelUpTimeMin = builder
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
                .defineInRange("maxLevelAttackMultiplier", 3, 1., Integer.MAX_VALUE);

        builder.comment("Parametric Transformer section")
                .push("parametric_transformer");

        parametricTranformerDelayMin = builder
                .comment("Delay in minutes for Parametric Transformer to perform transmutation")
                .translation(String.format("%s.configgui.parametricTranformerDelay", GenshinImpactMod.ModID))
                .defineInRange("parametricTranformerDelay", 60 * 24, 5, Integer.MAX_VALUE);

        Predicate<Object> isCorrectItem = o -> o instanceof String && ForgeRegistries.ITEMS.containsKey(new ResourceLocation((String) o));

        talentBooksMaterials = builder.comment("Possible catalysts for talent books")
                .translation(String.format("%s.configgui.parametricTransformerTalentBooks", GenshinImpactMod.ModID))
                .defineList("talent_books",
                        List.of(
                                "apple",
                                "golden_apple",
                                "enchanted_golden_apple",
                                "yellow_flower",
                                "red_flower",
                                "deadbush",
                                "web",
                                "speckled_melon",
                                "golden_carrot",
                                "poisonous_potato",
                                "melon_block",
                                "glow_berries",
                                "sweet_berries",
                                "gim:wind_astra"
                        ),
                        isCorrectItem);

        ascendingMaterials = builder.comment("Possible catalysts for character ascending")
                .translation(String.format("%s.configgui.parametricTransformerAscending", GenshinImpactMod.ModID))
                .defineList("ascending",
                        List.of(
                                "iron_ingot",
                                "gold_ingot",
                                "copper_ingot",
                                "netherite_ingot",
                                "coal",
                                "diamond",
                                "emerald",
                                "axolotl_bucket"
                        ),
                        isCorrectItem);
    }
}
