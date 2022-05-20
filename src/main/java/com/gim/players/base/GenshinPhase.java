package com.gim.players.base;

/**
 * Phase for Genhin abilities
 */
public enum GenshinPhase {

    /**
     * On skill/burst start
     */
    START,

    /**
     * Ticking ability
     */
    TICK,

    /**
     * On last tick, it should end
     */
    END
}
