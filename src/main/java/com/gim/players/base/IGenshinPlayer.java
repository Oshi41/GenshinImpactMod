package com.gim.players.base;

import com.gim.capability.genshin.GenshinEntityData;
import com.gim.capability.genshin.IGenshinInfo;
import com.gim.registry.Elementals;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IGenshinPlayer extends IForgeRegistryEntry<IGenshinPlayer> {

    /**
     * Localized name of character
     *
     * @return
     */
    BaseComponent getName();

    /**
     * Returns attributes of current entity
     *
     * @return the same instance! Should be used with wisdom
     */
    AttributeSupplier getAttributes();

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
     * Calculates time for next burst attacks
     *
     * @param entity  - holder
     * @param info    - entity info
     * @param attacks - current attack list
     */
    int ticksTillBurst(LivingEntity entity, GenshinEntityData info, List<CombatEntry> attacks);

    /**
     * Calculates time for next skill attacks
     *
     * @param entity
     * @param info
     * @param attacks
     * @return
     */
    int ticksTillSkill(LivingEntity entity, GenshinEntityData info, List<CombatEntry> attacks);

    /**
     * Called to perform attack
     *
     * @param holder         - current entity
     * @param data           - current character data
     * @param currentAttacks - attacks history
     */
    void performSkill(LivingEntity holder, IGenshinInfo data, List<CombatEntry> currentAttacks);

    /**
     * Called to perform a burst attack
     *
     * @param holder         - current entity
     * @param data           - entity data
     * @param currentAttacks - attacks history
     */
    void performBurst(LivingEntity holder, IGenshinInfo data, List<CombatEntry> currentAttacks);

    /**
     * Called every tick for current team
     *
     * @param holder         - entity holder
     * @param info           - current entity info
     * @param currentAttacks - current attack history
     */
    void onTick(LivingEntity holder, IGenshinInfo info, List<CombatEntry> currentAttacks);

    /**
     * Called on switch player
     *
     * @param holder         - current player
     * @param info           - current character info
     * @param currentAttacks - attack history
     * @param isActive       - is character active
     */
    void onSwitch(LivingEntity holder, IGenshinInfo info, List<CombatEntry> currentAttacks, boolean isActive);
}
