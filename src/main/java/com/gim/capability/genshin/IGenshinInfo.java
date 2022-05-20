package com.gim.capability.genshin;

import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Attributes;
import com.google.common.collect.Iterators;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.List;

public interface IGenshinInfo extends INBTSerializable<CompoundTag> {

    /**
     * Ticking method (called from LivingUpdateEvent)
     */
    void tick(LivingEntity entity);

    /**
     * Detects if can use skill
     */
    default boolean canUseSkill(LivingEntity holder) {
        return isSkillEnabled(holder, current());
    }

    /**
     * Detects if can use burst
     */
    default boolean canUseBurst(LivingEntity holder) {
        return isBurstEnabled(holder, current());
    }

    /**
     * If can switch to current player
     *
     * @param playerIndex
     * @param holder
     */
    default boolean canSwitchToPlayer(int playerIndex, LivingEntity holder) {
        // same playerIndex
        if (playerIndex == currentIndex()) {
            return false;
        }

        // incorrect value range
        if (playerIndex < 0 || playerIndex >= currentStack().size()) {
            return false;
        }

        IGenshinPlayer player = Iterators.get(currentStack().iterator(), playerIndex);
        if (player == null || getPersonInfo(player).getHealth() <= 0) {
            return false;
        }

        return ticksTillSwitch(holder) <= 0;
    }

    /**
     * Records current attack
     *
     * @param entry
     */
    void recordAttack(CombatEntry entry);

    /**
     * Returns active personage
     */
    default IGenshinPlayer current() {
        return Iterators.get(currentStack().iterator(), currentIndex());
    }

    /**
     * Returns current personage number
     */
    int currentIndex();

    /**
     * Called after switching to new one
     */
    void onSwitchToIndex(LivingEntity holder, int newIndex);

    /**
     * Returns ticks to next personage switch
     */
    int ticksTillSwitch(LivingEntity holder);

    /**
     * Returns tick to next possible skill usage
     *
     * @param holder
     * @param id
     * @return
     */
    int ticksTillSkill(LivingEntity holder, IGenshinPlayer id);

    /**
     * Returns tick to next possible burst usage
     *
     * @param holder
     * @param id     - personage ID
     */
    int ticksTillBurst(LivingEntity holder, IGenshinPlayer id);

    /**
     * Can enable skill of personage by ID
     *
     * @param holder
     * @param id     - personage ID
     */
    default boolean isSkillEnabled(LivingEntity holder, IGenshinPlayer id) {
        return id != null
                && currentStack().contains(id)
                && getPersonInfo(id).getHealth() > 0
                && ticksTillSkill(holder, id) <= 0;
    }

    /**
     * Can enable burst of personage by ID
     *
     * @param holder
     * @param id     - personage id
     */
    default boolean isBurstEnabled(LivingEntity holder, IGenshinPlayer id) {
        if (holder != null && id != null && currentStack().contains(id)) {
            GenshinEntityData data = getPersonInfo(id);
            if (data != null && data.getHealth() > 0 && ticksTillBurst(holder, id) <= 0) {
                return data.getEnergy() >= id.getAttributes().getValue(Attributes.burst_cost);
            }
        }

        return false;
    }

    /**
     * Returns current health / max health of person (from 0 to 1)
     *
     * @param id - personage ID
     * @return
     */
    GenshinEntityData getPersonInfo(IGenshinPlayer id);

    /**
     * Collection of all possible personages
     */
    List<IGenshinPlayer> getAllPersonages();

    /**
     * Returns current stack of personages
     *
     * @return
     */
    Collection<IGenshinPlayer> currentStack();

    /**
     * Called after player retrieves new character
     *
     * @param newPlayer - new character
     */
    void onAddPersonage(IGenshinPlayer newPlayer);

    /**
     * Updating data by current player
     */
    void updateByHolder(LivingEntity entity);

    void deserializeNBT(CompoundTag nbt, LivingEntity holder);

    void onSkill(LivingEntity holder);

    void onBurst(LivingEntity holder);
}
