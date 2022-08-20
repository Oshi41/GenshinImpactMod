package com.gim.tests;

import com.gim.GenshinImpactMod;
import com.gim.artifacts.base.ArtifactProperties;
import com.gim.artifacts.base.ArtifactRarity;
import com.gim.artifacts.base.ArtifactSlotType;
import com.gim.artifacts.base.ArtifactStat;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.items.ArtefactItem;
import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Capabilities;
import com.gim.tests.register.CustomGameTest;
import com.gim.tests.register.TestHelper;
import com.google.common.collect.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@GameTestHolder
public class ArtifactsTests {

    private static final DecimalFormat format = new DecimalFormat("###.##");

    @CustomGameTest(attempts = 3)
    public void artifacts_probability(GameTestHelper helper) {
        ServerPlayer serverPlayer = TestHelper.createFakePlayer(helper, false);
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

    @CustomGameTest(setupTicks = 3)
    public void artifacts_apply_changeAttributes(GameTestHelper helper) {
        // finds first possible artifact by current type
        // we don't care the type of set for finded artifacts
        HashMap<ArtifactSlotType, ArtefactItem> map = new HashMap<>();

        // finding first existing artifact bu currect slot type
        for (ArtifactSlotType slotType : ArtifactSlotType.values()) {
            Item artifact = ForgeRegistries.ITEMS.getValues().stream().filter(x -> x instanceof ArtefactItem && ((ArtefactItem) x).getType() == slotType)
                    .findFirst().orElse(null);

            if (artifact == null) {
                helper.fail(String.format("Cannot find any registered artifact item for slot %s", slotType));
            }

            map.put(slotType, (ArtefactItem) artifact);
        }

        ServerPlayer serverPlayer = TestHelper.createFakePlayer(helper, true);
        IGenshinInfo genshinInfo = TestHelper.getCap(helper, serverPlayer, Capabilities.GENSHIN_INFO);

        for (Map.Entry<ArtifactSlotType, ArtefactItem> e : map.entrySet()) {
            for (ArtifactRarity rarity : ArtifactRarity.values()) {
                for (ArtifactStat primalStat : e.getKey().getPrimal().keySet()) {
                    // creating test data for currect artifact item
                    // should contains all possible stats inside
                    for (final Map.Entry<ItemStack, Collection<Attribute>> entry : getAllPossibleArtifacts(rarity, primalStat, e.getKey(), e.getValue()).asMap().entrySet()) {
                        // trying to add same item to all characters of player
                        for (IGenshinPlayer character : genshinInfo.getAllPersonages()) {
                            // changing current stack
                            genshinInfo.setCurrentStack(serverPlayer, Lists.newArrayList(character));

                            GenshinEntityData entityData = genshinInfo.getPersonInfo(character);

                            // clear it's on content
                            entityData.getArtifactsContainer().clearContent();
                            // saving old attribute values
                            final Map<Attribute, Double> saved = from(entityData.getAttributes(), entry.getValue());

                            // put artifact inside
                            entityData.getArtifactsContainer().setItem(e.getKey().ordinal(), entry.getKey().copy());

                            // new attribute values
                            Map<Attribute, Double> current = from(entityData.getAttributes(), entry.getValue());
                            // calculate difference
                            MapDifference<Attribute, Double> mapDifference = Maps.difference(saved, current);

                            if (!mapDifference.entriesInCommon().isEmpty()) {
                                final StringBuilder builder = new StringBuilder();
                                mapDifference.entriesInCommon().forEach((attribute, doubleValueDifference) -> {
                                    builder.append(String.format("%s* [%s], [%s] didn't change",
                                            e.getKey(),
                                            rarity.ordinal() + 1,
                                            new TranslatableComponent(attribute.getDescriptionId()).getString()
                                    ));
                                });

                                helper.fail(builder.toString());
                            }

                            // attributes applied on player entity. Should be same as from current character
                            Map<Attribute, Double> fromCurrentPlayer = from(serverPlayer.getAttributes(), entry.getValue());
                            mapDifference = Maps.difference(fromCurrentPlayer, current);
                            if (!mapDifference.entriesDiffering().isEmpty()) {
                                final StringBuilder builder = new StringBuilder();

                                mapDifference.entriesDiffering().forEach((attribute, doubleValueDifference) -> {
                                    builder.append(String.format("%s* [%s], [%s] didn't apply on player",
                                            e.getKey(),
                                            rarity.ordinal() + 1,
                                            new TranslatableComponent(attribute.getDescriptionId()).getString()
                                    ));
                                });

                                helper.fail(builder.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    private Multimap<ItemStack, Attribute> getAllPossibleArtifacts(ArtifactRarity rarity, ArtifactStat primal, ArtifactSlotType slotType, ArtefactItem item) {
        HashMultimap<ItemStack, Attribute> result = HashMultimap.create();
        List<ArtifactStat> statList = slotType.getSub().keySet().stream().filter(x -> x != primal).collect(Collectors.toList());

        // special null value
        statList.add(null);

        for (ArtifactStat stat : statList) {
            List<Attribute> list = new ArrayList<>();

            ArtifactProperties artifactProperties = new ArtifactProperties()
                    .withRarity(rarity)
                    .withPrimal(primal);

            list.add(primal.getAttribute());

            if (stat != null) {
                artifactProperties.withSub(stat);
                list.add(stat.getAttribute());
            }

            ItemStack copy = item.getDefaultInstance();
            item.save(copy, artifactProperties);
            result.putAll(copy, list);
        }

        return result;
    }

    private Map<Attribute, Double> from(AttributeMap map, Collection<Attribute> attrs) {
        HashMap<Attribute, Double> hashMap = new HashMap<>();
        for (Attribute attribute : attrs) {
            hashMap.put(attribute, map.getValue(attribute));
        }
        return hashMap;
    }
}
