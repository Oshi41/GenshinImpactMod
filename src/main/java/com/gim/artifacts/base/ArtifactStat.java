package com.gim.artifacts.base;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.util.Lazy;
import oshi.util.tuples.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public enum ArtifactStat {
    HP(UUID.fromString("7bdfd2ab-18d0-4c22-9fb3-66cd8b464732"), "hp_stat", AttributeModifier.Operation.ADDITION, () -> Attributes.MAX_HEALTH, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(12.9, 32.4));
        put(ArtifactRarity.TWO, new Pair<>(25.8, 55.1));
        put(ArtifactRarity.THREE, new Pair<>(43.0, 189.3));
        put(ArtifactRarity.FOUR, new Pair<>(64.5, 357.1));
        put(ArtifactRarity.FIVE, new Pair<>(71.7, 478.0));
    }}), ATTACK(UUID.fromString("7bdfd2ab-18d1-4c22-9fb3-66cd8b464732"), "attack_stat", AttributeModifier.Operation.ADDITION, () -> Attributes.ATTACK_DAMAGE, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(1., 2.1));
        put(ArtifactRarity.TWO, new Pair<>(17., 3.6));
        put(ArtifactRarity.THREE, new Pair<>(2.8, 12.3));
        put(ArtifactRarity.FOUR, new Pair<>(4.2, 23.2));
        put(ArtifactRarity.FIVE, new Pair<>(4.7, 31.1));
    }}), HP_PERCENT(UUID.fromString("7bdfd2ab-18d2-4c22-9fb3-66cd8b464732"), "hp%_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> Attributes.MAX_HEALTH, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),
    ATTACK_PERCENT(UUID.fromString("7bdfd2ab-18d3-4c22-9fb3-66cd8b464732"), "attack%_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> Attributes.ATTACK_DAMAGE, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    PYRO(UUID.fromString("7bdfd2ab-18d4-4c22-9fb3-66cd8b464732"), "pyro%_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> com.gim.registry.Attributes.pyro_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    CRYO(UUID.fromString("7bdfd2ab-18d5-4c22-9fb3-66cd8b464732"), "cryo%_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> com.gim.registry.Attributes.cryo_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    HYDRO(UUID.fromString("7bdfd2ab-18d6-4c22-9fb3-66cd8b464732"), "hydro%_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> com.gim.registry.Attributes.hydro_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    ANEMO(UUID.fromString("7bdfd2ab-18d7-4c22-9fb3-66cd8b464732"), "anemo%_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> com.gim.registry.Attributes.anemo_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    GEO(UUID.fromString("7bdfd2ab-18d8-4c22-9fb3-66cd8b464732"), "geo%_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> com.gim.registry.Attributes.geo_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    ELECTRO(UUID.fromString("7bdfd2ab-18d9-4c22-9fb3-66cd8b464732"), "electro%_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> com.gim.registry.Attributes.electro_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}), //        DENDRO(UUID.fromString("7bdfd2ab-18da-4c22-9fb3-66cd8b464732"), "dendro%_stat", AttributeModifier.Operation.MULTIPLY_TOTAL,
    //                () -> com.gim.registry.Attributes.dendro_bonus,
//                new HashMap<>() {{
//                    put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
//                    put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
//                    put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
//                    put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
//                    put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
//                }}),
    MAJESTY_PERCENT(UUID.fromString("7bdfd2ab-18db-4c22-9fb3-66cd8b464732"), "majesty_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> com.gim.registry.Attributes.elemental_majesty, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(12.6, 31.6));
        put(ArtifactRarity.TWO, new Pair<>(16.8, 35.8));
        put(ArtifactRarity.THREE, new Pair<>(21., 92.3));
        put(ArtifactRarity.FOUR, new Pair<>(25.2, 139.3));
        put(ArtifactRarity.FIVE, new Pair<>(28., 186.5));
    }}),

    RECHARGE(UUID.fromString("7bdfd2ab-18dc-4c22-9fb3-66cd8b464732"), "recharge_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> com.gim.registry.Attributes.recharge_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.5, 8.8));
        put(ArtifactRarity.TWO, new Pair<>(4.7, 10.9));
        put(ArtifactRarity.THREE, new Pair<>(5.8, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(7., 38.7));
        put(ArtifactRarity.FIVE, new Pair<>(7.8, 51.8));
    }}),

    CRIT_RATE(UUID.fromString("7bdfd2ab-18de-4c22-9fb3-66cd8b464732"), "crit_rate_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> com.gim.registry.Attributes.crit_rate, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(2.1, 5.3));
        put(ArtifactRarity.TWO, new Pair<>(2.8, 9.2));
        put(ArtifactRarity.THREE, new Pair<>(3.5, 15.4));
        put(ArtifactRarity.FOUR, new Pair<>(4.2, 23.2));
        put(ArtifactRarity.FIVE, new Pair<>(4.7, 31.1));
    }}),

    CRIT_DAMAGE(UUID.fromString("7bdfd2ab-18df-4c22-9fb3-66cd8b464732"), "crit_damage_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> com.gim.registry.Attributes.crit_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(4.2, 10.5));
        put(ArtifactRarity.TWO, new Pair<>(5.6, 15.9));
        put(ArtifactRarity.THREE, new Pair<>(7., 30.8));
        put(ArtifactRarity.FOUR, new Pair<>(8.4, 46.4));
        put(ArtifactRarity.FIVE, new Pair<>(9.3, 62.2));
    }}),

    HEAL(UUID.fromString("d500297f-2723-485b-8f51-a6b9342ac94c"), "heal_stat", AttributeModifier.Operation.MULTIPLY_TOTAL, () -> com.gim.registry.Attributes.heal_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(2.4, 6.1));
        put(ArtifactRarity.TWO, new Pair<>(3.2, 9.5));
        put(ArtifactRarity.THREE, new Pair<>(4., 17.8));
        put(ArtifactRarity.FOUR, new Pair<>(4.8, 26.8));
        put(ArtifactRarity.FIVE, new Pair<>(5.4, 35.9));
    }}),

    DEFENCE_PERCENT(UUID.fromString("d500297f-2723-485b-9f51-a6b9342ac94c"), "defence_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.defence, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.9, 9.9));
        put(ArtifactRarity.TWO, new Pair<>(5.2, 14.2));
        put(ArtifactRarity.THREE, new Pair<>(6.6, 28.8));
        put(ArtifactRarity.FOUR, new Pair<>(7.9, 43.5));
        put(ArtifactRarity.FIVE, new Pair<>(8.7, 58.3));
    }}),

    ;

    private final UUID id;
    private final String name;
    private final AttributeModifier.Operation operation;
    private final Lazy<Attribute> attributeLazy;
    private final Map<ArtifactRarity, Pair<Double, Double>> valuesMap;

    ArtifactStat(UUID id, String name, AttributeModifier.Operation operation, Supplier<Attribute> attribute, Map<ArtifactRarity, Pair<Double, Double>> valuesMap) {
        this.id = id;
        this.name = name;
        this.operation = operation;
        this.attributeLazy = Lazy.of(attribute);
        this.valuesMap = valuesMap;
    }

    /**
     * Applying current stat for ImmutableMap.Builder
     *
     * @param builder    - map builder
     * @param props      - artifact props
     * @param multiplier - stat multiplier
     */
    public void apply(Multimap<Attribute, AttributeModifier> builder, ArtifactProperties props, double multiplier) {
        ArtifactRarity rarity = props.getRarity();
        double value = getForLevel(rarity, rarity.getLevel(props.getExp()));
        AttributeModifier modifier = new AttributeModifier(id, name, value * multiplier, operation);
        builder.put(attributeLazy.get(), modifier);
    }

    public double getForLevel(ArtifactRarity rarity, int level) {
        Pair<Double, Double> pair = valuesMap.get(rarity);
        // linear function
        double value = pair.getA() + (pair.getB() - pair.getA()) / rarity.getMaxLevel() * level;

        // in multiplier operations we need to get percentages
        if (operation != AttributeModifier.Operation.ADDITION) {
            value /= 100.;
        }

        return value;
    }

    public Attribute getAttribute() {
        return attributeLazy.get();
    }

    public AttributeModifier.Operation getOperation() {
        return operation;
    }
}
