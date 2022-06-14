package com.gim.players.base;

import com.gim.attack.GenshinCombatTracker;
import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Elementals;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;

public interface IGenshinPlayer extends IForgeRegistryEntry<IGenshinPlayer> {

    /**
     * Localized name of character
     *
     * @return
     */
    BaseComponent getName();

    /**
     * Returns attributeMap of current entity
     *
     * @return the same instance! Should be used with wisdom
     */
    AttributeMap cachedAttributes();

    /**
     * Returns builder for current attributeMap
     * Creates every call
     */
    AttributeSupplier.Builder attributesBuilder();

    /**
     * Is current stack applicable to character weapon
     *
     * @param entity - current entity
     * @param stack  - holding stack
     */
    boolean effectiveWeapon(LivingEntity entity, ItemStack stack);

    /**
     * Current character elemental
     */
    Elementals getElemental();

    /**
     * Returns possible star positions
     * Should be from 0 to 64
     * <p>
     * Not more than length of 6!
     */
    List<Vec2> starPoses();

    /**
     * Calculates time for next burst attacks
     *
     * @param entity  - holder
     * @param info    - entity info
     * @param tracker - attacks tracker
     */
    int ticksTillBurst(LivingEntity entity, GenshinEntityData info, GenshinCombatTracker tracker);

    /**
     * Calculates time for next skill attacks
     */
    int ticksTillSkill(LivingEntity entity, GenshinEntityData info, GenshinCombatTracker tracker);

    /**
     * Called to perform attack
     *
     * @param holder  - current entity
     * @param data    - current character data
     * @param tracker - attacks tracker
     */
    void performSkill(LivingEntity holder, IGenshinInfo data, GenshinCombatTracker tracker);

    /**
     * Called to perform a burst attack
     *
     * @param holder  - current entity
     * @param data    - entity data
     * @param tracker - attacks tracker
     */
    void performBurst(LivingEntity holder, IGenshinInfo data, GenshinCombatTracker tracker);

    /**
     * Called every tick for current team
     *
     * @param holder  - entity holder
     * @param info    - current entity info
     * @param tracker - attacks tracker
     */
    void onTick(LivingEntity holder, IGenshinInfo info, GenshinCombatTracker tracker);

    /**
     * Called on switch player
     *
     * @param holder   - current player
     * @param info     - current character info
     * @param tracker  - attacks tracker
     * @param isActive - is character active
     */
    void onSwitch(LivingEntity holder, IGenshinInfo info, GenshinCombatTracker tracker, boolean isActive);

    /**
     * Called for special handling for star adding event
     *
     * @param holder           - current holder
     * @param info             - current genshin info
     * @param currentStarCount - total stars
     */
    void onStarAdded(LivingEntity holder, IGenshinInfo info, int currentStarCount);
}
