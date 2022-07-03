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
        put(ArtifactRarity.ONE, new Pair<>(1.0, 2.0));
        put(ArtifactRarity.TWO, new Pair<>(2.0, 3.0));
        put(ArtifactRarity.THREE, new Pair<>(3.0, 7.0));
        put(ArtifactRarity.FOUR, new Pair<>(4.0, 10.0));
        put(ArtifactRarity.FIVE, new Pair<>(4.0, 12.0));
    }}), ATTACK(UUID.fromString("93453a4d-ea45-4135-8b3a-0b105cdf8663"), "attack_stat", AttributeModifier.Operation.ADDITION, () -> Attributes.ATTACK_DAMAGE, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(1., 2.1));
        put(ArtifactRarity.TWO, new Pair<>(17., 3.6));
        put(ArtifactRarity.THREE, new Pair<>(2.8, 12.3));
        put(ArtifactRarity.FOUR, new Pair<>(4.2, 23.2));
        put(ArtifactRarity.FIVE, new Pair<>(4.7, 31.1));
    }}), HP_PERCENT(UUID.fromString("53aeb8dc-17d3-4860-859b-175d8bd19451"), "hp%_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> Attributes.MAX_HEALTH, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),
    ATTACK_PERCENT(UUID.fromString("f0231523-1893-4e04-ad6b-63da9773f8a4"), "attack%_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> Attributes.ATTACK_DAMAGE, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    PHYSICAL(UUID.fromString("8f4eff97-ac32-440d-8a3e-38753b6cebdd"), "pyro%_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.physical_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),
    PYRO(UUID.fromString("d60c660c-7b27-46eb-93ca-c0aed458cd73"), "pyro%_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.pyro_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    CRYO(UUID.fromString("3ae64d73-3b0a-46a6-aa90-4937e089e6c0"), "cryo%_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.cryo_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    HYDRO(UUID.fromString("4a2162df-f626-48d0-b70b-fe0db6c586a8"), "hydro%_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.hydro_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    ANEMO(UUID.fromString("e4971e5c-4263-4f7e-a68c-096ff37abacc"), "anemo%_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.anemo_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    GEO(UUID.fromString("5fcca476-2a53-4f93-8a16-46853de7460a"), "geo%_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.geo_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}),

    ELECTRO(UUID.fromString("9542581c-e305-472c-ab40-c11775937525"), "electro%_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.electro_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
        put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
        put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
        put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
    }}), //        DENDRO(UUID.fromString("ea473fef-b9ee-42c7-a9ea-93640de43ea4"), "dendro%_stat", AttributeModifier.Operation.MULTIPLY_BASE,
    //                () -> com.gim.registry.Attributes.dendro_bonus,
//                new HashMap<>() {{
//                    put(ArtifactRarity.ONE, new Pair<>(3.1, 7.9));
//                    put(ArtifactRarity.TWO, new Pair<>(4.2, 9.));
//                    put(ArtifactRarity.THREE, new Pair<>(5.2, 23.1));
//                    put(ArtifactRarity.FOUR, new Pair<>(6.3, 34.8));
//                    put(ArtifactRarity.FIVE, new Pair<>(7., 46.6));
//                }}),
    MAJESTY(UUID.fromString("8712341e-66ce-43ff-a588-96cc074f40e4"), "majesty_stat", AttributeModifier.Operation.ADDITION, () -> com.gim.registry.Attributes.elemental_majesty, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(12.6, 31.6));
        put(ArtifactRarity.TWO, new Pair<>(16.8, 35.8));
        put(ArtifactRarity.THREE, new Pair<>(21., 92.3));
        put(ArtifactRarity.FOUR, new Pair<>(25.2, 139.3));
        put(ArtifactRarity.FIVE, new Pair<>(28., 186.5));
    }}),

    RECHARGE(UUID.fromString("87277686-2fc5-4a3c-b905-c198253f8a9f"), "recharge_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.recharge_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.5, 8.8));
        put(ArtifactRarity.TWO, new Pair<>(4.7, 10.9));
        put(ArtifactRarity.THREE, new Pair<>(5.8, 23.1));
        put(ArtifactRarity.FOUR, new Pair<>(7., 38.7));
        put(ArtifactRarity.FIVE, new Pair<>(7.8, 51.8));
    }}),

    CRIT_RATE(UUID.fromString("5d35041a-ece0-4123-a060-ae002da27896"), "crit_rate_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.crit_rate, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(2.1, 5.3));
        put(ArtifactRarity.TWO, new Pair<>(2.8, 9.2));
        put(ArtifactRarity.THREE, new Pair<>(3.5, 15.4));
        put(ArtifactRarity.FOUR, new Pair<>(4.2, 23.2));
        put(ArtifactRarity.FIVE, new Pair<>(4.7, 31.1));
    }}),

    CRIT_DAMAGE(UUID.fromString("08740ffb-a532-48d3-b84f-c7935b77811d"), "crit_damage_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.crit_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(4.2, 10.5));
        put(ArtifactRarity.TWO, new Pair<>(5.6, 15.9));
        put(ArtifactRarity.THREE, new Pair<>(7., 30.8));
        put(ArtifactRarity.FOUR, new Pair<>(8.4, 46.4));
        put(ArtifactRarity.FIVE, new Pair<>(9.3, 62.2));
    }}),

    HEAL(UUID.fromString("d500297f-2723-485b-8f51-a6b9342ac94c"), "heal_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.heal_bonus, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(2.4, 6.1));
        put(ArtifactRarity.TWO, new Pair<>(3.2, 9.5));
        put(ArtifactRarity.THREE, new Pair<>(4., 17.8));
        put(ArtifactRarity.FOUR, new Pair<>(4.8, 26.8));
        put(ArtifactRarity.FIVE, new Pair<>(5.4, 35.9));
    }}),

    DEFENCE_PERCENT(UUID.fromString("de5de49d-ffe7-4c01-a52d-4939b44965ae"), "defence_stat", AttributeModifier.Operation.MULTIPLY_BASE, () -> com.gim.registry.Attributes.defence, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(3.9, 9.9));
        put(ArtifactRarity.TWO, new Pair<>(5.2, 14.2));
        put(ArtifactRarity.THREE, new Pair<>(6.6, 28.8));
        put(ArtifactRarity.FOUR, new Pair<>(7.9, 43.5));
        put(ArtifactRarity.FIVE, new Pair<>(8.7, 58.3));
    }}),

    DEFENCE(UUID.fromString("43a60362-39b9-472e-9903-ba00aff918b6"), "defence_stat", AttributeModifier.Operation.ADDITION, () -> com.gim.registry.Attributes.defence, new HashMap<>() {{
        put(ArtifactRarity.ONE, new Pair<>(5.0, 7 * SUB_STAT_MODIFIER)); // leather armor
        put(ArtifactRarity.TWO, new Pair<>(5.5, 11 * SUB_STAT_MODIFIER)); // gold armor
        put(ArtifactRarity.THREE, new Pair<>(6.0, 12 * SUB_STAT_MODIFIER)); // chainmail armor
        put(ArtifactRarity.FOUR, new Pair<>(6.5, 15 * SUB_STAT_MODIFIER)); // iron armor
        put(ArtifactRarity.FIVE, new Pair<>(7.0, 20 * SUB_STAT_MODIFIER)); // diamond armor
    }}),

    ;

    /**
     * Substats take values from regular stats but lower their base values by this modifier
     */
    public static final double SUB_STAT_MODIFIER = 3.4d;
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
     * @param type
     */
    public void apply(Multimap<Attribute, AttributeModifier> builder, ArtifactProperties props, double multiplier, ArtifactSlotType type) {
        ArtifactRarity rarity = props.getRarity();
        double value = getForLevel(rarity, rarity.getLevel(props.getExp()));

        // ID basing on slot type, so different slots will apply with each other
        UUID currentId = new UUID(id.getMostSignificantBits(), id.getLeastSignificantBits() & type.ordinal());

        AttributeModifier modifier = new AttributeModifier(currentId, name, value * multiplier, operation);
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
