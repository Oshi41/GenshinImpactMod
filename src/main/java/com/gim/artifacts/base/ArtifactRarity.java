package com.gim.artifacts.base;

import java.util.Random;

public enum ArtifactRarity {
    ONE(4, 600, 3200, -1),
    TWO(6, 1200, 8800, 0),
    THREE(12, 1800, 52275, 1),
    FOUR(16, 2400, 122675, 2),
    FIVE(20, 3000, 270475, 3),
    ;

    public final int maxLevel;
    public final double minExp;
    public final double maxExp;
    private final int baseSubstatCount;

    ArtifactRarity(int maxLevel, double min, double max, int baseSubstatCount) {
        this.maxLevel = maxLevel;
        this.minExp = min;
        this.maxExp = max;
        this.baseSubstatCount = baseSubstatCount;
    }

    /**
     * Returns base value for power progression
     */
    private double base() {
        double difference = maxExp - minExp;
        return Math.pow(difference, 1.0 / maxLevel);
    }

    /**
     * Returns level from current Exp
     */
    public int getLevel(double exp) {
        if (maxExp >= exp) {
            return 0;
        }

        exp -= maxExp;
        // see https://www.baeldung.com/java-logarithms
        return (int) Math.floor(Math.log(exp) / Math.log(base()));
    }

    /**
     * Get amount of Exp from level to other level
     *
     * @param fromLvl - level from
     * @param toLvl   - level to
     * @return - exp amount
     */
    public int getAmount(int fromLvl, int toLvl) {
        double base = base();
        double maxAmount = Math.pow(base, toLvl);
        double minAmount = fromLvl == 0
                ? 0
                : Math.pow(base, fromLvl);

        return (int) Math.ceil(maxAmount - minAmount);
    }

    /**
     * Returns initital count of sub sets
     */
    public int getInititalSubstats(Random random) {
        int result = baseSubstatCount;
        if (random.nextBoolean()) {
            result += 1;
        }

        return Math.max(0, result);
    }
}
