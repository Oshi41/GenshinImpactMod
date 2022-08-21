package com.gim.capability.genshin;

import com.gim.players.base.IGenshinPlayer;
import com.gim.registry.Elementals;
import com.google.common.collect.Iterators;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

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

        if (holder instanceof Player && ((Player) holder).isCreative()) {
            return true;
        }

        return ticksTillSwitch(holder) <= 0;
    }

    /**
     * Returns active personage
     */
    default IGenshinPlayer current() {
        if (currentStack().isEmpty()) {
            return null;
        }

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
        // null checks
        if (holder != null && id != null
                // character in main team
                && currentStack().contains(id)) {
            GenshinEntityData info = getPersonInfo(id);
            // character is alive and not in animation
            if (info != null && info.getHealth() > 0 && info.getSkillTicksAnim() <= 0 && info.getBurstTicksAnim() <= 0) {

                if (holder instanceof Player && ((Player) holder).isCreative()) {
                    return true;
                }

                // countdown is over
                return ticksTillSkill(holder, id) <= 0;
            }
        }

        return false;
    }

    /**
     * Can enable burst of personage by ID
     *
     * @param holder
     * @param id     - personage id
     */
    default boolean isBurstEnabled(LivingEntity holder, IGenshinPlayer id) {
        // null checks
        if (holder != null && id != null
                // character in main team
                && currentStack().contains(id)) {
            GenshinEntityData data = getPersonInfo(id);

            if (data != null
                    // character is alive
                    && data.getHealth() > 0
                    // all animations are finished
                    && data.getSkillTicksAnim() <= 0
                    && data.getBurstTicksAnim() <= 0) {

                if (holder instanceof Player && ((Player) holder).isCreative()) {
                    return true;
                }

                // cooldown finished and enough energy
                return ticksTillBurst(holder, id) <= 0 && data.energy().getEnergyStored() >= data.energy().getMaxEnergyStored();
            }
        }

        return false;
    }

    /**
     * Returns current health / maxExp health of person (from 0 to 1)
     *
     * @param id - personage ID
     * @return
     */
    @Nullable
    GenshinEntityData getPersonInfo(IGenshinPlayer id);

    /**
     * Collection of all possible personages
     */
    Collection<IGenshinPlayer> getAllPersonages();

    /**
     * Returns current stack of personages
     *
     * @return
     */
    Collection<IGenshinPlayer> currentStack();

    /**
     * Called after player retrieves new character
     *
     * @param character - new character
     * @param holder
     */
    void addNewCharacter(IGenshinPlayer character, LivingEntity holder);

    void deserializeNBT(CompoundTag nbt, LivingEntity holder);

    void onSkill(LivingEntity holder);

    void onBurst(LivingEntity holder);

    /**
     * @param holder
     * @param energy
     * @param elemental
     * @return
     */
    double consumeEnergy(LivingEntity holder, double energy, Elementals elemental);

    /**
     * Changing player stack
     *
     * @param holder   - entity holder
     * @param newStack - bnew characters stack
     */
    void setCurrentStack(LivingEntity holder, Collection<IGenshinPlayer> newStack);
}
