package com.gim.artifacts.base;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public ArtifactStat getRandomSub(Random random, ArtifactStat primal, Stream<ArtifactStat> toExclude) {

        Set<ArtifactStat> statSet = toExclude.collect(Collectors.toSet());

        List<ArtifactStat> keys = sub.keySet().stream()
                .filter(x -> !Objects.equals(primal, x))
                .filter(x -> !statSet.contains(x))
                .collect(Collectors.toList());
        Collections.shuffle(keys, random);

        double current = 0;
        Table<ArtifactStat, Double, Double> table = HashBasedTable.create();
        for (ArtifactStat key : keys) {
            double next = current + sub.get(key);
            table.put(key, current, next);
            current = next;
        }

        double value = random.nextDouble() * table.cellSet().stream()
                .filter(x -> x.getValue() != null)
                .mapToDouble(Table.Cell::getValue)
                .max()
                .orElse(0);

        for (Table.Cell<ArtifactStat, Double, Double> cell : table.cellSet()) {
            if (cell.getColumnKey() <= value && value <= cell.getValue()) {
                return cell.getRowKey();
            }
        }

        Exception ex = new Exception("Cannot select sub stat for " + primal);

        throw new ReportedException(
                CrashReport.forThrowable(ex, "Cannot obtain substat for artifact of type " + primal)
        );
    }

    public ImmutableMap<ArtifactStat, Double> getPrimal() {
        return ImmutableMap.<ArtifactStat, Double>builder().putAll(primal).build();
    }

    public ImmutableMap<ArtifactStat, Double> getSub() {
        return ImmutableMap.<ArtifactStat, Double>builder().putAll(sub).build();
    }
}
