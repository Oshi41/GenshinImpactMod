package com.gim.artifacts.base;

import java.util.ArrayList;
import java.util.List;

public enum ArtifactRarity {
    ONE(4, 600, 3200, 0, 0),
    TWO(6, 1200, 8800, 0, 1),
    THREE(12, 1800, 52275, 1, 2),
    FOUR(16, 2400, 122675, 2, 3),
    FIVE(20, 3000, 270475, 3, 4),
    ;

    private final int baseSubstatCount;
    private final int totalSubstatCount;

    private final List<Double> expLevels = new ArrayList<>();
    private final List<Double> fullXpLevels = new ArrayList<>();

    ArtifactRarity(int maxLevel, double minExp, double maxExp, int baseSubstatCount, int totalSubstatCount) {
        this.baseSubstatCount = baseSubstatCount;
        this.totalSubstatCount = totalSubstatCount;

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
            Double amount = fullXpLevels.get(i);
            if (exp == amount) {
                return i;
            } else if (exp < amount) {
                return i - 1;
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

        return (int) (fullXpLevels.get(toLvl) - fullXpLevels.get(fromLvl)) + 1;
    }

    public double getMinExp() {
        return fullXpLevels.get(0);
    }

    public double getMaxExp() {
        return fullXpLevels.get(fullXpLevels.size() - 1) + 1;
    }

    public int getMaxLevel() {
        return expLevels.size() - 1;
    }

    /**
     * Returns amount of xp for current level
     *
     * @param exp - total xp count
     */
    public int getXpAmountForLevel(int exp) {
        for (int i = 0; i < fullXpLevels.size(); i++) {
            double perLevel = fullXpLevels.get(i);

            // found same level, return 0
            if (perLevel == exp) {
                return 0;
            } else if (perLevel > exp) {
                // find prev level
                int prevLevel = i - 1;

                // some error, looks like we on 0 level
                if (prevLevel < 0)
                    return 0;

                return (int) (exp - fullXpLevels.get(prevLevel));
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

    /**
     * Initial count of randomly generated substat of artifact
     * If zero no substat generating
     * Else generates from getInitSubstatCount() - 1 to getInitSubstatCount() substats
     */
    public int getInitSubstatCount() {
        return baseSubstatCount;
    }

    /**
     * Total substat count
     */
    public int getTotalSubstatCount() {
        return totalSubstatCount;
    }
}
