package com.gim.artifacts.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum ArtifactRarity {
    ONE(4, 600, 3200, -1),
    TWO(6, 1200, 8800, 0),
    THREE(12, 1800, 52275, 1),
    FOUR(16, 2400, 122675, 2),
    FIVE(20, 3000, 270475, 3),
    ;

    private final int baseSubstatCount;

    private final List<Double> expLevels = new ArrayList<>();
    private final List<Double> fullXpLevels = new ArrayList<>();

    ArtifactRarity(int maxLevel, double minExp, double maxExp, int baseSubstatCount) {
        this.baseSubstatCount = baseSubstatCount;

        expLevels.add(minExp);
        fullXpLevels.add(minExp);

        double perLevel = (maxExp - minExp) / maxLevel - 2;

        for (int i = 1; i <= maxLevel - 1; i++) {
            expLevels.add(perLevel * i);
        }

        expLevels.add(maxExp);

        for (int i = 1; i < expLevels.size(); i++) {
            fullXpLevels.add(fullXpLevels.get(i - 1) + expLevels.get(i));
        }
    }

    /**
     * Returns level from current Exp
     */
    public int getLevel(double exp) {
        for (int i = 0; i < fullXpLevels.size(); i++) {
            if (exp <= fullXpLevels.get(i)) {
                return i;
            }
        }

        return getMaxLevel();
    }

    /**
     * Get amount of Exp from level to other level
     *
     * @param fromLvl - level from
     * @param toLvl   - level to
     * @return - exp amount
     */
    public int getAmount(int fromLvl, int toLvl) {
        if (fromLvl >= toLvl
                || toLvl >= fullXpLevels.size()
                || fromLvl < 0) {
            return 0;
        }

        return (int) (fullXpLevels.get(toLvl) - fullXpLevels.get(fromLvl));
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

    public double getMinExp() {
        return fullXpLevels.get(0);
    }

    public double getMaxExp() {
        return fullXpLevels.get(expLevels.size() - 1);
    }

    public int getMaxLevel() {
        return expLevels.size() - 1;
    }

    /**
     * Returns amount of xp for current level
     *
     * @param exp - total xp count
     * @return
     */
    public int getXpAmountForLevel(int exp) {
        for (int i = 0; i < fullXpLevels.size(); i++) {
            Double perLevel = fullXpLevels.get(i);
            if (perLevel >= exp) {
                return (int) (perLevel - exp);
            }
        }

        return 0;
    }

    /**
     * Returns xp neede to upgrade this level
     *
     * @param level
     * @return
     */
    public int getXpForLevel(int level) {
        if (level < 0 || level >= expLevels.size()) {
            return -1;
        }

        double result = expLevels.get(level);
        return (int) result;
    }
}
