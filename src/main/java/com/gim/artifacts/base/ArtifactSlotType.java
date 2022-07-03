package com.gim.artifacts.base;

import java.util.*;
import java.util.stream.Collectors;

public enum ArtifactSlotType {
    FLOWER(Map.of(ArtifactStat.HP, 100.),
            new HashMap<>() {{
                put(ArtifactStat.ATTACK, 15.79);
                put(ArtifactStat.HP_PERCENT, 10.53);
                put(ArtifactStat.ATTACK_PERCENT, 10.53);
                put(ArtifactStat.DEFENCE_PERCENT, 10.53);
                put(ArtifactStat.RECHARGE, 10.53);
                put(ArtifactStat.MAJESTY, 10.53);
                put(ArtifactStat.CRIT_RATE, 7.89);
                put(ArtifactStat.CRIT_DAMAGE, 7.89);
            }}),
    FEATHER(Map.of(ArtifactStat.ATTACK, 100.),
            new HashMap<>() {{
                put(ArtifactStat.HP, 15.79);
                put(ArtifactStat.DEFENCE, 15.79);
                put(ArtifactStat.HP_PERCENT, 10.53);
                put(ArtifactStat.ATTACK_PERCENT, 10.53);
                put(ArtifactStat.DEFENCE_PERCENT, 10.53);
                put(ArtifactStat.RECHARGE, 10.53);
                put(ArtifactStat.MAJESTY, 10.53);
                put(ArtifactStat.CRIT_RATE, 7.89);
                put(ArtifactStat.CRIT_DAMAGE, 7.89);
            }}),
    CLOCK(Map.of(
            ArtifactStat.HP_PERCENT, 26.68,
            ArtifactStat.ATTACK_PERCENT, 26.68,
            ArtifactStat.DEFENCE_PERCENT, 26.68,
            ArtifactStat.RECHARGE, 10.,
            ArtifactStat.MAJESTY, 10.
    ),
            new HashMap<>() {{
                put(ArtifactStat.HP, 15.);
                put(ArtifactStat.DEFENCE, 15.);
                put(ArtifactStat.ATTACK, 15.);
                put(ArtifactStat.HP_PERCENT, 10.);
                put(ArtifactStat.ATTACK_PERCENT, 10.);
                put(ArtifactStat.DEFENCE_PERCENT, 10.);
                put(ArtifactStat.RECHARGE, 10.);
                put(ArtifactStat.MAJESTY, 10.);
                put(ArtifactStat.CRIT_RATE, 7.5);
                put(ArtifactStat.CRIT_DAMAGE, 7.5);
            }}),
    CUP(new HashMap<>() {{
        put(ArtifactStat.HP_PERCENT, 21.25);
        put(ArtifactStat.ATTACK_PERCENT, 21.25);
        put(ArtifactStat.DEFENCE_PERCENT, 20.);
        put(ArtifactStat.PHYSICAL, 5.);
        put(ArtifactStat.PYRO, 5.);
        put(ArtifactStat.ELECTRO, 5.);
        put(ArtifactStat.CRYO, 5.);
        put(ArtifactStat.HYDRO, 5.);
        put(ArtifactStat.ANEMO, 5.);
        put(ArtifactStat.GEO, 5.);
        put(ArtifactStat.MAJESTY, 2.5);
    }},
            new HashMap<>() {{
                put(ArtifactStat.HP, 15.);
                put(ArtifactStat.DEFENCE, 15.);
                put(ArtifactStat.ATTACK, 15.);
                put(ArtifactStat.HP_PERCENT, 10.);
                put(ArtifactStat.ATTACK_PERCENT, 10.);
                put(ArtifactStat.DEFENCE_PERCENT, 10.);
                put(ArtifactStat.RECHARGE, 10.);
                put(ArtifactStat.MAJESTY, 10.);
                put(ArtifactStat.CRIT_RATE, 7.5);
                put(ArtifactStat.CRIT_DAMAGE, 7.5);
            }}),
    CROWN(new HashMap<>() {{
        put(ArtifactStat.HP_PERCENT, 22.);
        put(ArtifactStat.ATTACK_PERCENT, 22.);
        put(ArtifactStat.DEFENCE_PERCENT, 22.);
        put(ArtifactStat.CRIT_RATE, 10.);
        put(ArtifactStat.CRIT_DAMAGE, 10.);
        put(ArtifactStat.HEAL, 10.);
        put(ArtifactStat.MAJESTY, 4.);
    }},
            new HashMap<>() {{
                put(ArtifactStat.HP, 15.);
                put(ArtifactStat.DEFENCE, 15.);
                put(ArtifactStat.ATTACK, 15.);
                put(ArtifactStat.HP_PERCENT, 10.);
                put(ArtifactStat.ATTACK_PERCENT, 10.);
                put(ArtifactStat.DEFENCE_PERCENT, 10.);
                put(ArtifactStat.RECHARGE, 10.);
                put(ArtifactStat.MAJESTY, 10.);
                put(ArtifactStat.CRIT_RATE, 7.5);
                put(ArtifactStat.CRIT_DAMAGE, 7.5);
            }});

    private final Map<ArtifactStat, Double> primal;
    private final Map<ArtifactStat, Double> sub;

    ArtifactSlotType(Map<ArtifactStat, Double> primal, Map<ArtifactStat, Double> sub) {
        this.primal = primal;
        this.sub = sub;
    }

    /**
     * Returns random primal
     *
     * @return
     */
    public ArtifactStat getRandomPrimal(Random random) {
        List<ArtifactStat> keys = new ArrayList<>(primal.keySet());
        Collections.shuffle(keys, random);

        for (ArtifactStat stat : keys) {
            double chance = primal.get(stat) / 100;
            if (random.nextFloat() < chance) {
                return stat;
            }
        }

        return keys.get(0);
    }

    /**
     * Returns random sub set
     *
     * @param primal - primal stat
     * @return
     */
    public ArtifactStat getRandomSub(Random random, ArtifactStat primal) {
        List<ArtifactStat> keys = sub.keySet().stream().filter(x -> !Objects.equals(primal, x)).collect(Collectors.toList());
        Collections.shuffle(keys, random);

        for (ArtifactStat stat : keys) {
            double chance = sub.get(stat) / 100;
            if (random.nextFloat() < chance) {
                return stat;
            }
        }

        return keys.get(0);
    }
}
