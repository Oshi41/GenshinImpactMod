package com.gim.tests;

import com.gim.GenshinImpactMod;
import com.gim.artifacts.base.ArtifactProperties;
import com.gim.artifacts.base.ArtifactRarity;
import com.gim.artifacts.base.ArtifactSlotType;
import com.gim.artifacts.base.ArtifactStat;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.items.ArtefactItem;
import com.gim.registry.Capabilities;
import com.gim.tests.register.CustomGameTest;
import com.gim.tests.register.TestHelper;
import com.google.common.collect.ImmutableMap;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GameTestHolder
public class ArtifactsTests {

    private static final DecimalFormat format = new DecimalFormat("###.##");

    @CustomGameTest(attempts = 3)
    public void artifacts_probability(GameTestHelper helper) {
        ServerPlayer serverPlayer = TestHelper.createPlayer(helper, false);
        final double end = 4000;

        // inacuracy based on total retry count.
        // for 100 we have near 50% inaccuracy
        // for 4 000 inaccuracy is near 5%
        double inacurancy = 1 / (end / 200.) * 100.;

        GenshinImpactMod.LOGGER.debug(String.format("Current artifact inaccuracy is %s", format.format(inacurancy)));
        GenshinImpactMod.LOGGER.debug(String.format("Total generation amount is %s", format.format(end)));

        for (ArtifactSlotType slot : ArtifactSlotType.values()) {
            HashMap<ArtifactStat, Integer> probabilityMain = new HashMap<>();
            HashMap<ArtifactStat, Integer> probabilitySub = new HashMap<>();
            HashMap<ArtifactStat, Integer> probabilityIncreaseSub = new HashMap<>();

            for (ArtifactRarity rarity : ArtifactRarity.values()) {
                for (int i = 0; i < end; i++) {
                    ArtifactProperties props = new ArtifactProperties(rarity, slot, serverPlayer.getRandom());

                    if (props.getSubModifiers().stream().map(ArtifactProperties::getPrimal).distinct().count() != props.getSubModifiers().size()) {
                        List<String> stats = props.getSubModifiers().stream().map(x -> x.getPrimal().toString()).toList();
                        helper.fail(String.format("Artifact contains repeating substats: %s", String.join(", ", stats)));
                    }

                    probabilityMain.compute(props.getPrimal(), (artifactStat, integer) -> integer == null ? 1 : integer + 1);
                    for (ArtifactProperties subModifier : props.getSubModifiers()) {
                        probabilitySub.compute(subModifier.getPrimal(), (artifactStat, integer) -> integer == null ? 1 : integer + 1);
                    }

                    int initCount = props.getSubModifiers().size();

                    // perform levelling
                    props = props.addExp((int) props.getRarity().getMaxExp(), slot, serverPlayer.getRandom(), null);
                    if (props.getSubModifiers().stream().map(ArtifactProperties::getPrimal).distinct().count() != props.getSubModifiers().size()) {
                        List<String> stats = props.getSubModifiers().stream().map(x -> x.getPrimal().toString()).toList();
                        helper.fail(String.format("Artifact contains repeating substats after levelling: %s", String.join(", ", stats)));
                    }

                    int stepsCount = props.getRarity().getMaxLevel() / 4;
                    final ArtifactProperties link = props;

                    int totalSteps = props.getSubModifiers().stream().map(x -> link.getRarity().getLevel(x.getExp())).filter(x -> x > 0).mapToInt(x -> x).sum()
                            + (props.getSubModifiers().size() - initCount);

                    if (stepsCount != totalSteps) {
                        helper.fail(String.format("[%s] increased stats [%s] times instead of [%s]", slot, totalSteps, stepsCount));
                    }

                    for (ArtifactProperties subModifier : props.getSubModifiers()) {
                        int level = props.getRarity().getLevel(subModifier.getExp());
                        if (level <= 0)
                            continue;

                        // sub-stat was increased
                        probabilityIncreaseSub.compute(subModifier.getPrimal(), (key, x) -> x == null ? level : x + level);
                    }
                }
            }

            ImmutableMap<ArtifactStat, Double> slotSubStats = slot.getSub();
            ImmutableMap<ArtifactStat, Double> slotPrimalStats = slot.getPrimal();

            double totalEnd = ArtifactRarity.values().length * end;

            if (slotPrimalStats.size() != probabilityMain.size()) {
                helper.fail(String.format("After %s tries slot type %s didn't receive all types of primal stats", totalEnd, slot));
            }

            for (Map.Entry<ArtifactStat, Integer> e : probabilityMain.entrySet()) {
                double percentage = e.getValue() / totalEnd * 100;
                Double idealPercentage = slotPrimalStats.get(e.getKey());
                double diff = Math.abs(idealPercentage - percentage);

                if (diff > inacurancy) {
                    helper.fail(String.format("Artifact slot [%s], after [%s] tries test generated [%s] artifacts with current stat [%s].\n" +
                                    "Probability should be [%s], in fact it's [%s]",
                            slot,
                            totalEnd,
                            e.getValue(),
                            e.getKey(),
                            format.format(idealPercentage),
                            format.format(percentage)));
                } else {
                    GenshinImpactMod.LOGGER.debug(String.format("[%s] [%s] generated [%s] times. Percentage [%s]%%, ideal is [%s]%% diff [%s]",
                            slot,
                            e.getKey(),
                            totalEnd,
                            format.format(percentage),
                            format.format(idealPercentage),
                            format.format(diff)));
                }
            }

            if (slotSubStats.size() != probabilitySub.size()) {
                helper.fail(String.format("After [%s] tries slot type [%s] didn't receive all sub-types", totalEnd, slot));
            }

            final double totalSubValues = probabilitySub.values().stream().mapToInt(value -> value).sum();

            GenshinImpactMod.LOGGER.debug("Total sub-stats are [" + format.format(totalSubValues), "], ["
                    + format.format(totalSubValues / totalEnd) + "] per item");

            for (Map.Entry<ArtifactStat, Integer> e : probabilitySub.entrySet()) {
                double percentage = e.getValue() / totalEnd * 100.;
                double ideal = slotSubStats.get(e.getKey());
                double diff = Math.abs(ideal - percentage);

                if (diff > inacurancy) {
                    helper.fail(String.format("Artifact slot [%s], total are [%s] sub stats, test generated [%s] artifacts with current sub-stat [%s].\n" +
                                    "Probability should be [%s], in fact it's [%s]",
                            slot,
                            (int) totalSubValues,
                            e.getValue(),
                            e.getKey(),
                            format.format(ideal),
                            format.format(percentage)));
                } else {
                    GenshinImpactMod.LOGGER.debug(String.format("[%s] [%s] generated [%s] times. Current [%s]%%, ideal [%s]%%, diff [%s]",
                            slot,
                            e.getKey(),
                            e.getValue(),
                            format.format(percentage),
                            format.format(ideal),
                            format.format(diff)));
                }
            }
        }
    }

    @CustomGameTest
    public void artifacts_statsIncrease_probability(GameTestHelper helper) {
        ServerPlayer serverPlayer = TestHelper.createPlayer(helper, true);
        IGenshinInfo genshinInfo = TestHelper.getCap(helper, serverPlayer, Capabilities.GENSHIN_INFO);

        HashMap<ArtifactSlotType, ItemStack> map = new HashMap<>();
        for (ArtifactSlotType slotType : ArtifactSlotType.values()) {
            Item artifact = ForgeRegistries.ITEMS.getValues().stream().filter(x -> x instanceof ArtefactItem && ((ArtefactItem) x).getType() == slotType)
                    .findFirst().orElse(null);

            if (artifact == null) {
                helper.fail(String.format("Cannpt find any registered artifact item for slot %s", slotType));
            }
        }

        for (Map.Entry<ArtifactSlotType, ItemStack> e : map.entrySet()) {
            ItemStack artifact = e.getValue();

            // todo
        }
    }
}
